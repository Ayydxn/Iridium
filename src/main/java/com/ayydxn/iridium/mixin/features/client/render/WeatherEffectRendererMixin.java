package com.ayydxn.iridium.mixin.features.client.render;

import com.ayydxn.iridium.IridiumClientMod;
import net.minecraft.client.renderer.WeatherEffectRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(WeatherEffectRenderer.class)
public class WeatherEffectRendererMixin
{
    @ModifyVariable(method = "render(Lnet/minecraft/world/level/Level;Lnet/minecraft/client/renderer/MultiBufferSource;IFLnet/minecraft/world/phys/Vec3;)V", at = @At("STORE"), ordinal = 1)
    public int getAmountOfRainDroplets(int original)
    {
        return switch (IridiumClientMod.getInstance().getGameOptions().qualityOptions.weatherQuality)
        {
            case Low -> 5;
            case Medium -> 10;
            case High -> 15;
        };
    }
}
