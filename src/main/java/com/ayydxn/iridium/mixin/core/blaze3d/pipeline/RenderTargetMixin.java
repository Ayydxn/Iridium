package com.ayydxn.iridium.mixin.core.blaze3d.pipeline;

import com.ayydxn.iridium.render.IridiumRenderer;
import com.mojang.blaze3d.pipeline.RenderTarget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RenderTarget.class)
public class RenderTargetMixin
{
    @Redirect(method = "bindWrite", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;_viewport(IIII)V"), remap = false)
    public void bindVulkanRenderTargetWrite(int x, int y, int width, int height)
    {
        IridiumRenderer.getInstance().setViewport(x, y, width, height);
    }
}
