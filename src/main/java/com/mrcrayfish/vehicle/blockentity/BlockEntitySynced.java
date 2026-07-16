package com.mrcrayfish.vehicle.blockentity;

import com.mrcrayfish.vehicle.util.BlockEntityUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.protocol.game.SUpdateBlockEntityPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import javax.annotation.Nullable;

public class BlockEntitySynced extends BlockEntity
{
    public BlockEntitySynced(BlockEntityType<?> tileEntityTypeIn)
    {
        super(tileEntityTypeIn);
    }

    public void syncToClient()
    {
        this.setChanged();
        BlockEntityUtil.sendUpdatePacket(this);
    }

    @Override
    public CompoundTag getUpdateTag()
    {
        return this.save(new CompoundTag());
    }

    @Nullable
    @Override
    public SUpdateBlockEntityPacket getUpdatePacket()
    {
        return new SUpdateBlockEntityPacket(this.getBlockPos(), 0, this.getUpdateTag());
    }

    @Override
    public void onDataPacket(final NetworkManager net, final SUpdateBlockEntityPacket pkt)
    {
        this.load(null, pkt.getTag());
    }
}