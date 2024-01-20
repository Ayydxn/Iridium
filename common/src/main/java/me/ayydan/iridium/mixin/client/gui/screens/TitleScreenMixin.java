package me.ayydan.iridium.mixin.client.gui.screens;

import me.ayydan.iridium.gui.screens.CorruptedIridiumConfigScreen;
import me.ayydan.iridium.options.IridiumGameOptions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
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
        if (IridiumGameOptions.isConfigCorrupted())
            MinecraftClient.getInstance().setScreen(new CorruptedIridiumConfigScreen());
    }
}
