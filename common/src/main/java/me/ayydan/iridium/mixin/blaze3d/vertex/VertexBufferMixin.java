package me.ayydan.iridium.mixin.blaze3d.vertex;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexBuffer;
import org.apache.commons.lang3.NotImplementedException;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VertexBuffer.class)
public class VertexBufferMixin
{
    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;_glGenBuffers()I"))
    public int cancelGLGenBuffers()
    {
        return 0;
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;_glGenVertexArrays()I"))
    public int cancelGLGenVertexArrays()
    {
        return 0;
    }

    @Inject(method = "upload", at = @At("HEAD"))
    public void uploadBuffer(BufferBuilder.RenderedBuffer renderedBuffer, CallbackInfo ci)
    {
        throw new NotImplementedException("VertexBuffer::upload has not been implemented by Iridium!");
    }

    @Redirect(method = "bind", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;_glBindVertexArray(I)V"))
    public void cancelBindVertexArray(int vertexArray)
    {
    }

    @Redirect(method = "unbind", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;_glBindVertexArray(I)V"), remap = false)
    private static void cancelUnbindVertexArray(int vertexArray)
    {
    }
}
