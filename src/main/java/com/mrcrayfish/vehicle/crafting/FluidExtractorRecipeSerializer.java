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
        ItemStack.CODEC.fieldOf("ingredient").forGetter(FluidExtractorRecipe::getIngredient),
        FluidEntry.CODEC.fieldOf("result").forGetter(FluidExtractorRecipe::getResult)
    ).apply(builder, FluidExtractorRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, FluidExtractorRecipe> STREAM_CODEC = StreamCodec.of(
        (buf, recipe) -> {
            ItemStack.STREAM_CODEC.encode(buf, recipe.getIngredient());
            FluidEntry.STREAM_CODEC.encode(buf, recipe.getResult());
        },
        (buf) -> {
            ItemStack ingredient = ItemStack.STREAM_CODEC.decode(buf);
            FluidEntry result = FluidEntry.STREAM_CODEC.decode(buf);
            return new FluidExtractorRecipe(ingredient, result);
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
