package me.ayydxn.iridium;

import me.ayydxn.iridium.buffers.IndexBuffer;
import me.ayydxn.iridium.buffers.VertexBuffer;
import me.ayydxn.iridium.options.IridiumRendererOptions;
import me.ayydxn.iridium.renderer.CommandBuffer;
import me.ayydxn.iridium.renderer.GraphicsContext;
import me.ayydxn.iridium.renderer.GraphicsPipeline;
import me.ayydxn.iridium.renderer.SwapChain;
import me.ayydxn.iridium.renderer.utils.VulkanUtils;
import me.ayydxn.iridium.shaders.IridiumShaderCompiler;
import me.ayydxn.iridium.utils.IridiumConstants;
import org.lwjgl.Version;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import static org.lwjgl.vulkan.KHRDynamicRendering.*;
import static org.lwjgl.vulkan.KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR;
import static org.lwjgl.vulkan.VK10.*;

public class IridiumRenderer
{
    private static IridiumRenderer INSTANCE;

    // (Ayydxn) There is definitely a better way to handle this.
    public static final String VERSION = "2025.1.0";

    private final IridiumRendererOptions iridiumRendererOptions;
    private final long windowHandle;

    private GraphicsContext graphicsContext;
    private CommandBuffer rendererCommandBuffer;
    private SwapChain activeSwapChain;
    private boolean skippingCurrentFrame;

    private IridiumRenderer(long windowHandle, IridiumRendererOptions iridiumRendererOptions)
    {
        IridiumShaderCompiler.initialize();

        this.iridiumRendererOptions = iridiumRendererOptions;
        this.windowHandle = windowHandle;
    }

    public static void initialize(long windowHandle, IridiumRendererOptions iridiumRendererOptions)
    {
        if (INSTANCE != null)
        {
            IridiumConstants.LOGGER.warn("Iridium's renderer cannot be initialized more than once!");
            return;
        }

        IridiumConstants.LOGGER.info("Initializing Iridium Renderer...\n- Version: {}\n- LWJGL Version: {}", VERSION,
                Version.getVersion());

        INSTANCE = new IridiumRenderer(windowHandle, iridiumRendererOptions);
        INSTANCE.initialize();
    }

    private void initialize()
    {
        this.graphicsContext = new GraphicsContext(this.iridiumRendererOptions);
        this.graphicsContext.initialize();

        this.rendererCommandBuffer = new CommandBuffer(this.iridiumRendererOptions.rendererOptions.framesInFlight);
    }

    public void shutdown()
    {
        IridiumShaderCompiler.getInstance().shutdown();

        this.rendererCommandBuffer.destroy();
        this.graphicsContext.destroy();
    }

    public void beginFrame(SwapChain swapChain)
    {
        VkCommandBuffer commandBuffer = this.rendererCommandBuffer.getCommandBuffer(0);
        int swapChainWidth = swapChain.getWidth();
        int swapChainHeight = swapChain.getHeight();

        this.skippingCurrentFrame = swapChainWidth == 0 || swapChainHeight == 0;

        if (this.skippingCurrentFrame)
            return;

        try (MemoryStack memoryStack = MemoryStack.stackPush())
        {
            swapChain.beginFrame(memoryStack);

            VkClearColorValue clearColorValue = VkClearColorValue.calloc(memoryStack)
                    .float32(0, 0.05f)
                    .float32(1, 0.05f)
                    .float32(2, 0.05f)
                    .float32(3, 1.0f);

            VkRect2D renderingArea = VkRect2D.calloc(memoryStack)
                    .extent(VkExtent2D.calloc(memoryStack).set(swapChainWidth, swapChainHeight))
                    .offset(VkOffset2D.calloc(memoryStack).set(0, 0));

            VkRenderingAttachmentInfoKHR.Buffer colorRenderingAttachmentInfo = VkRenderingAttachmentInfoKHR.calloc(1, memoryStack)
                    .sType(VK_STRUCTURE_TYPE_RENDERING_ATTACHMENT_INFO_KHR)
                    .imageView(swapChain.getCurrentImageView())
                    .imageLayout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL)
                    .loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR)
                    .storeOp(VK_ATTACHMENT_STORE_OP_STORE)
                    .clearValue(VkClearValue.calloc(memoryStack).color(clearColorValue));

