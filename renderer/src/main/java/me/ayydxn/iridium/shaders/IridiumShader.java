package me.ayydxn.iridium.shaders;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.GsonBuilder;
import me.ayydxn.iridium.IridiumRenderer;
import me.ayydxn.iridium.buffers.UniformBuffer;
import me.ayydxn.iridium.renderer.DescriptorSetManager;
import me.ayydxn.iridium.renderer.exceptions.IridiumRendererException;
import me.ayydxn.iridium.utils.IridiumConstants;
import net.minecraft.util.GsonHelper;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.ApiStatus;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static me.ayydxn.iridium.renderer.debug.VulkanDebugUtils.vkCheckResult;
import static org.lwjgl.vulkan.VK10.*;

public class IridiumShader
{
    private final VkDevice logicalDevice = IridiumRenderer.getInstance().getGraphicsContext().getGraphicsDevice().getLogicalDevice();

    private final Map<ShaderStage, ShaderSPIRV> shaderStageToSPIRV = Maps.newHashMap();
    private final Map<ShaderStage, Long> shaderStageToShaderModule = Maps.newHashMap();
    private final List<PushConstantRange> pushConstantRanges;
    private final DescriptorSetInfo descriptorSetInfo;
    private final DescriptorSetManager descriptorSetManager;

    private ShaderDefinition shaderDefinition;

    public IridiumShader(String filepath)
    {
        URL shaderJSONFileURL = IridiumShader.class.getResource('/' + filepath + ".json");
        if (shaderJSONFileURL == null)
            throw new IridiumRendererException(String.format("The JSON descriptor file for the shader '%s' wasn't found!", filepath));

        IridiumShaderCompiler shaderCompiler = IridiumShaderCompiler.getInstance();

        try
        {
            Path shaderJSONFilePath = Paths.get(shaderJSONFileURL.toURI());
            String shaderJSONFileContent = Files.readString(shaderJSONFilePath, StandardCharsets.UTF_8);

            this.shaderDefinition = GsonHelper.fromJson(new GsonBuilder().create(), shaderJSONFileContent, ShaderDefinition.class);
            String baseShaderFilepath = FilenameUtils.getPath(filepath);

            for (Map.Entry<String, String> shaderStage : this.shaderDefinition.getShaderStages().entrySet())
            {
                ShaderStage stage = ShaderStage.getFromString(shaderStage.getKey());
                String shaderFilepath = baseShaderFilepath + shaderStage.getValue() + stage.getFileExtension();
                ShaderSPIRV shaderSPIRV = shaderCompiler.compileShaderFromFile(shaderFilepath, stage);

                shaderStageToSPIRV.put(stage, shaderSPIRV);
            }
        }
        catch (Exception exception)
        {
            IridiumConstants.LOGGER.error(exception);
        }

        this.createShaderModules();

        List<ShaderResource> shaderResources = this.parseResourcesFromDefinition(Objects.requireNonNull(this.shaderDefinition));

        this.descriptorSetInfo = this.createDescriptorSetLayout(shaderResources);
        this.pushConstantRanges = this.parsePushConstantsFromDefinition(shaderDefinition);
        this.descriptorSetManager = new DescriptorSetManager();
    }

    public void destroy()
    {
        this.shaderStageToShaderModule.values().forEach(shaderModule -> vkDestroyShaderModule(this.logicalDevice, shaderModule, null));

        this.descriptorSetInfo.cleanup(this.logicalDevice);
    }

    public void bindUniformBuffer(String name, UniformBuffer uniformBuffer)
    {
        if (this.descriptorSetManager != null)
            this.descriptorSetManager.bindUniformBuffer(name, uniformBuffer);
    }

    public void setPushConstant(String name, ByteBuffer value)
    {
        if (this.descriptorSetManager != null)
            this.descriptorSetManager.setPushConstant(name, value);
    }

    @ApiStatus.Internal
    public void updateResources() {
        if (this.descriptorSetManager != null) {
            this.descriptorSetManager.updateDescriptorSets(this);
        }
    }

    @ApiStatus.Internal
    public void bindResources(VkCommandBuffer commandBuffer, long pipelineLayout, int pipelineBindPoint)
    {
        if (this.descriptorSetManager != null)
        {
            this.descriptorSetManager.bindDescriptorSets(commandBuffer, pipelineLayout, pipelineBindPoint);
            this.descriptorSetManager.updatePushConstants(commandBuffer, pipelineLayout, this);
        }
    }

    private void createShaderModules()
    {
        this.shaderStageToSPIRV.forEach((shaderStage, shaderSPIRV) ->
        {
            try (MemoryStack memoryStack = MemoryStack.stackPush())
            {
                LongBuffer pShaderModule = memoryStack.mallocLong(1);

                // (Ayydxn) Set the position of the ByteBuffer back to 0 if it isn't already so we can start reading from there.
                // Otherwise, Vulkan validation will complain that the bytecode isn't valid SPIR-V code.
                VkShaderModuleCreateInfo shaderModuleCreateInfo = VkShaderModuleCreateInfo.calloc(memoryStack)
                        .sType(VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO)
                        .pCode(shaderSPIRV.getShaderBytecode().position() == 0 ? shaderSPIRV.getShaderBytecode() : shaderSPIRV.getShaderBytecode().rewind());

                vkCheckResult(vkCreateShaderModule(this.logicalDevice, shaderModuleCreateInfo, null, pShaderModule));

                this.shaderStageToShaderModule.put(shaderStage, pShaderModule.get(0));
            }
        });
    }

