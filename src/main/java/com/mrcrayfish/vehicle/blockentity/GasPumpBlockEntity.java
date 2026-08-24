package com.mrcrayfish.vehicle.blockentity;


import net.minecraft.core.BlockPos;
import net.minecraft.nbt.Tag;

import com.mrcrayfish.obfuscate.common.data.SyncedPlayerData;
import com.mrcrayfish.vehicle.Config;
import com.mrcrayfish.vehicle.client.util.HermiteInterpolator;
import com.mrcrayfish.vehicle.init.ModDataKeys;
import com.mrcrayfish.vehicle.init.ModBlockEntities;
import com.mrcrayfish.vehicle.util.BlockEntityUtil;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import javax.annotation.Nullable;
import java.util.Optional;
import net.minecraft.core.HolderLookup;

/**
 * Author: MrCrayfish
 */
public class GasPumpBlockEntity extends BlockEntitySynced
{
    private int fuelingEntityId;
    private Player fuelingEntity;

    private HermiteInterpolator cachedSpline;
    private boolean recentlyUsed;

    public GasPumpBlockEntity(BlockPos pos, BlockState state)
    {
        super(ModBlockEntities.GAS_PUMP.get(), pos, state);
    }

    public HermiteInterpolator getCachedSpline()
    {
        return cachedSpline;
    }

    public void setCachedSpline(HermiteInterpolator cachedSpline)
    {
        this.cachedSpline = cachedSpline;
    }

    public boolean isRecentlyUsed()
    {
        return recentlyUsed;
    }

    public void setRecentlyUsed(boolean recentlyUsed)
    {
        this.recentlyUsed = recentlyUsed;
    }

    @Nullable
    public FluidTank getTank()
    {
        BlockEntity tileEntity = this.level.getBlockEntity(this.worldPosition.below());
        if(tileEntity instanceof GasPumpTankBlockEntity)
        {
            return ((GasPumpTankBlockEntity) tileEntity).getFluidTank();
        }
        return null;
    }

    public Player getFuelingEntity()
    {
        return this.fuelingEntity;
    }

    public void setFuelingEntity(@Nullable Player entity)
    {
        if(!this.level.isClientSide)
        {
            if(this.fuelingEntity != null)
            {
                SyncedPlayerData.instance().set(this.fuelingEntity, ModDataKeys.GAS_PUMP, Optional.empty());
            }
            this.fuelingEntity = null;
            this.fuelingEntityId = -1;
            if(entity != null)
            {
                this.fuelingEntityId = entity.getId();
                SyncedPlayerData.instance().set(entity, ModDataKeys.GAS_PUMP, Optional.of(this.getBlockPos()));
            }
            this.syncToClient();
        }
    }

    public void tick()
    {
        if(this.fuelingEntityId != -1)
        {
            if(this.fuelingEntity == null)
            {
                Entity entity = this.level.getEntity(this.fuelingEntityId);
                if(entity instanceof Player)
                {
                    this.fuelingEntity = (Player) entity;
                }
                else if(!this.level.isClientSide)
                {
                    this.fuelingEntityId = -1;
                    this.syncFuelingEntity();
                }
            }
        }
        else if(this.level.isClientSide && this.fuelingEntity != null)
        {
            this.fuelingEntity = null;
        }

        if(!this.level.isClientSide && this.fuelingEntity != null)
        {
            if(Math.sqrt(this.fuelingEntity.distanceToSqr(this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5, this.worldPosition.getZ() + 0.5)) > Config.SERVER.maxHoseDistance.get() || !this.fuelingEntity.isAlive())
            {
                if(this.fuelingEntity.isAlive())
                {
                    this.level.playSound(null, this.fuelingEntity.blockPosition(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
                }
                SyncedPlayerData.instance().set(this.fuelingEntity, ModDataKeys.GAS_PUMP, Optional.empty());
                this.fuelingEntityId = -1;
                this.fuelingEntity = null;
                this.syncFuelingEntity();
            }
        }
    }

    @Override
    protected void loadAdditional(CompoundTag compound, HolderLookup.Provider registries)
    {
        super.loadAdditional(compound, registries);
        if(compound.contains("FuelingEntity", Tag.TAG_INT))
        {
            this.fuelingEntityId = compound.getInt("FuelingEntity");
        }
    }

    @Override
    protected void saveAdditional(CompoundTag compound, HolderLookup.Provider registries)
    {
        compound.putInt("FuelingEntity", this.fuelingEntityId);
        super.saveAdditional(compound, registries);
    }

    private void syncFuelingEntity()
    {
        if (this.level != null)
        {
            CompoundTag compound = this.saveWithoutMetadata(this.level.registryAccess());
            compound.putInt("FuelingEntity", this.fuelingEntityId);
            BlockEntityUtil.sendUpdatePacket(this, compound);
        }
    }

    @Override
    public AABB getRenderBoundingBox()
    {
        return INFINITE_EXTENT_AABB;
    }

    @OnlyIn(Dist.CLIENT)
    public double getViewDistance()
    {
        return 65536.0D;
    }
}
