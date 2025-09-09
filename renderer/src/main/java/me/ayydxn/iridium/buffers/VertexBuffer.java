package me.ayydxn.iridium.buffers;

import java.nio.ByteBuffer;

import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT;

public class VertexBuffer extends GraphicsBuffer
{
    public VertexBuffer(ByteBuffer data)
    {
        super(data);
    }

    @Override
    public int getUsageFlags()
    {
        return VK_BUFFER_USAGE_VERTEX_BUFFER_BIT;
    }

    @Override
    public boolean isMappable()
    {
        return false;
    }
}
