package me.ayydan.iridium.mixin.client.gui.hud;

import me.ayydan.iridium.IridiumClientMod;
import net.minecraft.client.gui.hud.in_game.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(InGameHud.class)
public class InGameHudMixin
{
    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;isFancyGraphicsOrBetter()Z"))
    public boolean isIridiumVignetteEnabled()
    {
        return IridiumClientMod.getInstance().getGameOptions().enableVignette;
    }
}
