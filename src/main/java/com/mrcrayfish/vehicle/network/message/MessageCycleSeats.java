package com.mrcrayfish.vehicle.network.message;

import com.mrcrayfish.vehicle.network.play.ServerPlayHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;

import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public class MessageCycleSeats implements IMessage<MessageCycleSeats>
{
    public MessageCycleSeats() {}

    @Override
    public void encode(MessageCycleSeats message, FriendlyByteBuf buffer) {}

    @Override
    public MessageCycleSeats decode(FriendlyByteBuf buffer)
    {
        return new MessageCycleSeats();
    }

    @Override
    public void handle(MessageCycleSeats message, Supplier<CustomPayloadEvent.Context> supplier)
    {
        if(supplier.get().isServerSide())
        {
            supplier.get().enqueueWork(() ->
            {
                ServerPlayer player = supplier.get().getSender();
                if(player != null)
                {
                    ServerPlayHandler.handleCycleSeatsMessage(player, message);
                }
            });
            supplier.get().setPacketHandled(true);
        }
    }
}