    private DescriptorSetInfo createDescriptorSetLayout(List<ShaderResource> shaderResources)
    {
        Map<Integer, Long> descriptorSetLayouts = Maps.newHashMap();

        // Group resources by their descriptor set index
        Map<Integer, List<ShaderResource>> descriptorSetResources = Maps.newHashMap();
        for (ShaderResource shaderResource : shaderResources)
            descriptorSetResources.computeIfAbsent(shaderResource.set(), setIndex -> Lists.newArrayList()).add(shaderResource);

        try (MemoryStack memoryStack = MemoryStack.stackPush())
        {
            for (Map.Entry<Integer, List<ShaderResource>> descriptorSetResource : descriptorSetResources.entrySet())
            {
                List<ShaderResource> resources = descriptorSetResource.getValue();
                int setIndex = descriptorSetResource.getKey();

                VkDescriptorSetLayoutBinding.Buffer descriptorSetLayoutBindings = VkDescriptorSetLayoutBinding.calloc(resources.size(), memoryStack);

                for (int i = 0; i < resources.size(); i++)
                {
                    ShaderResource shaderResource = resources.get(i);

                    descriptorSetLayoutBindings.get(i)
                            .binding(shaderResource.binding())
                            .descriptorType(shaderResource.type().getVulkanDescriptorType())
                            .descriptorCount(1)
                            .stageFlags(shaderResource.shaderStageFlags());
                }

                VkDescriptorSetLayoutCreateInfo descriptorSetLayoutCreateInfo = VkDescriptorSetLayoutCreateInfo.calloc(memoryStack)
                        .sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO)
                        .pBindings(descriptorSetLayoutBindings);

                LongBuffer pDescriptorSetLayout = memoryStack.longs(VK_NULL_HANDLE);
                vkCheckResult(vkCreateDescriptorSetLayout(this.logicalDevice, descriptorSetLayoutCreateInfo, null, pDescriptorSetLayout));

                descriptorSetLayouts.put(setIndex, pDescriptorSetLayout.get(0));
            }
        }

        return new DescriptorSetInfo(descriptorSetLayouts, descriptorSetResources);
    }

    private List<ShaderResource> parseResourcesFromDefinition(ShaderDefinition shaderDefinition)
    {
        List<ShaderResource> resources = Lists.newArrayList();

        for (ShaderDefinition.ResourceDefinition resourceDefinition : shaderDefinition.getResources())
        {
            resources.add(new ShaderResource(resourceDefinition.name,
                    ShaderResource.Type.getFromString(resourceDefinition.type),
                    resourceDefinition.binding,
                    resourceDefinition.set,
                    this.parseShaderStageFlags(resourceDefinition.stages)
            ));
        }

        return resources;
    }

    private List<PushConstantRange> parsePushConstantsFromDefinition(ShaderDefinition shaderDefinition)
    {
        List<PushConstantRange> pushConstantRanges = Lists.newArrayList();

        for (ShaderDefinition.PushConstantDefinition pushConstantDefinition : shaderDefinition.getPushConstants())
        {
            pushConstantRanges.add(new PushConstantRange(
                    pushConstantDefinition.name,
                    pushConstantDefinition.size,
                    pushConstantDefinition.offset,
                    this.parseShaderStageFlags(pushConstantDefinition.stages)
            ));
        }

        return pushConstantRanges;
    }

    private int parseShaderStageFlags(List<String> shaderStages)
    {
        int combinedShaderStageFlag = 0;

        for (String shaderStage : shaderStages)
            combinedShaderStageFlag |= ShaderStage.getFromString(shaderStage).getVulkanID();

        return combinedShaderStageFlag;
    }

    public Map<ShaderStage, Long> getShaderStagesAndModules()
    {
        return this.shaderStageToShaderModule;
    }

    public DescriptorSetInfo getDescriptorSetInfo()
    {
        return this.descriptorSetInfo;
    }

    public List<PushConstantRange> getPushConstantRanges()
    {
        return this.pushConstantRanges;
    }

    public DescriptorSetManager getDescriptorSetManager()
    {
        return this.descriptorSetManager;
    }

    /**
     * @param descriptorSetLayouts Set Index -> Descriptor Set Layout Handle
     * @param resources            Set Index -> Resources
     */
    public record DescriptorSetInfo(Map<Integer, Long> descriptorSetLayouts, Map<Integer, List<ShaderResource>> resources)
    {
        public void cleanup(VkDevice logicalDevice)
        {
            for (long layout : this.descriptorSetLayouts.values())
                vkDestroyDescriptorSetLayout(logicalDevice, layout, null);
        }
    }

    public record PushConstantRange(String name, int size, int offset, int shaderStageFlags)
    {
    }
}
