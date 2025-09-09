package me.ayydxn.iridium.renderer;

import me.ayydxn.iridium.IridiumRenderer;
import me.ayydxn.iridium.shaders.IridiumShader;
import me.ayydxn.iridium.shaders.ShaderStage;
import me.ayydxn.iridium.vertex.VertexBufferElement;
import me.ayydxn.iridium.vertex.VertexBufferLayout;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.LongBuffer;
import java.util.List;
import java.util.Map;

import static me.ayydxn.iridium.renderer.debug.VulkanDebugUtils.vkCheckResult;
import static org.lwjgl.vulkan.VK10.*;
import static org.lwjgl.vulkan.VK13.VK_STRUCTURE_TYPE_PIPELINE_RENDERING_CREATE_INFO;

public class GraphicsPipeline
{
    private final IridiumShader shader;
    private final VertexBufferLayout vertexBufferLayout;
    private final SwapChain swapChain;
    private final VkDevice logicalDevice;

    private long graphicsPipelineLayout;
    private long graphicsPipelineCache;
    private long graphicsPipelineHandle;

    public GraphicsPipeline(@NotNull IridiumShader shader, @NotNull VertexBufferLayout vertexBufferLayout, @NotNull SwapChain swapChain)
    {
        this.shader = shader;
        this.vertexBufferLayout = vertexBufferLayout;
        this.swapChain = swapChain;
        this.logicalDevice = IridiumRenderer.getInstance().getGraphicsContext().getGraphicsDevice().getLogicalDevice();

        this.graphicsPipelineLayout = VK_NULL_HANDLE;
        this.graphicsPipelineCache = VK_NULL_HANDLE;
        this.graphicsPipelineHandle = VK_NULL_HANDLE;
    }

    public GraphicsPipeline(@NotNull IridiumShader shader, @NotNull SwapChain swapChain)
    {
        this(shader, new VertexBufferLayout(), swapChain);
    }

