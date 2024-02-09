package me.ayydan.iridium.shader;

import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.NativeResource;

import java.nio.ByteBuffer;

import static org.lwjgl.util.shaderc.Shaderc.shaderc_result_release;

public class ShaderSPIRV implements NativeResource
{
    private final long handle;

    private ByteBuffer shaderBytecode;

    public ShaderSPIRV(long handle, ByteBuffer shaderBytecode)
    {
        this.handle = handle;
        this.shaderBytecode = shaderBytecode;
    }

    @Override
    public void free()
    {
        if (this.handle != MemoryUtil.NULL)
            shaderc_result_release(handle);

        this.shaderBytecode = null;
    }

    public ByteBuffer getShaderBytecode() { return this.shaderBytecode; }
}
