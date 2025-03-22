package com.ayydxn.iridium.mixin.core.blaze3d.systems;

import com.ayydxn.iridium.render.IridiumRenderSystem;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderSystem.class)
public class RenderSystemMixin
{
    @Inject(method = "initRenderer", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GLX;_init(IZ)V"), remap = false)
    private static void initializeIridiumRenderer(int i, boolean bl, CallbackInfo ci)
    {
        IridiumRenderSystem.initRenderer();
    }

    @Redirect(method = "flipFrame", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwSwapBuffers(J)V"), remap = false)
    private static void presentCurrentSwapChainImage(long window)
    {
        Minecraft.getInstance().getWindow().getSwapChain().present();
    }

    @Inject(method = "maxSupportedTextureSize", at = @At("HEAD"), remap = false, cancellable = true)
    private static void getMaxSupportedTextureSize(CallbackInfoReturnable<Integer> cir)
    {
        cir.setReturnValue(IridiumRenderSystem.getMaxSupportedTextureSize());
    }
}
