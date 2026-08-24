package com.mrcrayfish.vehicle.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Author: MrCrayfish
 */
@OnlyIn(Dist.CLIENT)
public class CheckBox extends AbstractWidget
{
    private static final ResourceLocation GUI = ResourceLocation.fromNamespaceAndPath("vehicle", "textures/gui/components.png");

    private boolean toggled = false;

    public CheckBox(int left, int top, Component title)
    {
        super(left, top, 8, 8, title);
    }

    public void setToggled(boolean toggled)
    {
        this.toggled = toggled;
    }

    public boolean isToggled()
    {
        return this.toggled;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks)
    {
        guiGraphics.blit(GUI, this.getX(), this.getY(), 0, 0, 8, 8);
        if(this.toggled)
        {
            guiGraphics.blit(GUI, this.getX(), this.getY() - 1, 8, 0, 9, 8);
        }
        guiGraphics.drawString(Minecraft.getInstance().font, this.getMessage(), this.getX() + 12, this.getY(), 0xFFFFFF);
    }

    @Override
    public void onClick(double mouseX, double mouseY)
    {
        this.toggled = !this.toggled;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput)
    {
    }
}
