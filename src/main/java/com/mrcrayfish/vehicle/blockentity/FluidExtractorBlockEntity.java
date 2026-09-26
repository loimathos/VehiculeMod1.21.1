package com.mrcrayfish.vehicle.blockentity;



import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;

import net.minecraft.nbt.Tag;

import com.mrcrayfish.vehicle.Config;
import com.mrcrayfish.vehicle.block.FluidMixerBlock;
import com.mrcrayfish.vehicle.crafting.FluidExtractorRecipe;
import com.mrcrayfish.vehicle.crafting.RecipeType;
import com.mrcrayfish.vehicle.init.ModBlockEntities;
import com.mrcrayfish.vehicle.inventory.container.FluidExtractorContainer;
import com.mrcrayfish.vehicle.util.InventoryUtil;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.nbt.CompoundTag;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;


import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import net.minecraft.core.HolderLookup;

/**
 * Author: MrCrayfish
 */
public class FluidExtractorBlockEntity extends BlockFluidHandlerSynced implements Container, MenuProvider, RecipeInput
{
    private NonNullList<ItemStack> inventory = NonNullList.withSize(7, ItemStack.EMPTY);

    private static final int SLOT_FUEL_SOURCE = 0;
    public static final int SLOT_FLUID_SOURCE = 1;

    private FluidExtractorRecipe currentRecipe = null;
    private int remainingFuel;
    private int fuelMaxProgress;
    private int extractionProgress;
    private int capacity;
    private boolean extracting;

    private String customName;
    private int clientFluidId = 0;
    private int clientAmount = 0;

    protected final ContainerData fluidExtractorData = new ContainerData()
    {
        @Override
        public int get(int index)
        {
            switch(index)
            {
                case 0:
                    return extractionProgress;
                case 1:
                    return remainingFuel;
                case 2:
                    return fuelMaxProgress;
                case 3:
                    return BuiltInRegistries.FLUID.getId(tank.getFluid().getFluid());
                case 4:
                    return tank.getFluidAmount();
            }
            return 0;
        }

        @Override
        public void set(int index, int value)
        {
            switch(index)
            {
                case 0:
                    extractionProgress = value;
                    break;
                case 1:
                    remainingFuel = value;
                    break;
                case 2:
                    fuelMaxProgress = value;
                    break;
                case 3:
                    clientFluidId = value;
                    syncClientTank(tank, clientFluidId, clientAmount);
                    break;
                case 4:
                    clientAmount = value;
                    syncClientTank(tank, clientFluidId, clientAmount);
                    break;
            }

        }

        @Override
        public int getCount()
        {
            return 5;
        }
    };

    public FluidExtractorBlockEntity(BlockPos pos, BlockState state)
    {
        super(ModBlockEntities.FLUID_EXTRACTOR.get(), pos, state, Config.SERVER.extractorCapacity.get());
        this.capacity = Config.SERVER.extractorCapacity.get();
    }

    public void tick()
    {
        if(this.level != null && !this.level.isClientSide())
        {
            ItemStack source = this.getItem(SLOT_FLUID_SOURCE);
            ItemStack fuel = this.getItem(SLOT_FUEL_SOURCE);

            if(this.currentRecipe == null && !source.isEmpty())
            {
                this.currentRecipe = this.getRecipe().orElse(null);
            }
            else if(source.isEmpty())
            {
                this.currentRecipe = null;
                this.extractionProgress = 0;
            }

            this.updateFuel(source, fuel);

            if(this.remainingFuel > 0 && this.canFillWithFluid(source))
            {
                this.setExtracting(true);

                if(this.extractionProgress++ == Config.SERVER.extractorExtractTime.get())
                {
                    this.tank.fill(this.currentRecipe.getResult().createStack(), IFluidHandler.FluidAction.EXECUTE);
                    this.extractionProgress = 0;
                    this.shrinkItem(SLOT_FLUID_SOURCE);
                    this.currentRecipe = null;
                }
            }
            else
            {
                this.extractionProgress = 0;
                this.setExtracting(false);
            }

            if(this.remainingFuel > 0)
            {
                this.remainingFuel--;
                this.updateFuel(source, fuel);

                // Updates the enabled state of the fluid extractor
                if(this.remainingFuel == 0)
                {
                    this.setExtracting(false);
                }
            }
        }
    }

