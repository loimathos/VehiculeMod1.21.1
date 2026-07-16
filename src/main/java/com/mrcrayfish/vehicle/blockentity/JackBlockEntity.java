package com.mrcrayfish.vehicle.blockentity;

import com.mrcrayfish.vehicle.block.JackBlock;
import com.mrcrayfish.vehicle.entity.EntityJack;
import com.mrcrayfish.vehicle.entity.VehicleEntity;
import com.mrcrayfish.vehicle.init.ModEntities;
import com.mrcrayfish.vehicle.init.ModSounds;
import com.mrcrayfish.vehicle.init.ModBlockEntities;
import net.minecraft.world.level.block.BlockState;
//import net.minecraft.world.level.block.material.Material; // Removed in 1.21.1
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;

import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Author: MrCrayfish
 */
public class JackBlockEntity extends BlockEntitySynced implements BlockEntityTicker
{
    public static final int MAX_LIFT_PROGRESS = 20;

    private EntityJack jack = null;

    private boolean activated = false;
    public int prevLiftProgress;
    public int liftProgress;

    public JackBlockEntity()
    {
        super(ModBlockEntities.JACK.get());
    }

    public void setVehicle(VehicleEntity vehicle)
    {
        this.jack = new EntityJack(ModEntities.JACK.get(), this.level, this.worldPosition, 11 * 0.0625, vehicle.yRot);
        vehicle.startRiding(this.jack, true);
        this.jack.rideTick();
        this.level.addFreshEntity(this.jack);
    }

    @Nullable
    public EntityJack getJack()
    {
        return this.jack;
    }

    @Override
    public void tick()
    {
        if(!this.activated && this.liftProgress == 0 && this.prevLiftProgress == 1)
        {
            this.level.setBlock(this.worldPosition, this.getBlockState().setValue(JackBlock.ENABLED, false), Constants.BlockFlags.DEFAULT);
        }

        this.prevLiftProgress = this.liftProgress;

        if(this.jack == null)
        {
            List<EntityJack> jacks = this.level.getEntitiesOfClass(EntityJack.class, new AxisAlignedBB(this.worldPosition));
            if(jacks.size() > 0)
            {
                this.jack = jacks.get(0);
            }
        }

        if(this.jack != null && (this.jack.getPassengers().isEmpty() || !this.jack.isAlive()))
        {
            this.jack = null;
        }

        if(this.jack != null)
        {
            if(this.jack.getPassengers().size() > 0)
            {
                if(!this.activated)
                {
                    this.level.playSound(null, this.worldPosition, ModSounds.BLOCK_JACK_HEAD_UP.get(), SoundCategory.BLOCKS, 1.0F, 1.0F);
                    this.activated = true;
                    this.level.setBlock(this.worldPosition, this.getBlockState().setValue(JackBlock.ENABLED, true), Constants.BlockFlags.DEFAULT);
                }
            }
            else if(this.activated)
            {
                this.level.playSound(null, this.worldPosition, ModSounds.BLOCK_JACK_HEAD_DOWN.get(), SoundCategory.BLOCKS, 1.0F, 1.0F);
                this.activated = false;
            }
        }
        else if(this.activated)
        {
            this.level.playSound(null, this.worldPosition, ModSounds.BLOCK_JACK_HEAD_DOWN.get(), SoundCategory.BLOCKS, 1.0F, 1.0F);
            this.activated = false;
        }

        if(this.activated)
        {
            if(this.liftProgress < MAX_LIFT_PROGRESS)
            {
                this.liftProgress++;
                this.moveCollidedEntities();
            }
        }
        else if(this.liftProgress > 0)
        {
            this.liftProgress--;
            this.moveCollidedEntities();
        }
    }

    private void moveCollidedEntities()
    {
        BlockState state = this.level.getBlockState(this.getBlockPos());
        if(state.getBlock() instanceof JackBlock)
        {
            AxisAlignedBB boundingBox = state.getShape(this.level, this.worldPosition).bounds().move(this.worldPosition);
            List<Entity> list = this.level.getEntities(this.jack, boundingBox);
            if(!list.isEmpty())
            {
                for(Entity entity : list)
                {
                    if(entity.getPistonPushReaction() != PushReaction.IGNORE)
                    {
                        AxisAlignedBB entityBoundingBox = entity.getBoundingBox();
                        double posY = boundingBox.maxY - entityBoundingBox.minY;
                        entity.move(MoverType.PISTON, new Vector3d(0.0, posY, 0.0));
                    }
                }
            }
        }
    }

    public float getProgress()
    {
        return (float) this.liftProgress / (float) MAX_LIFT_PROGRESS;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox()
    {
        return INFINITE_EXTENT_AABB;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public double getViewDistance()
    {
        return 65536.0D;
    }
}
