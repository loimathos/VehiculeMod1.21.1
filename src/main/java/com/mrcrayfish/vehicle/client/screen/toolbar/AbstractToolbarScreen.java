package com.mrcrayfish.vehicle.client.screen.toolbar;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mrcrayfish.vehicle.Reference;
import com.mrcrayfish.vehicle.client.screen.DashboardScreen;
import com.mrcrayfish.vehicle.client.screen.toolbar.widget.IconButton;
import com.mrcrayfish.vehicle.client.screen.toolbar.widget.Spacer;
import com.mrcrayfish.vehicle.util.CommonUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;

import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractToolbarScreen extends Screen
{
    private static final ResourceLocation WINDOW_TEXTURE = ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "textures/gui/components.png");

    private Screen parent;
    private int contentWidth;

    protected AbstractToolbarScreen(Component titleIn, @Nullable Screen parent)
    {
        super(titleIn);
        this.parent = parent;
    }

    @Override
    protected void init()
    {
        List<AbstractWidget> widgets = new ArrayList<>();
        if(this.parent != null)
        {
            widgets.add(new IconButton(20, 20, DashboardScreen.Icons.BACK, Component.translatable("vehicle.toolbar.label.back"), onPress -> this.minecraft.setScreen(this.parent)));
            widgets.add(Spacer.of(5));
        }
        this.loadWidgets(widgets);

        int contentWidth = (widgets.size() - 1) * 2 + 4;
        for(AbstractWidget widget : widgets)
        {
            contentWidth += widget.getWidth();
        }
        this.contentWidth = contentWidth;

        Pair<Integer, Integer> dimensions = this.getDimensionsForWindow(this.contentWidth, 24);
        int startX = (this.width - dimensions.getLeft()) / 2;
        int startY = (this.height - dimensions.getRight()) - dimensions.getRight() / 2;
        int offset = 0;
        for(int i = 0; i < widgets.size(); i++)
        {
            AbstractWidget widget = widgets.get(i);
            widget.setX(startX + 4 + 2 + offset);
            widget.setY(startY + 4 + 2);
            offset += widget.getWidth() + 2;
            this.addRenderableWidget(widget);
        }
    }

    @Override
    public boolean isPauseScreen()
    {
        return false;
    }

    protected abstract void loadWidgets(List<AbstractWidget> widgets);

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks)
    {
        guiGraphics.fillGradient(0, this.height / 2, this.width, this.height, 0x00000000, 0xAA000000);

        Pair<Integer, Integer> dimensions = this.getDimensionsForWindow(this.contentWidth, 24);
        int startX = (this.width - dimensions.getLeft()) / 2;
        int startY = (this.height - dimensions.getRight()) - dimensions.getRight() / 2;
        this.drawWindow(guiGraphics, startX, startY, dimensions);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        AbstractWidget hoveredWidget = null;
        for(var child : this.children())
        {
            if(child instanceof AbstractWidget widget)
            {
                if(CommonUtils.isMouseWithin(mouseX, mouseY, widget.getX(), widget.getY(), widget.getWidth(), widget.getHeight()))
                {
                    hoveredWidget = widget;
                    break;
                }
            }
        }

        if(hoveredWidget instanceof IToolbarLabel)
        {
            Component message = ((IToolbarLabel) hoveredWidget).getLabel();
            int messageWidth = this.minecraft.font.width(message);
            guiGraphics.drawString(this.minecraft.font, message, this.width / 2 - messageWidth / 2, startY - 12, 0xFFFFFF);
        }
    }

    public void drawWindow(GuiGraphics guiGraphics, int x, int y, Pair<Integer, Integer> dimensions)
    {
        this.drawWindow(guiGraphics, x, y, dimensions.getLeft(), dimensions.getRight());
    }

    private void drawWindow(GuiGraphics guiGraphics, int x, int y, int width, int height)
    {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        int offset = 17;
        guiGraphics.blit(WINDOW_TEXTURE, x, y, offset, 0, 4, 4, 256, 256);                              /* Top left corner */
        guiGraphics.blit(WINDOW_TEXTURE, x + width - 4, y, 5 + offset, 0, 4, 4, 256, 256);              /* Top right corner */
        guiGraphics.blit(WINDOW_TEXTURE, x, y + height - 4, offset, 5, 4, 4, 256, 256);                 /* Bottom left corner */
        guiGraphics.blit(WINDOW_TEXTURE, x + width - 4, y + height - 4, 5 + offset, 5, 4, 4, 256, 256); /* Bottom right corner */
        guiGraphics.blit(WINDOW_TEXTURE, x + 4, y, 4 + offset, 0, width - 8, 4, 256, 256);              /* Top border */
        guiGraphics.blit(WINDOW_TEXTURE, x + 4, y + height - 4, 4 + offset, 5, width - 8, 4, 256, 256); /* Bottom border */
        guiGraphics.blit(WINDOW_TEXTURE, x, y + 4, offset, 4, 4, height - 8, 256, 256);                 /* Left border */
        guiGraphics.blit(WINDOW_TEXTURE, x + width - 4, y + 4, 5 + offset, 4, 4, height - 8, 256, 256); /* Right border */
        guiGraphics.blit(WINDOW_TEXTURE, x + 4, y + 4, 4 + offset, 4, width - 8, height - 8, 256, 256); /* Center */
    }

    private Pair<Integer, Integer> getDimensionsForWindow(int contentWidth, int contentHeight)
    {
        return Pair.of(contentWidth + 8, contentHeight + 8);
    }
}
