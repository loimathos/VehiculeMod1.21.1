package com.mrcrayfish.vehicle.block;


import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.Containers;

import com.mrcrayfish.vehicle.blockentity.FluidExtractorBlockEntity;
import com.mrcrayfish.vehicle.util.BlockEntityUtil;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
// // Removed in 1.21.1
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraft.world.ItemInteractionResult;

import javax.annotation.Nullable;

/**
 * Author: MrCrayfish
 */
public class FluidExtractorBlock extends RotatedObjectBlock implements EntityBlock
{
    public static final BooleanProperty ENABLED = BlockStateProperties.ENABLED;

    public FluidExtractorBlock()
    {
        super(BlockBehaviour.Properties.of().strength(1.0F).noOcclusion());
        this.registerDefaultState(this.getStateDefinition().any().setValue(DIRECTION, Direction.NORTH).setValue(ENABLED, false));
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player playerEntity, InteractionHand hand, BlockHitResult result)
    {
        if(stack.getItem() == Items.BUCKET)
        {
            if(!world.isClientSide)
            {
                FluidUtil.interactWithFluidHandler(playerEntity, hand, world, pos, result.getDirection());
            }
            return ItemInteractionResult.sidedSuccess(world.isClientSide);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player playerEntity, BlockHitResult result)
    {
        if(!world.isClientSide)
        {
            BlockEntity tileEntity = world.getBlockEntity(pos);
            if(tileEntity instanceof MenuProvider)
            {
                BlockEntityUtil.sendUpdatePacket(tileEntity, (ServerPlayer) playerEntity);
                playerEntity.openMenu((MenuProvider) tileEntity);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving)
    {
        if(state.getBlock() != newState.getBlock())
        {
            BlockEntity tileentity = worldIn.getBlockEntity(pos);
            if(tileentity instanceof Container)
            {
                Containers.dropContents(worldIn, pos, (Container) tileentity);
                worldIn.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(state, worldIn, pos, newState, isMoving);
        }
    }
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new FluidExtractorBlockEntity(pos, state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(ENABLED);
    }
}