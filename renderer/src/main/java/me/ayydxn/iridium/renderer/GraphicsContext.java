package me.ayydxn.iridium.renderer;

import me.ayydxn.iridium.options.IridiumRendererOptions;
import me.ayydxn.iridium.renderer.debug.VulkanDebugUtils;
import me.ayydxn.iridium.renderer.exceptions.IridiumRendererException;
import me.ayydxn.iridium.renderer.memory.VulkanMemoryAllocator;
import me.ayydxn.iridium.utils.IridiumConstants;
import me.ayydxn.iridium.utils.PointerUtils;
import me.ayydxn.iridium.utils.VersioningUtils;
import org.apache.commons.compress.utils.Sets;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.LongBuffer;
import java.util.HashSet;
import java.util.Set;

import static me.ayydxn.iridium.renderer.debug.VulkanDebugUtils.vkCheckResult;
import static org.lwjgl.glfw.GLFWVulkan.glfwGetRequiredInstanceExtensions;
import static org.lwjgl.glfw.GLFWVulkan.glfwVulkanSupported;
import static org.lwjgl.vulkan.EXTDebugUtils.*;
import static org.lwjgl.vulkan.VK10.*;
import static org.lwjgl.vulkan.VK12.VK_API_VERSION_1_2;

public class GraphicsContext
{
    private static Set<String> validationLayers;
    private static boolean enableValidationLayers;

    private VkInstance vulkanInstance;
    private long debugMessenger;
    private GraphicsDevice graphicsDevice;

    public GraphicsContext(IridiumRendererOptions iridiumRendererOptions)
    {
        validationLayers = iridiumRendererOptions.debugOptions.enableValidationLayers ? new HashSet<>(VulkanDebugUtils.getSupportedValidationLayers()) :
                Sets.newHashSet();

        enableValidationLayers = iridiumRendererOptions.debugOptions.enableValidationLayers;
    }

    public void initialize()
    {
        if (!glfwVulkanSupported())
            throw new IridiumRendererException("The Vulkan loader and an ICD (Installable Client Driver) weren't found!");

        if (enableValidationLayers && validationLayers.isEmpty())
        {
            IridiumConstants.LOGGER.error("Failed to enable the Vulkan Validation Layers! They couldn't be found by the Vulkan loader!");

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

        this.graphicsDevice = new GraphicsDevice(this.vulkanInstance);

        IridiumConstants.LOGGER.info("Graphics Card Info:");
        IridiumConstants.LOGGER.info("  Vendor: {}", this.graphicsDevice.getDeviceInfo().vendorName);
        IridiumConstants.LOGGER.info("  Device: {}", this.graphicsDevice.getDeviceInfo().deviceProperties.deviceNameString());
        IridiumConstants.LOGGER.info("  Driver Version: {}", this.graphicsDevice.getDeviceInfo().driverVersion);

        VulkanMemoryAllocator.initialize();
    }

    public void destroy()
    {
        VulkanMemoryAllocator.getInstance().shutdown();

        this.graphicsDevice.destroy();

        if (enableValidationLayers)
            VulkanDebugUtils.destroyDebugUtilsMessenger(this.vulkanInstance, this.debugMessenger, null);

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
                        IridiumConstants.LOGGER.warn("[Vulkan Validation - Performance] {}", messengerCallbackData.pMessageString());

                case VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT ->
                        IridiumConstants.LOGGER.warn("[Vulkan Validation - Validation] {}", messengerCallbackData.pMessageString());
            }
        }

        if (messageSeverity == VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT)
        {
            switch (messageTypes)
            {
                case VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT ->
                        IridiumConstants.LOGGER.error("[Vulkan Validation - Performance] {}", messengerCallbackData.pMessageString());

                case VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT ->
                        IridiumConstants.LOGGER.error("[Vulkan Validation - Validation] {}", messengerCallbackData.pMessageString());
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
            vkCheckResult(VulkanDebugUtils.createDebugUtilsMessenger(this.vulkanInstance, debugUtilsMessengerCreateInfo, null, pDebugMessenger));

            this.debugMessenger = pDebugMessenger.get(0);
        }
    }

    private void initializeDebugMessengerCreateInfo(VkDebugUtilsMessengerCreateInfoEXT debugMessengerCreateInfo)
    {
        debugMessengerCreateInfo.sType(VK_STRUCTURE_TYPE_DEBUG_UTILS_MESSENGER_CREATE_INFO_EXT)
                .messageSeverity(VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT | VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT)
                .messageType(VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT | VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT)
                .pfnUserCallback(GraphicsContext::debugMessengerCallback);
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

    public static Set<String> getValidationLayers()
    {
        return validationLayers;
    }

    public static boolean areValidationLayersEnabled()
    {
        return enableValidationLayers;
    }

    public VkInstance getVulkanInstance()
    {
        return this.vulkanInstance;
    }

    public GraphicsDevice getGraphicsDevice()
    {
        return graphicsDevice;
    }
}
