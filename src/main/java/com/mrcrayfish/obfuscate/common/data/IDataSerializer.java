package com.mrcrayfish.obfuscate.common.data;

import net.minecraft.network.FriendlyByteBuf;

public interface IDataSerializer<T> {
    void write(FriendlyByteBuf buffer, T value);
    T read(FriendlyByteBuf buffer);
}
