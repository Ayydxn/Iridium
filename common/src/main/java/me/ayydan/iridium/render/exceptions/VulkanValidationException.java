package me.ayydan.iridium.render.exceptions;

import me.ayydan.iridium.render.vulkan.utils.VulkanDebugUtils;

public class VulkanValidationException extends RuntimeException
{
    public VulkanValidationException(String message, int vkResultCode)
    {
        super(String.format("%s (Expression Result: %s)", message, VulkanDebugUtils.getVkResultString(vkResultCode)));
    }
}
