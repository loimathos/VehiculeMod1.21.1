package com.mrcrayfish.vehicle.common.inventory;

import com.mrcrayfish.vehicle.entity.VehicleEntity;
import com.mrcrayfish.vehicle.inventory.container.StorageContainer;
import com.mrcrayfish.vehicle.util.InventoryUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.nbt.CompoundTag;


import javax.annotation.Nullable;
import java.util.Map;

/**
 * Author: MrCrayfish
 */
public interface IStorage
{
    Map<String, StorageInventory> getStorageInventories();

    @Nullable
    default StorageInventory getStorageInventory(String key)
    {
        return this.getStorageInventories().get(key);
    }

    default void readInventories(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries)
    {
        CompoundTag storageTag = tag.getCompound("Storage");
        this.getStorageInventories().forEach((key, storage) -> {
            InventoryUtil.readInventoryToNBT(storageTag, key, storage, registries);
        });
    }

    default void writeInventories(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries)
    {
        CompoundTag storageTag = new CompoundTag();
        this.getStorageInventories().forEach((key, storage) -> {
            InventoryUtil.writeInventoryToNBT(storageTag, key, storage, registries);
        });
        tag.put("Storage", storageTag);
    }

    @Deprecated
    default void readInventories(CompoundTag tag)
    {
        this.readInventories(tag, net.minecraft.core.HolderLookup.Provider.create(java.util.stream.Stream.of()));
    }

    @Deprecated
    default void writeInventories(CompoundTag tag)
    {
        this.writeInventories(tag, net.minecraft.core.HolderLookup.Provider.create(java.util.stream.Stream.of()));
    }

    static <T extends VehicleEntity & IStorage> void openStorage(ServerPlayer player, T storage, String key)
    {
        StorageInventory inventory = storage.getStorageInventory(key);
        if(inventory == null)
            return;

        player.openMenu(new SimpleMenuProvider((windowId, playerInventory, playerEntity) -> {
            return new StorageContainer(windowId, playerInventory, inventory, playerEntity);
        }, inventory.getDisplayName()), buffer -> {
            buffer.writeVarInt(storage.getId());
            buffer.writeUtf(key);
        });
    }
}
