package com.mrcrayfish.vehicle.crafting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class FluidMixerRecipeSerializer implements RecipeSerializer<FluidMixerRecipe>
{
    public static final MapCodec<FluidMixerRecipe> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
        ItemStack.CODEC.fieldOf("ingredient").forGetter(FluidMixerRecipe::getIngredient)
    ).apply(builder, (ingredient) -> new FluidMixerRecipe(ingredient, null)));

    public static final StreamCodec<RegistryFriendlyByteBuf, FluidMixerRecipe> STREAM_CODEC = StreamCodec.of(
        (buf, recipe) -> {
            ItemStack.STREAM_CODEC.encode(buf, recipe.getIngredient());
        },
        (buf) -> {
            ItemStack ingredient = ItemStack.STREAM_CODEC.decode(buf);
            return new FluidMixerRecipe(ingredient, null);
        }
    );

    @Override
    public MapCodec<FluidMixerRecipe> codec()
    {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, FluidMixerRecipe> streamCodec()
    {
        return STREAM_CODEC;
    }
}
