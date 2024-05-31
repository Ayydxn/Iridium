package me.ayydan.iridium.render.shader.utils;

import com.mojang.blaze3d.shaders.Program;
import me.ayydan.iridium.render.shader.ShaderStage;

public class IridiumShaderUtils
{
    public static ShaderStage getIridiumStageFromMinecraft(Program.Type minecraftShaderStage)
    {
        return switch (minecraftShaderStage)
        {
            case VERTEX -> ShaderStage.VertexShader;
            case FRAGMENT -> ShaderStage.FragmentShader;
            default -> throw new IllegalArgumentException(String.format("Failed to get Iridium shader stage from Minecraft shader stage '%s'!", minecraftShaderStage.getName()));
        };
    }
}
