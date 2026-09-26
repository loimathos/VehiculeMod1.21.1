package com.mrcrayfish.vehicle.init;

import com.mrcrayfish.vehicle.Reference;
import com.mrcrayfish.vehicle.fluid.BlazeJuice;
import com.mrcrayfish.vehicle.fluid.EnderSap;
import com.mrcrayfish.vehicle.fluid.Fuelium;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Consumer;

/**
 * Author: MrCrayfish
 */
public class ModFluids
{
    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, Reference.MOD_ID);
    public static final DeferredRegister<Fluid> REGISTER = DeferredRegister.create(ForgeRegistries.FLUIDS, Reference.MOD_ID);

    public static final RegistryObject<FluidType> FUELIUM_TYPE = FLUID_TYPES.register("fuelium", () -> new BaseFluidType("fuelium"));
    public static final RegistryObject<Fluid> FUELIUM = REGISTER.register("fuelium", Fuelium.Source::new);
    public static final RegistryObject<FlowingFluid> FLOWING_FUELIUM = REGISTER.register("flowing_fuelium", Fuelium.Flowing::new);

    public static final RegistryObject<FluidType> ENDER_SAP_TYPE = FLUID_TYPES.register("ender_sap", () -> new BaseFluidType("ender_sap"));
    public static final RegistryObject<Fluid> ENDER_SAP = REGISTER.register("ender_sap", EnderSap.Source::new);
    public static final RegistryObject<FlowingFluid> FLOWING_ENDER_SAP = REGISTER.register("flowing_ender_sap", EnderSap.Flowing::new);

    public static final RegistryObject<FluidType> BLAZE_JUICE_TYPE = FLUID_TYPES.register("blaze_juice", () -> new BaseFluidType("blaze_juice"));
    public static final RegistryObject<Fluid> BLAZE_JUICE = REGISTER.register("blaze_juice", BlazeJuice.Source::new);
    public static final RegistryObject<FlowingFluid> FLOWING_BLAZE_JUICE = REGISTER.register("flowing_blaze_juice", BlazeJuice.Flowing::new);

    public static class BaseFluidType extends FluidType
    {
        private final ResourceLocation stillTexture;
        private final ResourceLocation flowingTexture;
        private final ResourceLocation overlayTexture;

        public BaseFluidType(String name)
        {
            super(FluidType.Properties.create());
            this.stillTexture = ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "block/" + name + "_still");
            this.flowingTexture = ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "block/" + name + "_flowing");
            this.overlayTexture = ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "block/" + name + "_overlay");
        }

        @Override
        public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer)
        {
            consumer.accept(new IClientFluidTypeExtensions()
            {
                @Override
                public ResourceLocation getStillTexture()
                {
                    return stillTexture;
                }

                @Override
                public ResourceLocation getFlowingTexture()
                {
                    return flowingTexture;
                }

                @Override
                public ResourceLocation getOverlayTexture()
                {
                    return overlayTexture;
                }
            });
        }
    }
}