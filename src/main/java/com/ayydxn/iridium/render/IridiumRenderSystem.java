package com.ayydxn.iridium.render;

import com.ayydxn.iridium.render.vulkan.VulkanContext;
import net.minecraft.client.Minecraft;
import org.lwjgl.vulkan.VkPhysicalDeviceProperties;

public class IridiumRenderSystem
{
    public static void initRenderer()
    {
        IridiumRenderer.initialize();
    }

    public static int getMaxSupportedTextureSize()
    {
        VulkanContext vulkanContext = Minecraft.getInstance().getWindow().getVulkanContext();
        VkPhysicalDeviceProperties physicalDeviceProperties = vulkanContext.getPhysicalDevice().getProperties();

        return physicalDeviceProperties.limits().maxImageDimension2D();
    }
}
