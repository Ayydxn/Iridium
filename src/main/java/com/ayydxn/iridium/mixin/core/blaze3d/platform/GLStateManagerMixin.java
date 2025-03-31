package com.ayydxn.iridium.mixin.core.blaze3d.platform;

import com.ayydxn.iridium.render.IridiumRenderSystem;
import com.mojang.blaze3d.platform.GlStateManager;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30C;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.IntBuffer;

@Mixin(GlStateManager.class)
public class GLStateManagerMixin
{
    @Redirect(method = "_disableDepthTest", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager$BooleanState;disable()V"), remap = false)
    private static void disableVulkanDepthTesting(GlStateManager.BooleanState instance)
    {
        IridiumRenderSystem.disableDepthTesting();
    }

    @Redirect(method = "_enableDepthTest", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager$BooleanState;enable()V"), remap = false)
    private static void enableVulkanDepthTesting(GlStateManager.BooleanState instance)
    {
        IridiumRenderSystem.enableDepthTesting();
    }

    @Inject(method = "_glGenBuffers", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;assertOnRenderThreadOrInit()V", shift = At.Shift.AFTER), cancellable = true, remap = false)
    private static void createVulkanBuffers(CallbackInfoReturnable<Integer> cir)
    {
        //throw new NotImplementedByIridiumException("GlStateManager::_glGenBuffers");

        cir.setReturnValue(0);
    }


    @Redirect(method = "glGenFramebuffers", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL30;glGenFramebuffers()I"), remap = false)
    private static int createVulkanFramebuffers()
    {
        //throw new NotImplementedByIridiumException("GlStateManager::glGenFramebuffers");

        return 0;
    }

    @Redirect(method = "glCheckFramebufferStatus", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL30;glCheckFramebufferStatus(I)I"), remap = false)
    private static int checkVulkanFramebufferStatus(int target)
    {
        //throw new NotImplementedByIridiumException("GlStateManager::glCheckFramebufferStatus");

        return 0x8CD5;
    }

    @Redirect(method = "_glFramebufferTexture2D", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL30;glFramebufferTexture2D(IIIII)V"), remap = false)
    private static void uploadVulkanFramebufferTexture(int target, int attachment, int textureTarget, int texture, int level)
    {
        //throw new NotImplementedByIridiumException("GlStateManager::_glFramebufferTexture2D");
    }

    @Inject(method = "_bindTexture", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;assertOnRenderThreadOrInit()V", shift = At.Shift.AFTER), cancellable = true, remap = false)
    private static void bindVulkanTexture(int i, CallbackInfo ci)
    {
        //throw new NotImplementedByIridiumException("GlStateManager::_bindTexture");

        ci.cancel();
    }

    @Inject(method = "_activeTexture", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;assertOnRenderThread()V", shift = At.Shift.AFTER), cancellable = true, remap = false)
    private static void setActiveVulkanTexture(int i, CallbackInfo ci)
    {
        //throw new NotImplementedByIridiumException("GlStateManager::_activeTexture");

        ci.cancel();
    }

    @Inject(method = "_texParameter(III)V", at = @At("HEAD"), cancellable = true, remap = false)
    private static void setVulkanTextureParameter(int i, int j, int k, CallbackInfo ci)
    {
        //throw new NotImplementedByIridiumException("GlStateManager::_texParameter");

        ci.cancel();
    }

    @Inject(method = "_texImage2D", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;assertOnRenderThreadOrInit()V", shift = At.Shift.AFTER), cancellable = true, remap = false)
    private static void uploadVulkanTexture(int i, int j, int k, int l, int m, int n, int o, int p, IntBuffer intBuffer, CallbackInfo ci)
    {
        //throw new NotImplementedByIridiumException("GlStateManager::_texImage2D");

        ci.cancel();
    }

    @Inject(method = "_texSubImage2D", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;assertOnRenderThreadOrInit()V", shift = At.Shift.AFTER), cancellable = true, remap = false)
    private static void uploadVulkanSubTexture(int target, int level, int xOffset, int yOffset, int width, int height, int format, int type, long pixels, CallbackInfo ci)
    {
        //throw new NotImplementedByIridiumException("GlStateManager::_texSubImage2D");

        ci.cancel();
    }

    @Inject(method = "_clearDepth", at = @At("HEAD"), cancellable = true, remap = false)
    private static void cancelOpenGLClear(double depth, CallbackInfo ci)
    {
        ci.cancel();
    }


    @Redirect(method = "_clearColor", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glClearColor(FFFF)V"), remap = false)
    private static void setVulkanClearColor(float red, float green, float blue, float alpha)
    {
        IridiumRenderSystem.setClearColor(red, green, blue, alpha);
    }

    @Redirect(method = "_clear", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glClear(I)V"), remap = false)
    private static void clearVulkanAttachments(int mask)
    {
        IridiumRenderSystem.clearAttachments(mask);
    }

    @Redirect(method = "_pixelStore", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glPixelStorei(II)V"), remap = false)
    private static void setVulkanPixelStorage(int pname, int param)
    {
        //throw new NotImplementedByIridiumException("GlStateManager::_pixelStore");
    }

    @Redirect(method = "_getError", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glGetError()I"), remap = false)
    private static int cancelGLGetError()
    {
        return 0;
    }
}
