package me.ayydan.iridium.mixin.features.client.hud;

import com.google.common.collect.Lists;
import me.ayydan.iridium.utils.VersioningUtils;
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
        iridiumDebugStrings.add("%sIridium Renderer (%s)".formatted(this.getVersionStringColor(), VersioningUtils.getIridiumVersion()));

        return iridiumDebugStrings;
    }

    @Unique
    private ChatFormatting getVersionStringColor()
    {
        String iridiumVersion = VersioningUtils.getIridiumVersion();

        if (iridiumVersion.contains("-local"))
        {
            return ChatFormatting.RED;
        }
        else if (iridiumVersion.contains("+snapshot"))
        {
            return ChatFormatting.GOLD;
        }

        return ChatFormatting.GREEN;
    }
}
