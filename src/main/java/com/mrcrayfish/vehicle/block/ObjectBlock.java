package com.mrcrayfish.vehicle.block;

import com.mrcrayfish.vehicle.util.RenderUtil;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.util.text.TextFormatting;

import net.minecraft.world.level.BlockGetter;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Author: MrCrayfish
 */
public class ObjectBlock extends Block
{
    public ObjectBlock(AbstractBlock.Properties properties)
    {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockAndTintGetter reader, List<Component> list, ITooltipFlag flag)
    {
        if(Screen.hasShiftDown())
        {
            list.addAll(RenderUtil.lines(Component.translatable(this.getDescriptionId() + ".info"), 150));
        }
        else
        {
            list.add(Component.translatable("vehicle.info_help").withStyle(TextFormatting.YELLOW));
        }
    }
}
