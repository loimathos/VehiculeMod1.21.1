package com.mrcrayfish.vehicle.blockentity;

import com.mrcrayfish.vehicle.util.BlockEntityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Predicate;

public class BlockFluidHandlerSynced extends BlockEntity
{
    protected FluidTank tank;

    public BlockFluidHandlerSynced(@Nonnull BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state)
    {
        super(tileEntityTypeIn, pos, state);
        this.tank = new FluidTank(0)
        {
            @Override
            protected void onContentsChanged()
            {
                BlockFluidHandlerSynced.this.syncFluidToClient();
            }
        };
    }

    public BlockFluidHandlerSynced(@Nonnull BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state, int capacity)
    {
        super(tileEntityTypeIn, pos, state);
        this.tank = new FluidTank(capacity)
        {
            @Override
            protected void onContentsChanged()
            {
                BlockFluidHandlerSynced.this.syncFluidToClient();
            }
        };
    }

    public BlockFluidHandlerSynced(@Nonnull BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state, int capacity, Predicate<FluidStack> validator)
    {
        super(tileEntityTypeIn, pos, state);
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
            CompoundTag compound = this.saveWithoutMetadata(this.level.registryAccess());
            BlockEntityUtil.sendUpdatePacket(this, compound);
        }
    }

    public void syncFluidToPlayer(ServerPlayer player)
    {
        if(this.level != null && !this.level.isClientSide())
        {
            CompoundTag compound = this.saveWithoutMetadata(this.level.registryAccess());
            BlockEntityUtil.sendUpdatePacket(this, compound);
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries)
    {
        return this.saveWithoutMetadata(registries);
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket()
    {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider registries)
    {
        if(pkt.getTag() != null)
        {
            this.loadWithComponents(pkt.getTag(), registries);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries)
    {
        super.saveAdditional(tag, registries);
        this.tank.writeToNBT(tag);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries)
    {
        super.loadAdditional(tag, registries);
        this.tank.readFromNBT(tag);
    }

    public FluidTank getFluidTank()
    {
        return this.tank;
    }
}