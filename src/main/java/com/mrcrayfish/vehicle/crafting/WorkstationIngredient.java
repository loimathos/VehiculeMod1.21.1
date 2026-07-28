package com.mrcrayfish.vehicle.crafting;

import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

public class WorkstationIngredient
{
    private final Ingredient ingredient;
    private final int count;

    public WorkstationIngredient(Ingredient ingredient, int count)
    {
        this.ingredient = ingredient;
        this.count = count;
    }

    public Ingredient getIngredient()
    {
        return this.ingredient;
    }

    public int getCount()
    {
        return this.count;
    }

    public ItemStack[] getItems()
    {
        return this.ingredient.getItems();
    }

    public boolean test(ItemStack stack)
    {
        return this.ingredient.test(stack) && stack.getCount() >= this.count;
    }

    public static WorkstationIngredient fromJson(JsonObject object)
    {
        Ingredient ingredient = Ingredient.CODEC.parse(com.mojang.serialization.JsonOps.INSTANCE, object).getOrThrow();
        int count = GsonHelper.getAsInt(object, "count", 1);
        return new WorkstationIngredient(ingredient, count);
    }

    public static WorkstationIngredient fromNetwork(RegistryFriendlyByteBuf buffer)
    {
        Ingredient ingredient = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
        int count = buffer.readVarInt();
        return new WorkstationIngredient(ingredient, count);
    }

    public void toNetwork(RegistryFriendlyByteBuf buffer)
    {
        Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, this.ingredient);
        buffer.writeVarInt(this.count);
    }

    public static WorkstationIngredient of(ItemLike provider, int count)
    {
        return new WorkstationIngredient(Ingredient.of(provider), count);
    }

    public static WorkstationIngredient of(ItemStack stack, int count)
    {
        return new WorkstationIngredient(Ingredient.of(stack), count);
    }

    public static WorkstationIngredient of(TagKey<Item> tag, int count)
    {
        return new WorkstationIngredient(Ingredient.of(tag), count);
    }

    public static WorkstationIngredient of(ResourceLocation id, int count)
    {
        Item item = BuiltInRegistries.ITEM.get(id);
        return new WorkstationIngredient(Ingredient.of(item), count);
    }
}
