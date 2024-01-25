package me.ayydan.iridium.mixin.client;

import me.ayydan.iridium.IridiumClientMod;
import me.ayydan.iridium.render.IridiumRenderer;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin
{
    @Inject(method = "close", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Util;shutdownExecutors()V"))
    public void shutdownIridium(CallbackInfo ci)
    {
        IridiumRenderer.getInstance().shutdown();
        IridiumClientMod.getInstance().shutdown();
    }
}
