package me.ayydan.iridium.mixin.core.blaze3d.platform;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import org.apache.commons.lang3.NotImplementedException;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER_COMPLETE;

@Mixin(GlStateManager.class)
public class GLStateManagerMixin
{
    @Redirect(method = "_enableDepthTest", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager$BooleanState;enable()V"), remap = false)
    private static void enableDepthTest(GlStateManager.BooleanState instance)
    {
        RenderSystem.assertOnRenderThreadOrInit();

        throw new NotImplementedException("GlStateManager::_enableDepthTest has not been implemented by Iridium!");
    }

    @Redirect(method = "_enableBlend", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager$BooleanState;enable()V"), remap = false)
    private static void enableBlend(GlStateManager.BooleanState capabilityTracker)
    {
        RenderSystem.assertOnRenderThread();

        throw new NotImplementedException("GlStateManager::_enableBlend has not been implemented by Iridium!");
    }

    @Redirect(method = "_blendFunc", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glBlendFunc(II)V"), remap = false)
    private static void blendFunc(int sourceFactor, int destFactor)
    {
        RenderSystem.assertOnRenderThread();

        throw new NotImplementedException("GlStateManager::_blendFunc has not been implemented by Iridium!");
    }

    @Redirect(method = "_depthFunc", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glDepthFunc(I)V"), remap = false)
    private static void depthFunc(int func)
    {
        RenderSystem.assertOnRenderThreadOrInit();

        throw new NotImplementedException("GlStateManager::_depthFunc has not been implemented by Iridium!");
    }

    @Redirect(method = "_depthMask", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glDepthMask(Z)V"), remap = false)
    private static void depthMask(boolean flag)
    {
        RenderSystem.assertOnRenderThread();

        throw new NotImplementedException("GlStateManager::_depthMask has not been implemented by Iridium!");
    }

    @Redirect(method = "_glBindFramebuffer", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL30;glBindFramebuffer(II)V"), remap = false)
    private static void bindFramebuffer(int target, int framebuffer)
    {
        RenderSystem.assertOnRenderThreadOrInit();

        throw new NotImplementedException("GlStateManager::_glBindFramebuffer has not been implemented by Iridium!");
    }

    @Redirect(method = "glGenFramebuffers", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL30;glGenFramebuffers()I"), remap = false)
    private static int genFramebuffers()
    {
        RenderSystem.assertOnRenderThreadOrInit();

        throw new NotImplementedException("GlStateManager::glGenFramebuffers has not been implemented by Iridium!");
    }

    @Redirect(method = "glCheckFramebufferStatus", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL30;glCheckFramebufferStatus(I)I"), remap = false)
    private static int checkFramebufferStatus(int target)
    {
        RenderSystem.assertOnRenderThreadOrInit();

        return GL_FRAMEBUFFER_COMPLETE;
    }

    @Redirect(method = "_glFramebufferTexture2D", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL30;glFramebufferTexture2D(IIIII)V"), remap = false)
    private static void framebufferTexture2D(int target, int attachment, int textureTarget, int texture, int level)
    {
        RenderSystem.assertOnRenderThreadOrInit();

        throw new NotImplementedException("GlStateManager::_glFramebufferTexture2D has not been implemented by Iridium!");
    }

    @Redirect(method = "_activeTexture", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;glActiveTexture(I)V"), remap = false)
    private static void activeTexture(int texture)
    {
        throw new NotImplementedException("GlStateManager::_activeTexture has not been implemented by Iridium!");
    }

    @Redirect(method = "_texParameter(IIF)V", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glTexParameterf(IIF)V"), remap = false)
    private static void texParameter(int target, int pname, float param)
    {
    }

    @Redirect(method = "_texParameter(III)V", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glTexParameteri(III)V"), remap = false)
    private static void texParameter(int target, int pname, int param)
    {
        throw new NotImplementedException("GlStateManager::_texParameter has not been implemented by Iridium!");
    }

    @Redirect(method = "_genTexture", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glGenTextures()I"), remap = false)
    private static int genTexture()
    {
        RenderSystem.assertOnRenderThreadOrInit();

        throw new NotImplementedException("GlStateManager::_genTexture has not been implemented by Iridium!");
    }

    @Redirect(method = "_bindTexture", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glBindTexture(II)V"), remap = false)
    private static void bindTexture(int target, int texture)
    {
        throw new NotImplementedException("GlStateManager::_bindTexture has not been implemented by Iridium!");
    }

    @Redirect(method = "_texImage2D", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glTexImage2D(IIIIIIIILjava/nio/IntBuffer;)V"), remap = false)
    private static void texImage2D(int target, int level, int internalformat, int width, int height, int border, int format, int type, IntBuffer pixels)
    {
        throw new NotImplementedException("GlStateManager::_texImage2D has not been implemented by Iridium!");
    }

    @Redirect(method = "_texSubImage2D", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glTexSubImage2D(IIIIIIIIJ)V"), remap = false)
    private static void texSubImage2D(int target, int level, int xOffset, int yOffset, int width, int height, int format, int type, long pixels)
    {
    }

    @Redirect(method = "_viewport", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glViewport(IIII)V"), remap = false)
    private static void setViewport(int x, int y, int width, int height)
    {
        throw new NotImplementedException("GlStateManager::_viewport has not been implemented by Iridium!");
    }

    @Redirect(method = "_clearDepth", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glClearDepth(D)V"), remap = false)
    private static void clearDepth(double depth)
    {
    }

    @Redirect(method = "_clearColor", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glClearColor(FFFF)V"), remap = false)
    private static void setClearColor(float red, float green, float blue, float alpha)
    {
        RenderSystem.assertOnRenderThreadOrInit();

        throw new NotImplementedException("GlStateManager::_clearColor has not been implemented by Iridium!");
    }

    @Redirect(method = "_clear", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glClear(I)V"), remap = false)
    private static void clearBufferMask(int mask)
    {
        RenderSystem.assertOnRenderThreadOrInit();

        throw new NotImplementedException("GlStateManager::_clear has not been implemented by Iridium!");
    }

    @Redirect(method = "_pixelStore", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glPixelStorei(II)V"), remap = false)
    private static void pixelStore(int pname, int param)
    {
    }

    @Redirect(method = "_getError", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glGetError()I"), remap = false)
    private static int getError()
    {
        return 0;
    }

    @Redirect(method = "_getInteger", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glGetInteger(I)I"), remap = false)
    private static int getInteger(int pname)
    {
        return 0;
    }
}
