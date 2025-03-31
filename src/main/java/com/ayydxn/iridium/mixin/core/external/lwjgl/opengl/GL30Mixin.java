package com.ayydxn.iridium.mixin.core.external.lwjgl.opengl;

import org.lwjgl.opengl.GL30;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GL30.class)
public class GL30Mixin
{
    @Redirect(method = "glDeleteFramebuffers(I)V", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL30C;glDeleteFramebuffers(I)V"), remap = false)
    private static void deleteVulkanFramebuffers(int framebuffers)
    {
        //throw new NotImplementedByIridiumException("GL30::glDeleteFramebuffers");
    }
}
