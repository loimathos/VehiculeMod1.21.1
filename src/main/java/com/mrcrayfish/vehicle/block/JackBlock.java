package com.mrcrayfish.vehicle.block;

import com.mrcrayfish.vehicle.blockentity.JackBlockEntity;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.CollisionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

/**
 * Author: MrCrayfish
 */
public class JackBlock extends RotatedObjectBlock
{
    public static final BooleanProperty ENABLED = BlockStateProperties.ENABLED;

    private static final VoxelShape SHAPE = Block.box(1, 0, 1, 15, 10, 15);

    public JackBlock()
    {
        super(AbstractBlock.Properties.of(Material.PISTON));
        this.registerDefaultState(this.getStateDefinition().any().setValue(DIRECTION, Direction.NORTH).setValue(ENABLED, false));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockAndTintGetter worldIn, BlockPos pos, CollisionContext context)
    {
        BlockEntity tileEntity = worldIn.getBlockEntity(pos);
        if(tileEntity instanceof JackBlockEntity)
        {
            JackBlockEntity jack = (JackBlockEntity) tileEntity;
            return VoxelShapes.create(SHAPE.bounds().expandTowards(0, 0.5 * jack.getProgress(), 0));
        }
        return SHAPE;
    }

    @Override
    public boolean hasTileEntity(BlockState state)
    {
        return true;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new JackBlockEntity();
    }

    @Override
    public BlockRenderType getRenderShape(BlockState state)
    {
        return BlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(ENABLED);
    }

    // Prevents the block entity from being removed if the replacement block is the same
    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState replaceState, boolean what)
    {
        if(!state.is(replaceState.getBlock()))
        {
            super.onRemove(state, world, pos, replaceState, what);
        }
    }
}
