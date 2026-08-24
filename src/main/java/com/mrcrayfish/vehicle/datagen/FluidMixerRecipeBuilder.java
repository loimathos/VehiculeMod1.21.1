package com.mrcrayfish.vehicle.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mrcrayfish.vehicle.crafting.FluidEntry;
import com.mrcrayfish.vehicle.init.ModRecipeSerializers;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * Author: MrCrayfish
 */
public class FluidMixerRecipeBuilder
{
    private final RecipeSerializer<?> serializer;
    private final FluidEntry[] input;
    private final Ingredient ingredient;
    private final FluidEntry output;

    public FluidMixerRecipeBuilder(RecipeSerializer<?> serializer, FluidEntry inputOne, FluidEntry inputTwo, Ingredient ingredient, FluidEntry output)
    {
        this.serializer = serializer;
        this.input = new FluidEntry[]{inputOne, inputTwo};
        this.ingredient = ingredient;
        this.output = output;
    }

    public static FluidMixerRecipeBuilder mixing(FluidEntry inputOne, FluidEntry inputTwo, Ingredient ingredient, FluidEntry output)
    {
        return new FluidMixerRecipeBuilder(ModRecipeSerializers.FLUID_MIXER.get(), inputOne, inputTwo, ingredient, output);
    }

    public void save(RecipeOutput consumer, String name)
    {
        this.save(consumer, ResourceLocation.parse(name));
    }

    public void save(RecipeOutput consumer, ResourceLocation id)
    {
        consumer.accept(id, new com.mrcrayfish.vehicle.crafting.FluidMixerRecipe(this.ingredient.getItems().length > 0 ? this.ingredient.getItems()[0] : net.minecraft.world.item.ItemStack.EMPTY, this.output), null);
    }
}