    private void updateFuel(ItemStack source, ItemStack fuel)
    {
        if(!fuel.isEmpty() && this.remainingFuel == 0 && this.canFillWithFluid(source))
        {
            this.fuelMaxProgress = ForgeHooks.getBurnTime(fuel, null);
            this.remainingFuel = this.fuelMaxProgress;
            this.shrinkItem(SLOT_FUEL_SOURCE);
        }
    }

    private boolean canFillWithFluid(ItemStack stack)
    {
        return this.currentRecipe != null && this.currentRecipe.getIngredient().getItem() == stack.getItem() && this.tank.getFluidAmount() < this.tank.getCapacity() && (this.tank.isEmpty() || this.tank.getFluid().getFluid() == this.currentRecipe.getResult().getFluid()) && (this.tank.getFluidAmount() + this.currentRecipe.getResult().getAmount()) <= this.tank.getCapacity();
    }

    @OnlyIn(Dist.CLIENT)
    public boolean canExtract()
    {
        ItemStack ingredient = this.getItem(SLOT_FLUID_SOURCE);
        if(!ingredient.isEmpty())
        {
            if(this.currentRecipe == null)
            {
                this.currentRecipe = this.getRecipe().orElse(null);
            }
        }
        else
        {
            this.currentRecipe = null;
        }
        return this.canFillWithFluid(ingredient) && this.remainingFuel >= 0;
    }

    @OnlyIn(Dist.CLIENT)
    public FluidExtractorRecipe getCurrentRecipe()
    {
        return currentRecipe;
    }

    public FluidStack getFluidStackTank()
    {
        return this.tank.getFluid();
    }

    public int getCapacity()
    {
        return capacity;
    }

    @Override
    public int getContainerSize()
    {
        return 2;
    }

    @Override
    public int size()
    {
        return this.getContainerSize();
    }

