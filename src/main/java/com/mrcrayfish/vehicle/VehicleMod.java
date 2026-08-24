package com.mrcrayfish.vehicle;

import com.mrcrayfish.vehicle.client.ClientHandler;
import com.mrcrayfish.vehicle.client.model.ComponentManager;
import com.mrcrayfish.vehicle.client.model.VehicleModels;
import com.mrcrayfish.vehicle.common.CommonEvents;
import com.mrcrayfish.vehicle.common.FluidNetworkHandler;
import com.mrcrayfish.vehicle.common.entity.HeldVehicleDataHandler;
import com.mrcrayfish.vehicle.crafting.RecipeType;
import com.mrcrayfish.vehicle.crafting.WorkstationIngredient;
import com.mrcrayfish.vehicle.datagen.LootTableGen;
import com.mrcrayfish.vehicle.datagen.RecipeGen;
import com.mrcrayfish.vehicle.datagen.VehiclePropertiesGen;
import com.mrcrayfish.vehicle.entity.properties.ExtendedProperties;
import com.mrcrayfish.vehicle.entity.properties.HelicopterProperties;
import com.mrcrayfish.vehicle.entity.properties.LandProperties;
import com.mrcrayfish.vehicle.entity.properties.MotorcycleProperties;
import com.mrcrayfish.vehicle.entity.properties.PlaneProperties;
import com.mrcrayfish.vehicle.entity.properties.PoweredProperties;
import com.mrcrayfish.vehicle.entity.properties.TrailerProperties;
import com.mrcrayfish.vehicle.entity.properties.VehicleProperties;
import com.mrcrayfish.vehicle.init.*;
import com.mrcrayfish.vehicle.network.PacketHandler;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Author: MrCrayfish
 */
@Mod(Reference.MOD_ID)
public class VehicleMod
{
    public static final Logger LOGGER = LogManager.getLogger(Reference.MOD_ID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(net.minecraft.core.registries.Registries.CREATIVE_MODE_TAB, Reference.MOD_ID);
    public static final RegistryObject<CreativeModeTab> CREATIVE_TAB = CREATIVE_MODE_TABS.register("tab", () -> CreativeModeTab.builder().title(net.minecraft.network.chat.Component.translatable("itemGroup.vehicle")).icon(() -> new ItemStack(ModItems.IRON_SMALL_ENGINE.get())).build());

    public VehicleMod()
    {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        CREATIVE_MODE_TABS.register(eventBus);
        ModBlocks.REGISTER.register(eventBus);
        ModItems.REGISTER.register(eventBus);
        ModEntities.REGISTER.register(eventBus);
        ModBlockEntities.REGISTER.register(eventBus);
        ModContainers.REGISTER.register(eventBus);
        ModParticleTypes.REGISTER.register(eventBus);
        ModSounds.REGISTER.register(eventBus);
        ModRecipeSerializers.REGISTER.register(eventBus);
        RecipeType.REGISTER.register(eventBus);
        ModFluids.FLUID_TYPES.register(eventBus);
        ModFluids.REGISTER.register(eventBus);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.serverSpec);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.clientSpec);
        eventBus.addListener(this::onCommonSetup);
        eventBus.addListener(this::onClientSetup);
        eventBus.addListener(this::onGatherData);
        MinecraftForge.EVENT_BUS.register(new CommonEvents());
        MinecraftForge.EVENT_BUS.register(new ModCommands());
        MinecraftForge.EVENT_BUS.register(FluidNetworkHandler.instance());
        ExtendedProperties.register(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "powered"), PoweredProperties.class, PoweredProperties::new);
        ExtendedProperties.register(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "land"), LandProperties.class, LandProperties::new);
        ExtendedProperties.register(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "motorcycle"), MotorcycleProperties.class, MotorcycleProperties::new);
        ExtendedProperties.register(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "plane"), PlaneProperties.class, PlaneProperties::new);
        ExtendedProperties.register(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "helicopter"), HelicopterProperties.class, HelicopterProperties::new);
        ExtendedProperties.register(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "trailer"), TrailerProperties.class, TrailerProperties::new);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ComponentManager.registerLoader(VehicleModels.LOADER));
    }

    private void onCommonSetup(FMLCommonSetupEvent event)
    {
        VehicleProperties.loadDefaultProperties();
        PacketHandler.registerPlayMessage();
        HeldVehicleDataHandler.register();
        ModDataKeys.register();
        ModLootFunctions.init();
        event.enqueueWork(() -> VehicleProperties.registerDynamicProvider(() -> new VehiclePropertiesGen(null)));
    }

    private void onClientSetup(FMLClientSetupEvent event)
    {
        ClientHandler.setup(event);
    }

    private void onGatherData(GatherDataEvent event)
    {
        // DataGenerator generator = event.getGenerator();
        // generator.addProvider(new LootTableGen(generator));
        // generator.addProvider(new RecipeGen(generator));
        // generator.addProvider(new VehiclePropertiesGen(generator));
    }
}
