package com.mrcrayfish.vehicle.common.data;

import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.core.BlockPos;

import java.util.Optional;

/**
 * Author: MrCrayfish
 */
public class Serializers
{
    public static final EntityDataSerializer<Optional<BlockPos>> OPTIONAL_BLOCK_POS = EntityDataSerializers.OPTIONAL_BLOCK_POS;
}
