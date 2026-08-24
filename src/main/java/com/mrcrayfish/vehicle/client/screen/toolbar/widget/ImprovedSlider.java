package com.mrcrayfish.vehicle.client.screen.toolbar.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.gui.widget.ForgeSlider;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

/**
 * A class that fixes a bug with the Forge slider. The slider won't stop sliding if the mouse
 * was released outside of the sliders area.
 *
 * Author: MrCrayfish
 */
public class ImprovedSlider extends ForgeSlider
{
    private boolean dragging;
    private Consumer<ForgeSlider> handler;

    public ImprovedSlider(int xPos, int yPos, int width, int height, String prefix, String suf, double minVal, double maxVal, double currentVal, boolean showDec, boolean drawStr, Consumer<ForgeSlider> handler)
    {
        super(xPos, yPos, width, height, Component.literal(prefix), Component.literal(suf), minVal, maxVal, currentVal, 1.0, 0, drawStr);
        this.handler = handler;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button)
    {
        if(button == GLFW.GLFW_MOUSE_BUTTON_LEFT)
        {
            this.onRelease(mouseX, mouseY);
            this.dragging = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    protected void applyValue()
    {
        if(this.handler != null)
        {
            this.handler.accept(this);
        }

        /* Fixes the slider not being released when mouse is released outside of slider area */
        Minecraft mc = Minecraft.getInstance();
        if(this.dragging && GLFW.glfwGetMouseButton(mc.getWindow().getWindow(), GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_RELEASE)
        {
            double mouseX = mc.mouseHandler.xpos() * (double) mc.getWindow().getGuiScaledWidth() / (double) mc.getWindow().getWidth();
            double mouseY = mc.mouseHandler.ypos() * (double) mc.getWindow().getGuiScaledHeight() / (double) mc.getWindow().getHeight();
            this.onRelease(mouseX, mouseY);
            this.dragging = false;
        }
    }
}
