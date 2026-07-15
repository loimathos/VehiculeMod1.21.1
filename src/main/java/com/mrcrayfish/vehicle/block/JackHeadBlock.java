package com.mrcrayfish.vehicle.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.CollisionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.level.BlockGetter;

/**
 * Author: MrCrayfish
 */
public class JackHeadBlock extends Block
{
    public JackHeadBlock()
    {
        super(AbstractBlock.Properties.of(Material.WOOD));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockAndTintGetter reader, BlockPos pos, CollisionContext context)
    {
        return VoxelShapes.empty();
    }
}
