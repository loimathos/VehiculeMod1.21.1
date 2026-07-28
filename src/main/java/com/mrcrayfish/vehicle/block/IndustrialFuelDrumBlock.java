package com.mrcrayfish.vehicle.block;


import net.minecraft.world.level.block.EntityBlock;
import com.mrcrayfish.vehicle.Config;
import com.mrcrayfish.vehicle.blockentity.IndustrialFuelDrumBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.BlockGetter;

import net.minecraft.core.BlockPos;

import javax.annotation.Nullable;

/**
 * Author: MrCrayfish
 */
public class IndustrialFuelDrumBlock extends FuelDrumBlock implements EntityBlock
{
    @Override
    public int getCapacity()
    {
        return Config.SERVER.industrialFuelDrumCapacity.get();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new IndustrialFuelDrumBlockEntity(pos, state);
    }
}
