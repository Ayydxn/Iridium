package me.ayydan.iridium.render.vulkan;

import me.ayydan.iridium.render.exceptions.IridiumRendererException;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Util;
import net.minecraft.util.WinNativeModuleLister;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.IntBuffer;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.lwjgl.vulkan.KHRDynamicRendering.VK_KHR_DYNAMIC_RENDERING_EXTENSION_NAME;
import static org.lwjgl.vulkan.KHRPushDescriptor.VK_KHR_PUSH_DESCRIPTOR_EXTENSION_NAME;
import static org.lwjgl.vulkan.KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME;
import static org.lwjgl.vulkan.KHRSynchronization2.VK_KHR_SYNCHRONIZATION_2_EXTENSION_NAME;
import static org.lwjgl.vulkan.VK10.*;
import static org.lwjgl.vulkan.VK12.VK_API_VERSION_1_2;

public class VulkanPhysicalDevice
{
    public static final Set<String> DEVICE_EXTENSIONS = Stream.of(VK_KHR_SWAPCHAIN_EXTENSION_NAME, VK_KHR_DYNAMIC_RENDERING_EXTENSION_NAME,
            VK_KHR_SYNCHRONIZATION_2_EXTENSION_NAME, VK_KHR_PUSH_DESCRIPTOR_EXTENSION_NAME).collect(Collectors.toSet());

    private final VkPhysicalDevice physicalDevice;
    private final PhysicalDeviceInfo physicalDeviceInfo;

    public VulkanPhysicalDevice(VkInstance vulkanInstance)
    {
        this.physicalDevice = this.selectPhysicalDevice(vulkanInstance);
        if (this.physicalDevice == null)
            throw new IridiumRendererException("No physical device was selected as a suitable graphics card isn't available!!");

        this.physicalDeviceInfo = new PhysicalDeviceInfo(this.physicalDevice);

        if (!(this.physicalDeviceInfo.physicalDeviceProperties.apiVersion() >= VK_API_VERSION_1_2))
        {
            String physicalDeviceName = this.physicalDeviceInfo.physicalDeviceProperties.deviceNameString();
            String crashMessage = String.format("Vulkan 1.2 isn't supported! Make your GPU drivers are up-to-date! (%s)", physicalDeviceName);

            CrashReport crashReport = new CrashReport(crashMessage, new IridiumRendererException(crashMessage));
            CrashReportSection crashReportSection = crashReport.addElement("Vulkan 1.2 isn't supported!");

            WinNativeModuleLister.addReportSection(crashReportSection);
            MinecraftClient.printCrashReport(MinecraftClient.getInstance(), MinecraftClient.getInstance().runDirectory, crashReport);
        }
    }

    private VkPhysicalDevice selectPhysicalDevice(VkInstance vulkanInstance)
    {
        try (MemoryStack memoryStack = MemoryStack.stackPush())
        {
            IntBuffer physicalDeviceCount = memoryStack.ints(0);
            vkEnumeratePhysicalDevices(vulkanInstance, physicalDeviceCount, null);

            if (physicalDeviceCount.get(0) == 0)
                throw new IridiumRendererException("No graphics cards with Vulkan support are available!");

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

    public VkPhysicalDevice getHandle()
    {
        return this.physicalDevice;
    }

    public VkPhysicalDeviceProperties getProperties()
    {
        return this.physicalDeviceInfo.physicalDeviceProperties;
    }

    public String getVendorName()
    {
        return this.physicalDeviceInfo.vendorName;
    }

    public String getDriverVersion()
    {
        return this.physicalDeviceInfo.driverVersion;
    }

    public String getVulkanAPIVersion()
    {
        return this.physicalDeviceInfo.vulkanAPIVersion;
    }

    private static final class PhysicalDeviceInfo
    {
        private final VkPhysicalDeviceProperties physicalDeviceProperties;
        private final String vendorName;
        private final String driverVersion;
        private final String vulkanAPIVersion;

        public PhysicalDeviceInfo(VkPhysicalDevice physicalDevice)
        {
            try (MemoryStack memoryStack = MemoryStack.stackPush())
            {
                this.physicalDeviceProperties = VkPhysicalDeviceProperties.calloc(memoryStack);
                vkGetPhysicalDeviceProperties(physicalDevice, this.physicalDeviceProperties);

                this.driverVersion = this.decodeDriverVersionNumber();
                this.vendorName = this.getVendorNameFromID();
                this.vulkanAPIVersion = this.decodeVulkanAPIVersionNumber();
            }
        }

        private String getVendorNameFromID()
        {
            return switch (this.physicalDeviceProperties.vendorID())
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
            int physicalDeviceVendorID = this.physicalDeviceProperties.vendorID();
            int physicalDeviceDriverVersion = this.physicalDeviceProperties.driverVersion();

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
            if (physicalDeviceVendorID == 0x8086 && Util.getOperatingSystem() == Util.OperatingSystem.WINDOWS)
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
            int physicalDeviceVulkanAPIVersion = this.physicalDeviceProperties.apiVersion();
            int vulkanAPIVersionMajor = VK_API_VERSION_MAJOR(physicalDeviceVulkanAPIVersion);
            int vulkanAPIVersionMinor = VK_API_VERSION_MINOR(physicalDeviceVulkanAPIVersion);
            int vulkanAPIVersionPatch = VK_VERSION_PATCH(physicalDeviceVulkanAPIVersion); // (Ayydan) VK_API_VERSION_PATCH returns an incorrect value. No clue why.

            return String.format("%d.%d.%d", vulkanAPIVersionMajor, vulkanAPIVersionMinor, vulkanAPIVersionPatch);
        }
    }
}
