package me.ayydxn.iridium.shaders;

import me.ayydxn.iridium.buffers.UniformBuffer;

public class ShaderResourceBinding
{
    private final String name;
    private final ShaderResource.Type resourceType;

    private UniformBuffer boundUniformBuffer;
    private boolean isDirty;

    public ShaderResourceBinding(String name, ShaderResource.Type resourceType)
    {
        this.name = name;
        this.resourceType = resourceType;

        this.isDirty = true;
    }

    public void markClean()
    {
        this.isDirty = false;
    }

    public UniformBuffer getUniformBuffer()
    {
        return this.boundUniformBuffer;
    }

    public void setUniformBuffer(UniformBuffer newUniformBuffer)
    {
        this.boundUniformBuffer = newUniformBuffer;
        this.isDirty = true;
    }

    public boolean isDirty()
    {
        return this.isDirty;
    }
}
