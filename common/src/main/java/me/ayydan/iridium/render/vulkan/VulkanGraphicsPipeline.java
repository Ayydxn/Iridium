package me.ayydan.iridium.render.vulkan;

import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import me.ayydan.iridium.render.IridiumRenderer;
import me.ayydan.iridium.render.interfaces.VertexFormatAccessor;
import me.ayydan.iridium.render.pipeline.GraphicsPipelineState;
import me.ayydan.iridium.render.shader.IridiumShader;
import me.ayydan.iridium.render.vulkan.utils.VulkanUtils;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.LongBuffer;

import static me.ayydan.iridium.render.vulkan.VulkanValidation.vkCheckResult;
import static org.lwjgl.vulkan.VK10.*;
import static org.lwjgl.vulkan.VK13.VK_STRUCTURE_TYPE_PIPELINE_RENDERING_CREATE_INFO;

public class VulkanGraphicsPipeline extends VulkanPipeline
{
    private final IridiumShader shader;
    private final VertexFormat vertexFormat;
    private final VkDevice logicalDevice;
    private final VulkanSwapChain swapChain;
    private final GraphicsPipelineState graphicsPipelineState;

    private long graphicsPipelineLayout;
    private long graphicsPipelineCache;
    private long graphicsPipelineHandle;

    protected VulkanGraphicsPipeline(@NotNull IridiumShader shader, VertexFormat vertexFormat)
    {
        this.shader = shader;
        this.vertexFormat = vertexFormat;
        this.logicalDevice = IridiumRenderer.getInstance().getVulkanContext().getLogicalDevice().getHandle();
        this.swapChain = IridiumRenderer.getInstance().getVulkanContext().getSwapChain();
        this.graphicsPipelineState = new GraphicsPipelineState(GraphicsPipelineState.DEFAULT_BLEND_STATE, GraphicsPipelineState.DEFAULT_DEPTH_STATE,
                GraphicsPipelineState.DEFAULT_LOGICOP_STATE, GraphicsPipelineState.DEFAULT_COLORMASK);

        this.graphicsPipelineLayout = VK_NULL_HANDLE;
        this.graphicsPipelineCache = VK_NULL_HANDLE;
        this.graphicsPipelineHandle = VK_NULL_HANDLE;
    }

    @Override
    public void create()
    {
        this.createPipelineLayoutAndCache();

        try (MemoryStack memoryStack = MemoryStack.stackPush())
        {
            int swapChainImageFormat = IridiumRenderer.getInstance().getVulkanContext().getSwapChain().getImageFormat();
            int depthFormat = IridiumRenderer.getInstance().getVulkanContext().getPhysicalDevice().getDepthFormat();

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

            VkPipelineShaderStageCreateInfo.Buffer pipelineShaderStageCreateInfos = VkPipelineShaderStageCreateInfo.calloc(2, memoryStack);

            VkPipelineShaderStageCreateInfo pipelineVertexShaderStageCreateInfo = pipelineShaderStageCreateInfos.get(0)
                    .sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO)
                    .stage(VK_SHADER_STAGE_VERTEX_BIT)
                    .module(this.shader.getVertexShaderModule())
                    .pName(memoryStack.UTF8("main"));

            VkPipelineShaderStageCreateInfo pipelineFragmentShaderStageCreateInfo = pipelineShaderStageCreateInfos.get(1)
                    .sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO)
                    .stage(VK_SHADER_STAGE_FRAGMENT_BIT)
                    .module(this.shader.getFragmentShaderModule())
                    .pName(memoryStack.UTF8("main"));

            VkPipelineDynamicStateCreateInfo pipelineDynamicStateCreateInfo = VkPipelineDynamicStateCreateInfo.calloc(memoryStack)
                    .sType(VK_STRUCTURE_TYPE_PIPELINE_DYNAMIC_STATE_CREATE_INFO)
                    .pDynamicStates(memoryStack.ints(VK_DYNAMIC_STATE_VIEWPORT, VK_DYNAMIC_STATE_SCISSOR));

            VkPipelineVertexInputStateCreateInfo pipelineVertexInputStateCreateInfo = VkPipelineVertexInputStateCreateInfo.calloc(memoryStack)
                    .sType(VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO);

