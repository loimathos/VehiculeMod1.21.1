package com.mrcrayfish.vehicle.client;

import net.minecraft.nbt.Tag;

import com.mrcrayfish.vehicle.Reference;
import com.mrcrayfish.vehicle.client.handler.CameraHandler;
import com.mrcrayfish.vehicle.client.handler.ControllerHandler;
import com.mrcrayfish.vehicle.client.handler.FuelingHandler;
import com.mrcrayfish.vehicle.client.handler.HeldVehicleHandler;
import com.mrcrayfish.vehicle.client.handler.InputHandler;
import com.mrcrayfish.vehicle.client.handler.OverlayHandler;
import com.mrcrayfish.vehicle.client.handler.PlayerModelHandler;
import com.mrcrayfish.vehicle.client.handler.SprayCanHandler;
import com.mrcrayfish.vehicle.client.model.ComponentLoader;
import com.mrcrayfish.vehicle.client.model.ComponentManager;
import com.mrcrayfish.vehicle.client.particle.DustParticle;
import com.mrcrayfish.vehicle.client.particle.TyreSmokeParticle;
import com.mrcrayfish.vehicle.client.raytrace.EntityRayTracer;
import com.mrcrayfish.vehicle.client.render.blockentity.FluidExtractorRenderer;
import com.mrcrayfish.vehicle.client.render.blockentity.FluidPumpRenderer;
import com.mrcrayfish.vehicle.client.render.blockentity.FuelDrumRenderer;
import com.mrcrayfish.vehicle.client.render.blockentity.GasPumpRenderer;
import com.mrcrayfish.vehicle.client.render.blockentity.GasPumpTankRenderer;
import com.mrcrayfish.vehicle.client.render.blockentity.VehicleCrateRenderer;
import com.mrcrayfish.vehicle.client.render.vehicle.*;
import com.mrcrayfish.vehicle.client.screen.EditVehicleScreen;
import com.mrcrayfish.vehicle.client.screen.FluidExtractorScreen;
import com.mrcrayfish.vehicle.client.screen.FluidMixerScreen;
import com.mrcrayfish.vehicle.client.screen.StorageScreen;
import com.mrcrayfish.vehicle.client.screen.WorkstationScreen;
import com.mrcrayfish.vehicle.entity.trailer.FertilizerTrailerEntity;
import com.mrcrayfish.vehicle.entity.trailer.FluidTrailerEntity;
import com.mrcrayfish.vehicle.entity.trailer.SeederTrailerEntity;
import com.mrcrayfish.vehicle.entity.trailer.StorageTrailerEntity;
import com.mrcrayfish.vehicle.entity.trailer.VehicleTrailerEntity;
import com.mrcrayfish.vehicle.entity.vehicle.MopedEntity;
import com.mrcrayfish.vehicle.entity.vehicle.SportsCarEntity;
import com.mrcrayfish.vehicle.init.ModBlocks;
import com.mrcrayfish.vehicle.init.ModContainers;
import com.mrcrayfish.vehicle.init.ModEntities;
import com.mrcrayfish.vehicle.init.ModFluids;
import com.mrcrayfish.vehicle.init.ModParticleTypes;
import com.mrcrayfish.vehicle.init.ModBlockEntities;
import com.mrcrayfish.vehicle.item.PartItem;
import com.mrcrayfish.vehicle.item.SprayCanItem;
import com.mrcrayfish.vehicle.util.FluidUtils;
import com.mrcrayfish.vehicle.util.VehicleUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
//import net.minecraft.client.renderer.ItemBlockRenderTypes; // Removed in 1.21.1
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Unit;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.AddGuiOverlayLayersEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Author: MrCrayfish
 */
