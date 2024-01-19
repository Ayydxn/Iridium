package me.ayydan.iridium.mixin.client.gui.screens;

import me.ayydan.iridium.gui.screens.IridiumOptionsScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OptionsScreen.class)
public class OptionsScreenMixin
{
    @Shadow @Final private Screen parent;

    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    public void openIridiumOptionsScreen(CallbackInfo ci)
    {
        MinecraftClient.getInstance().setScreen(new IridiumOptionsScreen(this.parent).getHandle());

        ci.cancel();
    }
}
