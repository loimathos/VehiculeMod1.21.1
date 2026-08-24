package com.mrcrayfish.vehicle.block;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
// // Removed in 1.21.1
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.BlockGetter;

/**
 * Author: MrCrayfish
 */
public class JackHeadBlock extends Block
{
    public JackHeadBlock()
    {
        super(BlockBehaviour.Properties.of());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter reader, BlockPos pos, CollisionContext context)
    {
        return Shapes.empty();
    }
}
