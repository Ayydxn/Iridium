package me.ayydxn.iridium.buffers;

import java.nio.ByteBuffer;

import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_INDEX_BUFFER_BIT;

public class IndexBuffer extends GraphicsBuffer
{
    public IndexBuffer(ByteBuffer data)
    {
        super(data);
    }

    @Override
    public int getUsageFlags()
    {
        return VK_BUFFER_USAGE_INDEX_BUFFER_BIT;
    }

    @Override
    public boolean isMappable()
    {
        return false;
    }

    public int getCount()
    {
        return this.getSize() / Integer.BYTES;
    }
}
