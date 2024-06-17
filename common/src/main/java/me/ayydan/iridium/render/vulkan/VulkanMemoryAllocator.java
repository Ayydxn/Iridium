package me.ayydan.iridium.render.vulkan;

import me.ayydan.iridium.render.IridiumRenderer;
import me.ayydan.iridium.render.memory.AllocatedImage;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.vma.VmaAllocationCreateInfo;
import org.lwjgl.util.vma.VmaAllocatorCreateInfo;
import org.lwjgl.util.vma.VmaVulkanFunctions;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkImageCreateInfo;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkPhysicalDevice;

import java.nio.LongBuffer;

import static me.ayydan.iridium.render.vulkan.VulkanValidation.vkCheckResult;
import static org.lwjgl.util.vma.Vma.*;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT;
import static org.lwjgl.vulkan.VK10.VK_NULL_HANDLE;
import static org.lwjgl.vulkan.VK12.VK_API_VERSION_1_2;

public class VulkanMemoryAllocator
{
    private static VulkanMemoryAllocator INSTANCE;

    private final long vmaAllocator;

    private VulkanMemoryAllocator()
    {
        VulkanContext vulkanContext = IridiumRenderer.getInstance().getVulkanContext();

        VkInstance vulkanInstance = vulkanContext.getVulkanInstance();
        VkPhysicalDevice physicalDevice = vulkanContext.getPhysicalDevice().getHandle();
        VkDevice logicalDevice = vulkanContext.getLogicalDevice().getHandle();

        try (MemoryStack memoryStack = MemoryStack.stackPush())
        {
            VmaVulkanFunctions vmaVulkanFunctions = VmaVulkanFunctions.calloc(memoryStack)
                    .set(vulkanInstance, logicalDevice);

            VmaAllocatorCreateInfo vmaAllocatorCreateInfo = VmaAllocatorCreateInfo.calloc(memoryStack)
                    .instance(vulkanInstance)
                    .physicalDevice(physicalDevice)
                    .device(logicalDevice)
                    .pVulkanFunctions(vmaVulkanFunctions)
                    .vulkanApiVersion(VK_API_VERSION_1_2);

            PointerBuffer pVmaAllocator = memoryStack.pointers(VK_NULL_HANDLE);
            vkCheckResult(vmaCreateAllocator(vmaAllocatorCreateInfo, pVmaAllocator));

            this.vmaAllocator = pVmaAllocator.get(0);
        }
    }

    public static void initialize()
    {
        if (INSTANCE != null)
        {
            IridiumRenderer.getLogger().warn("Iridium's Vulkan memory allocator cannot be initialized more than once!");
            return;
        }

        INSTANCE = new VulkanMemoryAllocator();
    }

    public void shutdown()
    {
        vmaDestroyAllocator(this.vmaAllocator);
    }

    public AllocatedImage allocateImage(VkImageCreateInfo imageCreateInfo)
    {
        try (MemoryStack memoryStack = MemoryStack.stackPush())
        {
            VmaAllocationCreateInfo imageAllocationCreateInfo = VmaAllocationCreateInfo.calloc(memoryStack)
                    .usage(VMA_MEMORY_USAGE_GPU_ONLY)
                    .requiredFlags(VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);

            LongBuffer pImage = memoryStack.longs(VK_NULL_HANDLE);
            PointerBuffer pImageAllocation = memoryStack.pointers(VK_NULL_HANDLE);
            vkCheckResult(vmaCreateImage(this.vmaAllocator, imageCreateInfo, imageAllocationCreateInfo, pImage, pImageAllocation, null));

            return new AllocatedImage(pImage.get(0), pImageAllocation.get(0));
        }
    }

    public void destroyImage(AllocatedImage allocatedImage)
    {
        vmaDestroyImage(this.vmaAllocator, allocatedImage.image(), allocatedImage.imageMemoryAllocation());
    }

    public static VulkanMemoryAllocator getInstance()
    {
        return INSTANCE;
    }
}
