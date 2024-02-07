package me.ayydan.iridium.render;

import me.ayydan.iridium.render.vulkan.VulkanContext;
import me.ayydan.iridium.subsystems.IridiumSubsystem;
import me.ayydan.iridium.utils.logging.IridiumLogger;

public class IridiumRendererSubsystem extends IridiumSubsystem
{
    private IridiumLogger rendererLogger;
    private VulkanContext vulkanContext;

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
