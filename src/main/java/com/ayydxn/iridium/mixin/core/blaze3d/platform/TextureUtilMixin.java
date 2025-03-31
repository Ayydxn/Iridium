package com.ayydxn.iridium.mixin.core.blaze3d.platform;

import com.ayydxn.iridium.render.exceptions.NotImplementedByIridiumException;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TextureUtil.class)
public class TextureUtilMixin
{
    @Inject(method = "generateTextureId", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;assertOnRenderThreadOrInit()V", shift = At.Shift.AFTER), cancellable = true, remap = false)
    private static void generateVulkanTextureID(CallbackInfoReturnable<Integer> cir)
    {
        //throw new NotImplementedByIridiumException("TextureUtil::generateTextureId");

        cir.setReturnValue(-1);
    }

    @Inject(method = "prepareImage(Lcom/mojang/blaze3d/platform/NativeImage$InternalGlFormat;IIII)V", at = @At("HEAD"), cancellable = true)
    private static void createVulkanImage(NativeImage.InternalGlFormat internalGlFormat, int i, int j, int k, int l, CallbackInfo ci)
    {
        //throw new NotImplementedByIridiumException("TextureUtil::prepareImage");

        ci.cancel();
    }
}
