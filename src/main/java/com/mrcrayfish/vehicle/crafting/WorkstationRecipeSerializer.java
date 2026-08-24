package com.mrcrayfish.vehicle.crafting;

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
        BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("vehicle").forGetter(WorkstationRecipe::getVehicle)
    ).apply(builder, (vehicle) -> new WorkstationRecipe(vehicle, java.util.Collections.emptyList())));

    public static final StreamCodec<RegistryFriendlyByteBuf, WorkstationRecipe> STREAM_CODEC = StreamCodec.of(
        (buf, recipe) -> {
            buf.writeResourceLocation(BuiltInRegistries.ENTITY_TYPE.getKey(recipe.getVehicle()));
        },
        (buf) -> {
            EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(buf.readResourceLocation());
            return new WorkstationRecipe(entityType, java.util.Collections.emptyList());
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
