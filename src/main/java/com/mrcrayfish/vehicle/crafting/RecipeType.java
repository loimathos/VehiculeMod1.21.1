package com.mrcrayfish.vehicle.crafting;

import com.mrcrayfish.vehicle.Reference;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/**
 * Author: MrCrayfish
 */
public class RecipeType
{
    public static final DeferredRegister<net.minecraft.world.item.crafting.RecipeType<?>> REGISTER =
            DeferredRegister.create(Registries.RECIPE_TYPE, Reference.MOD_ID);

    public static final RegistryObject<net.minecraft.world.item.crafting.RecipeType<FluidExtractorRecipe>> FLUID_EXTRACTOR = register("fluid_extractor");
    public static final RegistryObject<net.minecraft.world.item.crafting.RecipeType<FluidMixerRecipe>> FLUID_MIXER = register("fluid_mixer");
    public static final RegistryObject<net.minecraft.world.item.crafting.RecipeType<WorkstationRecipe>> WORKSTATION = register("workstation");

    static <T extends Recipe<?>> RegistryObject<net.minecraft.world.item.crafting.RecipeType<T>> register(final String key)
    {
        return REGISTER.register(key, () -> new net.minecraft.world.item.crafting.RecipeType<T>()
        {
            @Override
            public String toString()
            {
                return Reference.MOD_ID + ":" + key;
            }
        });
    }

    public static void init() {}
}
