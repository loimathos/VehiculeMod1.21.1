package com.mrcrayfish.vehicle.blockentity;

import com.mrcrayfish.vehicle.init.ModBlocks;
import com.mrcrayfish.vehicle.init.ModBlockEntities;
import net.minecraft.world.level.block.entity.BlockEntityType;

/**
 * Author: MrCrayfish
 */
public class FuelDrumBlockEntity extends BlockEntity
{
    public FuelDrumBlockEntity()
    {
        super(ModBlockEntities.FUEL_DRUM.get(), ModBlocks.FUEL_DRUM.get().getCapacity());
    }

    public FuelDrumBlockEntity(BlockEntityType<?> tileEntityType, int capacity)
    {
        super(tileEntityType, capacity);
    }

    public boolean hasFluid()
    {
        return !this.tank.getFluid().isEmpty();
    }

    public int getAmount()
    {
        return this.tank.getFluidAmount();
    }

    public int getCapacity()
    {
        return this.tank.getCapacity();
    }
}
