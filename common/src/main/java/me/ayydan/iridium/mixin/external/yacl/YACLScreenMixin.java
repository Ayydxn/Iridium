package me.ayydan.iridium.mixin.external.yacl;

import dev.isxander.yacl3.gui.YACLScreen;
import net.minecraft.client.gui.tab.TabManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// TODO: (Ayydan) Remove this when YACL releases an update which includes this fix.
@Mixin(YACLScreen.class)
public class YACLScreenMixin
{
    @Shadow
    private boolean pendingChanges;

    @Shadow
    @Final
    public TabManager tabManager;

    @Inject(method = "finishOrSave", at = @At(value = "INVOKE", target = "Ljava/util/Set;forEach(Ljava/util/function/Consumer;)V", shift = At.Shift.AFTER), remap = false)
    public void tempSaveButtonFix(CallbackInfo ci)
    {
        this.pendingChanges = false;

        if (this.tabManager.getSelectedTab() instanceof YACLScreen.CategoryTab categoryTab)
            categoryTab.updateButtons();
    }
}
