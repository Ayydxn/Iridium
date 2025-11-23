package me.ayydxn.iridium.texture;

import static org.lwjgl.vulkan.VK10.VK_FORMAT_R8G8B8A8_SRGB;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R8G8B8_SRGB;

public enum ImageFormat
{
    // (Ayydxn) Maybe add more in the future, but we'll just have these two since these are the only normal ones Minecraft supports.
    RGBA(VK_FORMAT_R8G8B8A8_SRGB),
    RGB(VK_FORMAT_R8G8B8_SRGB);

    private final int vulkanID;

    ImageFormat(int vulkanID)
    {
        this.vulkanID = vulkanID;
    }

    public int getVulkanID()
    {
        return this.vulkanID;
    }
}
