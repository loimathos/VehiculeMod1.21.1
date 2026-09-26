package com.mrcrayfish.vehicle.entity;

import net.minecraft.world.phys.Vec3;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import com.mrcrayfish.vehicle.util.CommonUtils;

import javax.annotation.Nullable;

/**
 * Author: MrCrayfish
 */
public abstract class BoatEntity extends PoweredVehicleEntity
{
    protected State state = State.IN_AIR;
    protected State previousState = State.IN_AIR;
    private double waterLevel;
    protected Vec3 velocity = Vec3.ZERO;

    public BoatEntity(EntityType<?> entityType, Level worldIn)
    {
        super(entityType, worldIn);
    }

    public float getNormalSpeed()
    {
        float enginePower = this.getEnginePower() * this.getEngineTier().map(IEngineTier::getPowerMultiplier).orElse(1.0F);
        return enginePower > 0F ? (float) (this.velocity.length() * 20.0F / enginePower) : 0F;
    }

    @Override
    public void updateVehicleMotion()
    {
        this.motion = Vec3.ZERO;

        boolean operating = this.canDrive() && this.getControllingPassenger() != null;
        float throttle = operating ? this.getThrottle() : 0F;
        float steeringAngle = this.getSteeringAngle();

        if(this.state == State.IN_WATER || this.state == State.UNDER_WATER || this.state == State.UNDER_FLOWING_WATER)
        {
            if(this.state == State.UNDER_WATER || this.state == State.UNDER_FLOWING_WATER)
            {
                this.setDeltaMovement(this.getDeltaMovement().add(0, 0.08, 0));
            }
            else
            {
                float normalSpeed = this.getNormalSpeed();
                double targetY = this.waterLevel - 0.35D + (0.25D * Math.min(1.0F, normalSpeed));
                double floatingY = (targetY - this.getY()) / (double) this.getBbHeight();
                this.setDeltaMovement(this.getDeltaMovement().add(0, floatingY * 0.05, 0));
                if(Math.abs(floatingY) < 0.1 && this.getDeltaMovement().y > 0 && Math.abs(this.getDeltaMovement().y) < 0.1)
                {
                    this.setPos(this.getX(), targetY, this.getZ());
                    this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 0.0, 1.0));
                }
                this.setDeltaMovement(this.getDeltaMovement().multiply(0.5, 0.75, 0.5));
            }

            // Steering
            if(operating && Math.abs(steeringAngle) > 0.01F)
            {
                float speedFactor = (float) Mth.clamp(this.velocity.length() * 20.0 / 5.0, 0.0, 1.0);
                if(Math.abs(throttle) > 0.01F && speedFactor < 0.25F)
                {
                    speedFactor = 0.25F;
                }
                float deltaYaw = steeringAngle * speedFactor * 0.15F * (throttle < 0 ? -1.0F : 1.0F);
                this.setYRot(this.getYRot() - deltaYaw);
            }

            Vec3 forward = Vec3.directionFromRotation(new Vec2(0, this.getYRot()));
            float enginePower = this.getEnginePower() * this.getEngineTier().map(IEngineTier::getPowerMultiplier).orElse(1.0F);
            float forwardForce = enginePower * Mth.clamp(throttle, -1.0F, 1.0F);
            if(this.isBoosting())
            {
                forwardForce += forwardForce * this.getSpeedMultiplier();
            }
            if(throttle < 0)
            {
                forwardForce *= 0.35F;
            }

            Vec3 acceleration = forward.scale(forwardForce).scale(0.05);
            Vec3 drag = this.velocity.scale(this.velocity.length()).scale(-0.02);
            Vec3 friction = this.velocity.scale(-0.05);
            acceleration = acceleration.add(drag).add(friction);
            this.velocity = this.velocity.add(acceleration);

            // Align velocity with heading
            if(this.velocity.length() > 0.001)
            {
                Vec3 heading = forward;
                if(heading.dot(this.velocity.normalize()) > 0)
                {
                    this.velocity = CommonUtils.lerp(this.velocity, heading.scale(this.velocity.length()), 0.15F);
                }
                else
                {
                    this.velocity = CommonUtils.lerp(this.velocity, heading.scale(-this.velocity.length()), 0.15F);
                }
            }

