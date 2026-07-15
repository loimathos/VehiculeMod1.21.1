package com.mrcrayfish.vehicle.blockentity;

import com.mrcrayfish.vehicle.init.ModBlocks;
import com.mrcrayfish.vehicle.init.ModBlockEntities;

/**
 * Author: MrCrayfish
 */
public class IndustrialFuelDrumBlockEntity extends FuelDrumBlockEntity
{
    public IndustrialFuelDrumBlockEntity()
    {
        super(ModBlockEntities.INDUSTRIAL_FUEL_DRUM.get(), ModBlocks.INDUSTRIAL_FUEL_DRUM.get().getCapacity());
    }
}
