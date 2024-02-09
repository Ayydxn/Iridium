package me.ayydan.iridium.render;

import me.ayydan.iridium.render.vulkan.VulkanContext;
import me.ayydan.iridium.subsystems.IridiumSubsystem;
import me.ayydan.iridium.utils.logging.IridiumLogger;
import net.minecraft.client.MinecraftClient;

public class IridiumRendererSubsystem extends IridiumSubsystem
{
    public int currentFrameIndex;

    private IridiumLogger rendererLogger;
    private VulkanContext vulkanContext;
    private boolean shouldSkipFrame;

    @Override
    public void initialize()
    {
        this.rendererLogger = new IridiumLogger("Iridium Renderer");
        this.rendererLogger.info("Initializing Iridium renderer...");

        this.vulkanContext = new VulkanContext();
        this.vulkanContext.create();
    }

    @Override
    public void shutdown()
    {
        this.vulkanContext.destroy();
    }

    @Override
    public boolean shouldInitializeSubsystem()
    {
        return true;
    }

    public void beginFrame()
    {
        int swapChainWidth = this.vulkanContext.getSwapChain().getWidth();
        int swapChainHeight = this.vulkanContext.getSwapChain().getHeight();

        this.shouldSkipFrame = swapChainWidth == 0 || swapChainHeight == 0;

        MinecraftClient.getInstance().skipGameRender = this.shouldSkipFrame;

        if (this.shouldSkipFrame)
            return;
    }

    public void endFrame()
    {
        // TODO: (Ayydan) Work that needs to be done when we end a frame goes here.
    }

    public boolean isCurrentFrameBeingSkipped()
    {
        return this.shouldSkipFrame;
    }

    @Override
    public String getName()
    {
        return "Iridium Renderer";
    }

    public IridiumLogger getLogger()
    {
        return this.rendererLogger;
    }

    public VulkanContext getVulkanContext()
    {
        return this.vulkanContext;
    }
}
