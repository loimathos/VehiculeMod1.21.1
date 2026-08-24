package com.mrcrayfish.vehicle.init;

import com.mrcrayfish.vehicle.Reference;
import com.mrcrayfish.vehicle.VehicleMod;
import com.mrcrayfish.vehicle.block.*;
import com.mrcrayfish.vehicle.item.FluidPipeItem;
import com.mrcrayfish.vehicle.item.ItemTrafficCone;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
// // Removed in 1.21.1
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public class ModBlocks
{
    public static final DeferredRegister<Block> REGISTER = DeferredRegister.create(ForgeRegistries.BLOCKS, Reference.MOD_ID);

    public static final RegistryObject<Block> TRAFFIC_CONE = register("traffic_cone", TrafficConeBlock::new, ItemTrafficCone::new);
    public static final RegistryObject<Block> FLUID_EXTRACTOR = register("fluid_extractor", FluidExtractorBlock::new);
    public static final RegistryObject<Block> FLUID_MIXER = register("fluid_mixer", FluidMixerBlock::new);
    public static final RegistryObject<Block> GAS_PUMP = register("gas_pump", GasPumpBlock::new);
    public static final RegistryObject<Block> FLUID_PIPE = register("fluid_pipe", FluidPipeBlock::new, FluidPipeItem::new);
    public static final RegistryObject<Block> FLUID_PUMP = register("fluid_pump", FluidPumpBlock::new, FluidPipeItem::new);
    public static final RegistryObject<FuelDrumBlock> FUEL_DRUM = register("fuel_drum", FuelDrumBlock::new);
    public static final RegistryObject<FuelDrumBlock> INDUSTRIAL_FUEL_DRUM = register("industrial_fuel_drum", IndustrialFuelDrumBlock::new);
    public static final RegistryObject<Block> WORKSTATION = register("workstation", WorkstationBlock::new);
    public static final RegistryObject<Block> VEHICLE_CRATE = register("vehicle_crate", VehicleCrateBlock::new, block -> new BlockItem(block, new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Block> JACK = register("jack", JackBlock::new);
    public static final RegistryObject<Block> JACK_HEAD = register("jack_head", JackHeadBlock::new, null);
    public static final RegistryObject<LiquidBlock> FUELIUM = register("fuelium", () -> new LiquidBlock(ModFluids.FLOWING_FUELIUM.get(), BlockBehaviour.Properties.of().noCollission().strength(100.0F).noLootTable()), null);
    public static final RegistryObject<LiquidBlock> ENDER_SAP = register("ender_sap", () -> new LiquidBlock(ModFluids.FLOWING_ENDER_SAP.get(), BlockBehaviour.Properties.of().noCollission().strength(100.0F).noLootTable()), null);
    public static final RegistryObject<LiquidBlock> BLAZE_JUICE = register("blaze_juice", () -> new LiquidBlock(ModFluids.FLOWING_BLAZE_JUICE.get(), BlockBehaviour.Properties.of().noCollission().strength(100.0F).noLootTable()), null);
    //public static final Block BOOST_PAD = registerConstructor(new BlockBoostPad(), null);
    //public static final Block BOOST_RAMP = registerConstructor(new BlockBoostRamp(), null); //ItemBoostRamp::new
    //public static final Block STEEP_BOOST_RAMP = registerConstructor(new BlockSteepBoostRamp(), null);

    private static <T extends Block> RegistryObject<T> register(String id, Supplier<T> blockSupplier)
    {
        return register(id, blockSupplier, block1 -> new BlockItem(block1, new Item.Properties()));
    }

    private static <T extends Block> RegistryObject<T> register(String id, Supplier<T> blockSupplier, @Nullable Function<T, BlockItem> supplier)
    {
        RegistryObject<T> registryObject = ModBlocks.REGISTER.register(id, blockSupplier);
        if(supplier != null)
        {
            ModItems.REGISTER.register(id, () -> supplier.apply(registryObject.get()));
        }
        return registryObject;
    }
}