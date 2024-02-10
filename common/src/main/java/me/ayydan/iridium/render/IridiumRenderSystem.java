package me.ayydan.iridium.render;

import dev.architectury.platform.Platform;
import me.ayydan.iridium.subsystems.IridiumSubsystemManager;

public class IridiumRenderSystem
{
    public static void initRenderer()
    {
        if (Platform.isNeoForge())
        {
            IridiumSubsystemManager.initialize();
            IridiumSubsystemManager.getInstance().addSubsystem(new IridiumRendererSubsystem());
        }
        else
        {
            IridiumSubsystemManager.getInstance().addSubsystem(new IridiumRendererSubsystem());
        }
    }

    public static int getMaxSupportedTextureSize()
    {
        return IridiumRenderer.getVulkanContext().getPhysicalDevice().getProperties().limits().maxImageDimension2D();
    }
}
