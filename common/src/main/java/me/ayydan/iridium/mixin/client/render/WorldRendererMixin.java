package me.ayydan.iridium.mixin.client.render;

import me.ayydan.iridium.IridiumClientMod;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin
{
    @ModifyVariable(method = "renderWeather", at = @At("STORE"), ordinal = 3)
    public int getAmountOfRainDroplets(int original)
    {
        return switch (IridiumClientMod.getGameOptions().getWeatherQuality())
        {
            case Low -> 5;
            case Medium -> 10;
            case High -> 15;
        };
    }
}
