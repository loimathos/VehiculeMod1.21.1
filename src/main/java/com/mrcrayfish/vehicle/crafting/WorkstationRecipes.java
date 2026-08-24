package com.mrcrayfish.vehicle.crafting;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

/**
 * Author: MrCrayfish
 */
public class WorkstationRecipes
{
    @Nullable
    public static WorkstationRecipe getRecipe(EntityType<?> entityType, Level world)
    {
        return world.getRecipeManager().getAllRecipesFor(RecipeType.WORKSTATION.get()).stream()
                .map(RecipeHolder::value)
                .filter(recipe -> recipe.getVehicle() == entityType)
                .findFirst()
                .orElse(null);
    }
}
