package me.ayydxn.moonblast.shaders;

import static org.lwjgl.util.shaderc.Shaderc.shaderc_glsl_fragment_shader;
import static org.lwjgl.util.shaderc.Shaderc.shaderc_glsl_vertex_shader;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_FRAGMENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_VERTEX_BIT;

public enum ShaderStage
{
    VERTEX("Vertex Shader", shaderc_glsl_vertex_shader, VK_SHADER_STAGE_VERTEX_BIT, ".vsh", "VertexCache"),
    FRAGMENT("Fragment Shader", shaderc_glsl_fragment_shader, VK_SHADER_STAGE_FRAGMENT_BIT, ".fsh", "FragmentCache");

    private final String name;
    private final int id;
    private final int vulkanID;
    private final String fileExtension;
    private final String cacheID;

    ShaderStage(String name, int id, int vulkanID, String fileExtension, String cacheID)
    {
        this.name = name;
        this.id = id;
        this.vulkanID = vulkanID;
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

    public int getVulkanID()
    {
        return this.vulkanID;
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
