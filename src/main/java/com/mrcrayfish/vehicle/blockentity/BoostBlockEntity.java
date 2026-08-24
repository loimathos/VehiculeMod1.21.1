package com.mrcrayfish.vehicle.blockentity;



import net.minecraft.core.HolderLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.entity.BlockEntity;

import com.mrcrayfish.vehicle.init.ModBlockEntities;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;

/**
 * Author: MrCrayfish
 */
public class BoostBlockEntity extends BlockEntity
{
    private float speedMultiplier;

    public BoostBlockEntity(BlockPos pos, BlockState state)
    {
        super(ModBlockEntities.BOOST.get(), pos, state);
    }

    public BoostBlockEntity(BlockPos pos, BlockState state, float defaultSpeedMultiplier)
    {
        super(ModBlockEntities.BOOST.get(), pos, state);
        this.speedMultiplier = defaultSpeedMultiplier;
    }

    public float getSpeedMultiplier()
    {
        return speedMultiplier;
    }

    @Override
    protected void loadAdditional(CompoundTag compound, HolderLookup.Provider registries)
    {
        super.loadAdditional(compound, registries);
        if(compound.contains("SpeedMultiplier", Tag.TAG_FLOAT))
        {
            this.speedMultiplier = compound.getFloat("SpeedMultiplier");
        }
    }

    @Override
    protected void saveAdditional(CompoundTag compound, HolderLookup.Provider registries)
    {
        super.saveAdditional(compound, registries);
        compound.putFloat("SpeedMultiplier", this.speedMultiplier);
    }
}

