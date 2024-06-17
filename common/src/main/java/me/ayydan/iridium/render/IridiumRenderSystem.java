package me.ayydan.iridium.render;

import org.joml.Vector4f;

public class IridiumRenderSystem
{
    private static Vector4f clearColor;

    public static boolean isCullingEnabled = true;

    public static void initRenderer()
    {
        IridiumRenderer.initialize();
    }

    public static void setClearColor(float red, float green, float blue, float alpha)
    {
        IridiumRenderSystem.clearColor = new Vector4f(red, green, blue, alpha);
    }

    public static Vector4f getClearColor()
    {
        return IridiumRenderSystem.clearColor;
    }

    public static int getMaxSupportedTextureSize()
    {
        return IridiumRenderer.getInstance().getVulkanContext().getPhysicalDevice().getProperties().limits().maxImageDimension2D();
    }
}
