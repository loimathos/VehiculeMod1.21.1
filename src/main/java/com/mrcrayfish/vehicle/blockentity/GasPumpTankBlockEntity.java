package com.mrcrayfish.vehicle.blockentity;




import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import com.mrcrayfish.vehicle.Config;
import com.mrcrayfish.vehicle.init.ModBlockEntities;

/**
 * Author: MrCrayfish
 */
public class GasPumpTankBlockEntity extends BlockFluidHandlerSynced
{
    public GasPumpTankBlockEntity(BlockPos pos, BlockState state)
    {
        super(ModBlockEntities.GAS_PUMP_TANK.get(), pos, state, Config.SERVER.gasPumpCapacity.get());
    }
}