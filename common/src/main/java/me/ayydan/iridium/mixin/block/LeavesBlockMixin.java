package me.ayydan.iridium.mixin.block;

import me.ayydan.iridium.IridiumClientMod;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LeavesBlock.class)
public class LeavesBlockMixin extends Block
{
    public LeavesBlockMixin()
    {
        super(Settings.method_9630(Blocks.AIR));
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isSideInvisible(BlockState state, BlockState stateFrom, Direction direction)
    {
        if (IridiumClientMod.getGameOptions().getLeavesQuality().isMediumOrBetter())
        {
            return super.isSideInvisible(state, stateFrom, direction);
        }
        else
        {
            return stateFrom.getBlock() instanceof LeavesBlock || super.isSideInvisible(state, stateFrom, direction);
        }
    }
}
