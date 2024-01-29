package me.ayydan.iridium.render.vulkan;

import dev.architectury.platform.Platform;
import me.ayydan.iridium.render.IridiumRenderer;
import me.ayydan.iridium.render.exceptions.IridiumRendererException;
import me.ayydan.iridium.render.vulkan.utils.VulkanDebugUtils;
import me.ayydan.iridium.utils.PointerUtils;
import me.ayydan.iridium.utils.VersioningUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.LongBuffer;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.lwjgl.glfw.GLFWVulkan.glfwGetRequiredInstanceExtensions;
import static org.lwjgl.glfw.GLFWVulkan.glfwVulkanSupported;
import static org.lwjgl.vulkan.EXTDebugUtils.*;
import static org.lwjgl.vulkan.VK10.*;
import static org.lwjgl.vulkan.VK12.VK_API_VERSION_1_2;

public class VulkanContext
{
    private static final Set<String> validationLayers;

    private static boolean enableValidationLayers = Platform.isDevelopmentEnvironment();

    private VkInstance vulkanInstance;
    private long debugMessenger;

    static
    {
        if (enableValidationLayers)
        {
            validationLayers = new HashSet<>(VulkanDebugUtils.getSupportedValidationLayers());
        }
        else
        {
            validationLayers = Collections.emptySet();
        }
    }

    public void create()
    {
        if (!glfwVulkanSupported())
            throw new IridiumRendererException("The Vulkan loader and an ICD (Installable Client Driver) weren't found!");

        if (enableValidationLayers && validationLayers.isEmpty())
        {
            IridiumRenderer.getLogger().error("Failed to enable the Vulkan Validation Layers! They couldn't be found by the Vulkan loader!");

            enableValidationLayers = false;
        }

        try (MemoryStack memoryStack = MemoryStack.stackPush())
        {
            int packedMinecraftVersion = VK_MAKE_VERSION(VersioningUtils.getMinecraftMajorVersion(), VersioningUtils.getMinecraftMinorVersion(),
                    VersioningUtils.getMinecraftPatchVersion());

            int packedIridiumVersion = VK_MAKE_VERSION(VersioningUtils.getIridiumMajorVersion(), VersioningUtils.getIridiumMinorVersion(),
                    VersioningUtils.getIridiumPatchVersion());
            
            VkApplicationInfo applicationInfo = VkApplicationInfo.calloc(memoryStack)
                    .sType(VK_STRUCTURE_TYPE_APPLICATION_INFO)
                    .pApplicationName(memoryStack.UTF8("Minecraft"))
                    .applicationVersion(packedMinecraftVersion)
                    .pEngineName(memoryStack.UTF8("Iridium"))
                    .engineVersion(packedIridiumVersion)
                    .apiVersion(VK_API_VERSION_1_2);

            VkInstanceCreateInfo instanceCreateInfo = VkInstanceCreateInfo.calloc(memoryStack)
                    .sType(VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO)
                    .pApplicationInfo(applicationInfo)
                    .ppEnabledExtensionNames(this.getRequiredInstanceExtensions(memoryStack));

            if (enableValidationLayers && !validationLayers.isEmpty())
            {
                VkDebugUtilsMessengerCreateInfoEXT debugUtilsMessengerCreateInfo = VkDebugUtilsMessengerCreateInfoEXT.calloc(memoryStack);
                this.initializeDebugMessengerCreateInfo(debugUtilsMessengerCreateInfo);

                instanceCreateInfo.ppEnabledLayerNames(PointerUtils.asPointerBuffer(validationLayers, memoryStack));
                instanceCreateInfo.pNext(debugUtilsMessengerCreateInfo.address());
            }

            PointerBuffer pVulkanInstance = memoryStack.callocPointer(1);
            vkCreateInstance(instanceCreateInfo, null, pVulkanInstance);

            this.vulkanInstance = new VkInstance(pVulkanInstance.get(0), instanceCreateInfo);
        }

        this.initializeDebugMessenger();
    }

    public void destroy()
    {
        if (enableValidationLayers)
            VulkanDebugUtils.vkDestroyDebugUtilsMessengerEXT(this.vulkanInstance, this.debugMessenger, null);

        vkDestroyInstance(this.vulkanInstance, null);
    }

    private static int debugMessengerCallback(int messageSeverity, int messageTypes, long pCallbackData, long pUserData)
    {
        VkDebugUtilsMessengerCallbackDataEXT messengerCallbackData = VkDebugUtilsMessengerCallbackDataEXT.create(pCallbackData);

        if (messageSeverity == VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT)
        {
            switch (messageTypes)
            {
                case VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT ->
                        IridiumRenderer.getLogger().warn("[Vulkan Validation - Performance] {}", messengerCallbackData.pMessageString());

                case VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT ->
                        IridiumRenderer.getLogger().warn("[Vulkan Validation - Validation] {}", messengerCallbackData.pMessageString());
            }
        }

        if (messageSeverity == VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT)
        {
            switch (messageTypes)
            {
                case VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT ->
                        IridiumRenderer.getLogger().error("[Vulkan Validation - Performance] {}", messengerCallbackData.pMessageString());

                case VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT ->
                        IridiumRenderer.getLogger().error("[Vulkan Validation - Validation] {}", messengerCallbackData.pMessageString());
            }
        }

        return VK_FALSE;
    }

    private void initializeDebugMessenger()
    {
        if (!enableValidationLayers)
            return;

        try (MemoryStack memoryStack = MemoryStack.stackPush())
        {
            VkDebugUtilsMessengerCreateInfoEXT debugUtilsMessengerCreateInfo = VkDebugUtilsMessengerCreateInfoEXT.calloc(memoryStack);
            this.initializeDebugMessengerCreateInfo(debugUtilsMessengerCreateInfo);

            LongBuffer pDebugMessenger = memoryStack.longs(VK_NULL_HANDLE);
            VulkanDebugUtils.vkCreateDebugUtilsMessengerEXT(this.vulkanInstance, debugUtilsMessengerCreateInfo, null, pDebugMessenger);

            this.debugMessenger = pDebugMessenger.get(0);
        }
    }

    private void initializeDebugMessengerCreateInfo(VkDebugUtilsMessengerCreateInfoEXT debugMessengerCreateInfo)
    {
        debugMessengerCreateInfo.sType(VK_STRUCTURE_TYPE_DEBUG_UTILS_MESSENGER_CREATE_INFO_EXT);
        debugMessengerCreateInfo.messageSeverity(VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT | VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT);
        debugMessengerCreateInfo.messageType(VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT | VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT);
        debugMessengerCreateInfo.pfnUserCallback(VulkanContext::debugMessengerCallback);
    }

    private PointerBuffer getRequiredInstanceExtensions(MemoryStack memoryStack)
    {
        PointerBuffer glfwInstanceExtensions = glfwGetRequiredInstanceExtensions();

        if (glfwInstanceExtensions == null)
            throw new NullPointerException("Failed to get required instance extensions from GLFW!");

        if (enableValidationLayers)
        {
            PointerBuffer instanceExtensionsWithValidationExtensions = memoryStack.mallocPointer(glfwInstanceExtensions.capacity() + 1);
            instanceExtensionsWithValidationExtensions.put(glfwInstanceExtensions);
            instanceExtensionsWithValidationExtensions.put(memoryStack.UTF8(VK_EXT_DEBUG_UTILS_EXTENSION_NAME));

            return instanceExtensionsWithValidationExtensions.rewind();
        }

        return glfwInstanceExtensions;
    }
}
