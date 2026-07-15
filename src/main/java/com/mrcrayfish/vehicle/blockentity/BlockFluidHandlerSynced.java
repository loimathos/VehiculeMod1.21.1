package com.mrcrayfish.vehicle.blockentity;

import com.mrcrayfish.vehicle.util.BlockEntityUtil;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateBlockEntityPacket;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.TileFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Predicate;

public class BlockFluidHandlerSynced extends TileFluidHandler
{
    public BlockFluidHandlerSynced(@Nonnull BlockEntityType<?> tileEntityTypeIn, int capacity)
    {
        super(tileEntityTypeIn);
        this.tank = new FluidTank(capacity)
        {
            @Override
            protected void onContentsChanged()
            {
                BlockFluidHandlerSynced.this.syncFluidToClient();
            }
        };
    }

    public BlockFluidHandlerSynced(@Nonnull BlockEntityType<?> tileEntityTypeIn, int capacity, Predicate<FluidStack> validator)
    {
        super(tileEntityTypeIn);
        this.tank = new FluidTank(capacity, validator)
        {
            @Override
            protected void onContentsChanged()
            {
                BlockFluidHandlerSynced.this.syncFluidToClient();
            }
        };
    }

    public void syncFluidToClient()
    {
        if(this.level != null && !this.level.isClientSide())
        {
            CompoundNBT compound = new CompoundNBT();
            super.save(compound);
            BlockEntityUtil.sendUpdatePacket(this, compound);
        }
    }

    public void syncFluidToPlayer(ServerPlayerEntity player)
    {
        if(this.level != null && !this.level.isClientSide())
        {
            CompoundNBT compound = new CompoundNBT();
            super.save(compound);
            BlockEntityUtil.sendUpdatePacket(this, compound);
        }
    }

    @Override
    public CompoundNBT getUpdateTag()
    {
        return this.save(new CompoundNBT());
    }

    @Nullable
    @Override
    public SUpdateBlockEntityPacket getUpdatePacket()
    {
        return new SUpdateBlockEntityPacket(this.worldPosition, 0, this.getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateBlockEntityPacket pkt)
    {
        this.load(null, pkt.getTag());
    }

    public FluidTank getFluidTank()
    {
        return this.tank;
    }
}