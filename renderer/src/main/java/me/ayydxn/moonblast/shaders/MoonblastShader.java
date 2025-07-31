package me.ayydxn.moonblast.shaders;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.ayydxn.moonblast.MoonblastRenderer;
import me.ayydxn.moonblast.renderer.exceptions.MoonblastRendererException;
import net.minecraft.util.GsonHelper;
import org.apache.commons.io.FilenameUtils;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.net.URL;
import java.nio.LongBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static me.ayydxn.moonblast.renderer.debug.VulkanDebugUtils.vkCheckResult;
import static org.lwjgl.vulkan.VK10.*;

public class MoonblastShader
{
    private final VkDevice logicalDevice = MoonblastRenderer.getInstance().getGraphicsContext().getGraphicsDevice().getLogicalDevice();
    private final Map<ShaderStage, ShaderSPIRV> shaderStageToSPIRV = Maps.newHashMap();
    private final Map<ShaderStage, Long> shaderStageToShaderModule = Maps.newHashMap();
    private final List<ShaderResources.ShaderDescriptorSet> shaderDescriptorSets = Lists.newArrayList();

    private List<Long> descriptorSetLayouts = Lists.newArrayList();

    public MoonblastShader(String filepath)
    {
        URL shaderJSONFileURL = MoonblastShader.class.getResource('/' + filepath + ".json");
        if (shaderJSONFileURL == null)
            throw new MoonblastRendererException(String.format("The JSON descriptor file for the shader '%s' wasn't found!", filepath));

        MoonblastShaderCompiler shaderCompiler = MoonblastShaderCompiler.getInstance();
        ShaderReflector reflector = new ShaderReflector();

        try
        {
            Path shaderJSONFilePath = Paths.get(shaderJSONFileURL.toURI());
            String shaderJSONFileContent = Files.readString(shaderJSONFilePath, StandardCharsets.UTF_8);

            JsonObject shaderJSONObject = GsonHelper.parse(shaderJSONFileContent);
            JsonObject shaderStages = GsonHelper.getAsJsonObject(shaderJSONObject, "stages");
            String baseShaderFilepath = FilenameUtils.getPath(filepath);

            for (Map.Entry<String, JsonElement> shaderStage : shaderStages.entrySet())
            {
                ShaderStage stage = ShaderStage.getFromString(shaderStage.getKey());
                String shaderFilepath = baseShaderFilepath + shaderStage.getValue().getAsString() + stage.getFileExtension();
                ShaderSPIRV shaderSPIRV = shaderCompiler.compileShaderFromFile(shaderFilepath, stage);

                this.shaderDescriptorSets.add(reflector.reflect(shaderSPIRV));

                shaderStageToSPIRV.put(stage, shaderSPIRV);
            }
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }

        reflector.destroy();

        this.createShaderModules();
        this.createDescriptorSetLayout();
    }

    public void destroy()
    {
        this.shaderStageToShaderModule.values().forEach(shaderModule -> vkDestroyShaderModule(this.logicalDevice, shaderModule, null));
        this.descriptorSetLayouts.forEach(descriptorSetLayout -> vkDestroyDescriptorSetLayout(this.logicalDevice, descriptorSetLayout, null));
        this.descriptorSetLayouts.clear();
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

    private void createDescriptorSetLayout()
    {
        try (MemoryStack memoryStack = MemoryStack.stackPush())
        {
            for (ShaderResources.ShaderDescriptorSet shaderDescriptorSet : this.shaderDescriptorSets)
            {
                int totalBindings = shaderDescriptorSet.uniformBuffers.size();
                VkDescriptorSetLayoutBinding.Buffer descriptorSetLayoutBindings = VkDescriptorSetLayoutBinding.calloc(totalBindings, memoryStack);

                /*-----------------------*/
                /* -- Uniform Buffers -- */
                /*-----------------------*/

                int index = 0;
                for (Map.Entry<Integer, ShaderResources.UniformBuffer> entry : shaderDescriptorSet.uniformBuffers.entrySet())
                {
                    int binding = entry.getKey();
                    ShaderResources.UniformBuffer uniformBuffer = entry.getValue();

                    VkDescriptorSetLayoutBinding descriptorSetLayoutBinding = descriptorSetLayoutBindings.get(index++)
                            .descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
                            .descriptorCount(1)
                            .binding(binding)
                            .stageFlags(uniformBuffer.shaderStage)
                            .pImmutableSamplers(null);

                    VkWriteDescriptorSet writeDescriptorSet = VkWriteDescriptorSet.calloc(memoryStack)
                            .sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
                            .descriptorType(descriptorSetLayoutBinding.descriptorType())
                            .descriptorCount(1)
                            .dstBinding(descriptorSetLayoutBinding.binding());

                    shaderDescriptorSet.writeDescriptorSets.put(uniformBuffer.name, writeDescriptorSet);
                }

                /*--------------------------------------*/
                /* -- Descriptor Set Layout Creation -- */
                /*--------------------------------------*/

                VkDescriptorSetLayoutCreateInfo descriptorSetLayoutCreateInfo = VkDescriptorSetLayoutCreateInfo.calloc(memoryStack)
                        .sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO)
                        .pBindings(descriptorSetLayoutBindings);

                LongBuffer pDescriptorSetLayout = memoryStack.mallocLong(1);
                vkCheckResult(vkCreateDescriptorSetLayout(this.logicalDevice, descriptorSetLayoutCreateInfo, null, pDescriptorSetLayout));

                this.descriptorSetLayouts.add(pDescriptorSetLayout.get(0));
            }
        }
    }

    public Map<ShaderStage, Long> getShaderStagesAndModules()
    {
        return shaderStageToShaderModule;
    }

    public List<Long> getDescriptorSetLayouts()
    {
        return this.descriptorSetLayouts;
    }
}
