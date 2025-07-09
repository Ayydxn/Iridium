package me.ayydxn.moonblast.renderer;

import com.google.common.collect.Lists;
import me.ayydxn.moonblast.renderer.exceptions.MoonblastRendererException;
import me.ayydxn.moonblast.renderer.utils.QueueFamilyIndices;
import me.ayydxn.moonblast.utils.PointerUtils;
import net.minecraft.Util;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static me.ayydxn.moonblast.renderer.debug.VulkanDebugUtils.vkCheckResult;
import static org.lwjgl.vulkan.KHRDynamicRendering.VK_KHR_DYNAMIC_RENDERING_EXTENSION_NAME;
import static org.lwjgl.vulkan.KHRPushDescriptor.VK_KHR_PUSH_DESCRIPTOR_EXTENSION_NAME;
import static org.lwjgl.vulkan.KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME;
import static org.lwjgl.vulkan.KHRSynchronization2.VK_KHR_SYNCHRONIZATION_2_EXTENSION_NAME;
import static org.lwjgl.vulkan.VK10.*;
import static org.lwjgl.vulkan.VK10.VK_VERSION_PATCH;
import static org.lwjgl.vulkan.VK12.VK_API_VERSION_1_2;
import static org.lwjgl.vulkan.VK13.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_VULKAN_1_3_FEATURES;

public class GraphicsDevice
{
    public static final Set<String> DEVICE_EXTENSIONS = Stream.of(VK_KHR_SWAPCHAIN_EXTENSION_NAME, VK_KHR_DYNAMIC_RENDERING_EXTENSION_NAME,
            VK_KHR_SYNCHRONIZATION_2_EXTENSION_NAME, VK_KHR_PUSH_DESCRIPTOR_EXTENSION_NAME).collect(Collectors.toSet());

    private final VkPhysicalDevice physicalDevice;
    private final VkDevice logicalDevice;
    private final DeviceInfo deviceInfo;
    private final int depthFormat;

    private VkQueue graphicsQueue;

    public GraphicsDevice(VkInstance vulkanInstance)
    {
        this.physicalDevice = this.selectPhysicalDevice(vulkanInstance);
        if (this.physicalDevice == null)
            throw new MoonblastRendererException("No physical device was selected as a suitable graphics card isn't available!");

        this.logicalDevice = this.createLogicalDevice(this.physicalDevice);
        this.deviceInfo = new DeviceInfo(this.physicalDevice);
        this.depthFormat = this.findDepthFormat();
    }

    public void destroy()
    {
        vkDestroyDevice(this.logicalDevice, null);
    }

    public void waitIdle()
    {
        vkDeviceWaitIdle(this.logicalDevice);
    }

    private VkPhysicalDevice selectPhysicalDevice(VkInstance vulkanInstance)
    {
        try (MemoryStack memoryStack = MemoryStack.stackPush())
        {
            IntBuffer physicalDeviceCount = memoryStack.ints(0);
            vkEnumeratePhysicalDevices(vulkanInstance, physicalDeviceCount, null);

            if (physicalDeviceCount.get(0) == 0)
                throw new MoonblastRendererException("No graphics cards with Vulkan support are available!");

            PointerBuffer pPhysicalDevices = memoryStack.mallocPointer(physicalDeviceCount.get(0));
            vkEnumeratePhysicalDevices(vulkanInstance, physicalDeviceCount, pPhysicalDevices);

            for (int i = 0; i < physicalDeviceCount.get(0); i++)
            {
                VkPhysicalDevice physicalDevice = new VkPhysicalDevice(pPhysicalDevices.get(i), vulkanInstance);
                if (this.isPhysicalDeviceSuitable(physicalDevice))
                    return physicalDevice;
            }
        }

        return null;
    }

    private boolean isPhysicalDeviceSuitable(VkPhysicalDevice physicalDevice)
    {
        try (MemoryStack memoryStack = MemoryStack.stackPush())
        {
            VkPhysicalDeviceProperties physicalDeviceProperties = VkPhysicalDeviceProperties.calloc(memoryStack);
            vkGetPhysicalDeviceProperties(physicalDevice, physicalDeviceProperties);

            boolean isDeviceDiscreteOrIntegrated = physicalDeviceProperties.deviceType() == VK_PHYSICAL_DEVICE_TYPE_DISCRETE_GPU ||
                    physicalDeviceProperties.deviceType() == VK_PHYSICAL_DEVICE_TYPE_INTEGRATED_GPU;

            boolean doesDeviceSupportRequiredExtensions = this.doesPhysicalDeviceSupportRequiredExtensions(physicalDevice);

            return isDeviceDiscreteOrIntegrated && doesDeviceSupportRequiredExtensions;
        }
    }

