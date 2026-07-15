package com.mrcrayfish.vehicle.util;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.play.server.SUpdateBlockEntityPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevel;

import java.util.stream.Stream;

/**
 * Author: MrCrayfish
 */
public class BlockEntityUtil
{
    /**
     * Sends an update packet to clients tracking a block entity.
     *
     * @param tileEntity the block entity to update
     */
    public static void sendUpdatePacket(BlockEntity tileEntity)
    {
        SUpdateBlockEntityPacket packet = tileEntity.getUpdatePacket();
        if(packet != null)
        {
            sendUpdatePacket(tileEntity.getLevel(), tileEntity.getBlockPos(), packet);
        }
    }

    /**
     * Sends an update packet to clients tracking a block entity with a specific CompoundNBT
     *
     * @param tileEntity the block entity to update
     */
    public static void sendUpdatePacket(BlockEntity tileEntity, CompoundNBT compound)
    {
        SUpdateBlockEntityPacket packet = new SUpdateBlockEntityPacket(tileEntity.getBlockPos(), 0, compound);
        sendUpdatePacket(tileEntity.getLevel(), tileEntity.getBlockPos(), packet);
    }

    /**
     * Sends an update packet but only to a specific player. This helps reduce overhead on the network
     * when you only want to update a block entity for a single player rather than everyone who is
     * tracking the block entity.
     *
     * @param tileEntity the block entity to update
     * @param player the player to send the update to
     */
    public static void sendUpdatePacket(BlockEntity tileEntity, ServerPlayerEntity player)
    {
        sendUpdatePacket(tileEntity, tileEntity.getUpdateTag(), player);
    }

    /**
     * Sends an update packet with a custom nbt compound but only to a specific player. This helps
     * reduce overhead on the network when you only want to update a block entity for a single player
     * rather than everyone who is tracking the block entity.
     *
     * @param tileEntity the block entity to update
     * @param compound the update tag to send
     * @param player the player to send the update to
     */
    public static void sendUpdatePacket(BlockEntity tileEntity, CompoundNBT compound, ServerPlayerEntity player)
    {
        SUpdateBlockEntityPacket packet = new SUpdateBlockEntityPacket(tileEntity.getBlockPos(), 0, compound);
        player.connection.send(packet);
    }

    private static void sendUpdatePacket(Level world, BlockPos pos, SUpdateBlockEntityPacket packet)
    {
        if(world instanceof ServerLevel)
        {
            ServerLevel server = (ServerLevel) world;
            Stream<ServerPlayerEntity> players = server.getChunkSource().chunkMap.getPlayers(new ChunkPos(pos), false);
            players.forEach(player -> player.connection.send(packet));
        }
    }
}
