package com.mrcrayfish.vehicle.blockentity;

import net.minecraft.nbt.CompoundTag;

/**
 * Author: MrCrayfish
 */
public interface IFluidTankWriter
{
    void writeTanks(CompoundTag compound);

    boolean areTanksEmpty();
}