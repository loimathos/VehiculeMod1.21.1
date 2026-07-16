package com.mrcrayfish.vehicle.block;

import net.minecraft.world.level.block.AbstractBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BlockState;
//import net.minecraft.world.level.block.material.Material; // Removed in 1.21.1
//import net.minecraft.world.level.block.material.Material; // Removed in 1.21.1
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;

/**
 * Author: MrCrayfish
 */
public class TrafficConeBlock extends ObjectBlock
{
    private static final VoxelShape COLLISION_SHAPE = Block.box(2, 0, 2, 14, 18, 14);
    private static final VoxelShape SELECTION_SHAPE = Block.box(1, 0, 1, 15, 16, 15);

    public TrafficConeBlock()
    {
        super(AbstractBlock.Properties.of(Material.CLAY, MaterialColor.TERRACOTTA_ORANGE).strength(0.5F));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockAndTintGetter worldIn, BlockPos pos, CollisionContext context)
    {
        return SELECTION_SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockAndTintGetter worldIn, BlockPos pos, CollisionContext context)
    {
        return COLLISION_SHAPE;
    }
}
