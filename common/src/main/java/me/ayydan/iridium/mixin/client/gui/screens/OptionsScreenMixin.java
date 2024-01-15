package me.ayydan.iridium.mixin.client.gui.screens;

import me.ayydan.iridium.options.IridiumOptionsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(OptionsScreen.class)
public class OptionsScreenMixin
{
    @Shadow @Final
    private Screen parent;

    @Inject(method = "method_19828", at = @At("RETURN"), cancellable = true)
    public void openIridiumVideoOptionsScreen(CallbackInfoReturnable<Screen> cir)
    {
        cir.setReturnValue(new IridiumOptionsScreen(this.parent).getScreen());
    }
}
