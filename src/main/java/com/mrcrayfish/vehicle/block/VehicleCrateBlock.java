package com.mrcrayfish.vehicle.block;


import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.nbt.Tag;

import com.google.common.base.Strings;
import com.mrcrayfish.vehicle.init.ModBlocks;
import com.mrcrayfish.vehicle.init.ModItems;
import com.mrcrayfish.vehicle.blockentity.VehicleCrateBlockEntity;
import com.mrcrayfish.vehicle.util.BlockEntityUtil;
import com.mrcrayfish.vehicle.util.RenderUtil;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item;
import com.mrcrayfish.vehicle.init.ModBlockEntities;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.ItemInteractionResult;

import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Author: MrCrayfish
 */
public class VehicleCrateBlock extends RotatedObjectBlock implements EntityBlock
{
    public static final List<ResourceLocation> REGISTERED_CRATES = new ArrayList<>();
    private static final VoxelShape PANEL = box(0, 0, 0, 16, 2, 16);

    public VehicleCrateBlock()
    {
        super(BlockBehaviour.Properties.of().mapColor(DyeColor.LIGHT_GRAY).dynamicShape().noOcclusion().strength(1.5F, 5.0F));
    }

    /**
     * One creative-tab entry per registered vehicle.
     *
     * <p>This replaces the old Block#fillItemCategory override, which stopped being a real override
     * in 1.19.3 when creative tab contents moved to BuildCreativeModeTabContentsEvent. It was left
     * in place through the port and silently did nothing, so no crate ever appeared in creative.</p>
     */
    public static List<ItemStack> getCreativeCrates()
    {
        List<ItemStack> items = new ArrayList<>();
        REGISTERED_CRATES.forEach(resourceLocation ->
        {
            CompoundTag blockEntityTag = new CompoundTag();
            blockEntityTag.putString("Vehicle", resourceLocation.toString());
            blockEntityTag.putBoolean("Creative", true);
            items.add(createStack(blockEntityTag));
        });
        return items;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos)
    {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context)
    {
        BlockEntity te = worldIn.getBlockEntity(pos);
        if(te instanceof VehicleCrateBlockEntity && ((VehicleCrateBlockEntity)te).isOpened())
            return PANEL;
        return Shapes.block();
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader reader, BlockPos pos)
    {
        return this.isBelowBlockTopSolid(reader, pos) && this.canOpen(reader, pos);
    }

    private boolean canOpen(LevelReader reader, BlockPos pos)
    {
        for(Direction side : Direction.Plane.HORIZONTAL)
        {
            BlockPos adjacentPos = pos.relative(side);
            BlockState state = reader.getBlockState(adjacentPos);
            if(state.isAir())
                continue;
            if(!state.canBeReplaced() || this.isBelowBlockTopSolid(reader, adjacentPos))
            {
                return false;
            }
        }
        return true;
    }

