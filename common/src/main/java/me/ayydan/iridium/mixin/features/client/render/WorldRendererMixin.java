package me.ayydan.iridium.mixin.features.client.render;

import me.ayydan.iridium.IridiumClientMod;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LevelRenderer.class)
public class WorldRendererMixin
{
    @ModifyVariable(method = "renderSnowAndRain", at = @At("STORE"), ordinal = 3)
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
