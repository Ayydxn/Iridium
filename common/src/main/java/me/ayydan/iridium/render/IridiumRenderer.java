package me.ayydan.iridium.render;

import me.ayydan.iridium.render.vulkan.VulkanContext;
import me.ayydan.iridium.subsystems.IridiumSubsystemManager;
import me.ayydan.iridium.utils.logging.IridiumLogger;

public class IridiumRenderer
{
    private static final IridiumRendererSubsystem RENDERER_SUBSYSTEM = (IridiumRendererSubsystem) IridiumSubsystemManager.getInstance().getSubsystemInstance(IridiumRendererSubsystem.class);

    public static void beginFrame()
    {
        RENDERER_SUBSYSTEM.beginFrame();
    }

    public static void endFrame()
    {
        RENDERER_SUBSYSTEM.endFrame();
    }

    public static IridiumLogger getLogger()
    {
        return RENDERER_SUBSYSTEM.getLogger();
    }

    public static VulkanContext getVulkanContext()
    {
        return RENDERER_SUBSYSTEM.getVulkanContext();
    }

    public static boolean isCurrentFrameBeingSkipped()
    {
        return RENDERER_SUBSYSTEM.isCurrentFrameBeingSkipped();
    }

    public static int getCurrentFrameIndex()
    {
        return RENDERER_SUBSYSTEM.currentFrameIndex;
    }

    public static void setCurrentFrameIndex(int currentFrameIndex)
    {
        RENDERER_SUBSYSTEM.currentFrameIndex = currentFrameIndex;
    }
}
