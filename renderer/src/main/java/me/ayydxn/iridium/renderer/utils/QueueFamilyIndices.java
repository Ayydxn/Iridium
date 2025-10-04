package me.ayydxn.iridium.renderer.utils;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkQueueFamilyProperties;

import java.nio.IntBuffer;
import java.util.stream.IntStream;

import static org.lwjgl.vulkan.VK10.*;

public class QueueFamilyIndices
{
    private Integer graphicsFamily;
    private Integer computeFamily;

    public static QueueFamilyIndices findQueueFamilies(VkPhysicalDevice physicalDevice)
    {
        QueueFamilyIndices queueFamilyIndices = new QueueFamilyIndices();

        try (MemoryStack memoryStack = MemoryStack.stackPush())
        {
            IntBuffer queueFamilyPropertyCount = memoryStack.ints(0);
            vkGetPhysicalDeviceQueueFamilyProperties(physicalDevice, queueFamilyPropertyCount, null);

            VkQueueFamilyProperties.Buffer queueFamilyProperties = VkQueueFamilyProperties.malloc(queueFamilyPropertyCount.get(0), memoryStack);
            vkGetPhysicalDeviceQueueFamilyProperties(physicalDevice, queueFamilyPropertyCount, queueFamilyProperties);

            for (int i = 0; i < queueFamilyProperties.capacity() || !queueFamilyIndices.isComplete(); i++)
            {
                VkQueueFamilyProperties familyProperties = queueFamilyProperties.get(i);

                if ((familyProperties.queueFlags() & VK_QUEUE_GRAPHICS_BIT) == VK_QUEUE_GRAPHICS_BIT)
                    queueFamilyIndices.graphicsFamily = i;

                // For compute, we want a queue that supports compute, but not graphics.
                if ((familyProperties.queueFlags() & VK_QUEUE_COMPUTE_BIT) == VK_QUEUE_COMPUTE_BIT && (familyProperties.queueFlags() & VK_QUEUE_GRAPHICS_BIT) == 0)
                    queueFamilyIndices.computeFamily = i;
            }
        }

        return queueFamilyIndices;
    }

    public int[] toArray()
    {
        return IntStream.of(this.graphicsFamily, this.computeFamily).toArray();
    }

    public boolean isComplete()
    {
        return this.graphicsFamily != null && this.computeFamily != null;
    }

    public Integer getGraphicsFamily()
    {
        return this.graphicsFamily;
    }

    public Integer getComputeFamily()
    {
        return this.computeFamily;
    }
}
