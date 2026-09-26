package com.mrcrayfish.vehicle.crafting;

import com.google.gson.JsonObject;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.core.registries.BuiltInRegistries;

/**
 * Author: MrCrayfish
 */
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class FluidEntry
{
    public static final Codec<FluidEntry> CODEC = RecordCodecBuilder.create(builder -> builder.group(
        BuiltInRegistries.FLUID.byNameCodec().fieldOf("fluid").forGetter(FluidEntry::getFluid),
        Codec.INT.fieldOf("amount").forGetter(FluidEntry::getAmount)
    ).apply(builder, FluidEntry::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, FluidEntry> STREAM_CODEC = StreamCodec.of(
        (buf, entry) -> {
            buf.writeResourceLocation(BuiltInRegistries.FLUID.getKey(entry.getFluid()));
            buf.writeVarInt(entry.getAmount());
        },
        (buf) -> {
            Fluid fluid = BuiltInRegistries.FLUID.get(buf.readResourceLocation());
            int amount = buf.readVarInt();
            return new FluidEntry(fluid, amount);
        }
    );

    private Fluid fluid;
    private int amount;

    public FluidEntry(Fluid fluid, int amount)
    {
        this.fluid = fluid;
        this.amount = amount;
    }

    public Fluid getFluid()
    {
        return this.fluid;
    }

    public int getAmount()
    {
        return this.amount;
    }

    public FluidStack createStack()
    {
        return new FluidStack(this.fluid, this.amount);
    }

    public static FluidEntry fromJson(JsonObject object)
    {
        if(!object.has("fluid") || !object.has("amount"))
        {
            throw new com.google.gson.JsonSyntaxException("Invalid fluid entry, missing fluid and amount");
        }
        ResourceLocation fluidId = ResourceLocation.parse(GsonHelper.getAsString(object, "fluid"));
        Fluid fluid = ForgeRegistries.FLUIDS.getValue(fluidId);
        if(fluid == null)
        {
            throw new com.google.gson.JsonSyntaxException("Invalid fluid entry, unknown fluid: " + fluidId.toString());
        }
        int amount = GsonHelper.getAsInt(object, "amount");
        if(amount < 1)
        {
            throw new com.google.gson.JsonSyntaxException("Invalid fluid entry, amount must be more than zero");
        }
        return new FluidEntry(fluid, amount);
    }

    public JsonObject toJson()
    {
        JsonObject object = new JsonObject();
        object.addProperty("fluid", BuiltInRegistries.FLUID.getKey(fluid).toString());
        object.addProperty("amount", this.amount);
        return object;
    }

    public void write(FriendlyByteBuf buffer)
    {
        buffer.writeUtf(BuiltInRegistries.FLUID.getKey(fluid).toString(), 256);
        buffer.writeInt(this.amount);
    }

    public static FluidEntry read(FriendlyByteBuf buffer)
    {
        Fluid fluid = ForgeRegistries.FLUIDS.getValue(ResourceLocation.parse(buffer.readUtf(256)));
        int amount = buffer.readInt();
        return new FluidEntry(fluid, amount);
    }

    public static FluidEntry of(Fluid fluid, int amount)
    {
        return new FluidEntry(fluid, amount);
    }
}
