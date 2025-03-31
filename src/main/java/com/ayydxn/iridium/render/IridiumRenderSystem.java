package com.ayydxn.iridium.render;

import com.ayydxn.iridium.render.vulkan.VulkanContext;
import net.minecraft.client.Minecraft;
import org.joml.Vector4f;
import org.lwjgl.vulkan.VkPhysicalDeviceProperties;

public class IridiumRenderSystem
{
    private static final Vector4f clearColor = new Vector4f(0.15f, 0.15f, 0.15f, 1.0f);

    private static boolean shouldPerformDepthTesting = true;

    public static void initRenderer()
    {
        IridiumRenderer.initialize();
    }

    public static void clearAttachments(int mask)
    {
        IridiumRenderer.getInstance().clearAttachments(mask);
    }

    public static void enableDepthTesting()
    {
        IridiumRenderSystem.shouldPerformDepthTesting = true;
    }

    public static void disableDepthTesting()
    {
        IridiumRenderSystem.shouldPerformDepthTesting = false;
    }

    public static Vector4f getClearColor()
    {
        return IridiumRenderSystem.clearColor;
    }

    public static void setClearColor(float red, float green, float blue, float alpha)
    {
        IridiumRenderSystem.clearColor.x = red;
        IridiumRenderSystem.clearColor.y = green;
        IridiumRenderSystem.clearColor.z = blue;
        IridiumRenderSystem.clearColor.w = alpha;
    }

    public static int getMaxSupportedTextureSize()
    {
        VulkanContext vulkanContext = Minecraft.getInstance().getWindow().getVulkanContext();
        VkPhysicalDeviceProperties physicalDeviceProperties = vulkanContext.getPhysicalDevice().getProperties();

        return physicalDeviceProperties.limits().maxImageDimension2D();
    }
}
