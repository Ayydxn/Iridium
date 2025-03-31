package com.ayydxn.iridium.mixin.core.blaze3d.pipeline;

import com.mojang.blaze3d.pipeline.MainTarget;
import com.mojang.blaze3d.pipeline.RenderTarget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MainTarget.class)
public class MainTargetMixin extends RenderTarget
{
    public MainTargetMixin(boolean useDepth)
    {
        super(useDepth);
    }

    @Inject(method = "createFrameBuffer", at = @At("HEAD"), cancellable = true)
    public void createVulkanMainTargetFramebuffer(int width, int height, CallbackInfo ci)
    {
        this.frameBufferId = 0;

        this.width = width;
        this.height = height;
        this.viewWidth = width;
        this.viewHeight = height;

        ci.cancel();
    }
}
