package com.mrcrayfish.vehicle.item;

import com.mrcrayfish.vehicle.Config;
import com.mrcrayfish.vehicle.util.FluidUtils;
import com.mrcrayfish.vehicle.util.RenderUtil;
import net.minecraft.client.gui.screens.Screen;
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


import com.mrcrayfish.vehicle.util.CommonUtils;
import net.minecraft.core.Direction;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.capabilities.Capability;
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
            FluidStack fluidStack = FluidStack.EMPTY;
            Optional<IFluidHandlerItem> cap = stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).resolve();
            if(cap.isPresent())
            {
                fluidStack = cap.get().getFluidInTank(0);
            }
            else
            {
                CompoundTag tag = CommonUtils.getOrCreateStackTag(stack);
                if(tag.contains("Fluid", Tag.TAG_COMPOUND))
                {
                    fluidStack = FluidStack.loadFluidStackFromNBT(tag.getCompound("Fluid"));
                }
            }

            if(!fluidStack.isEmpty())
            {
                tooltip.add(Component.translatable(fluidStack.getTranslationKey()).withStyle(ChatFormatting.BLUE));
                tooltip.add(Component.literal(fluidStack.getAmount() + " / " + this.capacitySupplier.get() + "mb").withStyle(ChatFormatting.GRAY));
            }
            else
            {
                tooltip.add(Component.translatable("item.vehicle.jerry_can.empty").withStyle(ChatFormatting.RED));
            }
            tooltip.add(Component.translatable("vehicle.info_help").withStyle(ChatFormatting.YELLOW));
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
        if(optional.isPresent())
        {
            return optional.get().getFluidInTank(0).getAmount();
        }
        CompoundTag tag = CommonUtils.getOrCreateStackTag(stack);
        if(tag.contains("Fluid", Tag.TAG_COMPOUND))
        {
            return tag.getCompound("Fluid").getInt("Amount");
        }
        return 0;
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
    @Override
    public ICapabilityProvider getCapabilityProvider(ItemStack stack)
    {
        return new JerryCanFluidHandler(stack, this.capacitySupplier.get());
    }

    public static class JerryCanFluidHandler implements IFluidHandlerItem, ICapabilityProvider
    {
        private final LazyOptional<IFluidHandlerItem> holder = LazyOptional.of(() -> this);
        protected final ItemStack container;
        protected final int capacity;

        public JerryCanFluidHandler(ItemStack container, int capacity)
        {
            this.container = container;
            this.capacity = capacity;
        }

        @Override
        public ItemStack getContainer()
        {
            return this.container;
        }

        public FluidStack getFluid()
        {
            CompoundTag tag = CommonUtils.getOrCreateStackTag(this.container);
            if(tag.contains("Fluid", Tag.TAG_COMPOUND))
            {
                return FluidStack.loadFluidStackFromNBT(tag.getCompound("Fluid"));
            }
            return FluidStack.EMPTY;
        }

        protected void setFluid(FluidStack fluid)
        {
            CommonUtils.updateStackTag(this.container, tag -> {
                if(fluid.isEmpty())
                {
                    tag.remove("Fluid");
                }
                else
                {
                    CompoundTag fluidTag = new CompoundTag();
                    fluid.writeToNBT(fluidTag);
                    tag.put("Fluid", fluidTag);
                }
            });
        }

        @Override
        public int getTanks()
        {
            return 1;
        }

        @Override
        public FluidStack getFluidInTank(int tank)
        {
            return this.getFluid();
        }

        @Override
        public int getTankCapacity(int tank)
        {
            return this.capacity;
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack)
        {
            return true;
        }

        @Override
        public int fill(FluidStack resource, IFluidHandler.FluidAction action)
        {
            if(this.container.getCount() != 1 || resource.isEmpty() || !this.isFluidValid(0, resource))
            {
                return 0;
            }

            FluidStack contained = this.getFluid();
            if(contained.isEmpty())
            {
                int fillAmount = Math.min(this.capacity, resource.getAmount());
                if(action.execute())
                {
                    FluidStack filled = new FluidStack(resource, fillAmount);
                    this.setFluid(filled);
                }
                return fillAmount;
            }

            if(!contained.isFluidEqual(resource))
            {
                return 0;
            }

            int fillAmount = Math.min(this.capacity - contained.getAmount(), resource.getAmount());
            if(fillAmount > 0 && action.execute())
            {
                contained.grow(fillAmount);
                this.setFluid(contained);
            }
            return fillAmount;
        }

        @Override
        public FluidStack drain(FluidStack resource, IFluidHandler.FluidAction action)
        {
            if(this.container.getCount() != 1 || resource.isEmpty() || !resource.isFluidEqual(this.getFluid()))
            {
                return FluidStack.EMPTY;
            }
            return this.drain(resource.getAmount(), action);
        }

        @Override
        public FluidStack drain(int maxDrain, IFluidHandler.FluidAction action)
        {
            if(this.container.getCount() != 1 || maxDrain <= 0)
            {
                return FluidStack.EMPTY;
            }

            FluidStack contained = this.getFluid();
            if(contained.isEmpty())
            {
                return FluidStack.EMPTY;
            }

            int drainAmount = Math.min(contained.getAmount(), maxDrain);
            FluidStack drained = new FluidStack(contained, drainAmount);

            if(action.execute())
            {
                contained.shrink(drainAmount);
                if(contained.isEmpty())
                {
                    this.setFluid(FluidStack.EMPTY);
                }
                else
                {
                    this.setFluid(contained);
                }
            }
            return drained;
        }

        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side)
        {
            if(cap == ForgeCapabilities.FLUID_HANDLER_ITEM || cap == ForgeCapabilities.FLUID_HANDLER)
            {
                return this.holder.cast();
            }
            return LazyOptional.empty();
        }
    }
}
