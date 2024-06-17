package me.ayydan.iridium.render;

import me.ayydan.iridium.render.shader.IridiumShaderCompiler;
import me.ayydan.iridium.render.vulkan.VulkanCommandBuffer;
import me.ayydan.iridium.render.vulkan.VulkanContext;
import me.ayydan.iridium.render.vulkan.utils.VulkanUtils;
import me.ayydan.iridium.utils.logging.IridiumLogger;
import net.minecraft.client.Minecraft;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import static org.lwjgl.vulkan.KHRDynamicRendering.*;
import static org.lwjgl.vulkan.KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR;
import static org.lwjgl.vulkan.VK10.*;

public class IridiumRenderer
{
    private static IridiumRenderer INSTANCE;
    private static IridiumLogger LOGGER;

    private final VulkanContext vulkanContext;
    private final VulkanCommandBuffer rendererCommandBuffer;

    private boolean shouldSkipFrame;

    public int currentFrameIndex;

    private IridiumRenderer()
    {
        INSTANCE = this;

        this.vulkanContext = new VulkanContext();
        this.vulkanContext.create();

        this.rendererCommandBuffer = new VulkanCommandBuffer(0);

        IridiumShaderCompiler.initialize();
    }

    public static void initialize()
    {
        if (INSTANCE != null)
        {
            LOGGER.warn("Iridium's renderer has already been initialized! You cannot initialize Iridium's renderer more than once!");
            return;
        }

        LOGGER = new IridiumLogger("Iridium Renderer");
        LOGGER.info("Initializing Iridium Renderer..");

        // Call the constructor to initialize the renderer.
        new IridiumRenderer();
    }

    public void shutdown()
    {
        IridiumShaderCompiler.getInstance().shutdown();

        this.rendererCommandBuffer.destroy();
        this.vulkanContext.destroy();

        LOGGER = null;
        INSTANCE = null;
    }

    public void beginFrame()
    {
        int swapChainWidth = this.vulkanContext.getSwapChain().getWidth();
        int swapChainHeight = this.vulkanContext.getSwapChain().getHeight();

        this.shouldSkipFrame = swapChainWidth == 0 || swapChainHeight == 0;

        Minecraft.getInstance().noRender = this.shouldSkipFrame;

        if (this.shouldSkipFrame)
            return;

        VkCommandBuffer commandBuffer = this.rendererCommandBuffer.getActiveCommandBuffer();
        long swapChainImage = this.vulkanContext.getSwapChain().getImages().get(this.currentFrameIndex);
        long swapChainDepthImage = this.vulkanContext.getSwapChain().getDepthImage().image();

        try (MemoryStack memoryStack = MemoryStack.stackPush())
        {
            VkClearColorValue clearColorValue = VkClearColorValue.calloc(memoryStack)
                    .float32(0, IridiumRenderSystem.getClearColor().x())
                    .float32(1, IridiumRenderSystem.getClearColor().y())
                    .float32(2, IridiumRenderSystem.getClearColor().z())
                    .float32(3, IridiumRenderSystem.getClearColor().w());

            VkClearDepthStencilValue clearDepthStencilValue = VkClearDepthStencilValue.calloc(memoryStack)
                    .depth(1.0f)
                    .stencil(0);

            VkRect2D renderingArea = VkRect2D.calloc(memoryStack)
                    .extent(VkExtent2D.calloc(memoryStack).set(swapChainWidth, swapChainHeight))
                    .offset(VkOffset2D.calloc(memoryStack).set(0, 0));

            VkRenderingAttachmentInfoKHR.Buffer colorRenderingAttachmentInfo = VkRenderingAttachmentInfoKHR.calloc(1, memoryStack)
                    .sType(VK_STRUCTURE_TYPE_RENDERING_ATTACHMENT_INFO_KHR)
                    .imageView(this.vulkanContext.getSwapChain().getImageViews().get(this.currentFrameIndex))
                    .imageLayout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL)
                    .loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR)
                    .storeOp(VK_ATTACHMENT_STORE_OP_STORE)
                    .clearValue(VkClearValue.calloc(memoryStack).color(clearColorValue));

            VkRenderingAttachmentInfoKHR depthStencilAttachmentInfo = VkRenderingAttachmentInfoKHR.calloc(memoryStack)
                    .sType(VK_STRUCTURE_TYPE_RENDERING_ATTACHMENT_INFO_KHR)
                    .imageView(this.vulkanContext.getSwapChain().getDepthImageView())
                    .imageLayout(VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL)
                    .loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR)
                    .storeOp(VK_ATTACHMENT_STORE_OP_STORE)
                    .clearValue(VkClearValue.calloc(memoryStack).depthStencil(clearDepthStencilValue));

            VkRenderingInfoKHR renderingInfo = VkRenderingInfoKHR.calloc(memoryStack)
                    .sType(VK_STRUCTURE_TYPE_RENDERING_INFO_KHR)
                    .renderArea(renderingArea)
                    .layerCount(1)
                    .pColorAttachments(colorRenderingAttachmentInfo)
                    .pDepthAttachment(depthStencilAttachmentInfo)
                    .pStencilAttachment(depthStencilAttachmentInfo);

            VkViewport.Buffer viewport = VkViewport.calloc(1, memoryStack)
                    .width((float) swapChainWidth)
                    .height((float) swapChainHeight)
                    .minDepth(0.0f)
                    .maxDepth(1.0f);

            VkRect2D.Buffer scissor = VkRect2D.calloc(1, memoryStack)
                    .extent(VkExtent2D.calloc(memoryStack).set(swapChainWidth, swapChainHeight))
                    .offset(VkOffset2D.calloc(memoryStack).set(0, 0));

            this.rendererCommandBuffer.begin();

            VulkanUtils.transitionImageLayout(commandBuffer, swapChainImage, VK_IMAGE_LAYOUT_UNDEFINED, VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);
            VulkanUtils.transitionImageLayout(commandBuffer, swapChainDepthImage, VK_IMAGE_LAYOUT_UNDEFINED, VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);

            vkCmdBeginRenderingKHR(commandBuffer, renderingInfo);
            vkCmdSetViewport(commandBuffer, 0, viewport);
            vkCmdSetScissor(commandBuffer, 0, scissor);
        }
    }

    public void endFrame()
    {
        if (this.shouldSkipFrame)
            return;

        VkCommandBuffer commandBuffer = this.rendererCommandBuffer.getActiveCommandBuffer();
        long swapChainImage = this.vulkanContext.getSwapChain().getImages().get(this.currentFrameIndex);

        vkCmdEndRenderingKHR(commandBuffer);

        VulkanUtils.transitionImageLayout(commandBuffer, swapChainImage, VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL, VK_IMAGE_LAYOUT_PRESENT_SRC_KHR);

        this.rendererCommandBuffer.end();
        this.rendererCommandBuffer.submit();
    }

    public static IridiumRenderer getInstance()
    {
        if (INSTANCE == null)
            throw new IllegalStateException("Tried to access an instance of Iridium's renderer when one isn't available!");

        return INSTANCE;
    }

    public static IridiumLogger getLogger()
    {
        return LOGGER;
    }

    public VulkanContext getVulkanContext()
    {
        return this.vulkanContext;
    }

    public boolean isCurrentFrameBeingSkipped()
    {
        return this.shouldSkipFrame;
    }
}
