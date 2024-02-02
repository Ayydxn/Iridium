package me.ayydan.iridium.render.vulkan;

import me.ayydan.iridium.render.vulkan.utils.QueueFamilyIndices;
import me.ayydan.iridium.utils.PointerUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.LongBuffer;

import static org.lwjgl.vulkan.KHRDynamicRendering.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_DYNAMIC_RENDERING_FEATURES_KHR;
import static org.lwjgl.vulkan.KHRSynchronization2.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_SYNCHRONIZATION_2_FEATURES_KHR;
import static org.lwjgl.vulkan.VK10.*;
import static org.lwjgl.vulkan.VK12.VK_API_VERSION_1_2;

public class VulkanLogicalDevice
{
    private final VkDevice logicalDevice;
    private final long graphicsCommandPool;

    private VkQueue graphicsQueue;

    public VulkanLogicalDevice(VkPhysicalDevice physicalDevice)
    {
        this.logicalDevice = this.createLogicalDevice(physicalDevice);
        this.graphicsCommandPool = this.createGraphicsCommandPool(physicalDevice);
    }

    public void destroy()
    {
        vkDestroyCommandPool(this.logicalDevice, this.graphicsCommandPool, null);
        vkDestroyDevice(this.logicalDevice, null);
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
                VkDeviceQueueCreateInfo deviceQueueCreateInfo = VkDeviceQueueCreateInfo.calloc(memoryStack);
                deviceQueueCreateInfo.sType(VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO);
                deviceQueueCreateInfo.queueFamilyIndex(queueFamilies[i]);
                deviceQueueCreateInfo.pQueuePriorities(memoryStack.floats(1.0f));

                deviceQueueCreateInfos.put(i, deviceQueueCreateInfo);
            }

            VkPhysicalDeviceDynamicRenderingFeaturesKHR dynamicRenderingFeaturesKHR = VkPhysicalDeviceDynamicRenderingFeaturesKHR.calloc(memoryStack)
                    .sType(VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_DYNAMIC_RENDERING_FEATURES_KHR)
                    .dynamicRendering(true);

            VkPhysicalDeviceSynchronization2FeaturesKHR synchronization2FeaturesKHR = VkPhysicalDeviceSynchronization2FeaturesKHR.calloc(memoryStack)
                    .sType(VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_SYNCHRONIZATION_2_FEATURES_KHR)
                    .synchronization2(true);

            VkDeviceCreateInfo logicalDeviceCreateInfo = VkDeviceCreateInfo.calloc(memoryStack)
                    .sType(VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO)
                    .pQueueCreateInfos(deviceQueueCreateInfos)
                    .ppEnabledExtensionNames(PointerUtils.asPointerBuffer(VulkanPhysicalDevice.DEVICE_EXTENSIONS, memoryStack))
                    .pNext(dynamicRenderingFeaturesKHR)
                    .pNext(synchronization2FeaturesKHR);

            if (VulkanContext.areValidationLayersEnabled())
                logicalDeviceCreateInfo.ppEnabledLayerNames(PointerUtils.asPointerBuffer(VulkanContext.getValidationLayers(), memoryStack));

            PointerBuffer pLogicalDevice = memoryStack.mallocPointer(1);
            vkCreateDevice(physicalDevice, logicalDeviceCreateInfo, null, pLogicalDevice);

            VkDevice logicalDevice = new VkDevice(pLogicalDevice.get(0), physicalDevice, logicalDeviceCreateInfo, VK_API_VERSION_1_2);

            this.graphicsQueue = this.createQueue(logicalDevice, queueFamilyIndices.getGraphicsFamily(), memoryStack);

            return logicalDevice;
        }
    }

    private long createGraphicsCommandPool(VkPhysicalDevice physicalDevice)
    {
        try (MemoryStack memoryStack = MemoryStack.stackPush())
        {
            QueueFamilyIndices queueFamilyIndices = QueueFamilyIndices.findQueueFamilies(physicalDevice);
            LongBuffer pGraphicsCommandPool = memoryStack.mallocLong(1);

            VkCommandPoolCreateInfo commandPoolCreateInfo = VkCommandPoolCreateInfo.calloc(memoryStack);
            commandPoolCreateInfo.sType(VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO);
            commandPoolCreateInfo.queueFamilyIndex(queueFamilyIndices.getGraphicsFamily());
            commandPoolCreateInfo.flags(VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT);

            vkCreateCommandPool(this.logicalDevice, commandPoolCreateInfo, null, pGraphicsCommandPool);

            return pGraphicsCommandPool.get(0);
        }
    }

    private VkQueue createQueue(VkDevice logicalDevice, int queueFamilyIndex, MemoryStack memoryStack)
    {
        PointerBuffer pQueue = memoryStack.mallocPointer(1);

        vkGetDeviceQueue(logicalDevice, queueFamilyIndex, 0, pQueue);

        return new VkQueue(pQueue.get(0), logicalDevice);
    }

    public VkQueue getGraphicsQueue()
    {
        return this.graphicsQueue;
    }

    public long getGraphicsCommandPool()
    {
        return this.graphicsCommandPool;
    }
}
