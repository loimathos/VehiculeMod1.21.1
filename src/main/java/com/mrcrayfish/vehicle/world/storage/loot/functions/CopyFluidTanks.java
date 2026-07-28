package com.mrcrayfish.vehicle.world.storage.loot.functions;


import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import com.mrcrayfish.vehicle.init.ModLootFunctions;
import com.mrcrayfish.vehicle.blockentity.IFluidTankWriter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;

/**
 * Author: MrCrayfish
 */
public class CopyFluidTanks extends LootItemConditionalFunction
{
    public static final com.mojang.serialization.MapCodec<CopyFluidTanks> CODEC = com.mojang.serialization.MapCodec.unit(new CopyFluidTanks(java.util.List.of()));

    private CopyFluidTanks(java.util.List<LootItemCondition> conditionsIn)
    {
        super(conditionsIn);
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context)
    {
        BlockState state = context.getParamOrNull(LootContextParams.BLOCK_STATE);
        if(state != null && stack.getItem() == state.getBlock().asItem())
        {
            BlockEntity blockEntity = context.getParamOrNull(LootContextParams.BLOCK_ENTITY);
            if(blockEntity != null)
            {
                CompoundTag blockEntityTag = new CompoundTag();
                LazyOptional<IFluidHandler> handler = blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER);
                handler.ifPresent(h ->
                {
                    if(h instanceof FluidTank tank && !tank.isEmpty())
                    {
                        tank.writeToNBT(blockEntityTag);
                    }
                });
                if(blockEntity instanceof IFluidTankWriter)
                {
                    IFluidTankWriter writer = (IFluidTankWriter) blockEntity;
                    if(!writer.areTanksEmpty())
                    {
                        writer.writeTanks(blockEntityTag);
                    }
                }

                if(!blockEntityTag.isEmpty())
                {
                    CompoundTag compound = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
                    if(compound == null)
                    {
                        compound = new CompoundTag();
                    }
                    compound.put("BlockEntityTag", blockEntityTag);
                    stack.set(DataComponents.CUSTOM_DATA, CustomData.of(compound));
                }
            }
        }
        return stack;
    }

    @Override
    public LootItemFunctionType getType()
    {
        return ModLootFunctions.COPY_FLUID_TANKS;
    }

    public static CopyFluidTanks.Builder copyFluidTanks()
    {
        return new CopyFluidTanks.Builder();
    }

    public static class Builder extends LootItemConditionalFunction.Builder<CopyFluidTanks.Builder>
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

}
