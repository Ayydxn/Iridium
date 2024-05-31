package me.ayydan.iridium.render.vulkan;

import dev.architectury.platform.Platform;
import me.ayydan.iridium.render.IridiumRenderer;
import me.ayydan.iridium.render.exceptions.IridiumRendererException;
import me.ayydan.iridium.utils.PointerUtils;
import me.ayydan.iridium.utils.VersioningUtils;
import net.minecraft.client.Minecraft;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.LongBuffer;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static me.ayydan.iridium.render.vulkan.VulkanValidation.vkCheckResult;
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
    private VulkanPhysicalDevice vulkanPhysicalDevice;
    private VulkanLogicalDevice vulkanLogicalDevice;
    private VulkanSwapChain vulkanSwapChain;

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

                instanceCreateInfo.ppEnabledLayerNames(PointerUtils.asPointerBuffer(validationLayers, memoryStack))
                        .pNext(debugUtilsMessengerCreateInfo.address());
            }

            PointerBuffer pVulkanInstance = memoryStack.callocPointer(1);
            vkCheckResult(vkCreateInstance(instanceCreateInfo, null, pVulkanInstance));

            this.vulkanInstance = new VkInstance(pVulkanInstance.get(0), instanceCreateInfo);
        }

        this.initializeDebugMessenger();

        this.vulkanPhysicalDevice = new VulkanPhysicalDevice(this.vulkanInstance);

        IridiumRenderer.getLogger().info("Graphics Card Info:");
        IridiumRenderer.getLogger().info("  Vendor: {}", this.vulkanPhysicalDevice.getVendorName());
        IridiumRenderer.getLogger().info("  Device: {}", this.vulkanPhysicalDevice.getProperties().deviceNameString());
        IridiumRenderer.getLogger().info("  Driver Version: {}", this.vulkanPhysicalDevice.getDriverVersion());

        this.vulkanLogicalDevice = new VulkanLogicalDevice(this.vulkanPhysicalDevice.getHandle());

        this.vulkanSwapChain = new VulkanSwapChain(this);
        this.vulkanSwapChain.initialize();
        this.vulkanSwapChain.create(Minecraft.getInstance().getWindow().getWidth(), Minecraft.getInstance().getWindow().getHeight(),
                Minecraft.getInstance().options.enableVsync().get());
    }

    public void destroy()
    {
        this.vulkanSwapChain.destroy();

        this.vulkanLogicalDevice.destroy();

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

    public static Set<String> getValidationLayers()
    {
        return VulkanContext.validationLayers;
    }

    public static boolean areValidationLayersEnabled()
    {
        return VulkanContext.enableValidationLayers;
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
            vkCheckResult(VulkanDebugUtils.vkCreateDebugUtilsMessengerEXT(this.vulkanInstance, debugUtilsMessengerCreateInfo, null, pDebugMessenger));

            this.debugMessenger = pDebugMessenger.get(0);
        }
    }

    private void initializeDebugMessengerCreateInfo(VkDebugUtilsMessengerCreateInfoEXT debugMessengerCreateInfo)
    {
        debugMessengerCreateInfo.sType(VK_STRUCTURE_TYPE_DEBUG_UTILS_MESSENGER_CREATE_INFO_EXT)
                .messageSeverity(VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT | VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT)
                .messageType(VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT | VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT)
                .pfnUserCallback(VulkanContext::debugMessengerCallback);
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

    public VkInstance getVulkanInstance()
    {
        return this.vulkanInstance;
    }

    public VulkanPhysicalDevice getPhysicalDevice()
    {
        return this.vulkanPhysicalDevice;
    }

    public VulkanLogicalDevice getLogicalDevice()
    {
        return this.vulkanLogicalDevice;
    }

    public VulkanSwapChain getSwapChain()
    {
        return this.vulkanSwapChain;
    }
}
