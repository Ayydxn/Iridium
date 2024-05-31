package me.ayydan.iridium.mixin.core.client;

import me.ayydan.iridium.render.IridiumRenderer;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftClientMixin
{
    @Inject(method = "runTick", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;clear(IZ)V", shift = At.Shift.BEFORE), remap = false)
    public void beginFrame(boolean renderLevel, CallbackInfo ci)
    {
        IridiumRenderer.getInstance().beginFrame();
    }

    @Inject(method = "runTick", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/Window;updateDisplay()V"))
    public void endFrame(boolean renderLevel, CallbackInfo ci)
    {
        IridiumRenderer.getInstance().endFrame();
    }

    @Inject(method = "close", at = @At(value = "INVOKE", target = "Lnet/minecraft/Util;shutdownExecutors()V"))
    public void shutdownIridium(CallbackInfo ci)
    {
        IridiumRenderer.getInstance().shutdown();
    }
}
