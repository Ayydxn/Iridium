package com.ayydxn.iridium.mixin.features.client.gui.hud;

import com.ayydxn.iridium.IridiumClientMod;
import com.google.common.collect.Lists;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;

@Mixin(DebugScreenOverlay.class)
public class DebugHudMixin
{
    @Redirect(method = "getSystemInformation", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Lists;newArrayList([Ljava/lang/Object;)Ljava/util/ArrayList;", remap = false))
    private ArrayList<String> addIridiumDebugInfo(Object[] elements)
    {
        ArrayList<String> iridiumDebugStrings = Lists.newArrayList((String[]) elements);
        iridiumDebugStrings.add("");
        iridiumDebugStrings.add("%sIridium Renderer (%s)".formatted(this.getVersionStringColor(), IridiumClientMod.getInstance().getModVersion()));

        return iridiumDebugStrings;
    }

    @Unique
    private ChatFormatting getVersionStringColor()
    {
        return ChatFormatting.GREEN;

        // (Ayydxn) This will be used in the future, but for now, we'll just always use green.
        /* String iridiumVersion = IridiumClientMod.getInstance().getModVersion();

        if (iridiumVersion.contains("-local"))
        {
            return ChatFormatting.RED;
        }
        else if (iridiumVersion.contains("+snapshot"))
        {
            return ChatFormatting.GOLD;
        } */
    }
}
