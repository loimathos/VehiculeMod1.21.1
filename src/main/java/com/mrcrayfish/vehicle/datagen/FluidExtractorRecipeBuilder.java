package com.mrcrayfish.vehicle.datagen;

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
public class FluidExtractorRecipeBuilder
{
    private final RecipeSerializer<?> serializer;
    private final Ingredient ingredient;
    private final FluidEntry entry;

    public FluidExtractorRecipeBuilder(RecipeSerializer<?> serializer, Ingredient ingredient, FluidEntry entry)
    {
        this.serializer = serializer;
        this.ingredient = ingredient;
        this.entry = entry;
    }

    public static FluidExtractorRecipeBuilder extracting(Ingredient ingredient, FluidEntry entry)
    {
        return new FluidExtractorRecipeBuilder(ModRecipeSerializers.FLUID_EXTRACTOR.get(), ingredient, entry);
    }

    public void save(RecipeOutput consumer, String name)
    {
        this.save(consumer, ResourceLocation.parse(name));
    }

    public void save(RecipeOutput consumer, ResourceLocation id)
    {
        consumer.accept(id, new com.mrcrayfish.vehicle.crafting.FluidExtractorRecipe(this.ingredient.getItems().length > 0 ? this.ingredient.getItems()[0] : net.minecraft.world.item.ItemStack.EMPTY, this.entry), null);
    }
}
