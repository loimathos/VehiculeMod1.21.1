package com.mrcrayfish.vehicle.datagen;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.mrcrayfish.vehicle.Reference;
import com.mrcrayfish.vehicle.init.ModBlocks;
import com.mrcrayfish.vehicle.world.storage.loot.functions.CopyFluidTanks;
import net.minecraft.world.level.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.functions.CopyComponentsFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.core.registries.BuiltInRegistries;

/**
 * Author: MrCrayfish
 */
public class LootTableGen extends LootTableProvider
{
    public LootTableGen(net.minecraft.data.PackOutput output, java.util.concurrent.CompletableFuture<net.minecraft.core.HolderLookup.Provider> registries)
    {
        super(output, java.util.Set.of(), java.util.List.of(new LootTableProvider.SubProviderEntry(BlockProvider::new, LootContextParamSets.BLOCK)), registries);
    }

    private static class BlockProvider extends BlockLootSubProvider
    {
        protected BlockProvider(net.minecraft.core.HolderLookup.Provider registries)
        {
            super(java.util.Set.of(), net.minecraft.world.flag.FeatureFlags.REGISTRY.allFlags(), registries);
        }

        @Override
        public void generate()
        {
            this.add(ModBlocks.FLUID_EXTRACTOR.get(), this::createFluidTankDrop);
            this.add(ModBlocks.FLUID_MIXER.get(), this::createFluidTankDrop);
            this.add(ModBlocks.FUEL_DRUM.get(), this::createFluidTankDrop);
            this.add(ModBlocks.INDUSTRIAL_FUEL_DRUM.get(), this::createFluidTankDrop);
            this.dropSelf(ModBlocks.FLUID_PIPE.get());
            this.dropSelf(ModBlocks.FLUID_PUMP.get());
            this.dropSelf(ModBlocks.GAS_PUMP.get());
            this.dropSelf(ModBlocks.TRAFFIC_CONE.get());
            this.dropSelf(ModBlocks.WORKSTATION.get());
            this.dropSelf(ModBlocks.JACK.get());
            this.dropSelf(ModBlocks.JACK_HEAD.get());
            this.add(ModBlocks.VEHICLE_CRATE.get(), this::createVehicleCrateDrop);
        }

        @Override
        protected Iterable<Block> getKnownBlocks()
        {
            return ForgeRegistries.BLOCKS.getValues().stream().filter(block -> BuiltInRegistries.BLOCK.getKey(block) != null && Reference.MOD_ID.equals(BuiltInRegistries.BLOCK.getKey(block).getNamespace())).collect(Collectors.toSet());
        }

        protected LootTable.Builder createFluidTankDrop(Block block)
        {
            return LootTable.lootTable().withPool(this.applyExplosionCondition(block, LootPool.lootPool().setRolls(ConstantValue.exactly(1)).add(LootItem.lootTableItem(block).apply(CopyFluidTanks.copyFluidTanks()))));
        }

        protected LootTable.Builder createVehicleCrateDrop(Block block)
        {
            return LootTable.lootTable().withPool(this.applyExplosionCondition(block, LootPool.lootPool().setRolls(ConstantValue.exactly(1)).add(LootItem.lootTableItem(block).apply(net.minecraft.world.level.storage.loot.functions.CopyComponentsFunction.copyComponents(net.minecraft.world.level.storage.loot.functions.CopyComponentsFunction.Source.BLOCK_ENTITY).include(net.minecraft.core.component.DataComponents.CUSTOM_DATA)))));
        }
    }
}
