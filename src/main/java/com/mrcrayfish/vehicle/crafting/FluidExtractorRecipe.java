package com.mrcrayfish.vehicle.crafting;

import com.mrcrayfish.vehicle.init.ModRecipeSerializers;
import com.mrcrayfish.vehicle.util.InventoryUtil;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

/**
 * Author: MrCrayfish
 */
public class FluidExtractorRecipe implements Recipe<RecipeInput>
{
    private ResourceLocation id;
    private ItemStack ingredient;
    private FluidEntry result;

    public FluidExtractorRecipe(ItemStack ingredient, FluidEntry result)
    {
        this.id = ResourceLocation.fromNamespaceAndPath("vehicle", "fluid_extractor");
        this.ingredient = ingredient;
        this.result = result;
    }

    public FluidExtractorRecipe(ResourceLocation id, ItemStack ingredient, FluidEntry result)
    {
        this.id = id;
        this.ingredient = ingredient;
        this.result = result;
    }

    public ItemStack getIngredient()
    {
        return ingredient;
    }

    public FluidEntry getResult()
    {
        return result;
    }

    @Override
    public boolean matches(RecipeInput input, Level level)
    {
        ItemStack source = input.getItem(1);
        return InventoryUtil.areItemStacksEqualIgnoreCount(source, this.ingredient);
    }

    @Override
    public ItemStack assemble(RecipeInput input, HolderLookup.Provider registries)
    {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height)
    {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries)
    {
        return ItemStack.EMPTY;
    }

    public ResourceLocation getId()
    {
        return this.id;
    }

    public RecipeSerializer<?> getSerializer()
    {
        return ModRecipeSerializers.FLUID_EXTRACTOR.get();
    }

    public RecipeType<?> getType()
    {
        return com.mrcrayfish.vehicle.crafting.RecipeType.FLUID_EXTRACTOR.get();
    }
}
