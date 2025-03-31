package com.ayydxn.iridium.mixin.core.blaze3d.vertex;

import com.ayydxn.iridium.render.exceptions.NotImplementedByIridiumException;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexBuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VertexBuffer.class)
public class VertexBufferMixin
{
    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;_glGenVertexArrays()I"), remap = false)
    public int cancelVertexArraysCreation()
    {
        return 0;
    }

    @Inject(method = "upload", at = @At("HEAD"), cancellable = true, remap = false)
    public void uploadVulkanVertexBuffer(MeshData meshData, CallbackInfo ci)
    {
        //throw new NotImplementedByIridiumException("VertexBuffer::upload");

        ci.cancel();
    }

    @Inject(method = "bind", at = @At("HEAD"), cancellable = true, remap = false)
    public void cancelVertexBufferBind(CallbackInfo ci)
    {
        ci.cancel();
    }

    @Inject(method = "unbind", at = @At("HEAD"), cancellable = true, remap = false)
    private static void cancelVertexBufferUnbind(CallbackInfo ci)
    {
        ci.cancel();
    }
}
