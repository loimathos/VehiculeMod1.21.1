package com.mrcrayfish.vehicle.crafting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.ArrayList;
import java.util.List;

public class FluidMixerRecipeSerializer implements RecipeSerializer<FluidMixerRecipe>
{
    public static final MapCodec<FluidMixerRecipe> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
        FluidEntry.CODEC.listOf().fieldOf("input").forGetter(FluidMixerRecipe::getInputsList),
        ItemStack.CODEC.fieldOf("ingredient").forGetter(FluidMixerRecipe::getIngredient),
        FluidEntry.CODEC.fieldOf("result").forGetter(FluidMixerRecipe::getResult)
    ).apply(builder, FluidMixerRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, FluidMixerRecipe> STREAM_CODEC = StreamCodec.of(
        (buf, recipe) -> {
            List<FluidEntry> inputs = recipe.getInputsList();
            buf.writeVarInt(inputs.size());
            for(FluidEntry input : inputs)
            {
                FluidEntry.STREAM_CODEC.encode(buf, input);
            }
            ItemStack.STREAM_CODEC.encode(buf, recipe.getIngredient());
            FluidEntry.STREAM_CODEC.encode(buf, recipe.getResult());
        },
        (buf) -> {
            int inputCount = buf.readVarInt();
            List<FluidEntry> inputs = new ArrayList<>(inputCount);
            for(int i = 0; i < inputCount; i++)
            {
                inputs.add(FluidEntry.STREAM_CODEC.decode(buf));
            }
            ItemStack ingredient = ItemStack.STREAM_CODEC.decode(buf);
            FluidEntry result = FluidEntry.STREAM_CODEC.decode(buf);
            return new FluidMixerRecipe(inputs, ingredient, result);
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
