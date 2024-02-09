package me.ayydan.iridium.shader;

import static org.lwjgl.util.shaderc.Shaderc.shaderc_glsl_fragment_shader;
import static org.lwjgl.util.shaderc.Shaderc.shaderc_glsl_vertex_shader;

public enum ShaderStage
{
    VertexShader("Vertex Shader", shaderc_glsl_vertex_shader),
    FragmentShader("Fragment Shader", shaderc_glsl_fragment_shader);

    private final String name;
    private final int id;

    ShaderStage(String name, int id)
    {
        this.name = name;
        this.id = id;
    }

    public int getID() { return this.id; }

    @Override
    public String toString()
    {
        return this.name;
    }
}
