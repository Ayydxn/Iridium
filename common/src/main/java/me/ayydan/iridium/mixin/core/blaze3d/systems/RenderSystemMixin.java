package me.ayydan.iridium.mixin.core.blaze3d.systems;

import com.mojang.blaze3d.systems.RenderSystem;
import me.ayydan.iridium.render.IridiumRenderSystem;
import me.ayydan.iridium.render.IridiumRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.util.TimeSource;
import org.apache.commons.lang3.NotImplementedException;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderSystem.class)
public class RenderSystemMixin
{
    @Inject(method = "initBackendSystem", at = @At(value = "INVOKE", target = "Ljava/util/Objects;requireNonNull(Ljava/lang/Object;)Ljava/lang/Object;", shift = At.Shift.AFTER), remap = false)
    private static void initializeIridiumRenderer(CallbackInfoReturnable<TimeSource.NanoTimeSource> cir)
    {
        IridiumRenderSystem.initRenderer();
    }

    @Redirect(method = "flipFrame", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwSwapBuffers(J)V"), remap = false)
    private static void presentCurrentSwapChainImage(long window)
    {
        Minecraft.getInstance().getWindow().getSwapChain().present();
    }

    @Redirect(method = "enableCull", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;_enableCull()V"), remap = false)
    private static void enableCulling()
    {
        RenderSystem.assertOnRenderThread();

        throw new NotImplementedException("RenderSystem::enableCull has not been implemented by Iridium!");
    }

    @Redirect(method = "clearColor", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;_clearColor(FFFF)V"), remap = false)
    private static void setClearColor(float red, float green, float blue, float alpha)
    {
        IridiumRenderSystem.setClearColor(red, green, blue, alpha);
    }

    @Inject(method = "maxSupportedTextureSize", at = @At("HEAD"), remap = false, cancellable = true)
    private static void getMaxSupportedTextureSize(CallbackInfoReturnable<Integer> cir)
    {
        cir.setReturnValue(IridiumRenderSystem.getMaxSupportedTextureSize());
    }
}
