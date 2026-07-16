package com.mrcrayfish.vehicle.world.storage.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mrcrayfish.vehicle.init.ModLootFunctions;
import com.mrcrayfish.vehicle.blockentity.IFluidTankWriter;
import net.minecraft.world.level.block.BlockState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable.LootFunction;
import net.minecraft.world.level.storage.loot.LootFunctionType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.TileFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;

/**
 * Author: MrCrayfish
 */
public class CopyFluidTanks extends LootFunction
{
    private CopyFluidTanks(ILootCondition[] conditionsIn)
    {
        super(conditionsIn);
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context)
    {
        BlockState state = context.getParamOrNull(LootParameters.BLOCK_STATE);
        if(state != null && stack.getItem() == state.getBlock().asItem())
        {
            BlockEntity blockEntity = context.getParamOrNull(LootParameters.BLOCK_ENTITY);
            if(blockEntity != null)
            {
                CompoundTag blockEntityTag = new CompoundTag();
                if(blockEntity instanceof TileFluidHandler)
                {
                    LazyOptional<IFluidHandler> handler = blockEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY);
                    handler.ifPresent(h ->
                    {
                        FluidTank tank = (FluidTank) h;
                        if(!tank.isEmpty())
                        {
                            tank.writeToNBT(blockEntityTag);
                        }
                    });
                }
                else if(blockEntity instanceof IFluidTankWriter)
                {
                    IFluidTankWriter writer = (IFluidTankWriter) blockEntity;
                    if(!writer.areTanksEmpty())
                    {
                        writer.writeTanks(blockEntityTag);
                    }
                }

                if(!blockEntityTag.isEmpty())
                {
                    CompoundTag compound = stack.getTag();
                    if(compound == null)
                    {
                        compound = new CompoundTag();
                    }
                    compound.put("BlockEntityTag", blockEntityTag);
                    stack.setTag(compound);
                }
            }
        }
        return stack;
    }

    @Override
    public LootFunctionType getType()
    {
        return ModLootFunctions.COPY_FLUID_TANKS;
    }

    public static CopyFluidTanks.Builder copyFluidTanks()
    {
        return new CopyFluidTanks.Builder();
    }

    public static class Builder extends LootFunction.Builder<CopyFluidTanks.Builder>
    {
        private Builder() {}

        protected CopyFluidTanks.Builder getThis()
        {
            return this;
        }

        public LootItemFunction build()
        {
            return new CopyFluidTanks(this.getConditions());
        }
    }

    public static class Serializer extends LootFunction.Serializer<CopyFluidTanks>
    {
        @Override
        public CopyFluidTanks deserialize(JsonObject object, JsonDeserializationContext deserializationContext, ILootCondition[] conditionsIn)
        {
            return new CopyFluidTanks(conditionsIn);
        }
    }
}
