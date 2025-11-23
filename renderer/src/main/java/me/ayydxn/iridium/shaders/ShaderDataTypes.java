package me.ayydxn.iridium.shaders;

import static org.lwjgl.vulkan.VK10.*;

public enum ShaderDataTypes
{
    Float(4, VK_FORMAT_R32_SFLOAT),
    Float2(4 * 2, VK_FORMAT_R32G32_SFLOAT),
    Float3(4 * 3, VK_FORMAT_R32G32B32_SFLOAT),
    Float4(4 * 4, VK_FORMAT_R32G32B32A32_SFLOAT),

    Matrix3x3(4 * 3 * 3, VK_FORMAT_UNDEFINED),
    Matrix4x4(4 * 4 * 4, VK_FORMAT_UNDEFINED),

    Int(4, VK_FORMAT_R32_SINT),
    Int2(4 * 2, VK_FORMAT_R32G32_SINT),
    Int3(4 * 3, VK_FORMAT_R32G32B32_SINT),
    Int4(4 * 4, VK_FORMAT_R32G32B32A32_SINT),

    Boolean(1, VK_FORMAT_UNDEFINED);

    private final int size;
    private final int vulkanFormat;

    ShaderDataTypes(int size, int vulkanFormat)
    {
        this.size = size;
        this.vulkanFormat = vulkanFormat;
    }

    public int getSize()
    {
        return this.size;
    }

    public int getVulkanFormat()
    {
        return this.vulkanFormat;
    }
}
