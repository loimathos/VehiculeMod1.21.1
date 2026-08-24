package com.mrcrayfish.vehicle.network;

import com.mrcrayfish.vehicle.Reference;
import com.mrcrayfish.vehicle.network.message.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.SimpleChannel;

public class PacketHandler
{
    private static final int PROTOCOL_VERSION = 1;
    private static final SimpleChannel HANDSHAKE_CHANNEL = ChannelBuilder.named(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "handshake")).networkProtocolVersion(PROTOCOL_VERSION).simpleChannel();
    private static final SimpleChannel PLAY_CHANNEL = ChannelBuilder.named(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "play")).networkProtocolVersion(PROTOCOL_VERSION).simpleChannel();
    private static int nextId = 0;

    public static void registerPlayMessage()
    {
        registerPlayMessage(MessageTurnAngle.class, new MessageTurnAngle());
        registerPlayMessage(MessageHandbrake.class, new MessageHandbrake());
        registerPlayMessage(MessageHorn.class, new MessageHorn());
        registerPlayMessage(MessageThrowVehicle.class, new MessageThrowVehicle());
        registerPlayMessage(MessagePickupVehicle.class, new MessagePickupVehicle());
        registerPlayMessage(MessageAttachChest.class, new MessageAttachChest());
        registerPlayMessage(MessageAttachTrailer.class, new MessageAttachTrailer());
        registerPlayMessage(MessageFuelVehicle.class, new MessageFuelVehicle());
        registerPlayMessage(MessageInteractKey.class, new MessageInteractKey());
        registerPlayMessage(MessageHelicopterInput.class, new MessageHelicopterInput());
        registerPlayMessage(MessageCraftVehicle.class, new MessageCraftVehicle());
        registerPlayMessage(MessageHitchTrailer.class, new MessageHitchTrailer());
        registerPlayMessage(MessageSyncStorage.class, new MessageSyncStorage());
        registerPlayMessage(MessageOpenStorage.class, new MessageOpenStorage());
        registerPlayMessage(MessageThrottle.class, new MessageThrottle());
        registerPlayMessage(MessageEntityFluid.class, new MessageEntityFluid());
        registerPlayMessage(MessageSyncPlayerSeat.class, new MessageSyncPlayerSeat());
        registerPlayMessage(MessageCycleSeats.class, new MessageCycleSeats());
        registerPlayMessage(MessageSetSeat.class, new MessageSetSeat());
        registerPlayMessage(MessageSyncHeldVehicle.class, new MessageSyncHeldVehicle());
        registerPlayMessage(MessagePlaneInput.class, new MessagePlaneInput());
        registerPlayMessage(MessageSyncCosmetics.class, new MessageSyncCosmetics());
        registerPlayMessage(MessageInteractCosmetic.class, new MessageInteractCosmetic());
        registerPlayMessage(MessageSyncActionData.class, new MessageSyncActionData());
    }

    private static <T> void registerPlayMessage(Class<T> clazz, IMessage<T> message)
    {
        PLAY_CHANNEL.messageBuilder(clazz).encoder(message::encode).decoder(message::decode).consumerMainThread((msg, ctx) -> message.handle(msg, () -> ctx)).add();
    }

    /**
     * Gets the handshake network channel for MrCrayfish's Vehicle Mod
     */
    public static SimpleChannel getHandshakeChannel()
    {
        return HANDSHAKE_CHANNEL;
    }

    /**
     * Gets the play network channel for MrCrayfish's Vehicle Mod
     */
    public static SimpleChannel getPlayChannel()
    {
        return PLAY_CHANNEL;
    }

    public static <MSG> void sendToServer(MSG message)
    {
        PLAY_CHANNEL.send(message, net.minecraftforge.network.PacketDistributor.SERVER.noArg());
    }

    public static <MSG> void sendToPlayer(net.minecraft.server.level.ServerPlayer player, MSG message)
    {
        PLAY_CHANNEL.send(message, net.minecraftforge.network.PacketDistributor.PLAYER.with(player));
    }

    public static <MSG> void sendToTrackingAndSelf(net.minecraft.world.entity.Entity entity, MSG message)
    {
        PLAY_CHANNEL.send(message, net.minecraftforge.network.PacketDistributor.TRACKING_ENTITY_AND_SELF.with(entity));
    }
}
