package com.ayydxn.iridium.mixin.features.block;

import com.ayydxn.iridium.IridiumClientMod;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LeavesBlock.class)
public class LeavesBlockMixin extends Block
{
    public LeavesBlockMixin()
    {
        super(Properties.ofFullCopy(Blocks.AIR));
    }

    @Override
    public boolean skipRendering(@NotNull BlockState state, @NotNull BlockState adjacentState, @NotNull Direction direction)
    {
        if (IridiumClientMod.getInstance().getGameOptions().qualityOptions.leavesQuality.isMediumOrBetter())
        {
            return super.skipRendering(state, adjacentState, direction);
        }
        else
        {
            return adjacentState.getBlock() instanceof LeavesBlock || super.skipRendering(state, adjacentState, direction);
        }
    }
}
