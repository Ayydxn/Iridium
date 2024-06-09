package me.ayydan.iridium.mixin.features.block;

import me.ayydan.iridium.IridiumClientMod;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LeavesBlock.class)
public class LeavesBlockMixin extends Block
{
    public LeavesBlockMixin()
    {
        super(Properties.ofFullCopy(Blocks.AIR));
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean skipRendering(BlockState state, BlockState adjacentState, Direction direction)
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
