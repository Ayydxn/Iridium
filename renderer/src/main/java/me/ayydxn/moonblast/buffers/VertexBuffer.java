package me.ayydxn.moonblast.buffers;

import me.ayydxn.moonblast.renderer.memory.VulkanMemoryAllocator;
import me.ayydxn.moonblast.renderer.memory.AllocatedBuffer;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VkBufferCreateInfo;

import java.nio.ByteBuffer;

import static org.lwjgl.util.vma.Vma.VMA_MEMORY_USAGE_CPU_TO_GPU;
import static org.lwjgl.vulkan.VK10.*;

public class VertexBuffer
{
    private final VulkanMemoryAllocator vulkanMemoryAllocator;
    private final ByteBuffer data;

    private long vertexBufferHandle;
    private long memoryAllocation;

    public VertexBuffer(ByteBuffer data)
    {
        this.vulkanMemoryAllocator = VulkanMemoryAllocator.getInstance();
        this.data = data;
        this.vertexBufferHandle = VK_NULL_HANDLE;

        this.memoryAllocation = VK_NULL_HANDLE;
    }

    public void create()
    {
        try (MemoryStack memoryStack = MemoryStack.stackPush())
        {
            VkBufferCreateInfo vertexBufferCreateInfo =  VkBufferCreateInfo.calloc(memoryStack)
                    .sType(VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO)
                    .size(data.remaining())
                    .usage(VK_BUFFER_USAGE_VERTEX_BUFFER_BIT);

            AllocatedBuffer bufferAllocation = this.vulkanMemoryAllocator.allocateBuffer(vertexBufferCreateInfo, VMA_MEMORY_USAGE_CPU_TO_GPU);

            this.vertexBufferHandle = bufferAllocation.buffer();
            this.memoryAllocation = bufferAllocation.bufferAllocation();

            PointerBuffer pDestinationBuffer = this.vulkanMemoryAllocator.mapMemory(this.memoryAllocation);

            MemoryUtil.memCopy(this.data, pDestinationBuffer.getByteBuffer(0, this.data.remaining()));

            this.vulkanMemoryAllocator.unmapMemory(this.memoryAllocation);
        }
    }

    public void destroy()
    {
        this.vulkanMemoryAllocator.destroyBuffer(this.vertexBufferHandle, this.memoryAllocation);

        MemoryUtil.memFree(this.data);
    }

    public long getHandle()
    {
        return this.vertexBufferHandle;
    }

    public int getSize()
    {
        return this.data.remaining();
    }
}
