package com.mrcrayfish.vehicle.block;


import net.minecraft.world.level.block.EntityBlock;
import com.mrcrayfish.vehicle.blockentity.WorkstationBlockEntity;
import com.mrcrayfish.vehicle.util.VoxelShapeHelper;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
// // Removed in 1.21.1
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;


import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: MrCrayfish
 */
public class WorkstationBlock extends RotatedObjectBlock implements EntityBlock
{
    private static final VoxelShape SHAPE = Util.make(() -> {
        List<VoxelShape> shapes = new ArrayList<>();
        shapes.add(Block.box(0, 1, 0, 16, 16, 16));
        shapes.add(Block.box(1, 0, 1, 3, 1, 3));
        shapes.add(Block.box(1, 0, 13, 3, 1, 15));
        shapes.add(Block.box(13, 0, 1, 15, 1, 3));
        shapes.add(Block.box(13, 0, 13, 15, 1, 15));
        return VoxelShapeHelper.combineAll(shapes);
    });

    public WorkstationBlock()
    {
        super(BlockBehaviour.Properties.of().strength(1.0F));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter reader, BlockPos pos, CollisionContext context)
    {
        return SHAPE;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player playerEntity, BlockHitResult result)
    {
        if(!world.isClientSide)
        {
            BlockEntity tileEntity = world.getBlockEntity(pos);
            if(tileEntity instanceof MenuProvider && playerEntity instanceof ServerPlayer serverPlayer)
            {
                serverPlayer.openMenu((MenuProvider) tileEntity, buffer -> buffer.writeBlockPos(pos));
                return InteractionResult.CONSUME;
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new WorkstationBlockEntity(pos, state);
    }
}
