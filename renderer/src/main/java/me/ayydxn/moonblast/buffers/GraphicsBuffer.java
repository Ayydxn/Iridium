package me.ayydxn.moonblast.buffers;

import me.ayydxn.moonblast.renderer.CommandBuffer;
import me.ayydxn.moonblast.renderer.memory.AllocatedBuffer;
import me.ayydxn.moonblast.renderer.memory.VulkanMemoryAllocator;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VkBufferCopy;
import org.lwjgl.vulkan.VkBufferCreateInfo;

import java.nio.ByteBuffer;

import static org.lwjgl.util.vma.Vma.VMA_MEMORY_USAGE_CPU_TO_GPU;
import static org.lwjgl.util.vma.Vma.VMA_MEMORY_USAGE_GPU_ONLY;
import static org.lwjgl.vulkan.VK10.*;

public abstract class GraphicsBuffer
{
    private final VulkanMemoryAllocator vulkanMemoryAllocator;
    private final ByteBuffer data;

    private AllocatedBuffer handle;

    // (Ayydxn) Not currently used by Moonblast, but maybe useful to end users.
    public GraphicsBuffer(ByteBuffer data)
    {
        this.vulkanMemoryAllocator = VulkanMemoryAllocator.getInstance();
        this.data = data;

        this.handle = null;
    }

    public final void create()
    {
        try (MemoryStack memoryStack = MemoryStack.stackPush())
        {
            if (this.isMappable())
            {
                // Directly create a mappable buffer which is host visible
                VkBufferCreateInfo bufferCreateInfo = VkBufferCreateInfo.calloc(memoryStack)
                        .sType(VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO)
                        .size(this.data.remaining())
                        .usage(this.getUsageFlags())
                        .sharingMode(VK_SHARING_MODE_EXCLUSIVE);

                this.handle = this.vulkanMemoryAllocator.allocateBuffer(bufferCreateInfo, VMA_MEMORY_USAGE_CPU_TO_GPU);

                // Copy data to the buffer
                PointerBuffer pDestinationBuffer = this.vulkanMemoryAllocator.mapMemory(this.handle.bufferAllocation());

                MemoryUtil.memCopy(this.data, pDestinationBuffer.getByteBuffer(0, this.data.remaining()));

                this.vulkanMemoryAllocator.unmapMemory(this.handle.bufferAllocation());
            }
            else
            {
                // Create the staging buffer
                VkBufferCreateInfo stagingBufferCreateInfo = VkBufferCreateInfo.calloc(memoryStack)
                        .sType(VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO)
                        .size(this.data.remaining())
                        .usage(VK_BUFFER_USAGE_TRANSFER_SRC_BIT)
                        .sharingMode(VK_SHARING_MODE_EXCLUSIVE);

                AllocatedBuffer stagingBuffer = this.vulkanMemoryAllocator.allocateBuffer(stagingBufferCreateInfo, VMA_MEMORY_USAGE_CPU_TO_GPU);

                // Copy data to staging buffer
                PointerBuffer pDestinationBuffer = this.vulkanMemoryAllocator.mapMemory(stagingBuffer.bufferAllocation());

                MemoryUtil.memCopy(this.data, pDestinationBuffer.getByteBuffer(0, this.data.remaining()));

                this.vulkanMemoryAllocator.unmapMemory(stagingBuffer.bufferAllocation());

                // Create the device-local buffer
                VkBufferCreateInfo vertexBufferCreateInfo = VkBufferCreateInfo.calloc(memoryStack)
                        .sType(VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO)
                        .size(this.data.remaining())
                        .usage(this.getUsageFlags() | VK_BUFFER_USAGE_TRANSFER_DST_BIT);

                this.handle = this.vulkanMemoryAllocator.allocateBuffer(vertexBufferCreateInfo, VMA_MEMORY_USAGE_GPU_ONLY);

                // Copy data from the staging buffer to the buffer
                VkBufferCopy.Buffer bufferCopy = VkBufferCopy.calloc(1, memoryStack)
                        .size(this.data.remaining());

                CommandBuffer commandBuffer = new CommandBuffer(1);
                commandBuffer.begin();

                vkCmdCopyBuffer(commandBuffer.getActiveCommandBuffer(), stagingBuffer.buffer(), this.handle.buffer(), bufferCopy);

                commandBuffer.end();
                commandBuffer.submit();

                // We don't need the staging buffer or command buffer anymore, so destroy them.
                this.vulkanMemoryAllocator.destroyBuffer(stagingBuffer);
                commandBuffer.destroy();
            }
        }
    }

    public void destroy()
    {
        this.vulkanMemoryAllocator.destroyBuffer(this.handle);

        MemoryUtil.memFree(this.data);
    }

    public abstract int getUsageFlags();

    public abstract boolean isMappable();

    public long getHandle()
    {
        return this.handle.buffer();
    }

    public int getSize()
    {
        return this.data.remaining();
    }
}
