package me.ayydxn.moonblast.renderer.exceptions;

import me.ayydxn.moonblast.renderer.debug.VulkanDebugUtils;

public class VulkanOperationException extends RuntimeException
{
    public VulkanOperationException(String message, int vkResultCode)
    {
        super(String.format("%s (Expression Result: %s)", message, VulkanDebugUtils.getVkResultString(vkResultCode)));
    }
}
