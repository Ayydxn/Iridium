package me.ayydan.iridium.render.vulkan;

import me.ayydan.iridium.render.exceptions.VulkanValidationException;

import static org.lwjgl.vulkan.VK10.VK_SUCCESS;

public class VulkanValidation
{
    public static void vkCheckResult(int expression)
    {
        if (expression != VK_SUCCESS)
            throw new VulkanValidationException("A (presumably) Vulkan related expression failed!", expression);
    }
}