    private boolean doesPhysicalDeviceSupportRequiredExtensions(VkPhysicalDevice physicalDevice)
    {
        try (MemoryStack memoryStack = MemoryStack.stackPush())
        {
            IntBuffer physicalDeviceExtensionCount = memoryStack.ints(0);
            vkEnumerateDeviceExtensionProperties(physicalDevice, (String) null, physicalDeviceExtensionCount, null);

            VkExtensionProperties.Buffer physicalDeviceExtensions = VkExtensionProperties.calloc(physicalDeviceExtensionCount.get(0), memoryStack);
            vkEnumerateDeviceExtensionProperties(physicalDevice, (String) null, physicalDeviceExtensionCount, physicalDeviceExtensions);

            return physicalDeviceExtensions.stream()
                    .map(VkExtensionProperties::extensionNameString)
                    .collect(Collectors.toSet())
                    .containsAll(DEVICE_EXTENSIONS);
        }
    }

    private VkDevice createLogicalDevice(VkPhysicalDevice physicalDevice)
    {
        try (MemoryStack memoryStack = MemoryStack.stackPush())
        {
            QueueFamilyIndices queueFamilyIndices = QueueFamilyIndices.findQueueFamilies(physicalDevice);
            int[] queueFamilies = queueFamilyIndices.toArray();

            VkDeviceQueueCreateInfo.Buffer deviceQueueCreateInfos = VkDeviceQueueCreateInfo.calloc(queueFamilies.length, memoryStack);

            for (int i = 0; i < queueFamilies.length; i++)
            {
                VkDeviceQueueCreateInfo deviceQueueCreateInfo = VkDeviceQueueCreateInfo.calloc(memoryStack)
                        .sType(VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO)
                        .queueFamilyIndex(queueFamilies[i])
                        .pQueuePriorities(memoryStack.floats(1.0f));

                deviceQueueCreateInfos.put(i, deviceQueueCreateInfo);
            }

            VkPhysicalDeviceVulkan13Features physicalDeviceVulkan13Features = VkPhysicalDeviceVulkan13Features.calloc(memoryStack)
                    .sType(VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_VULKAN_1_3_FEATURES)
                    .dynamicRendering(true)
                    .synchronization2(true);

            VkDeviceCreateInfo logicalDeviceCreateInfo = VkDeviceCreateInfo.calloc(memoryStack)
                    .sType(VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO)
                    .pQueueCreateInfos(deviceQueueCreateInfos)
                    .ppEnabledExtensionNames(PointerUtils.asPointerBuffer(DEVICE_EXTENSIONS, memoryStack))
                    .pNext(physicalDeviceVulkan13Features);

            if (GraphicsContext.areValidationLayersEnabled())
                logicalDeviceCreateInfo.ppEnabledLayerNames(PointerUtils.asPointerBuffer(GraphicsContext.getValidationLayers(), memoryStack));

            PointerBuffer pLogicalDevice = memoryStack.mallocPointer(1);
            vkCheckResult(vkCreateDevice(physicalDevice, logicalDeviceCreateInfo, null, pLogicalDevice));

            VkDevice logicalDevice = new VkDevice(pLogicalDevice.get(0), physicalDevice, logicalDeviceCreateInfo, VK_API_VERSION_1_2);

            this.graphicsQueue = this.createQueue(logicalDevice, queueFamilyIndices.getGraphicsFamily(), memoryStack);

            return logicalDevice;
        }
    }

    private VkQueue createQueue(VkDevice logicalDevice, int queueFamilyIndex, MemoryStack memoryStack)
    {
        PointerBuffer pQueue = memoryStack.mallocPointer(1);
        vkGetDeviceQueue(logicalDevice, queueFamilyIndex, 0, pQueue);

        return new VkQueue(pQueue.get(0), logicalDevice);
    }

    private int findDepthFormat()
    {
        ArrayList<Integer> depthFormatCandidates = Lists.newArrayList(VK_FORMAT_D32_SFLOAT_S8_UINT, VK_FORMAT_D32_SFLOAT, VK_FORMAT_D24_UNORM_S8_UINT,
                VK_FORMAT_D16_UNORM_S8_UINT, VK_FORMAT_D16_UNORM);

        try (MemoryStack memoryStack = MemoryStack.stackPush())
        {
            for (Integer depthFormat : depthFormatCandidates)
            {
                VkFormatProperties formatProperties = VkFormatProperties.calloc(memoryStack);
                vkGetPhysicalDeviceFormatProperties(this.physicalDevice, depthFormat, formatProperties);

                if ((formatProperties.optimalTilingFeatures() & VK_FORMAT_FEATURE_DEPTH_STENCIL_ATTACHMENT_BIT) == VK_FORMAT_FEATURE_DEPTH_STENCIL_ATTACHMENT_BIT)
                    return depthFormat;
            }
        }

        return VK_FORMAT_UNDEFINED;
    }

