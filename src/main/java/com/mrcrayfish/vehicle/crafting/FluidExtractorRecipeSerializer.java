package com.mrcrayfish.vehicle.crafting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class FluidExtractorRecipeSerializer implements RecipeSerializer<FluidExtractorRecipe>
{
    public static final MapCodec<FluidExtractorRecipe> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
        ItemStack.CODEC.fieldOf("ingredient").forGetter(FluidExtractorRecipe::getIngredient)
    ).apply(builder, (ingredient) -> new FluidExtractorRecipe(ingredient, null)));

    public static final StreamCodec<RegistryFriendlyByteBuf, FluidExtractorRecipe> STREAM_CODEC = StreamCodec.of(
        (buf, recipe) -> {
            ItemStack.STREAM_CODEC.encode(buf, recipe.getIngredient());
        },
        (buf) -> {
            ItemStack ingredient = ItemStack.STREAM_CODEC.decode(buf);
            return new FluidExtractorRecipe(ingredient, null);
        }
    );

    @Override
    public MapCodec<FluidExtractorRecipe> codec()
    {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, FluidExtractorRecipe> streamCodec()
    {
        return STREAM_CODEC;
    }
}
