package com.ayydxn.iridium.mixin.core.client.renderer.texture;

import net.minecraft.client.renderer.texture.AbstractTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractTexture.class)
public class AbstractTextureMixin
{
    @Inject(method = "setFilter(ZZ)V", at = @At("HEAD"), cancellable = true)
    public void setVulkanTextureFilter(boolean blur, boolean mipmap, CallbackInfo ci)
    {
        //throw new NotImplementedByIridiumException("AbstractTexture::setFilter");

        ci.cancel();
    }
}