            if(this.velocity.length() < 0.005)
            {
                this.velocity = Vec3.ZERO;
            }
            this.velocity = CommonUtils.clampSpeed(this.velocity);
            this.motion = this.motion.add(this.velocity);
        }
        else if(this.state == State.IN_AIR)
        {
            this.setDeltaMovement(this.getDeltaMovement().add(0, -0.08, 0));
            this.velocity = this.velocity.scale(0.98);
            this.motion = this.motion.add(this.velocity);
        }
        else // ON_LAND
        {
            this.velocity = this.velocity.scale(0.6);
            this.motion = this.motion.add(this.velocity);
        }
    }

    @Override
    public void onVehicleTick()
    {
        this.previousState = this.state;
        this.state = this.getState();
        if(this.state == State.IN_AIR)
        {
            //this.deltaYaw *= 2;
        }
    }

    private boolean checkInWater()
    {
        AABB boundingBox = this.getBoundingBox();
        int minX = Mth.floor(boundingBox.minX);
        int maxX = Mth.ceil(boundingBox.maxX);
        int minY = Mth.floor(boundingBox.minY);
        int maxY = Mth.ceil(boundingBox.minY + 0.001D);
        int minZ = Mth.floor(boundingBox.minZ);
        int maxZ = Mth.ceil(boundingBox.maxZ);
        boolean inWater = false;
        this.waterLevel = Double.MIN_VALUE;

        BlockPos.MutableBlockPos pooledMutable = new BlockPos.MutableBlockPos();
        for(int x = minX; x < maxX; x++)
        {
            for(int y = minY; y < maxY; y++)
            {
                for(int z = minZ; z < maxZ; z++)
                {
                    pooledMutable.set(x, y, z);
                    FluidState fluidState = this.level().getFluidState(pooledMutable);
                    if(fluidState.is(FluidTags.WATER))
                    {
                        float waterLevel = (float) y + fluidState.getHeight(this.level(), pooledMutable);
                        this.waterLevel = Math.max((double) waterLevel, this.waterLevel);
                        inWater |= boundingBox.minY < (double) waterLevel;
                    }
                }
            }
        }

        return inWater;
    }

    @Nullable
    private State getUnderwaterState()
    {
        AABB axisalignedbb = this.getBoundingBox();
        double height = axisalignedbb.maxY + 0.001D;
        int minX = Mth.floor(axisalignedbb.minX);
        int maxX = Mth.ceil(axisalignedbb.maxX);
        int minY = Mth.floor(axisalignedbb.maxY);
        int maxY = Mth.ceil(height);
        int minZ = Mth.floor(axisalignedbb.minZ);
        int maxZ = Mth.ceil(axisalignedbb.maxZ);
        boolean underWater = false;

        BlockPos.MutableBlockPos pooledMutable = new BlockPos.MutableBlockPos();
        for(int x = minX; x < maxX; x++)
        {
            for(int y = minY; y < maxY; y++)
            {
                for(int z = minZ; z < maxZ; z++)
                {
                    pooledMutable.set(x, y, z);
                    FluidState fluidState = this.level().getFluidState(pooledMutable);
                    if(fluidState.is(FluidTags.WATER) && height < (double) ((float) pooledMutable.getY() + fluidState.getHeight(this.level(), pooledMutable)))
                    {
                        if(!fluidState.isSource())
                        {
                            return State.UNDER_FLOWING_WATER;
                        }
                        underWater = true;
                    }
                }
            }
        }

        return underWater ? State.UNDER_WATER : null;
    }

    protected State getState()
    {
        State state = this.getUnderwaterState();
        if(state != null)
        {
            return state;
        }
        else if(this.checkInWater())
        {
            return State.IN_WATER;
        }
        else if(this.onGround())
        {
            return State.ON_LAND;
        }
        return State.IN_AIR;
    }

    //TODO figure out new way to reimplement
    /*@Override
    protected void updateGroundState()
    {
        this.wheelsOnGround = this.getState() == State.IN_WATER || this.getState() == State.UNDER_WATER;
    }*/

    protected enum State
    {
        IN_WATER,
        UNDER_WATER,
        UNDER_FLOWING_WATER,
        ON_LAND,
        IN_AIR;
    }
}
