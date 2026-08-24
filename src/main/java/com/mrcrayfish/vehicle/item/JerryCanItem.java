package com.mrcrayfish.vehicle.item;

import com.mrcrayfish.vehicle.Config;
import com.mrcrayfish.vehicle.util.FluidUtils;
import com.mrcrayfish.vehicle.util.RenderUtil;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;

import net.minecraft.ChatFormatting;

import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;


import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public class JerryCanItem extends Item
{
    private final DecimalFormat FUEL_FORMAT = new DecimalFormat("0.#%");

    private final Supplier<Integer> capacitySupplier;

    public JerryCanItem(Supplier<Integer> capacity, Item.Properties properties)
    {
        super(properties);
        this.capacitySupplier = capacity;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flagIn)
    {
        if(Screen.hasShiftDown())
        {
            tooltip.addAll(RenderUtil.lines(Component.translatable(this.getDescriptionId() + ".info"), 150));
        }
        else
        {
            stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).ifPresent(handler ->
            {
                FluidStack fluidStack = handler.getFluidInTank(0);
                if(!fluidStack.isEmpty())
                {
                    tooltip.add(Component.translatable(fluidStack.getTranslationKey()).withStyle(ChatFormatting.BLUE));
                    tooltip.add(Component.literal(this.getCurrentFuel(stack) + " / " + this.capacitySupplier.get() + "mb").withStyle(ChatFormatting.GRAY));
                }
                else
                {
                    tooltip.add(Component.translatable("item.vehicle.jerry_can.empty").withStyle(ChatFormatting.RED));
                }
            });
            tooltip.add(Component.literal(ChatFormatting.YELLOW + I18n.get("vehicle.info_help")));
        }
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context)
    {
        // This is such ugly code
        BlockEntity tileEntity = context.getLevel().getBlockEntity(context.getClickedPos());
        if(tileEntity != null && context.getPlayer() != null)
        {
            LazyOptional<IFluidHandler> lazyOptional = tileEntity.getCapability(ForgeCapabilities.FLUID_HANDLER, context.getClickedFace());
            if(lazyOptional.isPresent())
            {
                Optional<IFluidHandler> optional = lazyOptional.resolve();
                if(optional.isPresent())
                {
                    IFluidHandler source = optional.get();
                    Optional<IFluidHandlerItem> itemOptional = stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).resolve();
                    if(itemOptional.isPresent())
                    {
                        if(context.getPlayer().isCrouching())
                        {
                            FluidUtils.transferFluid(source, itemOptional.get(), this.getFillRate());
                        }
                        else
                        {
                            FluidUtils.transferFluid(itemOptional.get(), source, this.getFillRate());
                        }
                        return InteractionResult.SUCCESS;
                    }
                }
            }
        }
        return super.onItemUseFirst(stack, context);
    }

    public int getCurrentFuel(ItemStack stack)
    {
        Optional<IFluidHandlerItem> optional = stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).resolve();
        return optional.map(handler -> handler.getFluidInTank(0).getAmount()).orElse(0);
    }

    public int getCapacity()
    {
        return this.capacitySupplier.get();
    }

    public int getFillRate()
    {
        return Config.SERVER.jerryCanFillRate.get();
    }

    @Override
    public boolean isBarVisible(ItemStack stack)
    {
        return this.getCurrentFuel(stack) > 0;
    }

    @Override
    public int getBarWidth(ItemStack stack)
    {
        return Math.round(13.0F * this.getCurrentFuel(stack) / (float) this.capacitySupplier.get());
    }

    @Override
    public int getBarColor(ItemStack stack)
    {
        return 0x00FFFF;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged)
    {
        return slotChanged;
    }

    @Nullable
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt)
    {
        return new ICapabilityProvider() {
            @Override
            public <T> LazyOptional<T> getCapability(net.minecraftforge.common.capabilities.Capability<T> cap, @Nullable net.minecraft.core.Direction side) {
                if(cap == ForgeCapabilities.FLUID_HANDLER_ITEM) {
                    return LazyOptional.of(() -> new IFluidHandlerItem() {
                        private FluidStack fluid = FluidStack.EMPTY;

                        @Override
                        public ItemStack getContainer() {
                            return stack;
                        }

                        @Override
                        public int getTanks() {
                            return 1;
                        }

                        @Override
                        public FluidStack getFluidInTank(int tank) {
                            return fluid;
                        }

                        @Override
                        public int getTankCapacity(int tank) {
                            return capacitySupplier.get();
                        }

                        @Override
                        public boolean isFluidValid(int tank, FluidStack fluidStack) {
                            return true;
                        }

                        @Override
                        public int fill(FluidStack resource, IFluidHandler.FluidAction action) {
                            if(resource.isEmpty() || !isFluidValid(0, resource)) return 0;
                            int capacity = capacitySupplier.get();
                            if(fluid.isEmpty()) {
                                int fillAmount = Math.min(capacity, resource.getAmount());
                                if(action.execute()) {
                                    fluid = new FluidStack(resource, fillAmount);
                                }
                                return fillAmount;
                            }
                            if(!fluid.isFluidEqual(resource)) return 0;
                            int fillAmount = Math.min(capacity - fluid.getAmount(), resource.getAmount());
                            if(action.execute()) {
                                fluid.grow(fillAmount);
                            }
                            return fillAmount;
                        }

                        @Override
                        public FluidStack drain(FluidStack resource, IFluidHandler.FluidAction action) {
                            if(resource.isEmpty() || !resource.isFluidEqual(fluid)) return FluidStack.EMPTY;
                            return drain(resource.getAmount(), action);
                        }

                        @Override
                        public FluidStack drain(int maxDrain, IFluidHandler.FluidAction action) {
                            if(fluid.isEmpty() || maxDrain <= 0) return FluidStack.EMPTY;
                            int drained = Math.min(fluid.getAmount(), maxDrain);
                            FluidStack result = new FluidStack(fluid, drained);
                            if(action.execute()) {
                                fluid.shrink(drained);
                                if(fluid.isEmpty()) fluid = FluidStack.EMPTY;
                            }
                            return result;
                        }
                    }).cast();
                }
                return LazyOptional.empty();
            }
        };
    }
}
