package com.mrcrayfish.vehicle.crafting;

import com.mrcrayfish.vehicle.init.ModRecipeSerializers;
import com.mrcrayfish.vehicle.blockentity.FluidExtractorBlockEntity;
import com.mrcrayfish.vehicle.util.InventoryUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.level.Level;

/**
 * Author: MrCrayfish
 */
public class FluidExtractorRecipe implements IRecipe<FluidExtractorBlockEntity>
{
    private ResourceLocation id;
    private ItemStack ingredient;
    private FluidEntry result;

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
    public boolean matches(FluidExtractorBlockEntity fluidExtractor, Level worldIn)
    {
        ItemStack source = fluidExtractor.getItem(FluidExtractorBlockEntity.SLOT_FLUID_SOURCE);
        return InventoryUtil.areItemStacksEqualIgnoreCount(source, this.ingredient);
    }

    @Override
    public ItemStack assemble(FluidExtractorBlockEntity inv)
    {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height)
    {
        return true;
    }

    @Override
    public ItemStack getResultItem()
    {
        return ItemStack.EMPTY;
    }

    @Override
    public ResourceLocation getId()
    {
        return this.id;
    }

    @Override
    public IRecipeSerializer<?> getSerializer()
    {
        return ModRecipeSerializers.FLUID_EXTRACTOR.get();
    }

    @Override
    public IRecipeType<?> getType()
    {
        return RecipeType.FLUID_EXTRACTOR;
    }
}
