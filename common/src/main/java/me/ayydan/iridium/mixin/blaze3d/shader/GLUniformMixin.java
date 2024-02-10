package me.ayydan.iridium.mixin.blaze3d.shader;

import com.mojang.blaze3d.shader.GlUniform;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GlUniform.class)
public class GLUniformMixin
{
    @Inject(method = "upload", at = @At("HEAD"), cancellable = true)
    public void cancelUniformUpload(CallbackInfo ci)
    {
        ci.cancel();
    }

    @Inject(method = "getUniformLocation", at = @At("HEAD"), cancellable = true)
    private static void cancelGetUniformLocation(int program, CharSequence name, CallbackInfoReturnable<Integer> cir)
    {
        cir.setReturnValue(1);
    }

    @Inject(method = "getAttribLocation", at = @At("HEAD"), cancellable = true)
    private static void cancelGetAttribLocation(int program, CharSequence name, CallbackInfoReturnable<Integer> cir)
    {
        cir.setReturnValue(0);
    }
}
