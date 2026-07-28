package com.mrcrayfish.vehicle.init;

import com.mrcrayfish.vehicle.Reference;
import com.mrcrayfish.vehicle.VehicleMod;
import com.mrcrayfish.vehicle.block.VehicleCrateBlock;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Populates the mod's creative tab.
 *
 * <p>Creative tab contents moved to this event in 1.19.3. The tab itself was registered during the
 * port but nothing ever filled it - there was no displayItems() on the builder and no listener for
 * this event - so it rendered completely empty.</p>
 *
 * <p>Everything the mod registers ends up in {@link ModItems#REGISTER}, including the BlockItems
 * (ModBlocks#register funnels them there), so iterating that one registry covers all items and
 * blocks. Vehicle crates are added separately because they are generated per registered vehicle
 * rather than being distinct registry entries.</p>
 */
@Mod.EventBusSubscriber(modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModCreativeTab
{
    @SubscribeEvent
    public static void onBuildContents(BuildCreativeModeTabContentsEvent event)
    {
        if(!event.getTabKey().equals(VehicleMod.CREATIVE_TAB.getKey()))
        {
            return;
        }

        ModItems.REGISTER.getEntries().forEach(item -> event.accept(new ItemStack(item.get())));

        for(ItemStack crate : VehicleCrateBlock.getCreativeCrates())
        {
            event.accept(crate);
        }
    }
}
