package me.ayydan.iridium.mixin.features.client.gui.screens;

import me.ayydan.iridium.gui.screens.CorruptedIridiumConfigScreen;
import me.ayydan.iridium.options.IridiumGameOptions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin
{
    @Inject(method = "init", at = @At("TAIL"))
    public void displayCorruptedIridiumConfigScreen(CallbackInfo ci)
    {
        if (IridiumGameOptions.isGameOptionsConfigCorrupted())
            Minecraft.getInstance().setScreen(new CorruptedIridiumConfigScreen());
    }
}
