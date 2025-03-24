package com.ayydxn.iridium.render.shader;

import static org.lwjgl.util.shaderc.Shaderc.*;

public enum ShaderStage
{
    VERTEX("Vertex Shader", shaderc_glsl_vertex_shader, "VertexCache"),
    FRAGMENT("Fragment Shader", shaderc_glsl_fragment_shader, "FragmentCache");

    private final String name;
    private final int id;
    private final String cacheID;

    ShaderStage(String name, int id, String cacheID)
    {
        this.name = name;
        this.id = id;
        this.cacheID = cacheID;
    }

    public int getID()
    {
        return this.id;
    }

    public String getCacheID()
    {
        return this.cacheID;
    }

    @Override
    public String toString()
    {
        return this.name;
    }
}