            VkRenderingInfoKHR renderingInfo = VkRenderingInfoKHR.calloc(memoryStack)
                    .sType(VK_STRUCTURE_TYPE_RENDERING_INFO_KHR)
                    .renderArea(renderingArea)
                    .layerCount(1)
                    .pColorAttachments(colorRenderingAttachmentInfo);

            VkViewport.Buffer viewport = VkViewport.calloc(1, memoryStack)
                    .width((float) swapChainWidth)
                    .height((float) swapChainHeight)
                    .minDepth(0.0f)
                    .maxDepth(1.0f);

            VkRect2D.Buffer scissor = VkRect2D.calloc(1, memoryStack)
                    .extent(VkExtent2D.calloc(memoryStack).set(swapChainWidth, swapChainHeight))
                    .offset(VkOffset2D.calloc(memoryStack).set(0, 0));

            long swapChainImage = swapChain.getCurrentImage();

            this.rendererCommandBuffer.begin();

            VulkanUtils.transitionImageLayout(commandBuffer, swapChainImage, VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT,
                    VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT, 0, VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT, VK_IMAGE_LAYOUT_UNDEFINED,
                    VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);

            vkCmdBeginRenderingKHR(commandBuffer, renderingInfo);
            vkCmdSetViewport(commandBuffer, 0, viewport);
            vkCmdSetScissor(commandBuffer, 0, scissor);
        }

        this.activeSwapChain = swapChain;
    }

    public void draw(GraphicsPipeline graphicsPipeline, VertexBuffer vertexBuffer, IndexBuffer indexBuffer)
    {
        VkCommandBuffer commandBuffer = this.rendererCommandBuffer.getActiveCommandBuffer();

        try (MemoryStack memoryStack = MemoryStack.stackPush())
        {
            graphicsPipeline.bind(commandBuffer);

            vkCmdBindVertexBuffers(commandBuffer, 0, memoryStack.longs(vertexBuffer.getHandle()), memoryStack.longs(0L));
            vkCmdBindIndexBuffer(commandBuffer, indexBuffer.getHandle(), 0, VK_INDEX_TYPE_UINT32);

            vkCmdDrawIndexed(commandBuffer, indexBuffer.getCount(), 1, 0, 0, 0);
        }
    }

    public void endFrame()
    {
        if (this.skippingCurrentFrame)
            return;

        VkCommandBuffer commandBuffer = this.rendererCommandBuffer.getActiveCommandBuffer();
        long swapChainImage = this.activeSwapChain.getCurrentImage();

        vkCmdEndRenderingKHR(commandBuffer);

        VulkanUtils.transitionImageLayout(commandBuffer, swapChainImage, VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT,
                VK_PIPELINE_STAGE_BOTTOM_OF_PIPE_BIT, VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT, 0, VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL,
                VK_IMAGE_LAYOUT_PRESENT_SRC_KHR);

        this.rendererCommandBuffer.end();
        this.rendererCommandBuffer.submit();
    }

    public static IridiumRenderer getInstance()
    {
        if (INSTANCE == null)
            throw new IllegalStateException("Tried to access an instance of Iridium's renderer before one was available!");

        return INSTANCE;
    }

    public static String getVersion()
    {
        return VERSION;
    }

    public GraphicsContext getGraphicsContext()
    {
        return this.graphicsContext;
    }

    public IridiumRendererOptions getOptions()
    {
        return this.iridiumRendererOptions;
    }

    public long getWindowHandle()
    {
        return this.windowHandle;
    }
}
