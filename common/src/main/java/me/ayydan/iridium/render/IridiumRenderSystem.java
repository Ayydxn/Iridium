package me.ayydan.iridium.render;

public class IridiumRenderSystem
{
    public static void initRenderer()
    {
        IridiumRenderer.initialize();
    }

    public static int getMaxSupportedTextureSize()
    {
        return IridiumRenderer.getInstance().getVulkanContext().getPhysicalDevice().getProperties().limits().maxImageDimension2D();
    }
}
