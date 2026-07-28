package com.mrcrayfish.vehicle.init;

import com.mrcrayfish.vehicle.Reference;
import com.mrcrayfish.vehicle.common.inventory.IStorage;
import com.mrcrayfish.vehicle.common.inventory.StorageInventory;
import com.mrcrayfish.vehicle.entity.PoweredVehicleEntity;
import com.mrcrayfish.vehicle.inventory.container.EditVehicleContainer;
import com.mrcrayfish.vehicle.inventory.container.FluidExtractorContainer;
import com.mrcrayfish.vehicle.inventory.container.FluidMixerContainer;
import com.mrcrayfish.vehicle.inventory.container.StorageContainer;
import com.mrcrayfish.vehicle.inventory.container.WorkstationContainer;
import com.mrcrayfish.vehicle.blockentity.FluidExtractorBlockEntity;
import com.mrcrayfish.vehicle.blockentity.FluidMixerBlockEntity;
import com.mrcrayfish.vehicle.blockentity.WorkstationBlockEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;

/**
 * Author: MrCrayfish
 */
public class ModContainers
{
    public static final DeferredRegister<MenuType<?>> REGISTER = DeferredRegister.create(ForgeRegistries.MENU_TYPES, Reference.MOD_ID);

    public static final RegistryObject<MenuType<FluidExtractorContainer>> FLUID_EXTRACTOR = register("fluid_extractor", (IContainerFactory<FluidExtractorContainer>) (windowId, playerInventory, data) -> {
        FluidExtractorBlockEntity fluidExtractor = (FluidExtractorBlockEntity) playerInventory.player.level().getBlockEntity(data.readBlockPos());
        return new FluidExtractorContainer(windowId, playerInventory, fluidExtractor);
    });
    public static final RegistryObject<MenuType<FluidMixerContainer>> FLUID_MIXER = register("fluid_mixer", (IContainerFactory<FluidMixerContainer>) (windowId, playerInventory, data) -> {
        FluidMixerBlockEntity fluidMixer = (FluidMixerBlockEntity) playerInventory.player.level().getBlockEntity(data.readBlockPos());
        return new FluidMixerContainer(windowId, playerInventory, fluidMixer);
    });
    public static final RegistryObject<MenuType<EditVehicleContainer>> EDIT_VEHICLE = register("edit_vehicle", (IContainerFactory<EditVehicleContainer>) (windowId, playerInventory, data) -> {
        PoweredVehicleEntity entity = (PoweredVehicleEntity) playerInventory.player.level().getEntity(data.readInt());
        return new EditVehicleContainer(windowId, entity.getVehicleInventory(), entity, playerInventory.player, playerInventory);
    });
    public static final RegistryObject<MenuType<WorkstationContainer>> WORKSTATION = register("workstation", (IContainerFactory<WorkstationContainer>) (windowId, playerInventory, data) -> {
        WorkstationBlockEntity workstation = (WorkstationBlockEntity) playerInventory.player.level().getBlockEntity(data.readBlockPos());
        return new WorkstationContainer(windowId, playerInventory, workstation);
    });
    public static final RegistryObject<MenuType<StorageContainer>> STORAGE = register("storage", (IContainerFactory<StorageContainer>) (windowId, playerInventory, data) -> {
        Entity entity = playerInventory.player.level().getEntity(data.readVarInt());
        StorageInventory storage = ((IStorage) entity).getStorageInventory(data.readUtf());
        return new StorageContainer(windowId, playerInventory, storage, playerInventory.player);
    });

    private static <T extends AbstractContainerMenu> RegistryObject<MenuType<T>> register(String id, IContainerFactory<T> factory)
    {
        return REGISTER.register(id, () -> IForgeMenuType.create(factory));
    }
}