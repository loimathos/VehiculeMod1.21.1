package com.mrcrayfish.vehicle.blockentity;




import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import com.mrcrayfish.vehicle.init.ModBlocks;
import com.mrcrayfish.vehicle.init.ModBlockEntities;

/**
 * Author: MrCrayfish
 */
public class IndustrialFuelDrumBlockEntity extends FuelDrumBlockEntity
{
    public IndustrialFuelDrumBlockEntity(BlockPos pos, BlockState state)
    {
        super(ModBlockEntities.INDUSTRIAL_FUEL_DRUM.get(), pos, state, ModBlocks.INDUSTRIAL_FUEL_DRUM.get().getCapacity());
    }
}
