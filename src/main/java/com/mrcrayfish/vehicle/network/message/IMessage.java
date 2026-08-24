package com.mrcrayfish.vehicle.network.message;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;

import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public interface IMessage<T>
{
    void encode(T message, FriendlyByteBuf buffer);

    T decode(FriendlyByteBuf buffer);

    void handle(T message, Supplier<CustomPayloadEvent.Context> supplier);

    static void enqueueTask(Supplier<CustomPayloadEvent.Context> supplier, Runnable runnable)
    {
        supplier.get().enqueueWork(runnable);
        supplier.get().setPacketHandled(true);
    }
}
