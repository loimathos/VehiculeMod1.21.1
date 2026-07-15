package com.mrcrayfish.vehicle.blockentity;

import com.mrcrayfish.vehicle.init.ModBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.Constants;

/**
 * Author: MrCrayfish
 */
public class BoostBlockEntity extends BlockEntity
{
    private float speedMultiplier;

    public BoostBlockEntity()
    {
        super(ModBlockEntities.BOOST.get());
    }

    public BoostBlockEntity(float defaultSpeedMultiplier)
    {
        super(ModBlockEntities.BOOST.get());
        this.speedMultiplier = defaultSpeedMultiplier;
    }

    public float getSpeedMultiplier()
    {
        return speedMultiplier;
    }

    @Override
    public void load(BlockState state, CompoundNBT compound)
    {
        super.load(state, compound);
        if(compound.contains("SpeedMultiplier", Constants.NBT.TAG_FLOAT))
        {
            this.speedMultiplier = compound.getFloat("SpeedMultiplier");
        }
    }

    @Override
    public CompoundNBT save(CompoundNBT compound)
    {
        compound.putFloat("SpeedMultiplier", this.speedMultiplier);
        return super.save(compound);
    }
}

