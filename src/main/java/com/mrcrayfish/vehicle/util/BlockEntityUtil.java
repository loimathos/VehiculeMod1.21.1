package com.mrcrayfish.vehicle.util;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

import java.util.List;
import java.util.stream.Stream;

/**
 * Author: MrCrayfish
 */
public class BlockEntityUtil
{
    /**
     * Sends an update packet to clients tracking a block entity.
     *
     * @param blockEntity the block entity to update
     */
    public static void sendUpdatePacket(BlockEntity blockEntity)
    {
        net.minecraft.network.protocol.Packet<?> packet = blockEntity.getUpdatePacket();
        if(packet != null && blockEntity.getLevel() != null)
        {
            sendUpdatePacket(blockEntity.getLevel(), blockEntity.getBlockPos(), packet);
        }
    }

    /**
     * Sends an update packet to clients tracking a block entity with a specific CompoundTag
     *
     * @param blockEntity the block entity to update
     */
    public static void sendUpdatePacket(BlockEntity blockEntity, CompoundTag compound)
    {
        if(blockEntity.getLevel() != null)
        {
            ClientboundBlockEntityDataPacket packet = ClientboundBlockEntityDataPacket.create(blockEntity, (be, reg) -> compound);
            sendUpdatePacket(blockEntity.getLevel(), blockEntity.getBlockPos(), packet);
        }
    }

    /**
     * Sends an update packet but only to a specific player. This helps reduce overhead on the network
     * when you only want to update a block entity for a single player rather than everyone who is
     * tracking the block entity.
     *
     * @param blockEntity the block entity to update
     * @param player the player to send the update to
     */
    public static void sendUpdatePacket(BlockEntity blockEntity, ServerPlayer player)
    {
        if(blockEntity.getLevel() != null)
        {
            sendUpdatePacket(blockEntity, blockEntity.getUpdateTag(blockEntity.getLevel().registryAccess()), player);
        }
    }

    /**
     * Sends an update packet with a custom nbt compound but only to a specific player. This helps
     * reduce overhead on the network when you only want to update a block entity for a single player
     * rather than everyone who is tracking the block entity.
     *
     * @param blockEntity the block entity to update
     * @param compound the update tag to send
     * @param player the player to send the update to
     */
    public static void sendUpdatePacket(BlockEntity blockEntity, CompoundTag compound, ServerPlayer player)
    {
        ClientboundBlockEntityDataPacket packet = ClientboundBlockEntityDataPacket.create(blockEntity, (be, reg) -> compound);
        player.connection.send(packet);
    }

    private static void sendUpdatePacket(Level world, BlockPos pos, net.minecraft.network.protocol.Packet<?> packet)
    {
        if(world instanceof ServerLevel)
        {
            ServerLevel server = (ServerLevel) world;
            List<ServerPlayer> players = server.getChunkSource().chunkMap.getPlayers(new ChunkPos(pos), false);
            players.forEach(player -> player.connection.send(packet));
        }
    }
}