    @Override
    public boolean isEmpty()
    {
        for(ItemStack stack : this.inventory)
        {
            if(!stack.isEmpty())
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int index)
    {
        return this.inventory.get(index);
    }

    @Override
    public ItemStack removeItem(int index, int count)
    {
        ItemStack stack = ContainerHelper.removeItem(this.inventory, index, count);
        if(!stack.isEmpty())
        {
            this.setChanged();
        }
        return stack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int index)
    {
        return ContainerHelper.takeItem(this.inventory, index);
    }

    @Override
    public void setItem(int index, ItemStack stack)
    {
        this.inventory.set(index, stack);
        if(stack.getCount() > this.getMaxStackSize())
        {
            stack.setCount(this.getMaxStackSize());
        }
        this.setChanged();
    }

    @Override
    public boolean stillValid(Player player)
    {
        return this.level.getBlockEntity(this.worldPosition) == this && player.distanceToSqr((double) this.worldPosition.getX() + 0.5D, (double) this.worldPosition.getY() + 0.5D, (double) this.worldPosition.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack)
    {
        if(index == 0)
        {
            return ForgeHooks.getBurnTime(stack, null) > 0;
        }
        else if(index == 1)
        {
            return this.isValidIngredient(stack);
        }
        return false;
    }

    @Override
    public void clearContent()
    {
        this.inventory.clear();
    }

    public int getExtractionProgress()
    {
        return this.fluidExtractorData.get(0);
    }

    public int getRemainingFuel()
    {
        return this.fluidExtractorData.get(1);
    }

    public int getFuelMaxProgress()
    {
        return this.fluidExtractorData.get(2);
    }

    public int getFluidLevel()
    {
        return this.fluidExtractorData.get(4);
    }

    @Override
    protected void loadAdditional(CompoundTag compound, HolderLookup.Provider registries)
    {
        super.loadAdditional(compound, registries);
        if(compound.contains("ExtractionProgress", Tag.TAG_INT))
        {
            this.extractionProgress = compound.getInt("ExtractionProgress");
        }
        if(compound.contains("RemainingFuel", Tag.TAG_INT))
        {
            this.remainingFuel = compound.getInt("RemainingFuel");
        }
        if(compound.contains("FuelMaxProgress", Tag.TAG_INT))
        {
            this.fuelMaxProgress = compound.getInt("FuelMaxProgress");
        }
        if(compound.contains("Items", Tag.TAG_LIST))
        {
            this.inventory = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
            ContainerHelper.loadAllItems(compound, this.inventory, registries);
        }
        if(compound.contains("CustomName", Tag.TAG_STRING))
        {
            this.customName = compound.getString("CustomName");
        }
    }

    @Override
    protected void saveAdditional(CompoundTag compound, HolderLookup.Provider registries)
    {
        super.saveAdditional(compound, registries);
        compound.putInt("ExtractionProgress", this.extractionProgress);
        compound.putInt("RemainingFuel", this.remainingFuel);
        compound.putInt("FuelMaxProgress", this.fuelMaxProgress);

        ContainerHelper.saveAllItems(compound, this.inventory, registries);

        if(this.hasCustomName())
        {
            compound.putString("CustomName", this.customName);
        }
    }

    public Component getName()
    {
        return this.getDisplayName();
    }


    public boolean hasCustomName()
    {
        return this.customName != null && !this.customName.isEmpty();
    }

    @Override
    public Component getDisplayName()
    {
        return this.hasCustomName() ? Component.literal(this.customName) : Component.translatable("container.fluid_extractor");
    }

    private void shrinkItem(int index)
    {
        ItemStack stack = this.getItem(index);
        stack.shrink(1);
        if(stack.isEmpty())
        {
            this.setItem(index, ItemStack.EMPTY);
        }
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player playerEntity)
    {
        return new FluidExtractorContainer(windowId, playerInventory, this);
    }

    public ContainerData getFluidExtractorData()
    {
        return fluidExtractorData;
    }

    private void syncClientTank(FluidTank tank, int fluidId, int amount)
    {
        Fluid fluid = BuiltInRegistries.FLUID.byId(fluidId);
        if(fluid != null && fluid != Fluids.EMPTY && amount > 0)
        {
            tank.setFluid(new FluidStack(fluid, amount));
        }
        else
        {
            tank.setFluid(FluidStack.EMPTY);
        }
    }

    public Optional<FluidExtractorRecipe> getRecipe()
    {
        if(this.level == null) return Optional.empty();
        ItemStack stack = this.getItem(1);
        return this.level.getRecipeManager().getAllRecipesFor(RecipeType.FLUID_EXTRACTOR.get()).stream()
                .map(RecipeHolder::value)
                .filter(recipe -> InventoryUtil.areItemStacksEqualIgnoreCount(stack, recipe.getIngredient()))
                .findFirst();
    }

    public boolean isValidIngredient(ItemStack ingredient)
    {
        return this.level.getRecipeManager().getAllRecipesFor(RecipeType.FLUID_EXTRACTOR.get()).stream().anyMatch(holder -> InventoryUtil.areItemStacksEqualIgnoreCount(ingredient, holder.value().getIngredient()));
    }

    private final net.minecraftforge.common.util.LazyOptional<?> itemHandler = net.minecraftforge.common.util.LazyOptional.of(this::createUnSidedHandler);

    @Nonnull
    protected net.minecraftforge.items.IItemHandler createUnSidedHandler()
    {
        return new net.minecraftforge.items.wrapper.InvWrapper(this);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side)
    {
        if (!this.remove && cap == null /* TODO: Capabilities removed in 1.21.1, use IItemHandler attachment */ )
            return this.itemHandler.cast();
        return super.getCapability(cap, side);
    }

    private void setExtracting(boolean state)
    {
        if(this.extracting != state)
        {
            this.extracting = state;
            this.level.setBlock(this.worldPosition, this.getBlockState().setValue(FluidMixerBlock.ENABLED, state), Block.UPDATE_ALL);
        }
    }
}