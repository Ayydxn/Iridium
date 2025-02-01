package com.ayydxn.iridium.mixin.features.client.render;

import com.ayydxn.iridium.IridiumClientMod;
import com.ayydxn.iridium.options.IridiumGameOptions;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemBlockRenderTypes.class)
public class RenderLayersMixin
{
    @Inject(method = { "getChunkRenderType", "getMovingBlockRenderType" }, at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/ItemBlockRenderTypes;renderCutout:Z"), require = 2, cancellable = true)
    private static void getLeavesRenderTypeBasedOnIridiumOption(BlockState state, CallbackInfoReturnable<RenderType> cir)
    {
        IridiumGameOptions.GraphicsQuality leavesGraphicsQuality = IridiumClientMod.getInstance().getGameOptions().qualityOptions.leavesQuality;

        if (state.getBlock() instanceof LeavesBlock)
        {
            switch (leavesGraphicsQuality)
            {
                case Low -> cir.setReturnValue(RenderType.solid());
                case Medium -> cir.setReturnValue(RenderType.cutout());
                case High -> cir.setReturnValue(RenderType.cutoutMipped());
            }
        }
    }
}
