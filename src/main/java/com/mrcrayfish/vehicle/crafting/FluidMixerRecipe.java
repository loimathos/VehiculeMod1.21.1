package com.mrcrayfish.vehicle.crafting;

import com.mrcrayfish.vehicle.init.ModRecipeSerializers;
import com.mrcrayfish.vehicle.blockentity.FluidMixerBlockEntity;
import com.mrcrayfish.vehicle.util.InventoryUtil;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.Objects;

/**
 * Author: MrCrayfish
 */
public class FluidMixerRecipe implements Recipe<RecipeInput>
{
    private ResourceLocation id;
    private FluidEntry[] inputs;
    private ItemStack ingredient;
    private FluidEntry result;
    private int hashCode;

    public FluidMixerRecipe(ItemStack ingredient, FluidEntry result)
    {
        this.id = ResourceLocation.fromNamespaceAndPath("vehicle", "fluid_mixer");
        this.inputs = new FluidEntry[0];
        this.ingredient = ingredient;
        this.result = result;
    }

    public FluidMixerRecipe(ResourceLocation id, FluidEntry fluidOne, FluidEntry fluidTwo, ItemStack ingredient, FluidEntry result)
    {
        this.id = id;
        this.inputs = new FluidEntry[]{fluidOne, fluidTwo};
        this.ingredient = ingredient;
        this.result = result;
    }

    public FluidEntry[] getInputs()
    {
        return inputs;
    }

    public ItemStack getIngredient()
    {
        return this.ingredient;
    }

    public FluidEntry getResult()
    {
        return result;
    }

    public int getFluidAmount(Fluid fluid)
    {
        for(int i = 0; i < 2; i++)
        {
            FluidEntry entry = this.inputs[i];
            if(entry.getFluid().equals(fluid))
            {
                return entry.getAmount();
            }
        }
        return -1;
    }

    @Override
    public boolean equals(Object obj)
    {
        if(!(obj instanceof FluidMixerRecipe)) return false;
        FluidMixerRecipe other = (FluidMixerRecipe) obj;
        int index = -1;
        for(int i = 0; i < 2; i++)
        {
            if(other.inputs[0].getFluid().equals(this.inputs[i].getFluid()))
            {
                index = i == 1 ? 0 : 1;
            }
        }
        if(index == -1) return false;
        if(!other.inputs[1].getFluid().equals(this.inputs[index].getFluid())) return false;
        return InventoryUtil.areItemStacksEqualIgnoreCount(other.ingredient, this.ingredient);
    }

    @Override
    public int hashCode()
    {
        if(this.hashCode == 0)
        {
            this.hashCode = Objects.hash(BuiltInRegistries.FLUID.getKey(this.inputs[0].getFluid()), BuiltInRegistries.FLUID.getKey(this.inputs[1].getFluid()), BuiltInRegistries.ITEM.getKey(this.ingredient.getItem()));
        }
        return this.hashCode;
    }

    @Override
    public boolean matches(RecipeInput input, Level level)
    {
        if(!(input instanceof FluidMixerBlockEntity be))
            return false;
        if(be.getEnderSapTank().isEmpty() || be.getBlazeTank().isEmpty())
            return false;
        Fluid inputFluid = be.getEnderSapTank().getFluid().getFluid();
        int index = -1;
        for(int i = 0; i < 2; i++)
        {
            if(inputFluid.equals(this.inputs[i].getFluid()))
            {
                index = i == 1 ? 0 : 1;
            }
        }
        if(index == -1) return false;
        Fluid inputTwo = be.getBlazeTank().getFluid().getFluid();
        if(!inputTwo.equals(this.inputs[index].getFluid())) return false;
        return InventoryUtil.areItemStacksEqualIgnoreCount(be.getItem(1), this.ingredient);
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
        return ModRecipeSerializers.FLUID_MIXER.get();
    }

    public RecipeType<?> getType()
    {
        return com.mrcrayfish.vehicle.crafting.RecipeType.FLUID_MIXER.get();
    }
}