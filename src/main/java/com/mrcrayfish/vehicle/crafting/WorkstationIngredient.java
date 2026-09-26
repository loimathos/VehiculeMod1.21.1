package com.mrcrayfish.vehicle.crafting;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.RecordBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

public class WorkstationIngredient
{
    public static final Codec<WorkstationIngredient> CODEC = new Codec<WorkstationIngredient>()
    {
        @Override
        public <T> DataResult<Pair<WorkstationIngredient, T>> decode(DynamicOps<T> ops, T input)
        {
            int count = ops.get(input, "count")
                .flatMap(ops::getNumberValue)
                .map(Number::intValue)
                .result()
                .orElse(1);

            DataResult<Ingredient> ingResult = ops.get(input, "ingredient")
                .result()
                .map(ingInput -> Ingredient.CODEC.parse(ops, ingInput))
                .orElseGet(() -> {
                    DataResult<Ingredient> directResult = Ingredient.CODEC.parse(ops, input);
                    if (directResult.isSuccess())
                    {
                        return directResult;
                    }
                    T cleanInput = ops.remove(input, "count");
                    return Ingredient.CODEC.parse(ops, cleanInput);
                });

            return ingResult.map(ingredient -> Pair.of(new WorkstationIngredient(ingredient, count), input));
        }

        @Override
        public <T> DataResult<T> encode(WorkstationIngredient input, DynamicOps<T> ops, T prefix)
        {
            DataResult<T> ingResult = Ingredient.CODEC.encodeStart(ops, input.getIngredient());
            return ingResult.flatMap(encodedIng -> {
                DataResult<T> mapResult = ops.mergeToMap(encodedIng, ops.createString("count"), ops.createInt(input.getCount()));
                if (mapResult.isSuccess())
                {
                    return mapResult;
                }
                RecordBuilder<T> record = ops.mapBuilder();
                record.add("ingredient", encodedIng);
                record.add("count", ops.createInt(input.getCount()));
                return record.build(prefix);
            });
        }
    };

    public static final StreamCodec<RegistryFriendlyByteBuf, WorkstationIngredient> STREAM_CODEC = StreamCodec.of(
        (buf, val) -> val.toNetwork(buf),
        WorkstationIngredient::fromNetwork
    );

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
        return this.ingredient.test(stack);
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
