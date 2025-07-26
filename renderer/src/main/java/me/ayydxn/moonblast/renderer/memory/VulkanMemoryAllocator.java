package me.ayydxn.moonblast.renderer.memory;

import me.ayydxn.moonblast.MoonblastRenderer;
import me.ayydxn.moonblast.renderer.GraphicsContext;
import me.ayydxn.moonblast.utils.MoonblastConstants;
import org.apache.commons.lang3.Validate;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.vma.VmaAllocationCreateInfo;
import org.lwjgl.util.vma.VmaAllocatorCreateInfo;
import org.lwjgl.util.vma.VmaVulkanFunctions;
import org.lwjgl.vulkan.VkBufferCreateInfo;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkPhysicalDevice;

import java.nio.LongBuffer;

import static me.ayydxn.moonblast.renderer.debug.VulkanDebugUtils.vkCheckResult;
import static org.lwjgl.util.vma.Vma.*;
import static org.lwjgl.vulkan.VK10.VK_NULL_HANDLE;
import static org.lwjgl.vulkan.VK12.VK_API_VERSION_1_2;

public class VulkanMemoryAllocator
{
    private static VulkanMemoryAllocator INSTANCE;

    private final long vmaAllocator;

    private VulkanMemoryAllocator()
    {
        GraphicsContext graphicsContext = MoonblastRenderer.getInstance().getGraphicsContext();
        VkInstance vulkanInstance = graphicsContext.getVulkanInstance();
        VkPhysicalDevice physicalDevice = graphicsContext.getGraphicsDevice().getPhysicalDevice();
        VkDevice logicalDevice = graphicsContext.getGraphicsDevice().getLogicalDevice();

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
            MoonblastConstants.LOGGER.warn("The memory allocator cannot be initialized more than once!");
            return;
        }

        INSTANCE = new VulkanMemoryAllocator();
    }

    public void shutdown()
    {
        vmaDestroyAllocator(this.vmaAllocator);

        INSTANCE = null;
    }

    public AllocatedBuffer allocateBuffer(VkBufferCreateInfo bufferCreateInfo, int vmaMemoryUsage)
    {
        Validate.isTrue(bufferCreateInfo.size() > 0, "Cannot allocate an empty buffer!");

        try (MemoryStack memoryStack = MemoryStack.stackPush())
        {
            VmaAllocationCreateInfo allocationCreateInfo = VmaAllocationCreateInfo.calloc(memoryStack)
                    .usage(vmaMemoryUsage);

            LongBuffer pBuffer = memoryStack.longs(VK_NULL_HANDLE);
            PointerBuffer pAllocation = memoryStack.pointers(VK_NULL_HANDLE);
            vkCheckResult(vmaCreateBuffer(this.vmaAllocator, bufferCreateInfo, allocationCreateInfo, pBuffer, pAllocation, null));

            return new AllocatedBuffer(pBuffer.get(0), pAllocation.get(0));
        }
    }

    public void destroyBuffer(long buffer, long bufferAllocation)
    {
        vmaDestroyBuffer(this.vmaAllocator, buffer, bufferAllocation);
    }

    public PointerBuffer mapMemory(long allocation)
    {
        try (MemoryStack memoryStack = MemoryStack.stackPush())
        {
            PointerBuffer pMappedMemory = memoryStack.pointers(VK_NULL_HANDLE);
            vkCheckResult(vmaMapMemory(this.vmaAllocator, allocation, pMappedMemory));

            return pMappedMemory;
        }
    }

    public void unmapMemory(long allocation)
    {
        vmaUnmapMemory(this.vmaAllocator, allocation);
    }

    public static VulkanMemoryAllocator getInstance()
    {
        if (INSTANCE == null)
            throw new IllegalStateException("Tried to access an instance of Moonblast's memory allocator before one was available!");

        return INSTANCE;
    }
}