            if (this.vertexFormat != null)
            {
                int vertexFormatElementCount = this.vertexFormat.getElements().size();
                if (this.vertexFormat.getElements().stream().anyMatch(vertexFormatElement -> vertexFormatElement.getUsage() == VertexFormatElement.Usage.PADDING))
                    vertexFormatElementCount--;

                VkVertexInputBindingDescription.Buffer vertexInputBindingDescription = VkVertexInputBindingDescription.calloc(1, memoryStack)
                        .binding(0)
                        .stride(this.vertexFormat.getVertexSize())
                        .inputRate(VK_VERTEX_INPUT_RATE_VERTEX);

                VkVertexInputAttributeDescription.Buffer vertexInputAttributeDescriptions = VkVertexInputAttributeDescription.calloc(vertexFormatElementCount, memoryStack);

                for (int i = 0; i < vertexFormatElementCount; i++)
                {
                    VertexFormatElement vertexFormatElement = this.vertexFormat.getElements().get(i);
                    int vertexFormatElementVulkanFormat = VulkanUtils.getVertexFormatElementVulkanFormat(vertexFormatElement);
                    int vertexFormatOffset = ((VertexFormatAccessor) this.vertexFormat).getOffset(i);

                    vertexInputAttributeDescriptions.get(i)
                            .binding(0)
                            .location(i)
                            .format(vertexFormatElementVulkanFormat)
                            .offset(vertexFormatOffset);
                }

                pipelineVertexInputStateCreateInfo.pVertexBindingDescriptions(vertexInputBindingDescription);
                pipelineVertexInputStateCreateInfo.pVertexAttributeDescriptions(vertexInputAttributeDescriptions);
            }
            else
            {
                pipelineVertexInputStateCreateInfo.pVertexBindingDescriptions(null)
                        .pVertexAttributeDescriptions(null);
            }

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
                    .cullMode(this.graphicsPipelineState.cullState ? VK_CULL_MODE_BACK_BIT : VK_CULL_MODE_NONE)
                    .frontFace(VK_FRONT_FACE_CLOCKWISE)
                    .depthBiasEnable(false);

            // TODO: (Ayydan) Make multisampling usable and configurable. Currently, it's forced off.
            VkPipelineMultisampleStateCreateInfo pipelineMultisampleStateCreateInfo = VkPipelineMultisampleStateCreateInfo.calloc(memoryStack)
                    .sType(VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO)
                    .sampleShadingEnable(false)
                    .rasterizationSamples(VK_SAMPLE_COUNT_1_BIT);

            VkPipelineDepthStencilStateCreateInfo pipelineDepthStencilStateCreateInfo = VkPipelineDepthStencilStateCreateInfo.calloc(memoryStack)
                    .sType(VK_STRUCTURE_TYPE_PIPELINE_DEPTH_STENCIL_STATE_CREATE_INFO)
                    .depthTestEnable(this.graphicsPipelineState.depthState.depthTest)
                    .depthWriteEnable(this.graphicsPipelineState.depthState.depthMask)
                    .depthCompareOp(this.graphicsPipelineState.depthState.function)
                    .depthBoundsTestEnable(false)
                    .stencilTestEnable(false);

            pipelineDepthStencilStateCreateInfo.back()
                    .failOp(VK_STENCIL_OP_KEEP)
                    .passOp(VK_STENCIL_OP_KEEP)
                    .compareMask(VK_COMPARE_OP_ALWAYS);

            pipelineDepthStencilStateCreateInfo.front(pipelineDepthStencilStateCreateInfo.back());

            VkPipelineColorBlendAttachmentState.Buffer pipelineColorBlendAttachmentState = VkPipelineColorBlendAttachmentState.calloc(1, memoryStack)
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
            }

            VkPipelineColorBlendStateCreateInfo pipelineColorBlendStateCreateInfo = VkPipelineColorBlendStateCreateInfo.calloc(memoryStack)
                    .sType(VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO)
                    .logicOpEnable(this.graphicsPipelineState.logicOpState.enabled)
                    .logicOp(this.graphicsPipelineState.logicOpState.getLogicOp())
                    .pAttachments(pipelineColorBlendAttachmentState)
                    .blendConstants(memoryStack.floats(0.0f, 0.0f, 0.0f, 0.0f));

            VkPipelineRenderingCreateInfoKHR pipelineRenderingCreateInfo = VkPipelineRenderingCreateInfoKHR.calloc(memoryStack)
                    .sType(VK_STRUCTURE_TYPE_PIPELINE_RENDERING_CREATE_INFO)
                    .pColorAttachmentFormats(memoryStack.ints(swapChainImageFormat))
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

    @Override
    public void destroy()
    {
        vkDestroyPipeline(this.logicalDevice, this.graphicsPipelineHandle, null);
        vkDestroyPipelineLayout(this.logicalDevice, this.graphicsPipelineLayout, null);
        vkDestroyPipelineCache(this.logicalDevice, this.graphicsPipelineCache, null);
    }

    @Override
    public long getHandle()
    {
        return this.graphicsPipelineHandle;
    }

    private void createPipelineLayoutAndCache()
    {
        try (MemoryStack memoryStack = MemoryStack.stackPush())
        {
            VkPipelineLayoutCreateInfo pipelineLayoutCreateInfo = VkPipelineLayoutCreateInfo.calloc(memoryStack)
                    .sType(VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO)
                    .pSetLayouts(memoryStack.longs(VK_NULL_HANDLE)) // TODO: (Ayydxn) Fill this in when shaders create descriptor sets.
                    .pPushConstantRanges(null);

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
}
