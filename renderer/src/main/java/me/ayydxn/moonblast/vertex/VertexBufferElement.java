package me.ayydxn.moonblast.vertex;

import me.ayydxn.moonblast.shaders.ShaderDataTypes;

public class VertexBufferElement
{
    public String name;
    public ShaderDataTypes shaderDataType;
    public int size;
    public int offset;
    public boolean isNormalized;

    public VertexBufferElement(String name, ShaderDataTypes shaderDataType)
    {
        this(name, shaderDataType, false);
    }

    public VertexBufferElement(String name, ShaderDataTypes shaderDataType, boolean isNormalized)
    {
        this.name = name;
        this.shaderDataType = shaderDataType;
        this.size = shaderDataType.getSize();
        this.offset = 0;
        this.isNormalized = isNormalized;
    }

    public int getComponentCount()
    {
        return switch (this.shaderDataType)
        {
            case Float, Int, Boolean -> 1;
            case Float2, Int2 -> 2;
            case Float3, Int3 -> 3;
            case Float4, Int4 -> 4;
            case Matrix3x3 -> 3 * 3;
            case Matrix4x4 -> 4 * 4;
            default -> throw new IllegalArgumentException(String.format("Unknown shader data type '%s'!", shaderDataType.name()));
        };
    }
}
