package com.mrcrayfish.vehicle.client.screen.toolbar.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;

/**
 * Author: MrCrayfish
 */
public class Spacer extends AbstractWidget
{
    public Spacer(int widthIn)
    {
        super(0, 0, widthIn, 20, Component.empty());
    }

    public static Spacer of(int width)
    {
        return new Spacer(width);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks)
    {
        guiGraphics.fill(this.getX() + this.getWidth() / 2, this.getY(), this.getX() + this.getWidth() / 2 + 1, this.getY() + this.getHeight(), 0xFF888888);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button)
    {
        return false;
    }

    @Override
    protected void updateWidgetNarration(net.minecraft.client.gui.narration.NarrationElementOutput narrationElementOutput) {}
}
