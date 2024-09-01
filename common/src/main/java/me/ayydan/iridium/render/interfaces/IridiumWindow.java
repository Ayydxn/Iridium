package me.ayydan.iridium.render.interfaces;

import me.ayydan.iridium.render.vulkan.VulkanSwapChain;

public interface IridiumWindow
{
    default VulkanSwapChain getSwapChain() {
        return null;
    }
}
