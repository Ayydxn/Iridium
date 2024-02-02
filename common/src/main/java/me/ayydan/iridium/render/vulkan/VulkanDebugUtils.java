package me.ayydan.iridium.render.vulkan;

import com.google.common.collect.Lists;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.*;

import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.List;

import static org.lwjgl.vulkan.EXTDebugReport.VK_ERROR_VALIDATION_FAILED_EXT;
import static org.lwjgl.vulkan.EXTFullScreenExclusive.VK_ERROR_FULL_SCREEN_EXCLUSIVE_MODE_LOST_EXT;
import static org.lwjgl.vulkan.EXTImageDrmFormatModifier.VK_ERROR_INVALID_DRM_FORMAT_MODIFIER_PLANE_LAYOUT_EXT;
import static org.lwjgl.vulkan.KHRDeferredHostOperations.*;
import static org.lwjgl.vulkan.KHRDisplaySwapchain.VK_ERROR_INCOMPATIBLE_DISPLAY_KHR;
import static org.lwjgl.vulkan.KHRGlobalPriority.VK_ERROR_NOT_PERMITTED_KHR;
import static org.lwjgl.vulkan.KHRSurface.VK_ERROR_NATIVE_WINDOW_IN_USE_KHR;
import static org.lwjgl.vulkan.KHRSurface.VK_ERROR_SURFACE_LOST_KHR;
import static org.lwjgl.vulkan.KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR;
import static org.lwjgl.vulkan.KHRSwapchain.VK_SUBOPTIMAL_KHR;
import static org.lwjgl.vulkan.NVGLSLShader.VK_ERROR_INVALID_SHADER_NV;
import static org.lwjgl.vulkan.VK10.*;
import static org.lwjgl.vulkan.VK11.VK_ERROR_INVALID_EXTERNAL_HANDLE;
import static org.lwjgl.vulkan.VK11.VK_ERROR_OUT_OF_POOL_MEMORY;
import static org.lwjgl.vulkan.VK12.VK_ERROR_FRAGMENTATION;
import static org.lwjgl.vulkan.VK12.VK_ERROR_INVALID_OPAQUE_CAPTURE_ADDRESS;
import static org.lwjgl.vulkan.VK13.VK_PIPELINE_COMPILE_REQUIRED;

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

    public static String getVkResultString(int vkResult)
    {
        return switch (vkResult)
        {
            case VK_SUCCESS -> "VK_SUCCESS";
            case VK_NOT_READY -> "VK_NOT_READY";
            case VK_TIMEOUT -> "VK_TIMEOUT";
            case VK_EVENT_SET -> "VK_EVENT_SET";
            case VK_EVENT_RESET -> "VK_EVENT_RESET";
            case VK_INCOMPLETE -> "VK_INCOMPLETE";
            case VK_ERROR_OUT_OF_HOST_MEMORY -> "VK_ERROR_OUT_OF_HOST_MEMORY";
            case VK_ERROR_OUT_OF_DEVICE_MEMORY -> "VK_ERROR_OUT_OF_DEVICE_MEMORY";
            case VK_ERROR_INITIALIZATION_FAILED -> "VK_ERROR_INITIALIZATION_FAILED";
            case VK_ERROR_DEVICE_LOST -> "VK_ERROR_DEVICE_LOST";
            case VK_ERROR_MEMORY_MAP_FAILED -> "VK_ERROR_MEMORY_MAP_FAILED";
            case VK_ERROR_LAYER_NOT_PRESENT -> "VK_ERROR_LAYER_NOT_PRESENT";
            case VK_ERROR_EXTENSION_NOT_PRESENT -> "VK_ERROR_EXTENSION_NOT_PRESENT";
            case VK_ERROR_FEATURE_NOT_PRESENT -> "VK_ERROR_FEATURE_NOT_PRESENT";
            case VK_ERROR_INCOMPATIBLE_DRIVER -> "VK_ERROR_INCOMPATIBLE_DRIVER";
            case VK_ERROR_TOO_MANY_OBJECTS -> "VK_ERROR_TOO_MANY_OBJECTS";
            case VK_ERROR_FORMAT_NOT_SUPPORTED -> "VK_ERROR_FORMAT_NOT_SUPPORTED";
            case VK_ERROR_FRAGMENTED_POOL -> "VK_ERROR_FRAGMENTED_POOL";
            case VK_ERROR_UNKNOWN -> "VK_ERROR_UNKNOWN";

            case VK_ERROR_OUT_OF_POOL_MEMORY -> "VK_ERROR_OUT_OF_POOL_MEMORY";
            case VK_ERROR_INVALID_EXTERNAL_HANDLE -> "VK_ERROR_INVALID_EXTERNAL_HANDLE";

            case VK_ERROR_FRAGMENTATION -> "VK_ERROR_FRAGMENTATION";
            case VK_ERROR_INVALID_OPAQUE_CAPTURE_ADDRESS -> "VK_ERROR_INVALID_OPAQUE_CAPTURE_ADDRESS";

            case VK_PIPELINE_COMPILE_REQUIRED -> "VK_PIPELINE_COMPILE_REQUIRED";

            case VK_ERROR_SURFACE_LOST_KHR -> "VK_ERROR_SURFACE_LOST_KHR";
            case VK_ERROR_NATIVE_WINDOW_IN_USE_KHR -> "VK_ERROR_NATIVE_WINDOW_IN_USE_KHR";
            case VK_SUBOPTIMAL_KHR -> "VK_SUBOPTIMAL_KHR";
            case VK_ERROR_OUT_OF_DATE_KHR -> "VK_ERROR_OUT_OF_DATE_KHR";
            case VK_ERROR_INCOMPATIBLE_DISPLAY_KHR -> "VK_ERROR_INCOMPATIBLE_DISPLAY_KHR";
            case VK_ERROR_VALIDATION_FAILED_EXT -> "VK_ERROR_VALIDATION_FAILED_EXT ";
            case VK_ERROR_INVALID_SHADER_NV -> "VK_ERROR_INVALID_SHADER_NV";
            case VK_ERROR_INVALID_DRM_FORMAT_MODIFIER_PLANE_LAYOUT_EXT -> "VK_ERROR_INVALID_DRM_FORMAT_MODIFIER_PLANE_LAYOUT_EXT";
            case VK_ERROR_NOT_PERMITTED_KHR -> "VK_ERROR_NOT_PERMITTED_KHR";
            case VK_ERROR_FULL_SCREEN_EXCLUSIVE_MODE_LOST_EXT -> "VK_ERROR_FULL_SCREEN_EXCLUSIVE_MODE_LOST_EXT ";
            case VK_THREAD_IDLE_KHR -> "VK_THREAD_IDLE_KHR";
            case VK_THREAD_DONE_KHR -> "VK_THREAD_DONE_KHR";
            case VK_OPERATION_DEFERRED_KHR -> "VK_OPERATION_DEFERRED_KHR";
            case VK_OPERATION_NOT_DEFERRED_KHR -> "VK_OPERATION_NOT_DEFERRED_KHR";

            default -> throw new IllegalStateException("Failed to convert VkResult to a string: " + vkResult);
        };
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