@Mod.EventBusSubscriber(modid = Reference.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientHandler
{
    private static boolean controllableLoaded = false;

    public static boolean isControllableLoaded()
    {
        return controllableLoaded;
    }

    public static void setup(FMLClientSetupEvent event)
    {
        if(ModList.get().isLoaded("controllable"))
        {
            ClientHandler.controllableLoaded = true;
            MinecraftForge.EVENT_BUS.register(new ControllerHandler());
            ControllerHandler.init();
        }

        MinecraftForge.EVENT_BUS.register(EntityRayTracer.instance());
        MinecraftForge.EVENT_BUS.register(CosmeticCache.instance());
        MinecraftForge.EVENT_BUS.register(CameraHandler.instance());
        MinecraftForge.EVENT_BUS.register(new FuelingHandler());
        MinecraftForge.EVENT_BUS.register(new HeldVehicleHandler());
        MinecraftForge.EVENT_BUS.register(new InputHandler());
        MinecraftForge.EVENT_BUS.register(new PlayerModelHandler());
        MinecraftForge.EVENT_BUS.register(new SprayCanHandler());
        MinecraftForge.EVENT_BUS.register(new ClientEvents());

        setupCustomBlockModels();
        setupInteractableVehicles();

        // FMLClientSetupEvent is dispatched on Forge's parallel executor, so anything that mutates
        // shared client state has to be pushed onto the main thread. MenuScreens.SCREENS and the
        // ItemBlockRenderTypes maps are plain HashMaps with no thread guards - writing to them off
        // -thread is unsafe publication and shows up later as "Failed to create screen for menu".
        event.enqueueWork(() ->
        {
            setupRenderLayers();
            setupScreenFactories();

            ResourceManager manager = Minecraft.getInstance().getResourceManager();
            if(manager instanceof ReloadableResourceManager)
            {
                ((ReloadableResourceManager) manager).registerReloadListener((stage, resourceManager, preparationsProfiler, reloadProfiler, backgroundExecutor, gameExecutor) -> {
                    return stage.wait(Unit.INSTANCE).thenRun(() -> {
                        FluidUtils.clearCacheFluidColor();
                        EntityRayTracer.instance().clearDataForReregistration();
                        ComponentManager.clearCache();
                    });
                });
            }
        });
    }

    private static void setupCustomBlockModels()
    {
        //TODO add custom loader
        //ModelLoaderRegistry.registerLoader(new CustomLoader());
        //ModelLoaderRegistry.registerLoader(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "ramp"), new CustomLoader());
    }

    private static void setupRenderLayers()
    {
        // ItemBlockRenderTypes was NOT removed in 1.21.1 - both setRenderLayer overloads still exist.
        // These used to go through a no-op stub, which is why every cutout block rendered solid
        // (transparent pixels came out opaque black) and none of the fluids were translucent.
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.WORKSTATION.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.FLUID_EXTRACTOR.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.GAS_PUMP.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModFluids.FUELIUM.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModFluids.FLOWING_FUELIUM.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModFluids.ENDER_SAP.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModFluids.FLOWING_ENDER_SAP.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModFluids.BLAZE_JUICE.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModFluids.FLOWING_BLAZE_JUICE.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.FUEL_DRUM.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.INDUSTRIAL_FUEL_DRUM.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.TRAFFIC_CONE.get(), RenderType.cutout());
    }

    @SubscribeEvent
    public static void onAddGuiOverlayLayers(AddGuiOverlayLayersEvent event)
    {
        event.getLayeredDraw().add(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "stats"), OverlayHandler::render);
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event)
    {
        com.mrcrayfish.vehicle.entity.properties.VehicleProperties.loadDefaultProperties();
        setupVehicleRenders(event);
        setupBlockEntityRenderers(event);
        event.registerEntityRenderer(ModEntities.JACK.get(), com.mrcrayfish.vehicle.client.render.JackRenderer::new);
    }

    private static void setupVehicleRenders(EntityRenderersEvent.RegisterRenderers event)
    {
        /* Register Vehicles */
        VehicleUtil.registerVehicleRenderer(event, ModEntities.QUAD_BIKE.get(), QuadBikeRenderer::new);
        VehicleUtil.registerVehicleRenderer(event, ModEntities.SPORTS_CAR.get(), SportsCarRenderer::new);
        VehicleUtil.registerVehicleRenderer(event, ModEntities.GO_KART.get(), GoKartRenderer::new);
        VehicleUtil.registerVehicleRenderer(event, ModEntities.JET_SKI.get(), JetSkiRenderer::new);
        VehicleUtil.registerVehicleRenderer(event, ModEntities.LAWN_MOWER.get(), LawnMowerRenderer::new);
        VehicleUtil.registerVehicleRenderer(event, ModEntities.MOPED.get(), MopedRenderer::new);
        VehicleUtil.registerVehicleRenderer(event, ModEntities.SPORTS_PLANE.get(), SportsPlaneRenderer::new);
        VehicleUtil.registerVehicleRenderer(event, ModEntities.GOLF_CART.get(), GolfCartRenderer::new);
        VehicleUtil.registerVehicleRenderer(event, ModEntities.OFF_ROADER.get(), OffRoaderRenderer::new);
        VehicleUtil.registerVehicleRenderer(event, ModEntities.TRACTOR.get(), TractorRenderer::new);
        VehicleUtil.registerVehicleRenderer(event, ModEntities.MINI_BUS.get(), MiniBusRenderer::new);
        VehicleUtil.registerVehicleRenderer(event, ModEntities.DIRT_BIKE.get(), DirtBikeRenderer::new);
        VehicleUtil.registerVehicleRenderer(event, ModEntities.COMPACT_HELICOPTER.get(), CompactHelicopterRenderer::new);

        /* Register Trailers */
        VehicleUtil.registerVehicleRenderer(event, ModEntities.VEHICLE_TRAILER.get(), VehicleTrailerRenderer::new);
        VehicleUtil.registerVehicleRenderer(event, ModEntities.STORAGE_TRAILER.get(), StorageTrailerRenderer::new);
        VehicleUtil.registerVehicleRenderer(event, ModEntities.FLUID_TRAILER.get(), FluidTrailerRenderer::new);
        VehicleUtil.registerVehicleRenderer(event, ModEntities.SEEDER.get(), SeederTrailerRenderer::new);
        VehicleUtil.registerVehicleRenderer(event, ModEntities.FERTILIZER.get(), FertilizerTrailerRenderer::new);

        /* Register Mod Exclusive Vehicles */
        if(ModList.get().isLoaded("cfm"))
        {
            VehicleUtil.registerVehicleRenderer(event, ModEntities.SOFACOPTER.get(), SofaHelicopterRenderer::new);
        }
    }

    private static void setupBlockEntityRenderers(EntityRenderersEvent.RegisterRenderers event)
    {
        event.registerBlockEntityRenderer(ModBlockEntities.FLUID_EXTRACTOR.get(), FluidExtractorRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.FUEL_DRUM.get(), FuelDrumRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.INDUSTRIAL_FUEL_DRUM.get(), FuelDrumRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.VEHICLE_CRATE.get(), VehicleCrateRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.JACK.get(), com.mrcrayfish.vehicle.client.render.blockentity.JackRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.GAS_PUMP.get(), GasPumpRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.GAS_PUMP_TANK.get(), GasPumpTankRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.FLUID_PUMP.get(), FluidPumpRenderer::new);
    }

    private static void setupScreenFactories()
    {
        MenuScreens.register(ModContainers.FLUID_EXTRACTOR.get(), FluidExtractorScreen::new);
        MenuScreens.register(ModContainers.FLUID_MIXER.get(), FluidMixerScreen::new);
        MenuScreens.register(ModContainers.EDIT_VEHICLE.get(), EditVehicleScreen::new);
        MenuScreens.register(ModContainers.WORKSTATION.get(), WorkstationScreen::new);
        MenuScreens.register(ModContainers.STORAGE.get(), StorageScreen::new);
    }

    /**
     * Item colours belong on this MOD-bus event, not on FMLClientSetupEvent. Reaching for
     * Minecraft.getInstance().getItemColors() during parallel setup both races the client and runs
     * before the colour registry is meant to be populated.
     */
    @SubscribeEvent
    public static void onRegisterItemColors(RegisterColorHandlersEvent.Item event)
    {
        ItemColor color = (stack, index) ->
        {
            if(index == 0)
            {
                var customData = stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
                if(customData != null)
                {
                    var tag = customData.copyTag();
                    if(tag.contains("Color", Tag.TAG_INT))
                    {
                        return tag.getInt("Color");
                    }
                }
            }
            return 0xFFFFFF;
        };

        ForgeRegistries.ITEMS.forEach(item ->
        {
            if(item instanceof SprayCanItem || (item instanceof PartItem && ((PartItem) item).isColored()))
            {
                event.register(color, item);
            }
        });
    }

    private static void setupInteractableVehicles()
    {
        MopedEntity.registerInteractionBoxes();
        FertilizerTrailerEntity.registerInteractionBoxes();
        FluidTrailerEntity.registerInteractionBoxes();
        SeederTrailerEntity.registerInteractionBoxes();
        StorageTrailerEntity.registerInteractionBoxes();
        VehicleTrailerEntity.registerInteractionBoxes();
        SportsCarEntity.registerInteractionBoxes();
    }

    @SubscribeEvent
    public static void registerParticleProviders(net.minecraftforge.client.event.RegisterParticleProvidersEvent event)
    {
        event.registerSpriteSet(ModParticleTypes.TYRE_SMOKE.get(), TyreSmokeParticle.Factory::new);
        event.registerSpriteSet(ModParticleTypes.DUST.get(), DustParticle.Factory::new);
    }
}
