package com.ayydxn.iridium.render.vulkan.exceptions;

import com.ayydxn.iridium.render.vulkan.util.VulkanDebugUtils;

public class VulkanOperationException extends RuntimeException
{
    public VulkanOperationException(String message, int vkResultCode)
    {
        super(String.format("%s (Expression Result: %s)", message, VulkanDebugUtils.getVkResultString(vkResultCode)));
    }
}
