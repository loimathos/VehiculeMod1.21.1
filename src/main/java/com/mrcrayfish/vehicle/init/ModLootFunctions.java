package com.mrcrayfish.vehicle.init;

import com.mrcrayfish.vehicle.Reference;
import com.mrcrayfish.vehicle.world.storage.loot.functions.CopyFluidTanks;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

/**
 * Author: MrCrayfish
 */
public class ModLootFunctions
{
    public static final LootItemFunctionType COPY_FLUID_TANKS = register("copy_fluid_tanks");

    // Load class
    public static void init()
    {
    }

    private static LootItemFunctionType register(String id)
    {
        return Registry.register(BuiltInRegistries.LOOT_FUNCTION_TYPE, ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, id), new LootItemFunctionType(CopyFluidTanks.CODEC));
    }
}