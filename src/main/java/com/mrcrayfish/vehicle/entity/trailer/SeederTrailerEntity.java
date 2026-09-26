package com.mrcrayfish.vehicle.entity.trailer;

import net.minecraft.nbt.Tag;

import net.minecraft.world.Containers;

import com.google.common.collect.ImmutableMap;
import com.mrcrayfish.vehicle.Config;
import com.mrcrayfish.vehicle.client.raytrace.EntityRayTracer;
import com.mrcrayfish.vehicle.common.inventory.IStorage;
import com.mrcrayfish.vehicle.common.inventory.StorageInventory;
import com.mrcrayfish.vehicle.entity.TrailerEntity;
import com.mrcrayfish.vehicle.init.ModEntities;
import com.mrcrayfish.vehicle.inventory.container.StorageContainer;
import com.mrcrayfish.vehicle.item.SprayCanItem;
import com.mrcrayfish.vehicle.network.PacketHandler;
import com.mrcrayfish.vehicle.network.message.MessageAttachTrailer;
import com.mrcrayfish.vehicle.network.message.MessageSyncStorage;
import com.mrcrayfish.vehicle.util.InventoryUtil;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.Mth;

import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import net.minecraftforge.network.PacketDistributor;

import java.util.Map;

/**
 * Author: MrCrayfish
 */
public class SeederTrailerEntity extends TrailerEntity implements IStorage
{
    private static final String INVENTORY_STORAGE_KEY = "Inventory";

    private int inventoryTimer;
    private StorageInventory inventory;

    public SeederTrailerEntity(EntityType<? extends SeederTrailerEntity> type, Level worldIn)
    {
        super(type, worldIn);
        this.initInventory();
    }

    @Override
    protected boolean canAddPassenger(Entity passenger)
    {
        return false;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand)
    {
        ItemStack heldItem = player.getItemInHand(hand);
        if((heldItem.isEmpty() || !(heldItem.getItem() instanceof SprayCanItem)) && player instanceof ServerPlayer)
        {
            IStorage.openStorage((ServerPlayer) player, this, INVENTORY_STORAGE_KEY);
            return InteractionResult.SUCCESS;
        }
        return super.interact(player, hand);
    }

    @Override
    public void tick()
    {
        super.tick();
        if(!this.level().isClientSide && Config.SERVER.trailerInventorySyncCooldown.get() > 0 && inventoryTimer++ == Config.SERVER.trailerInventorySyncCooldown.get())
        {
            this.inventoryTimer = 0;
            PacketHandler.getPlayChannel().send(new MessageSyncStorage(this, INVENTORY_STORAGE_KEY), PacketDistributor.TRACKING_ENTITY.with(this));
        }
    }

    @Override
    public void onUpdateVehicle()
    {
        super.onUpdateVehicle();

        Vec3 lookVec = this.getLookAngle();
        this.plantSeed(lookVec.yRot((float) Math.toRadians(90F)).scale(0.85));
        this.plantSeed(Vec3.ZERO);
        this.plantSeed(lookVec.yRot((float) Math.toRadians(-90F)).scale(0.85));
    }

    private void plantSeed(Vec3 vec)
    {
        BlockPos pos = new BlockPos(Mth.floor(xo + vec.x), (int)(yo + 0.25), Mth.floor(zo + vec.z));
        if(this.level().isEmptyBlock(pos) && this.level().getBlockState(pos.below()).getBlock() instanceof FarmBlock)
        {
            ItemStack seed = this.getSeed();
            if(seed.isEmpty() && this.getPullingEntity() instanceof StorageTrailerEntity)
            {
                seed = this.getSeedFromStorage((StorageTrailerEntity) this.getPullingEntity());
            }
            if(this.isSeed(seed))
            {
                Block seedBlock = ((BlockItem) seed.getItem()).getBlock();
                this.level().setBlockAndUpdate(pos, seedBlock.defaultBlockState());
                seed.shrink(1);
            }
        }
    }

    private ItemStack getSeed()
    {
        for(int i = 0; i < this.inventory.getContainerSize(); i++)
        {
            ItemStack stack = this.inventory.getItem(i);
            if(this.isSeed(stack))
            {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private boolean isSeed(ItemStack stack)
    {
        return !stack.isEmpty() && stack.getItem() instanceof BlockItem && ((BlockItem) stack.getItem()).getBlock() instanceof CropBlock;
    }

    private ItemStack getSeedFromStorage(StorageTrailerEntity storageTrailer)
    {
        if(storageTrailer == null)
            return ItemStack.EMPTY;

        if(storageTrailer.getInventory() != null)
        {
            StorageInventory storage = storageTrailer.getInventory();
            for(int i = 0; i < storage.getContainerSize(); i++)
            {
                ItemStack stack = storage.getItem(i);
                if(!stack.isEmpty() && stack.getItem() instanceof net.minecraftforge.common.IPlantable)
                {
                    return stack;
                }
            }

            if(storageTrailer.getPullingEntity() instanceof StorageTrailerEntity)
            {
                return this.getSeedFromStorage((StorageTrailerEntity) storageTrailer.getPullingEntity());
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound)
    {
        super.readAdditionalSaveData(compound);
        if(compound.contains(INVENTORY_STORAGE_KEY, Tag.TAG_LIST))
        {
            this.initInventory();
            InventoryUtil.readInventoryToNBT(compound, INVENTORY_STORAGE_KEY, this.inventory, this.level().registryAccess());
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound)
    {
        super.addAdditionalSaveData(compound);
        if(this.inventory != null)
        {
            InventoryUtil.writeInventoryToNBT(compound, INVENTORY_STORAGE_KEY, this.inventory, this.level().registryAccess());
        }
    }

    private void initInventory()
    {
        StorageInventory original = this.inventory;
        this.inventory = new StorageInventory(this, this.getDisplayName(), 3, stack ->
                !stack.isEmpty() && stack.getItem() instanceof net.minecraft.world.item.BlockItem && ((net.minecraft.world.item.BlockItem) stack.getItem()).getBlock() instanceof net.minecraft.world.level.block.CropBlock);
        // Copies the inventory if it exists already over to the new instance
        if(original != null)
        {
            for(int i = 0; i < original.getContainerSize(); i++)
            {
                ItemStack stack = original.getItem(i);
                if(!stack.isEmpty())
                {
                    this.inventory.setItem(i, stack.copy());
                }
            }
        }
    }

    @Override
    protected void onVehicleDestroyed(LivingEntity entity)
    {
        super.onVehicleDestroyed(entity);
        if(this.inventory != null)
        {
            Containers.dropContents(this.level(), this, this.inventory);
        }
    }

    @Override
    public Map<String, StorageInventory> getStorageInventories()
    {
        return ImmutableMap.of(INVENTORY_STORAGE_KEY, this.inventory);
    }

    public StorageInventory getInventory()
    {
        return this.inventory;
    }

    @OnlyIn(Dist.CLIENT)
    public static void registerInteractionBoxes()
    {
        EntityRayTracer.instance().registerInteractionBox(ModEntities.SEEDER.get(), () -> {
            return createScaledBoundingBox(-7.0, 1.5, 6.0, 7.0, 3.5, 17.0, 0.0625);
        }, (entity, rightClick) -> {
            if(rightClick) {
                PacketHandler.getPlayChannel().send(new MessageAttachTrailer(entity.getId()), PacketDistributor.SERVER.noArg());
                Minecraft.getInstance().player.swing(InteractionHand.MAIN_HAND);
            }
        }, entity -> true);
    }
}
