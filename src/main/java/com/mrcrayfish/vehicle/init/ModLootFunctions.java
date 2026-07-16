package com.mrcrayfish.vehicle.init;

import com.mrcrayfish.vehicle.Reference;
import com.mrcrayfish.vehicle.world.storage.loot.functions.CopyFluidTanks;
import net.minecraft.world.level.storage.loot.Serializer;
import net.minecraft.world.level.storage.loot.LootFunctionType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Registry;

/**
 * Author: MrCrayfish
 */
public class ModLootFunctions
{
    public static final LootFunctionType COPY_FLUID_TANKS = register("copy_fluid_tanks", new CopyFluidTanks.Serializer());

    // Load class
    public static void init()
    {
    }

    private static LootFunctionType register(String id, LootItemSerializer<? extends LootItemFunction> serializer)
    {
        return Registry.register(Registry.LOOT_FUNCTION_TYPE, new ResourceLocation(Reference.MOD_ID, id), new LootFunctionType(serializer));
    }
}