package me.ayydan.iridium.mixin.client.gui.hud;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlDebugInfo;
import me.ayydan.iridium.platform.IridiumPlatformUtils;
import net.minecraft.client.MinecraftClient;
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
        int framebufferWidth = MinecraftClient.getInstance().getWindow().getFramebufferWidth();
        int framebufferHeight = MinecraftClient.getInstance().getWindow().getFramebufferHeight();

        iridiumDebugStrings.set(7, String.format("Current Resolution: %d x %d", framebufferWidth, framebufferHeight));
        iridiumDebugStrings.set(8, String.format("Vendor: %s", GlDebugInfo.getVendor()));
        iridiumDebugStrings.set(9, String.format("Device: %s", GlDebugInfo.getRenderer()));
        iridiumDebugStrings.add(String.format("Driver Version: %s", GlDebugInfo.getVersion()));

        iridiumDebugStrings.add("");

        iridiumDebugStrings.add("%sIridium Renderer (%s)".formatted(this.getVersionStringColor(), IridiumPlatformUtils.getCurrentVersion()));

        return iridiumDebugStrings;
    }

    @Unique
    private Formatting getVersionStringColor()
    {
        String iridiumVersion = IridiumPlatformUtils.getCurrentVersion();

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
