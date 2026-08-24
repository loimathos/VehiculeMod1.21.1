package com.mrcrayfish.vehicle.common.entity;

import com.mrcrayfish.vehicle.network.PacketHandler;
import com.mrcrayfish.vehicle.network.message.MessageSyncHeldVehicle;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.PacketDistributor;

/**
 * Author: MrCrayfish
 */
public class HeldVehicleDataHandler
{
    private static final String KEY = "vehicle:held_vehicle";

    public static void register()
    {
        MinecraftForge.EVENT_BUS.register(new HeldVehicleDataHandler());
    }

    public static boolean isHoldingVehicle(Player player)
    {
        return !getHeldVehicle(player).isEmpty();
    }

    public static CompoundTag getHeldVehicle(Player player)
    {
        if(player != null && player.getPersistentData().contains(KEY, Tag.TAG_COMPOUND))
        {
            return player.getPersistentData().getCompound(KEY);
        }
        return new CompoundTag();
    }

    public static void setHeldVehicle(Player player, CompoundTag vehicleTag)
    {
        if(player != null)
        {
            if(vehicleTag == null || vehicleTag.isEmpty())
            {
                player.getPersistentData().remove(KEY);
            }
            else
            {
                player.getPersistentData().put(KEY, vehicleTag);
            }
            if(!player.level().isClientSide && player instanceof ServerPlayer serverPlayer)
            {
                PacketHandler.sendToTrackingAndSelf(player, new MessageSyncHeldVehicle(player.getId(), vehicleTag));
            }
        }
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event)
    {
        if(event.isWasDeath())
            return;

        CompoundTag vehicleTag = getHeldVehicle(event.getOriginal());
        if(!vehicleTag.isEmpty())
        {
            setHeldVehicle(event.getEntity(), vehicleTag);
        }
    }

    @SubscribeEvent
    public void onStartTracking(PlayerEvent.StartTracking event)
    {
        if(event.getTarget() instanceof Player player && event.getEntity() instanceof ServerPlayer serverPlayer)
        {
            CompoundTag vehicleTag = getHeldVehicle(player);
            PacketHandler.sendToPlayer(serverPlayer, new MessageSyncHeldVehicle(player.getId(), vehicleTag));
        }
    }

    @SubscribeEvent
    public void onPlayerJoinWorld(EntityJoinLevelEvent event)
    {
        Entity entity = event.getEntity();
        if(entity instanceof ServerPlayer serverPlayer && !event.getLevel().isClientSide)
        {
            CompoundTag vehicleTag = getHeldVehicle(serverPlayer);
            PacketHandler.sendToPlayer(serverPlayer, new MessageSyncHeldVehicle(serverPlayer.getId(), vehicleTag));
        }
    }
}
