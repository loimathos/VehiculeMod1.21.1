package com.mrcrayfish.vehicle.blockentity;


import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.nbt.Tag;

import com.mrcrayfish.vehicle.init.ModBlockEntities;
import com.mrcrayfish.vehicle.util.BlockEntityUtil;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.HolderLookup;

/**
 * Author: MrCrayfish
 */
public class PipeBlockEntity extends BlockEntitySynced
{
    protected Set<BlockPos> pumps = new HashSet<>();
    protected boolean[] disabledConnections = new boolean[Direction.values().length];

    public PipeBlockEntity(BlockPos pos, BlockState state)
    {
        super(ModBlockEntities.FLUID_PIPE.get(), pos, state);
    }

    public PipeBlockEntity(BlockEntityType<?> tileEntityType, BlockPos pos, BlockState state)
    {
        super(tileEntityType, pos, state);
    }

    public void addPump(BlockPos pos)
    {
        this.pumps.add(pos);
    }

    public void removePump(BlockPos pos)
    {
        this.pumps.remove(pos);
    }

    public Set<BlockPos> getPumps()
    {
        return this.pumps;
    }

    public boolean[] getDisabledConnections()
    {
        return this.disabledConnections;
    }

    public void setConnectionState(Direction direction, boolean state)
    {
        this.disabledConnections[direction.get3DDataValue()] = state;
        this.syncDisabledConnections();
    }

    public boolean isConnectionDisabled(Direction direction)
    {
        return this.disabledConnections[direction.get3DDataValue()];
    }

    public void syncDisabledConnections()
    {
        if(this.level != null && !this.level.isClientSide())
        {
            CompoundTag compound = new CompoundTag();
            this.writeConnections(compound);
            BlockEntityUtil.sendUpdatePacket(this, this.saveWithoutMetadata(this.level.registryAccess()));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag compound, HolderLookup.Provider registries)
    {
        super.loadAdditional(compound, registries);
        if(compound.contains("DisabledConnections", Tag.TAG_BYTE_ARRAY))
        {
            byte[] connections = compound.getByteArray("DisabledConnections");
            for(int i = 0; i < connections.length; i++)
            {
                this.disabledConnections[i] = connections[i] == (byte) 1;
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag compound, HolderLookup.Provider registries)
    {
        super.saveAdditional(compound, registries);
        this.writeConnections(compound);
    }

    private void writeConnections(CompoundTag compound)
    {
        byte[] connections = new byte[this.disabledConnections.length];
        for(int i = 0; i < connections.length; i++)
        {
            connections[i] = (byte) (this.disabledConnections[i] ? 1 : 0);
        }
        compound.putByteArray("DisabledConnections", connections);
    }
}
