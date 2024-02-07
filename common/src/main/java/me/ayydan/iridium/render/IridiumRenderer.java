package me.ayydan.iridium.render;

import me.ayydan.iridium.render.vulkan.VulkanContext;
import me.ayydan.iridium.subsystems.IridiumSubsystemManager;
import me.ayydan.iridium.utils.logging.IridiumLogger;

public class IridiumRenderer
{
    public static IridiumLogger getLogger()
    {
        return ((IridiumRendererSubsystem) IridiumSubsystemManager.getInstance().getSubsystemInstance(IridiumRendererSubsystem.class)).getLogger();
    }

    public static VulkanContext getVulkanContext()
    {
        return ((IridiumRendererSubsystem) IridiumSubsystemManager.getInstance().getSubsystemInstance(IridiumRendererSubsystem.class)).getVulkanContext();
    }
}
