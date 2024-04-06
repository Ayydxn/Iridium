package me.ayydan.iridium.mixin.blaze3d.systems;

import com.mojang.blaze3d.systems.RenderSystem;
import me.ayydan.iridium.render.IridiumRenderSystem;
import me.ayydan.iridium.render.IridiumRenderer;
import org.apache.commons.lang3.NotImplementedException;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderSystem.class)
public class RenderSystemMixin
{
    @Redirect(method = "initRenderer", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GLX;_init(IZ)V"), remap = false)
    private static void initializeIridiumRenderer(int verbosity, boolean sync)
    {
        IridiumRenderSystem.initRenderer();
    }

    @Redirect(method = "flipFrame", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwSwapBuffers(J)V"), remap = false)
    private static void presentCurrentSwapChainImage(long window)
    {
        IridiumRenderer.getInstance().getVulkanContext().getSwapChain().present();
    }

    @Redirect(method = "enableCull", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;_enableCull()V"), remap = false)
    private static void enableCulling()
    {
        RenderSystem.assertOnGameThread();

        throw new NotImplementedException("RenderSystem::enableCull has not been implemented by Iridium!");
    }

    @Inject(method = "maxSupportedTextureSize", at = @At("HEAD"), remap = false, cancellable = true)
    private static void getMaxSupportedTextureSize(CallbackInfoReturnable<Integer> cir)
    {
        cir.setReturnValue(IridiumRenderSystem.getMaxSupportedTextureSize());
    }
}
