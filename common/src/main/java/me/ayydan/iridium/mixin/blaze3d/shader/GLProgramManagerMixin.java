package me.ayydan.iridium.mixin.blaze3d.shader;

import com.mojang.blaze3d.shader.GlProgram;
import com.mojang.blaze3d.shader.GlProgramManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GlProgramManager.class)
public class GLProgramManagerMixin
{
    @Inject(method = "createProgram", at = @At("HEAD"), cancellable = true)
    private static void stopCreationOfShaderProgram(CallbackInfoReturnable<Integer> cir)
    {
        cir.setReturnValue(-1);
    }

    @Inject(method = "linkShader", at = @At("HEAD"), cancellable = true)
    private static void stopLinkOfShader(GlProgram shader, CallbackInfo ci)
    {
        ci.cancel();
    }
}
