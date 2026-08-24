package com.mrcrayfish.obfuscate.common.data;

import net.minecraft.world.entity.player.Player;

public class SyncedPlayerData {
    private static final SyncedPlayerData INSTANCE = new SyncedPlayerData();
    public static SyncedPlayerData instance() {
        return INSTANCE;
    }
    public void set(Player player, SyncedDataKey<?> key, Object value) {}
    @SuppressWarnings("unchecked")
    public <T> T get(Player player, SyncedDataKey<T> key) {
        if(key != null && key.getDefaultValueSupplier() != null) {
            return key.getDefaultValueSupplier().get();
        }
        return null;
    }
    public void registerKey(SyncedDataKey<?> key) {}
}
