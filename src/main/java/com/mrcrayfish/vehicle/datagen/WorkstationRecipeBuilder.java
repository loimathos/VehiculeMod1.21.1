package com.mrcrayfish.vehicle.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mrcrayfish.vehicle.crafting.WorkstationIngredient;
import com.mrcrayfish.vehicle.init.ModRecipeSerializers;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.conditions.ICondition;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Author: MrCrayfish
 */
public class WorkstationRecipeBuilder
{
    private final RecipeSerializer<?> serializer;
    private final ResourceLocation entityId;
    private final List<WorkstationIngredient> ingredients;
    private final List<ICondition> conditions = new ArrayList<>();

    public WorkstationRecipeBuilder(RecipeSerializer<?> serializer, ResourceLocation entityId, List<WorkstationIngredient> ingredients)
    {
        this.serializer = serializer;
        this.entityId = entityId;
        this.ingredients = ingredients;
    }

    public static WorkstationRecipeBuilder crafting(ResourceLocation entityId, List<WorkstationIngredient> ingredients)
    {
        return new WorkstationRecipeBuilder(ModRecipeSerializers.WORKSTATION.get(), entityId, ingredients);
    }

    public WorkstationRecipeBuilder addCondition(ICondition condition)
    {
        this.conditions.add(condition);
        return this;
    }

    public void save(RecipeOutput consumer, String name)
    {
        this.save(consumer, ResourceLocation.parse(name));
    }

    public void save(RecipeOutput consumer, ResourceLocation id)
    {
        net.minecraft.world.entity.EntityType<?> entityType = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.get(this.entityId);
        consumer.accept(id, new com.mrcrayfish.vehicle.crafting.WorkstationRecipe(id, entityType, com.google.common.collect.ImmutableList.copyOf(this.ingredients)), null);
    }
}
