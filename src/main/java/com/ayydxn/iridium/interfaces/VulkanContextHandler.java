package com.ayydxn.iridium.interfaces;

import com.ayydxn.iridium.render.vulkan.VulkanContext;
import com.ayydxn.iridium.render.vulkan.VulkanSwapChain;

public interface VulkanContextHandler
{
    default VulkanContext getVulkanContext() {
        return null;
    }

    default VulkanSwapChain getSwapChain() {
        return null;
    }
}
