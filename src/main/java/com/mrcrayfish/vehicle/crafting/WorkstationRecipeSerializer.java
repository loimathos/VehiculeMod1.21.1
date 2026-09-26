package com.mrcrayfish.vehicle.crafting;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class WorkstationRecipeSerializer implements RecipeSerializer<WorkstationRecipe>
{
    public static final MapCodec<WorkstationRecipe> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
        BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("vehicle").forGetter(WorkstationRecipe::getVehicle),
        WorkstationIngredient.CODEC.listOf().fieldOf("materials").forGetter(WorkstationRecipe::getMaterials)
    ).apply(builder, WorkstationRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, WorkstationRecipe> STREAM_CODEC = StreamCodec.of(
        (buf, recipe) -> {
            buf.writeResourceLocation(BuiltInRegistries.ENTITY_TYPE.getKey(recipe.getVehicle()));
            buf.writeVarInt(recipe.getMaterials().size());
            for(WorkstationIngredient ingredient : recipe.getMaterials())
            {
                WorkstationIngredient.STREAM_CODEC.encode(buf, ingredient);
            }
        },
        (buf) -> {
            EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(buf.readResourceLocation());
            int count = buf.readVarInt();
            ImmutableList.Builder<WorkstationIngredient> materials = ImmutableList.builder();
            for(int i = 0; i < count; i++)
            {
                materials.add(WorkstationIngredient.STREAM_CODEC.decode(buf));
            }
            return new WorkstationRecipe(entityType, materials.build());
        }
    );

    @Override
    public MapCodec<WorkstationRecipe> codec()
    {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, WorkstationRecipe> streamCodec()
    {
        return STREAM_CODEC;
    }
}
