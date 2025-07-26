package me.ayydxn.moonblast;

import me.ayydxn.moonblast.buffers.VertexBuffer;
import me.ayydxn.moonblast.options.MoonblastRendererOptions;
import me.ayydxn.moonblast.renderer.CommandBuffer;
import me.ayydxn.moonblast.renderer.GraphicsContext;
import me.ayydxn.moonblast.renderer.GraphicsPipeline;
import me.ayydxn.moonblast.renderer.SwapChain;
import me.ayydxn.moonblast.renderer.utils.VulkanUtils;
import me.ayydxn.moonblast.shaders.MoonblastShaderCompiler;
import me.ayydxn.moonblast.utils.MoonblastConstants;
import org.jetbrains.annotations.ApiStatus;
import org.lwjgl.Version;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import static org.lwjgl.vulkan.KHRDynamicRendering.*;
import static org.lwjgl.vulkan.KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR;
import static org.lwjgl.vulkan.VK10.*;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT;

public class MoonblastRenderer
{
    private static MoonblastRenderer INSTANCE;

    // (Ayydxn) There is definitely a better way to handle this.
    public static final String VERSION = "2025.1.0";

    private final MoonblastRendererOptions moonblastRendererOptions;
    private final long windowHandle;

    private GraphicsContext graphicsContext;
    private CommandBuffer rendererCommandBuffer;
    private SwapChain activeSwapChain;
    private boolean skippingCurrentFrame;

    private MoonblastRenderer(long windowHandle, MoonblastRendererOptions moonblastRendererOptions)
    {
        MoonblastShaderCompiler.initialize();

        this.moonblastRendererOptions = moonblastRendererOptions;
        this.windowHandle = windowHandle;
    }

    public static void initialize(long windowHandle, MoonblastRendererOptions moonblastRendererOptions)
    {
        if (INSTANCE != null)
        {
            MoonblastConstants.LOGGER.warn("Moonblast cannot be initialized more than once!");
            return;
        }

        MoonblastConstants.LOGGER.info("Initializing Moonblast Renderer...\n- Version: {}\n- LWJGL Version: {}", VERSION,
                Version.getVersion());

        INSTANCE = new MoonblastRenderer(windowHandle, moonblastRendererOptions);
        INSTANCE.initialize();
    }

    private void initialize()
    {
        this.graphicsContext = new GraphicsContext(this.moonblastRendererOptions);
        this.graphicsContext.initialize();

        this.rendererCommandBuffer = new CommandBuffer(this.moonblastRendererOptions.rendererOptions.framesInFlight);
    }

    public void shutdown()
    {
        MoonblastShaderCompiler.getInstance().shutdown();

        this.rendererCommandBuffer.destroy();
        this.graphicsContext.destroy();
    }

    public void beginFrame(SwapChain swapChain)
    {
        VkCommandBuffer commandBuffer = this.rendererCommandBuffer.getCommandBuffer(0);
        int swapChainWidth = swapChain.getWidth();
        int swapChainHeight = swapChain.getHeight();
        long swapChainImage = swapChain.getCurrentImage();

        this.skippingCurrentFrame = swapChainWidth == 0 || swapChainHeight == 0;

        if (this.skippingCurrentFrame)
            return;

        try (MemoryStack memoryStack = MemoryStack.stackPush())
        {
            VkClearColorValue clearColorValue = VkClearColorValue.calloc(memoryStack)
                    .float32(0, 0.2f)
                    .float32(1, 0.2f)
                    .float32(2, 0.2f)
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

    public void draw(GraphicsPipeline graphicsPipeline, VertexBuffer vertexBuffer)
    {
        VkCommandBuffer commandBuffer = this.rendererCommandBuffer.getActiveCommandBuffer();

        try (MemoryStack memoryStack = MemoryStack.stackPush())
        {
            vkCmdBindPipeline(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, graphicsPipeline.getHandle());
            vkCmdBindVertexBuffers(commandBuffer, 0, memoryStack.longs(vertexBuffer.getHandle()), memoryStack.longs(0L));
            vkCmdDraw(commandBuffer, vertexBuffer.getSize() / Float.BYTES, 1, 0, 0);
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

    public static MoonblastRenderer getInstance()
    {
        if (INSTANCE == null)
            throw new IllegalStateException("Tried to access an instance of Moonblast before one was available!");

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

    public MoonblastRendererOptions getOptions()
    {
        return this.moonblastRendererOptions;
    }

    public long getWindowHandle()
    {
        return this.windowHandle;
    }
}
