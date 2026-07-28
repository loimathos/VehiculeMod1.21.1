package com.mrcrayfish.vehicle.blockentity;



import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import com.mrcrayfish.vehicle.init.ModBlockEntities;
import com.mrcrayfish.vehicle.inventory.IStorageBlock;
import com.mrcrayfish.vehicle.inventory.container.WorkstationContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;


import javax.annotation.Nullable;
import net.minecraft.core.HolderLookup;

/**
 * Author: MrCrayfish
 */
public class WorkstationBlockEntity extends BlockEntitySynced implements IStorageBlock
{
    private NonNullList<ItemStack> inventory = NonNullList.withSize(3, ItemStack.EMPTY);

    public WorkstationBlockEntity(BlockPos pos, BlockState state)
    {
        super(ModBlockEntities.WORKSTATION.get(), pos, state);
    }

    @Override
    public NonNullList<ItemStack> getInventory()
    {
        return this.inventory;
    }

    @Override
    protected void loadAdditional(CompoundTag compound, HolderLookup.Provider registries)
    {
        super.loadAdditional(compound, registries);
        ContainerHelper.loadAllItems(compound, this.inventory, registries);
    }

    @Override
    protected void saveAdditional(CompoundTag compound, HolderLookup.Provider registries)
    {
        ContainerHelper.saveAllItems(compound, this.inventory, registries);
        super.saveAdditional(compound, registries);
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack)
    {
        return index != 0 || (stack.getItem() instanceof DyeItem && this.inventory.get(index).getCount() < 1);
    }

    @Override
    public Component getDisplayName()
    {
        return Component.translatable("container.vehicle.workstation");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player playerEntity)
    {
        return new WorkstationContainer(windowId, playerInventory, this);
    }
}
