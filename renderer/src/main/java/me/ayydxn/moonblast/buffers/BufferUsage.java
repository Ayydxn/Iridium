package me.ayydxn.moonblast.buffers;

import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_INDEX_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT;

public enum BufferUsage
{
    Vertex(VK_BUFFER_USAGE_VERTEX_BUFFER_BIT),
    Index(VK_BUFFER_USAGE_INDEX_BUFFER_BIT);

    private final int vulkanBufferUsage;

    BufferUsage(int vulkanBufferUsage)
    {
        this.vulkanBufferUsage = vulkanBufferUsage;
    }

    public int vulkanUsage()
    {
        return this.vulkanBufferUsage;
    }
}
