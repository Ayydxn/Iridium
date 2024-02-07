package me.ayydan.iridium.mixin.client.render;

import me.ayydan.iridium.IridiumClientMod;
import me.ayydan.iridium.options.IridiumGameOptions;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderLayers.class)
public class RenderLayersMixin
{
    @Inject(method = "getBlockLayer", at = @At("RETURN"), cancellable = true)
    private static void getLeavesRenderBasedOnIridiumOption(BlockState state, CallbackInfoReturnable<RenderLayer> cir)
    {
        IridiumGameOptions.GraphicsQuality leavesGraphicsQuality = IridiumClientMod.getGameOptions().getLeavesQuality();

        if (state.getBlock() instanceof LeavesBlock)
        {
            switch (leavesGraphicsQuality)
            {
                case Low -> cir.setReturnValue(RenderLayer.getSolid());
                case Medium -> cir.setReturnValue(RenderLayer.getCutoutMipped());
                case High -> cir.setReturnValue(RenderLayer.getCutout());
            }
        }
    }
}
