package com.mrcrayfish.vehicle.client.render.blockentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mrcrayfish.vehicle.block.FluidExtractorBlock;
import com.mrcrayfish.vehicle.blockentity.FluidExtractorBlockEntity;
import com.mrcrayfish.vehicle.util.FluidUtils;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.fluids.capability.templates.FluidTank;

/**
 * Author: MrCrayfish
 */
public class FluidExtractorRenderer extends TileEntityRenderer<FluidExtractorBlockEntity>
{
    private static final FluidUtils.FluidSides FLUID_SIDES = new FluidUtils.FluidSides(Direction.WEST, Direction.EAST, Direction.SOUTH, Direction.UP);

    public FluidExtractorRenderer(TileEntityRendererDispatcher dispatcher)
    {
        super(dispatcher);
    }

    @Override
    public void render(FluidExtractorBlockEntity fluidExtractor, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer, int light, int p_225616_6_)
    {
        FluidTank tank = fluidExtractor.getFluidTank();
        if(tank.isEmpty())
            return;

        matrixStack.pushPose();
        matrixStack.translate(0.5, 0.5, 0.5);
        Direction direction = fluidExtractor.getBlockState().getValue(FluidExtractorBlock.DIRECTION);
        matrixStack.mulPose(Vector3f.YP.rotationDegrees(direction.get2DDataValue() * -90F - 90F));
        matrixStack.translate(-0.5, -0.5, -0.5);
        float height = 12.0F * tank.getFluidAmount() / (float) tank.getCapacity();
        FluidUtils.drawFluidInWorld(tank, fluidExtractor.getLevel(), fluidExtractor.getBlockPos(), matrixStack, renderTypeBuffer, 9F * 0.0625F, 2F * 0.0625F, 0.01F * 0.0625F, 6.99F * 0.0625F, height * 0.0625F, (16 - 0.02F) * 0.0625F, light, FLUID_SIDES);
        matrixStack.popPose();
    }
}
