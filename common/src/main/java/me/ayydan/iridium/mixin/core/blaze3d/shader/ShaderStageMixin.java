package me.ayydan.iridium.mixin.core.blaze3d.shader;

import com.mojang.blaze3d.preprocessor.GlslPreprocessor;
import com.mojang.blaze3d.shaders.Program;
import me.ayydan.iridium.render.shader.IridiumShaderCompiler;
import me.ayydan.iridium.render.shader.utils.IridiumShaderUtils;
import org.apache.commons.io.IOUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Mixin(Program.class)
public class ShaderStageMixin
{
    @Inject(method = "compileShaderInternal", at = @At("HEAD"), cancellable = true)
    private static void compileShaderInternal(Program.Type type, String name, InputStream shaderData, String sourceName, GlslPreprocessor preprocessor,
                                              CallbackInfoReturnable<Integer> cir) throws IOException
    {
        String shaderSource = IOUtils.toString(shaderData, StandardCharsets.UTF_8);
        if (shaderSource == null)
        {
            throw new IOException(String.format("Failed to load shader program '%s'! (Type: %s)", name, type.getName()));
        }
        else
        {
            preprocessor.process(shaderSource);
            IridiumShaderCompiler.getInstance().compileShader(name, shaderSource, IridiumShaderUtils.getIridiumStageFromMinecraft(type));
        }

        cir.setReturnValue(0);
        cir.cancel();
    }
}
