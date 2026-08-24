package com.mrcrayfish.vehicle.common;

import com.mrcrayfish.vehicle.entity.IWheelType;
import com.mrcrayfish.vehicle.entity.PoweredVehicleEntity;
import com.mrcrayfish.vehicle.entity.Wheel;
import com.mrcrayfish.vehicle.entity.properties.VehicleProperties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.tags.BlockTags;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Categories block states into a surface type to determine vehicle behavior.
 * Author: MrCrayfish
 */
public class SurfaceHelper
{
    public static SurfaceType getSurfaceTypeForState(BlockState state)
    {
        if(state.isAir())
            return SurfaceType.NONE;

        if(state.is(BlockTags.ICE))
            return SurfaceType.ICE;

        if(state.is(BlockTags.SNOW) || state.is(BlockTags.LEAVES) || state.is(BlockTags.CROPS))
            return SurfaceType.SNOW;

        if(state.is(BlockTags.DIRT) || state.is(BlockTags.SAND) || state.is(BlockTags.WOOL))
            return SurfaceType.DIRT;

        return SurfaceType.SOLID;
    }

    private static float getValue(PoweredVehicleEntity vehicle, BiFunction<IWheelType, SurfaceType, Float> function, float defaultValue)
    {
        VehicleProperties properties = vehicle.getProperties();
        List<Wheel> wheels = properties.getWheels();
        if(!vehicle.hasWheelStack() || wheels.isEmpty())
            return defaultValue;

        Optional<IWheelType> optional = vehicle.getWheelType();
        if(!optional.isPresent())
            return defaultValue;

        int wheelCount = 0;
        float surfaceModifier = 0F;
        double[] wheelPositions = vehicle.getWheelPositions();
        for(int i = 0; i < wheels.size(); i++)
        {
            double wheelX = wheelPositions[i * 3];
            double wheelY = wheelPositions[i * 3 + 1];
            double wheelZ = wheelPositions[i * 3 + 2];
            int x = Mth.floor(vehicle.getX() + wheelX);
            int y = Mth.floor(vehicle.getY() + wheelY - 0.2D);
            int z = Mth.floor(vehicle.getZ() + wheelZ);
            BlockState state = vehicle.level().getBlockState(new BlockPos(x, y, z));
            SurfaceType surfaceType = getSurfaceTypeForState(state);
            if(surfaceType == SurfaceType.NONE)
                continue;
            IWheelType wheelType = optional.get();
            surfaceModifier += function.apply(wheelType, surfaceType);
            wheelCount++;
        }
        return surfaceModifier / Math.max(1F, wheelCount);
    }

    public static float getFriction(PoweredVehicleEntity vehicle)
    {
        return getValue(vehicle, (wheelType, surfaceType) -> surfaceType.friction * surfaceType.wheelFunction.apply(wheelType), 0.0F);
    }

    public static float getSurfaceTraction(PoweredVehicleEntity vehicle, float original)
    {
        return getValue(vehicle, (wheelType, surfaceType) -> surfaceType.tractionFactor, 1.0F) * original;
    }

    public enum SurfaceType
    {
        SOLID(IWheelType::getRoadFrictionFactor, 0.9F, 1.0F),
        DIRT(IWheelType::getDirtFrictionFactor, 1.1F, 0.9F),
        SNOW(IWheelType::getSnowFrictionFactor, 1.5F, 0.9F),
        ICE(type -> 1F, 1.5F, 0.01F),
        NONE(type -> 0F, 1.0F, 1.0F);

        private final Function<IWheelType, Float> wheelFunction;
        private final float friction;
        private final float tractionFactor;

        SurfaceType(Function<IWheelType, Float> frictionFunction, float friction, float tractionFactor)
        {
            this.wheelFunction = frictionFunction;
            this.friction = friction;
            this.tractionFactor = tractionFactor;
        }
    }
}