    public VkPhysicalDevice getPhysicalDevice()
    {
        return this.physicalDevice;
    }

    public VkDevice getLogicalDevice()
    {
        return this.logicalDevice;
    }

    public VkQueue getGraphicsQueue()
    {
        return this.graphicsQueue;
    }

    public DeviceInfo getDeviceInfo()
    {
        return this.deviceInfo;
    }

    public int getDepthFormat()
    {
        return this.depthFormat;
    }

    public static final class DeviceInfo
    {
        public final VkPhysicalDeviceProperties deviceProperties;
        public final String vendorName;
        public final String driverVersion;
        public final String vulkanAPIVersion;

        public DeviceInfo(VkPhysicalDevice physicalDevice)
        {
            try (MemoryStack memoryStack = MemoryStack.stackPush())
            {
                this.deviceProperties = VkPhysicalDeviceProperties.calloc(memoryStack);
                vkGetPhysicalDeviceProperties(physicalDevice, this.deviceProperties);

                this.driverVersion = this.decodeDriverVersionNumber();
                this.vendorName = this.getVendorNameFromID();
                this.vulkanAPIVersion = this.decodeVulkanAPIVersionNumber();
            }
        }

        private String getVendorNameFromID()
        {
            return switch (this.deviceProperties.vendorID())
            {
                case 0x1002 -> "AMD Incorporated";
                case 0x1010 -> "Imagination Technologies Limited";
                case 0x10DE -> "NVIDIA Corporation";
                case 0x13B5 -> "ARM";
                case 0x5143 -> "Qualcomm";
                case 0x8080 -> "Intel Corporation";

                default -> "Unknown Vendor";
            };
        }

        private String decodeDriverVersionNumber()
        {
            int physicalDeviceVendorID = this.deviceProperties.vendorID();
            int physicalDeviceDriverVersion = this.deviceProperties.driverVersion();

            // NVIDIA
            if (physicalDeviceVendorID == 0x10DE)
            {
                int driverMajorVersion = (physicalDeviceDriverVersion >> 22) & 0x3ff;
                int driverMinorVersion = (physicalDeviceDriverVersion >> 14) & 0x0ff;
                int driverPatchVersion = (physicalDeviceDriverVersion >> 6) & 0x0ff;
                int driverVariantVersion = physicalDeviceDriverVersion & 0x003f;

                return String.format("%d.%d.%d.%d", driverMajorVersion, driverMinorVersion, driverPatchVersion, driverVariantVersion);
            }

            // Intel
            if (physicalDeviceVendorID == 0x8086 && Util.getPlatform() == Util.OS.WINDOWS)
            {
                int driverMajorVersion = physicalDeviceDriverVersion >> 14;
                int driverMinorVersion = physicalDeviceDriverVersion & 0x3fff;

                return String.format("%d.%d", driverMajorVersion, driverMinorVersion);
            }

            // Use Vulkan version conventions if a vendor mapping isn't available.
            int driverMajorVersion = physicalDeviceDriverVersion >> 22;
            int driverMinorVersion = (physicalDeviceDriverVersion >> 12) & 0x3ff;
            int driverPatchVersion = physicalDeviceDriverVersion & 0xfff;

            return String.format("%d.%d.%d", driverMajorVersion, driverMinorVersion, driverPatchVersion);
        }

        private String decodeVulkanAPIVersionNumber()
        {
            int physicalDeviceVulkanAPIVersion = this.deviceProperties.apiVersion();
            int vulkanAPIVersionMajor = VK_API_VERSION_MAJOR(physicalDeviceVulkanAPIVersion);
            int vulkanAPIVersionMinor = VK_API_VERSION_MINOR(physicalDeviceVulkanAPIVersion);
            int vulkanAPIVersionPatch = VK_VERSION_PATCH(physicalDeviceVulkanAPIVersion); // (Ayydan) VK_API_VERSION_PATCH returns an incorrect value. No clue why.

            return String.format("%d.%d.%d", vulkanAPIVersionMajor, vulkanAPIVersionMinor, vulkanAPIVersionPatch);
        }
    }
}
