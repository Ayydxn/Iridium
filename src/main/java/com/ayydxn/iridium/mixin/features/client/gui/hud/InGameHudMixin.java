package com.ayydxn.iridium.mixin.features.client.gui.hud;

import com.ayydxn.iridium.IridiumClientMod;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Gui.class)
public class InGameHudMixin
{
    @Redirect(method = "renderCameraOverlays", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;useFancyGraphics()Z"))
    public boolean isIridiumVignetteEnabled()
    {
        return IridiumClientMod.getInstance().getGameOptions().qualityOptions.enableVignette;
    }
}
