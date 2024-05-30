package me.ayydan.iridium.mixin.core.blaze3d.shader;

import com.mojang.blaze3d.shader.GlslImportProcessor;
import com.mojang.blaze3d.shader.ShaderStage;
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

@Mixin(ShaderStage.class)
public class ShaderStageMixin
{
    @Inject(method = "compileShaderInternal", at = @At("HEAD"), cancellable = true)
    private static void compileShaderInternal(ShaderStage.Type type, String name, InputStream inputStream, String domain, GlslImportProcessor importProcessor,
                                              CallbackInfoReturnable<Integer> cir) throws IOException
    {
        String shaderSource = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        if (shaderSource == null)
        {
            throw new IOException(String.format("Failed to load shader program '%s'! (Type: %s)", name, type.getName()));
        }
        else
        {
            importProcessor.process(shaderSource);
            IridiumShaderCompiler.getInstance().compileShader(name, shaderSource, IridiumShaderUtils.getIridiumStageFromMinecraft(type));
        }

        cir.setReturnValue(0);
        cir.cancel();
    }
}
