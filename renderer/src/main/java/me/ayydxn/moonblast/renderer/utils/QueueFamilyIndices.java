package me.ayydxn.moonblast.renderer.utils;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkQueueFamilyProperties;

import java.nio.IntBuffer;
import java.util.stream.IntStream;

import static org.lwjgl.vulkan.VK10.VK_QUEUE_GRAPHICS_BIT;
import static org.lwjgl.vulkan.VK10.vkGetPhysicalDeviceQueueFamilyProperties;

public class QueueFamilyIndices
{
    private Integer graphicsFamily;

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
                if ((queueFamilyProperties.get(i).queueFlags() & VK_QUEUE_GRAPHICS_BIT) == VK_QUEUE_GRAPHICS_BIT)
                    queueFamilyIndices.graphicsFamily = i;
            }
        }

        return queueFamilyIndices;
    }

    public int[] toArray()
    {
        return IntStream.of(this.graphicsFamily).toArray();
    }

    public boolean isComplete()
    {
        return this.graphicsFamily != null;
    }

    public Integer getGraphicsFamily()
    {
        return this.graphicsFamily;
    }
}
