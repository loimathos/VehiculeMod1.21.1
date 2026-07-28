package com.mrcrayfish.vehicle.blockentity;




import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import com.mrcrayfish.vehicle.init.ModBlocks;
import com.mrcrayfish.vehicle.init.ModBlockEntities;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.fluids.capability.templates.FluidTank;

/**
 * Author: MrCrayfish
 */
public class FuelDrumBlockEntity extends BlockFluidHandlerSynced
{
    public FuelDrumBlockEntity(BlockPos pos, BlockState state)
    {
        super(ModBlockEntities.FUEL_DRUM.get(), pos, state, ModBlocks.FUEL_DRUM.get().getCapacity());
    }

    public FuelDrumBlockEntity(BlockEntityType<?> tileEntityType, BlockPos pos, BlockState state, int capacity)
    {
        super(tileEntityType, pos, state, capacity);
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
