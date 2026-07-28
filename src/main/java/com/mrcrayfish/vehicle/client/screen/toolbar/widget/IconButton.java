package com.mrcrayfish.vehicle.client.screen.toolbar.widget;

import com.mrcrayfish.vehicle.client.screen.toolbar.IToolbarLabel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;

public class IconButton extends Button implements IToolbarLabel
{
    private IconProvider icon;
    private Component label;

    public IconButton(int width, int height, @Nullable IconProvider icon, Component label, Button.OnPress onPress)
    {
        super(0, 0, width, height, Component.empty(), onPress, DEFAULT_NARRATION);
        this.icon = icon;
        this.label = label;
    }

    public IconButton setLabel(Component label)
    {
        this.label = label;
        return this;
    }

    @Override
    public Component getLabel()
    {
        return this.label;
    }

    public IconButton setIcon(@Nullable IconProvider icon)
    {
        this.icon = icon;
        return this;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks)
    {
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTicks);
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;
        int combinedWidth = this.icon != null ? 10 : 0;
        String message = this.getMessage().getString();
        if(!message.isEmpty())
        {
            combinedWidth += font.width(message);
            if(this.icon != null)
            {
                combinedWidth += 4;
            }
        }
        if(this.icon != null)
        {
            this.drawIcon(guiGraphics, this.getX() + this.width / 2 - combinedWidth / 2, this.getY() + 5, this.icon.getU(), this.icon.getV());
        }
        if(!message.isEmpty())
        {
            guiGraphics.drawString(font, message, this.getX() + this.width / 2 - combinedWidth / 2 + 10 + (this.icon == null ? 0 : 4), this.getY() + 6, 0xFFFFFF);
        }
    }

    protected void drawIcon(GuiGraphics guiGraphics, int x, int y, int u, int v)
    {
        guiGraphics.blit(this.icon.getTextureLocation(), x, y, u, v, 10, 10, 100, 100);
    }

    public interface IconProvider
    {
        ResourceLocation getTextureLocation();

        int getU();

        int getV();
    }
}
