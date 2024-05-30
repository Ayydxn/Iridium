package me.ayydan.iridium.mixin.features.client.hud;

import com.google.common.collect.Lists;
import dev.architectury.platform.Platform;
import me.ayydan.iridium.utils.VersioningUtils;
import net.minecraft.client.gui.hud.debug.DebugHud;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;

@Mixin(DebugHud.class)
public class DebugHudMixin
{
    @Redirect(method = "getRightText", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Lists;newArrayList([Ljava/lang/Object;)Ljava/util/ArrayList;", remap = false))
    private ArrayList<String> addIridiumDebugInfo(Object[] elements)
    {
        ArrayList<String> iridiumDebugStrings = Lists.newArrayList((String[]) elements);
        iridiumDebugStrings.add("");
        iridiumDebugStrings.add("%sIridium Renderer (%s)".formatted(this.getVersionStringColor(), VersioningUtils.getIridiumVersion()));

        return iridiumDebugStrings;
    }

    @Unique
    private Formatting getVersionStringColor()
    {
        String iridiumVersion = VersioningUtils.getIridiumVersion();

        if (iridiumVersion.contains("-local"))
        {
            return Formatting.RED;
        }
        else if (iridiumVersion.contains("+snapshot"))
        {
            return Formatting.GOLD;
        }

        return Formatting.GREEN;
    }
}
