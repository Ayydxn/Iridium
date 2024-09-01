package me.ayydan.iridium.mixin.core.blaze3d.platform;

import com.mojang.blaze3d.platform.GLX;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GLX.class)
public class GLXMixin
{
    @Redirect(method = "_init", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlDebug;enableDebugCallback(IZ)V"), remap = false)
    private static void redirectEnableDebugCallback(int gLCapabilities, boolean i)
    {
    }
}
