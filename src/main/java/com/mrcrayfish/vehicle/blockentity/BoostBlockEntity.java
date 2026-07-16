package com.mrcrayfish.vehicle.blockentity;

import com.mrcrayfish.vehicle.init.ModBlockEntities;
import net.minecraft.world.level.block.BlockState;
import net.minecraft.nbt.CompoundTag;
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
    public void load(BlockState state, CompoundTag compound)
    {
        super.load(state, compound);
        if(compound.contains("SpeedMultiplier", Constants.NBT.TAG_FLOAT))
        {
            this.speedMultiplier = compound.getFloat("SpeedMultiplier");
        }
    }

    @Override
    public CompoundTag save(CompoundTag compound)
    {
        compound.putFloat("SpeedMultiplier", this.speedMultiplier);
        return super.save(compound);
    }
}

