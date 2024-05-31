package me.ayydan.iridium.mixin.core.blaze3d.shader;

import com.mojang.blaze3d.shaders.ProgramManager;
import com.mojang.blaze3d.shaders.Shader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ProgramManager.class)
public class GLProgramManagerMixin
{
    @Inject(method = "createProgram", at = @At("HEAD"), cancellable = true)
    private static void stopCreationOfShaderProgram(CallbackInfoReturnable<Integer> cir)
    {
        cir.setReturnValue(-1);
    }

    @Inject(method = "linkShader", at = @At("HEAD"), cancellable = true)
    private static void stopLinkOfShader(Shader shader, CallbackInfo ci)
    {
        ci.cancel();
    }
}
