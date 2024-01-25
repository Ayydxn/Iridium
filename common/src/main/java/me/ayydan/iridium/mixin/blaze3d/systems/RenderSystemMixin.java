package me.ayydan.iridium.mixin.blaze3d.systems;

import com.mojang.blaze3d.systems.RenderSystem;
import me.ayydan.iridium.render.IridiumRenderSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RenderSystem.class)
public class RenderSystemMixin
{
    @Redirect(method = "initRenderer", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GLX;_init(IZ)V"), remap = false)
    private static void initializeIridiumRenderer(int verbosity, boolean sync)
    {
        IridiumRenderSystem.initRenderer();
    }
}