    private boolean isBelowBlockTopSolid(LevelReader reader, BlockPos pos)
    {
        return reader.getBlockState(pos.below()).isFaceSturdy(reader, pos.below(), Direction.UP);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player playerEntity, InteractionHand hand, BlockHitResult result)
    {
        if(result.getDirection() == Direction.UP && stack.getItem() == ModItems.WRENCH.get())
        {
            this.openCrate(world, pos, state, playerEntity);
            return ItemInteractionResult.sidedSuccess(world.isClientSide);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player playerEntity, BlockHitResult result)
    {
        return InteractionResult.SUCCESS;
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity livingEntity, ItemStack stack)
    {
        if(livingEntity instanceof Player && ((Player) livingEntity).isCreative())
        {
            this.openCrate(world, pos, state, livingEntity);
        }
    }

    private void openCrate(Level world, BlockPos pos, BlockState state, LivingEntity placer)
    {
        BlockEntity tileEntity = world.getBlockEntity(pos);
        if(tileEntity instanceof VehicleCrateBlockEntity && this.canOpen(world, pos))
        {
            if(world.isClientSide)
            {
                this.spawnCrateOpeningParticles((ClientLevel) world, pos, state);
            }
            else
            {
                ((VehicleCrateBlockEntity) tileEntity).open(placer.getUUID());
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void spawnCrateOpeningParticles(ClientLevel world, BlockPos pos, BlockState state)
    {
        double y = 0.875;
        double x, z;
        for(int j = 0; j < 4; ++j)
        {
            for(int l = 0; l < 4; ++l)
            {
                x = (j + 0.5D) / 4.0D;
                z = (l + 0.5D) / 4.0D;
                world.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, state), pos.getX() + x, pos.getY() + y, pos.getZ() + z, x - 0.5D, y - 0.5D, z - 0.5D);
            }
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new VehicleCrateBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type)
    {
        return BlockEntityUtil.createTicker(type, ModBlockEntities.VEHICLE_CRATE.get(), (level1, pos1, state1, crate) -> crate.tick());
    }

    @Override
    public RenderShape getRenderShape(BlockState state)
    {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> list, TooltipFlag advanced)
    {
        Component vehicleName = EntityType.PIG.getDescription();
        CustomData customData = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        if(customData != null)
        {
            CompoundTag blockEntityTag = customData.copyTag();
            String entityType = blockEntityTag.getString("Vehicle");
            if(!Strings.isNullOrEmpty(entityType))
            {
                vehicleName = EntityType.byString(entityType).orElse(EntityType.PIG).getDescription();
            }
        }
        if(Screen.hasShiftDown())
        {
            list.addAll(RenderUtil.lines(Component.translatable(this.getDescriptionId() + ".info", vehicleName), 150));
        }
        else
        {
            list.add(vehicleName.copy().withStyle(ChatFormatting.BLUE));
            list.add(Component.translatable("vehicle.info_help").withStyle(ChatFormatting.YELLOW));
        }
    }

    public static ItemStack create(ResourceLocation entityId, int color, ItemStack engine, ItemStack wheel)
    {
        return create(entityId, color, engine, wheel, net.minecraft.core.HolderLookup.Provider.create(java.util.stream.Stream.of()));
    }

    public static ItemStack create(ResourceLocation entityId, int color, ItemStack engine, ItemStack wheel, net.minecraft.core.HolderLookup.Provider registryAccess)
    {
        CompoundTag blockEntityTag = new CompoundTag();
        blockEntityTag.putString("Vehicle", entityId.toString());
        blockEntityTag.putInt("Color", color);
        blockEntityTag.put("EngineStack", engine.saveOptional(registryAccess));
        blockEntityTag.put("WheelStack", wheel.saveOptional(registryAccess));
        return createStack(blockEntityTag);
    }

    /**
     * Builds a crate stack carrying block entity data.
     *
     * <p>The 1.16 form of this wrapped the payload in a "BlockEntityTag" compound stored under
     * CUSTOM_DATA. That tag no longer exists: 1.20.5 replaced it with the BLOCK_ENTITY_DATA
     * component, which BlockItem#updateCustomBlockEntityTag applies to the block entity on
     * placement. Stored under CUSTOM_DATA it was simply inert, so every crate placed empty.</p>
     *
     * <p>Must go through BlockItem#setBlockEntityData rather than setting the component directly:
     * that helper stamps the block entity type "id" into the tag, and BLOCK_ENTITY_DATA's codec
     * rejects tags without it. The creative-slot packet re-validates every component with that
     * codec (ItemStack#validatedStreamCodec), so an id-less crate kicks the player with
     * "Failed to decode packet 'serverbound/minecraft:set_creative_mode_slot'" the moment they
     * click one in the creative menu.</p>
     */
    public static ItemStack createStack(CompoundTag blockEntityTag)
    {
        ItemStack stack = new ItemStack(ModBlocks.VEHICLE_CRATE.get());
        BlockItem.setBlockEntityData(stack, ModBlockEntities.VEHICLE_CRATE.get(), blockEntityTag);
        return stack;
    }

    public static synchronized void registerVehicle(ResourceLocation id)
    {
        if(!REGISTERED_CRATES.contains(id))
        {
            REGISTERED_CRATES.add(id);
            Collections.sort(REGISTERED_CRATES);
        }
    }
}
