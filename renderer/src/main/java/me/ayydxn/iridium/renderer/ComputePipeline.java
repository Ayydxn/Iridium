package me.ayydxn.iridium.renderer;

import me.ayydxn.iridium.IridiumRenderer;
import me.ayydxn.iridium.shaders.IridiumShader;
import me.ayydxn.iridium.shaders.ShaderStage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.LongBuffer;
import java.util.List;
import java.util.Map;

import static me.ayydxn.iridium.renderer.debug.VulkanDebugUtils.vkCheckResult;
import static org.lwjgl.vulkan.VK10.*;

public class ComputePipeline
{
    private final IridiumShader computeShader;
    private final VkDevice logicalDevice;

    private long pipelineLayout;
    private long pipelineCache;
    private long pipelineHandle;

    public ComputePipeline(IridiumShader computeShader)
    {
        this.computeShader = computeShader;
        this.logicalDevice = IridiumRenderer.getInstance().getGraphicsContext().getGraphicsDevice().getLogicalDevice();

        this.createPipelineLayoutAndCache();
    }

    public void create()
    {
        try (MemoryStack memoryStack = MemoryStack.stackPush())
        {
            // (Ayydxn) When setting the shader module, we should be able to safely assume that there is only ever one entry in the map and
            // therefore just get that first entry's value.
            VkPipelineShaderStageCreateInfo shaderStageCreateInfo = VkPipelineShaderStageCreateInfo.calloc(memoryStack)
                    .sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO)
                    .stage(VK_SHADER_STAGE_COMPUTE_BIT)
                    .module(this.computeShader.getShaderStagesAndModules().entrySet().iterator().next().getValue())
                    .pName(memoryStack.UTF8("main"));

            VkComputePipelineCreateInfo.Buffer computePipelineCreateInfo = VkComputePipelineCreateInfo.calloc(1, memoryStack)
                    .sType(VK_STRUCTURE_TYPE_COMPUTE_PIPELINE_CREATE_INFO)
                    .layout(this.pipelineLayout)
                    .stage(shaderStageCreateInfo);

            LongBuffer pComputePipeline = memoryStack.longs(VK_NULL_HANDLE);
            vkCheckResult(vkCreateComputePipelines(this.logicalDevice, this.pipelineCache, computePipelineCreateInfo, null, pComputePipeline));

            this.pipelineHandle = pComputePipeline.get(0);
        }
    }

    public void destroy()
    {
        vkDestroyPipeline(this.logicalDevice, this.pipelineHandle, null);
        vkDestroyPipelineLayout(this.logicalDevice, this.pipelineLayout, null);
        vkDestroyPipelineCache(this.logicalDevice, this.pipelineCache, null);

        this.computeShader.destroy();
        this.computeShader.getDescriptorSetManager().destroy();
    }

    @SuppressWarnings("ConstantConditions")
    private void createPipelineLayoutAndCache()
    {
        // TODO: (Ayydxn) Move to the IridiumShader class as a function.
        try (MemoryStack memoryStack = MemoryStack.stackPush())
        {
            // Setup descriptor set layouts
            LongBuffer pSetLayouts = null;
            Map<Integer, Long> descriptorSetLayouts = this.computeShader.getDescriptorSetInfo().descriptorSetLayouts();
            if (!descriptorSetLayouts.isEmpty())
            {
                pSetLayouts = memoryStack.mallocLong(descriptorSetLayouts.size());

                for (int setIndex = 0; setIndex <  descriptorSetLayouts.size(); setIndex++)
                {
                    long descriptorSetLayout = descriptorSetLayouts.get(setIndex);
                    if (descriptorSetLayout != VK_NULL_HANDLE)
                        pSetLayouts.put(setIndex, descriptorSetLayout);
                }
            }

            // Setup push constants
            List<IridiumShader.PushConstantRange> pushConstants = this.computeShader.getPushConstantRanges();
            VkPushConstantRange.Buffer pushConstantRanges = VkPushConstantRange.calloc(pushConstants.size(), memoryStack);

            for (int i = 0; i < pushConstants.size(); ++i)
            {
                IridiumShader.PushConstantRange pushConstantRange = pushConstants.get(i);

                pushConstantRanges.get(i)
                        .size(pushConstantRange.size())
                        .offset(pushConstantRange.offset())
                        .stageFlags(pushConstantRange.shaderStageFlags());
            }

            // Create the pipeline layout
            VkPipelineLayoutCreateInfo pipelineLayoutCreateInfo = VkPipelineLayoutCreateInfo.calloc(memoryStack)
                    .sType(VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO)
                    .pSetLayouts(pSetLayouts)
                    .pPushConstantRanges(pushConstantRanges);

            // We don't use the pipeline cache for anything so we can leave it like this.
            VkPipelineCacheCreateInfo pipelineCacheCreateInfo = VkPipelineCacheCreateInfo.calloc(memoryStack)
                    .sType(VK_STRUCTURE_TYPE_PIPELINE_CACHE_CREATE_INFO);

            LongBuffer pPipelineLayout = memoryStack.longs(VK_NULL_HANDLE);
            LongBuffer pPipelineCache = memoryStack.longs(VK_NULL_HANDLE);

            vkCheckResult(vkCreatePipelineLayout(this.logicalDevice, pipelineLayoutCreateInfo, null, pPipelineLayout));
            vkCheckResult(vkCreatePipelineCache(this.logicalDevice, pipelineCacheCreateInfo, null, pPipelineCache));

            this.pipelineLayout = pPipelineLayout.get(0);
            this.pipelineCache = pPipelineCache.get(0);
        }
    }
}
