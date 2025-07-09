package me.ayydxn.moonblast.shaders;

import static org.lwjgl.util.shaderc.Shaderc.shaderc_glsl_fragment_shader;
import static org.lwjgl.util.shaderc.Shaderc.shaderc_glsl_vertex_shader;

public enum ShaderStage
{
    VERTEX("Vertex Shader", shaderc_glsl_vertex_shader, ".vsh", "VertexCache"),
    FRAGMENT("Fragment Shader", shaderc_glsl_fragment_shader, ".fsh", "FragmentCache");

    private final String name;
    private final int id;
    private final String fileExtension;
    private final String cacheID;

    ShaderStage(String name, int id, String fileExtension, String cacheID)
    {
        this.name = name;
        this.id = id;
        this.fileExtension = fileExtension;
        this.cacheID = cacheID;
    }

    public static ShaderStage getFromString(String shaderStage)
    {
        return switch(shaderStage)
        {
            case "vertex" -> VERTEX;
            case "fragment" -> FRAGMENT;
            default -> throw new IllegalArgumentException(String.format("Unknown shader stage '%s'!", shaderStage));
        };
    }

    public int getID()
    {
        return this.id;
    }

    public String getFileExtension()
    {
        return this.fileExtension;
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
