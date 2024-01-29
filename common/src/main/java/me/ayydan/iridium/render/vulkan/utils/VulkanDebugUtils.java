package me.ayydan.iridium.render.vulkan.utils;

import com.google.common.collect.Lists;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.*;

import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.List;

import static org.lwjgl.vulkan.VK10.*;

public class VulkanDebugUtils
{
    public static int vkCreateDebugUtilsMessengerEXT(VkInstance instance, VkDebugUtilsMessengerCreateInfoEXT debugMessengerCreateInfo,
                                                      VkAllocationCallbacks allocationCallbacks, LongBuffer pDebugMessenger)
    {
        if (vkGetInstanceProcAddr(instance, "vkCreateDebugUtilsMessengerEXT") != MemoryUtil.NULL)
            return EXTDebugUtils.vkCreateDebugUtilsMessengerEXT(instance, debugMessengerCreateInfo, allocationCallbacks, pDebugMessenger);

        return VK_ERROR_EXTENSION_NOT_PRESENT;
    }

    public static void vkDestroyDebugUtilsMessengerEXT(VkInstance instance, long debugMessenger, VkAllocationCallbacks allocationCallbacks)
    {
        if (vkGetInstanceProcAddr(instance, "vkDestroyDebugUtilsMessengerEXT") != MemoryUtil.NULL)
            EXTDebugUtils.vkDestroyDebugUtilsMessengerEXT(instance, debugMessenger, allocationCallbacks);
    }

    public static List<String> getSupportedValidationLayers()
    {
        List<String> supportedInstanceLayers = VulkanDebugUtils.getSupportedLayers();
        List<String> supportedValidationLayers = Lists.newArrayList();

        // Main validation layers
        if (supportedInstanceLayers.contains("VK_LAYER_KHRONOS_validation"))
        {
            supportedValidationLayers.add("VK_LAYER_KHRONOS_validation");
            return supportedValidationLayers;
        }

        // Fallback #1
        if (supportedInstanceLayers.contains("VK_LAYER_LUNARG_standard_validation"))
        {
            supportedValidationLayers.add("VK_LAYER_LUNARG_standard_validation");
            return supportedValidationLayers;
        }

        // Fallback #2
        List<String> fallbackValidationLayers = Lists.newArrayList("VK_LAYER_GOOGLE_threading", "VK_LAYER_LUNARG_parameter_validation",
                "VK_LAYER_LUNARG_object_tracker", "VK_LAYER_LUNARG_core_validation", "VK_LAYER_GOOGLE_unique_objects");

        return fallbackValidationLayers.stream()
                .filter(supportedInstanceLayers::contains)
                .toList();
    }

    private static List<String> getSupportedLayers()
    {
        try (MemoryStack memoryStack = MemoryStack.stackPush())
        {
            IntBuffer instanceLayerCount = memoryStack.ints(0);
            vkEnumerateInstanceLayerProperties(instanceLayerCount, null);

            VkLayerProperties.Buffer instanceLayers = VkLayerProperties.calloc(instanceLayerCount.get(0), memoryStack);
            vkEnumerateInstanceLayerProperties(instanceLayerCount, instanceLayers);

            return instanceLayers.stream()
                    .map(VkLayerProperties::layerNameString)
                    .toList();
        }
    }
}
