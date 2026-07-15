package com.mrcrayfish.vehicle.blockentity;

import com.mrcrayfish.vehicle.Config;
import com.mrcrayfish.vehicle.init.ModBlockEntities;

/**
 * Author: MrCrayfish
 */
public class GasPumpTankBlockEntity extends BlockEntity
{
    public GasPumpTankBlockEntity()
    {
        super(ModBlockEntities.GAS_PUMP_TANK.get(), Config.SERVER.gasPumpCapacity.get());
    }
}