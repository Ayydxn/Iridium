package me.ayydan.iridium.render;

import me.ayydan.iridium.render.vulkan.VulkanContext;
import me.ayydan.iridium.utils.logging.IridiumLogger;
import net.minecraft.client.MinecraftClient;

public class IridiumRenderer
{
    private static IridiumRenderer INSTANCE;
    private static IridiumLogger LOGGER;

    public int currentFrameIndex;

    private final VulkanContext vulkanContext;

    private boolean shouldSkipFrame;

    private IridiumRenderer()
    {
        this.vulkanContext = new VulkanContext();
        this.vulkanContext.create();
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

        INSTANCE = new IridiumRenderer();
    }

    public void shutdown()
    {
        this.vulkanContext.destroy();

        LOGGER = null;
        INSTANCE = null;
    }

    public void beginFrame()
    {
        int swapChainWidth = this.vulkanContext.getSwapChain().getWidth();
        int swapChainHeight = this.vulkanContext.getSwapChain().getHeight();

        this.shouldSkipFrame = swapChainWidth == 0 || swapChainHeight == 0;

        MinecraftClient.getInstance().skipGameRender = this.shouldSkipFrame;

        if (this.shouldSkipFrame)
            return;

        // TODO: (Ayydan) Do Vulkan related setup so that we can start rendering a frame.
    }

    public void endFrame()
    {
        // TODO: (Ayydan) Work that needs to be done when we end a frame goes here.
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