    public void create()
    {
        this.createPipelineLayoutAndCache();

        try (MemoryStack memoryStack = MemoryStack.stackPush())
        {
            int depthFormat = IridiumRenderer.getInstance().getGraphicsContext().getGraphicsDevice().getDepthFormat();

            VkViewport.Buffer viewport = VkViewport.calloc(1, memoryStack)
                    .x(0.0f)
                    .y(0.0f)
                    .width(this.swapChain.getExtent().width())
                    .height(this.swapChain.getExtent().height())
                    .minDepth(0.0f)
                    .maxDepth(1.0f);

            VkRect2D.Buffer viewportScissor = VkRect2D.calloc(1, memoryStack)
                    .offset(VkOffset2D.calloc(memoryStack).set(0, 0))
                    .extent(this.swapChain.getExtent());

            VkPipelineShaderStageCreateInfo.Buffer pipelineShaderStageCreateInfos = VkPipelineShaderStageCreateInfo.calloc(this.shader.getShaderStagesAndModules().size(), memoryStack);

            int currentShaderStageIndex = 0;
            for (Map.Entry<ShaderStage, Long> shaderStageAndModule : this.shader.getShaderStagesAndModules().entrySet())
            {
                pipelineShaderStageCreateInfos.get(currentShaderStageIndex)
                        .sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO)
                        .stage(this.convertMoonblastShaderStageToVulkan(shaderStageAndModule.getKey()))
                        .module(shaderStageAndModule.getValue())
                        .pName(memoryStack.UTF8("main"));

                currentShaderStageIndex++;
            }

            VkPipelineDynamicStateCreateInfo pipelineDynamicStateCreateInfo = VkPipelineDynamicStateCreateInfo.calloc(memoryStack)
                    .sType(VK_STRUCTURE_TYPE_PIPELINE_DYNAMIC_STATE_CREATE_INFO)
                    .pDynamicStates(memoryStack.ints(VK_DYNAMIC_STATE_VIEWPORT, VK_DYNAMIC_STATE_SCISSOR));

            VkPipelineVertexInputStateCreateInfo pipelineVertexInputStateCreateInfo = VkPipelineVertexInputStateCreateInfo.calloc(memoryStack)
                    .sType(VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO);

            VkVertexInputBindingDescription.Buffer vertexInputBindingDescription = VkVertexInputBindingDescription.calloc(1, memoryStack)
                    .binding(0)
                    .stride(this.vertexBufferLayout.getStride())
                    .inputRate(VK_VERTEX_INPUT_RATE_VERTEX);

            VkVertexInputAttributeDescription.Buffer vertexInputAttributeDescriptions = VkVertexInputAttributeDescription.calloc(this.vertexBufferLayout.getElementCount(), memoryStack);
            int index = 0;

            for (VertexBufferElement vertexBufferElement : this.vertexBufferLayout)
            {
                vertexInputAttributeDescriptions.get(index)
                        .binding(0)
                        .location(index)
                        .format(vertexBufferElement.shaderDataType.getVulkanFormat())
                        .offset(vertexBufferElement.offset);

                index++;
            }

            pipelineVertexInputStateCreateInfo.pVertexBindingDescriptions(vertexInputBindingDescription)
                    .pVertexAttributeDescriptions(vertexInputAttributeDescriptions);

            VkPipelineInputAssemblyStateCreateInfo pipelineInputAssemblyStateCreateInfo = VkPipelineInputAssemblyStateCreateInfo.calloc(memoryStack)
                    .sType(VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO)
                    .topology(VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST)
                    .primitiveRestartEnable(false);

            VkPipelineViewportStateCreateInfo pipelineViewportStateCreateInfo = VkPipelineViewportStateCreateInfo.calloc(memoryStack)
                    .sType(VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO)
                    .pViewports(viewport)
                    .pScissors(viewportScissor);

            VkPipelineRasterizationStateCreateInfo pipelineRasterizationStateCreateInfo = VkPipelineRasterizationStateCreateInfo.calloc(memoryStack)
                    .sType(VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO)
                    .depthClampEnable(false)
                    .rasterizerDiscardEnable(false)
                    .polygonMode(VK_POLYGON_MODE_FILL)
                    .lineWidth(1.0f)
                    .cullMode(VK_CULL_MODE_BACK_BIT) // TODO: (Ayydxn) Might have to make this configurable.
                    .frontFace(VK_FRONT_FACE_CLOCKWISE)
                    .depthBiasEnable(false);

            // TODO: (Ayydan) Make multisampling usable and configurable. Currently, it's forced off.
            VkPipelineMultisampleStateCreateInfo pipelineMultisampleStateCreateInfo = VkPipelineMultisampleStateCreateInfo.calloc(memoryStack)
                    .sType(VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO)
                    .sampleShadingEnable(false)
                    .rasterizationSamples(VK_SAMPLE_COUNT_1_BIT);

            VkPipelineDepthStencilStateCreateInfo pipelineDepthStencilStateCreateInfo = VkPipelineDepthStencilStateCreateInfo.calloc(memoryStack)
                    .sType(VK_STRUCTURE_TYPE_PIPELINE_DEPTH_STENCIL_STATE_CREATE_INFO)
                    .depthTestEnable(true)
                    .depthWriteEnable(true)
                    .depthCompareOp(VK_COMPARE_OP_LESS)
                    .depthBoundsTestEnable(false)
                    .stencilTestEnable(false);

            pipelineDepthStencilStateCreateInfo.back()
                    .failOp(VK_STENCIL_OP_KEEP)
                    .passOp(VK_STENCIL_OP_KEEP)
                    .compareMask(VK_COMPARE_OP_ALWAYS);

            pipelineDepthStencilStateCreateInfo.front(pipelineDepthStencilStateCreateInfo.back());

            VkPipelineColorBlendAttachmentState.Buffer pipelineColorBlendAttachmentState = VkPipelineColorBlendAttachmentState.calloc(1, memoryStack)
                    .colorWriteMask(VK_COLOR_COMPONENT_R_BIT | VK_COLOR_COMPONENT_G_BIT | VK_COLOR_COMPONENT_B_BIT | VK_COLOR_COMPONENT_A_BIT)
                    .blendEnable(false);

            // (Ayydxn) Will need this later.
            /* VkPipelineColorBlendAttachmentState.Buffer pipelineColorBlendAttachmentState = VkPipelineColorBlendAttachmentState.calloc(1, memoryStack)
                    .colorWriteMask(this.graphicsPipelineState.colorMask.colorMask);

            if (this.graphicsPipelineState.blendState.enabled)
            {
                pipelineColorBlendAttachmentState.blendEnable(true)
                        .srcColorBlendFactor(this.graphicsPipelineState.blendState.srcRgbFactor)
                        .dstColorBlendFactor(this.graphicsPipelineState.blendState.dstRgbFactor)
                        .colorBlendOp(this.graphicsPipelineState.blendState.blendOp)
                        .srcAlphaBlendFactor(this.graphicsPipelineState.blendState.srcAlphaFactor)
                        .dstAlphaBlendFactor(this.graphicsPipelineState.blendState.dstAlphaFactor)
                        .alphaBlendOp(this.graphicsPipelineState.blendState.blendOp);
            }
            else
            {
                pipelineColorBlendAttachmentState.blendEnable(false);
            } */

            VkPipelineColorBlendStateCreateInfo pipelineColorBlendStateCreateInfo = VkPipelineColorBlendStateCreateInfo.calloc(memoryStack)
                    .sType(VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO)
                    .logicOpEnable(false)
                    .logicOp(VK_LOGIC_OP_COPY)
                    .pAttachments(pipelineColorBlendAttachmentState)
                    .blendConstants(memoryStack.floats(0.0f, 0.0f, 0.0f, 0.0f));

            VkPipelineRenderingCreateInfoKHR pipelineRenderingCreateInfo = VkPipelineRenderingCreateInfoKHR.calloc(memoryStack)
                    .sType(VK_STRUCTURE_TYPE_PIPELINE_RENDERING_CREATE_INFO)
                    .pColorAttachmentFormats(memoryStack.ints(this.swapChain.getImageFormat()))
                    .depthAttachmentFormat(depthFormat)
                    .stencilAttachmentFormat(depthFormat);

            VkGraphicsPipelineCreateInfo.Buffer graphicsPipelineCreateInfo = VkGraphicsPipelineCreateInfo.calloc(1, memoryStack)
                    .sType(VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO)
                    .pStages(pipelineShaderStageCreateInfos)
                    .pDynamicState(pipelineDynamicStateCreateInfo)
                    .pVertexInputState(pipelineVertexInputStateCreateInfo)
                    .pInputAssemblyState(pipelineInputAssemblyStateCreateInfo)
                    .pViewportState(pipelineViewportStateCreateInfo)
                    .pRasterizationState(pipelineRasterizationStateCreateInfo)
                    .pMultisampleState(pipelineMultisampleStateCreateInfo)
                    .pDepthStencilState(pipelineDepthStencilStateCreateInfo)
                    .pColorBlendState(pipelineColorBlendStateCreateInfo)
                    .layout(this.graphicsPipelineLayout)
                    .renderPass(VK_NULL_HANDLE)
                    .basePipelineHandle(VK_NULL_HANDLE)
                    .basePipelineIndex(-1)
                    .pNext(pipelineRenderingCreateInfo);

            LongBuffer pGraphicsPipeline = memoryStack.longs(VK_NULL_HANDLE);
            vkCheckResult(vkCreateGraphicsPipelines(this.logicalDevice, this.graphicsPipelineCache, graphicsPipelineCreateInfo, null, pGraphicsPipeline));

            this.shader.destroy();

            this.graphicsPipelineHandle = pGraphicsPipeline.get(0);
        }
    }

    public void destroy()
    {
        vkDestroyPipeline(this.logicalDevice, this.graphicsPipelineHandle, null);
        vkDestroyPipelineLayout(this.logicalDevice, this.graphicsPipelineLayout, null);
        vkDestroyPipelineCache(this.logicalDevice, this.graphicsPipelineCache, null);
    }

    public long getHandle()
    {
        return this.graphicsPipelineHandle;
    }

    private void createPipelineLayoutAndCache()
    {
        try (MemoryStack memoryStack = MemoryStack.stackPush())
        {
            // Setup descriptor set layouts
            Map<Integer, Long> descriptorSetLayouts = this.shader.getDescriptorSetInfo().descriptorSetLayouts();
            LongBuffer pSetLayouts = memoryStack.mallocLong(descriptorSetLayouts.size());

            for (long descriptorSetLayout : descriptorSetLayouts.values())
                pSetLayouts.put(descriptorSetLayout);

            pSetLayouts.flip();

            // Setup push constants
            List<IridiumShader.PushConstantRange> pushConstants = this.shader.getPushConstantRanges();
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

            this.graphicsPipelineLayout = pPipelineLayout.get(0);
            this.graphicsPipelineCache = pPipelineCache.get(0);
        }
    }

    private int convertMoonblastShaderStageToVulkan(ShaderStage shaderStage)
    {
        return switch(shaderStage)
        {
            case VERTEX -> VK_SHADER_STAGE_VERTEX_BIT;
            case  FRAGMENT -> VK_SHADER_STAGE_FRAGMENT_BIT;
            default -> throw new IllegalArgumentException(String.format("No valid Vulkan shader stage exists for '%s'", shaderStage.name()));
        };
    }
}
