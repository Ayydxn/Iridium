package com.ayydxn.iridium.mixin.features.client.gui.screens;

import com.ayydxn.iridium.gui.screens.IridiumOptionsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OptionsScreen.class)
public class OptionsScreenMixin
{
    @Shadow @Final private Screen lastScreen;

    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    public void openIridiumOptionsScreen(CallbackInfo ci)
    {
        Minecraft.getInstance().setScreen(new IridiumOptionsScreen(this.lastScreen).getHandle());

        ci.cancel();
    }
}
