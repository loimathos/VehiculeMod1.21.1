package com.mrcrayfish.vehicle.client.screen;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mrcrayfish.vehicle.Config;
import com.mrcrayfish.vehicle.crafting.FluidEntry;
import com.mrcrayfish.vehicle.inventory.container.FluidExtractorContainer;
import com.mrcrayfish.vehicle.blockentity.FluidExtractorBlockEntity;
import com.mrcrayfish.vehicle.util.FluidUtils;
import com.mrcrayfish.vehicle.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;

import net.minecraft.ChatFormatting;
import net.minecraftforge.fluids.FluidStack;

import java.util.Arrays;
import java.util.Collections;

public class FluidExtractorScreen extends AbstractContainerScreen<FluidExtractorContainer>
{
    private static final ResourceLocation GUI = ResourceLocation.parse("vehicle:textures/gui/fluid_extractor.png");

    private Inventory playerInventory;
    private FluidExtractorBlockEntity fluidExtractorTileEntity;

    public FluidExtractorScreen(FluidExtractorContainer container, Inventory playerInventory, Component title)
    {
        super(container, playerInventory, title);
        this.playerInventory = playerInventory;
        this.fluidExtractorTileEntity = container.getFluidExtractor();
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks)
    {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        int startX = (this.width - this.imageWidth) / 2;
        int startY = (this.height - this.imageHeight) / 2;

        if(this.fluidExtractorTileEntity.getFluidStackTank() != null)
        {
            FluidStack stack = this.fluidExtractorTileEntity.getFluidStackTank();
            if(this.isMouseWithinRegion(startX + 127, startY + 14, 16, 59, mouseX, mouseY))
            {
                if(stack.getAmount() > 0)
                {
                    guiGraphics.renderTooltip(this.minecraft.font, Arrays.asList(Component.literal(stack.getDisplayName().getString()), Component.literal(ChatFormatting.GRAY.toString() + this.fluidExtractorTileEntity.getFluidLevel() + "/" + this.fluidExtractorTileEntity.getCapacity() + " mB")), java.util.Optional.empty(), mouseX, mouseY);
                }
                else
                {
                    guiGraphics.renderTooltip(this.minecraft.font, Collections.singletonList(Component.literal("No Fluid")), java.util.Optional.empty(), mouseX, mouseY);
                }
            }
        }

        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY)
    {
        guiGraphics.drawString(this.minecraft.font, this.fluidExtractorTileEntity.getDisplayName().getString(), 8, 6, 4210752, false);
        guiGraphics.drawString(this.minecraft.font, this.playerInventory.getDisplayName().getString(), 8, this.imageHeight - 96 + 2, 4210752, false);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY)
    {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int startX = (this.width - this.imageWidth) / 2;
        int startY = (this.height - this.imageHeight) / 2;

        guiGraphics.blit(GUI, startX, startY, 0, 0, this.imageWidth, this.imageHeight);

        if(this.fluidExtractorTileEntity.getRemainingFuel() >= 0)
        {
            int remainingFuel = (int) (14 * (this.fluidExtractorTileEntity.getRemainingFuel() / (double) this.fluidExtractorTileEntity.getFuelMaxProgress()));
            guiGraphics.blit(GUI, startX + 64, startY + 53 + 14 - remainingFuel, 176, 14 - remainingFuel, 14, remainingFuel + 1);
        }

        if(this.fluidExtractorTileEntity.canExtract())
        {
            int left = startX + 93 + 1;
            int top = startY + 34;
            int right = left + 23 - 1;
            int bottom = top + 16;
            FluidEntry fluidExtract = this.fluidExtractorTileEntity.getCurrentRecipe().getResult();
            int fluidColor = -1;
            if(fluidExtract != null)
            {
                fluidColor = (255 << 24) | FluidUtils.getAverageFluidColor(fluidExtract.getFluid());
            }
            RenderUtil.drawGradientRectHorizontal(left, top, right, bottom, -1, fluidColor);
            guiGraphics.blit(GUI, startX + 93, startY + 34, 176, 14, 23, 16);
            double extractionPercentage = this.fluidExtractorTileEntity.getExtractionProgress() / (double) Config.SERVER.extractorExtractTime.get();
            int extractionProgress = (int) (22 * extractionPercentage + 1);
            guiGraphics.blit(GUI, startX + 93 + extractionProgress, startY + 34, 93 + extractionProgress, 34, 23 - extractionProgress, 17);
        }

        this.drawFluidTank(this.fluidExtractorTileEntity.getFluidStackTank(), guiGraphics, startX + 127, startY + 14, this.fluidExtractorTileEntity.getFluidLevel() / (double) this.fluidExtractorTileEntity.getCapacity(), 59);
    }

    private void drawFluidTank(FluidStack fluid, GuiGraphics guiGraphics, int x, int y, double level, int height)
    {
        FluidUtils.drawFluidTankInGUI(fluid, x, y, level, height);
        guiGraphics.blit(GUI, x, y, 176, 44, 16, 59);
    }

    private boolean isMouseWithinRegion(int x, int y, int width, int height, int mouseX, int mouseY)
    {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }
}
