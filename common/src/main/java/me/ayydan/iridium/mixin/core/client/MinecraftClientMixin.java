package me.ayydan.iridium.mixin.core.client;

import me.ayydan.iridium.render.IridiumRenderer;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin
{
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;clear(IZ)V", shift = At.Shift.BEFORE))
    public void beginFrame(boolean tick, CallbackInfo ci)
    {
        IridiumRenderer.getInstance().beginFrame();
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/glfw/Window;swapBuffers()V"))
    public void endFrame(boolean tick, CallbackInfo ci)
    {
        IridiumRenderer.getInstance().endFrame();
    }

    @Inject(method = "close", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Util;shutdownExecutors()V"))
    public void shutdownIridium(CallbackInfo ci)
    {
        IridiumRenderer.getInstance().shutdown();
    }
}
