package me.ayydxn.moonblast.buffers;

import me.ayydxn.moonblast.renderer.CommandBuffer;
import me.ayydxn.moonblast.renderer.memory.VulkanMemoryAllocator;
import me.ayydxn.moonblast.renderer.memory.AllocatedBuffer;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VkBufferCopy;
import org.lwjgl.vulkan.VkBufferCreateInfo;

import java.nio.ByteBuffer;

import static org.lwjgl.util.vma.Vma.VMA_MEMORY_USAGE_CPU_TO_GPU;
import static org.lwjgl.util.vma.Vma.VMA_MEMORY_USAGE_GPU_ONLY;
import static org.lwjgl.vulkan.VK10.*;

public class VertexBuffer
{
    private final VulkanMemoryAllocator vulkanMemoryAllocator;
    private final ByteBuffer data;

    private AllocatedBuffer vertexBuffer;

    public VertexBuffer(ByteBuffer data)
    {
        this.vulkanMemoryAllocator = VulkanMemoryAllocator.getInstance();
        this.data = data;

        this.vertexBuffer = null;
    }

    public void create()
    {
        CommandBuffer commandBuffer = new CommandBuffer(1);

        try (MemoryStack memoryStack = MemoryStack.stackPush())
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

            // Create the vertex buffer
            VkBufferCreateInfo vertexBufferCreateInfo =  VkBufferCreateInfo.calloc(memoryStack)
                    .sType(VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO)
                    .size(this.data.remaining())
                    .usage(VK_BUFFER_USAGE_VERTEX_BUFFER_BIT | VK_BUFFER_USAGE_TRANSFER_DST_BIT);

            AllocatedBuffer vertexBuffer = this.vulkanMemoryAllocator.allocateBuffer(vertexBufferCreateInfo, VMA_MEMORY_USAGE_GPU_ONLY);

            // Copy data from the staging buffer to the vertex buffer
            VkBufferCopy.Buffer bufferCopy = VkBufferCopy.calloc(1, memoryStack)
                    .size(this.data.remaining());

            commandBuffer.begin();

            vkCmdCopyBuffer(commandBuffer.getActiveCommandBuffer(), stagingBuffer.buffer(), vertexBuffer.buffer(), bufferCopy);

            commandBuffer.end();
            commandBuffer.submit();

            // We don't need the staging buffer or command buffer anymore, so destroy them.
            this.vulkanMemoryAllocator.destroyBuffer(stagingBuffer);
            commandBuffer.destroy();

            this.vertexBuffer = vertexBuffer;
        }
    }

    public void destroy()
    {
        this.vulkanMemoryAllocator.destroyBuffer(this.vertexBuffer);

        MemoryUtil.memFree(this.data);
    }

    public long getHandle()
    {
        return this.vertexBuffer.buffer();
    }

    public int getSize()
    {
        return this.data.remaining();
    }
}
