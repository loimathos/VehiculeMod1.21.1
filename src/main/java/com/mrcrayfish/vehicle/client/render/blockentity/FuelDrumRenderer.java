package com.mrcrayfish.vehicle.client.render.blockentity;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mrcrayfish.vehicle.blockentity.FuelDrumBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.joml.Matrix4f;
import net.minecraft.client.gui.Font;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;

/**
 * Author: MrCrayfish
 */
public class FuelDrumRenderer implements BlockEntityRenderer<FuelDrumBlockEntity>
{
    // In 1.21.1, use built-in render types instead of custom lambda-based ones
    public static final RenderType LABEL_BACKGROUND = RenderType.gui();
    public static final RenderType LABEL_FLUID = RenderType.entityTranslucent(TextureAtlas.LOCATION_BLOCKS);

    public FuelDrumRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(FuelDrumBlockEntity fuelDrumTileEntity, float partialTicks, PoseStack matrixStack, MultiBufferSource renderTypeBuffer, int lightTexture, int overlayTexture)
    {
        if(Minecraft.getInstance().player.isCrouching())
        {
            if(fuelDrumTileEntity.hasFluid() && Minecraft.getInstance().hitResult != null && Minecraft.getInstance().hitResult.getType() == HitResult.Type.BLOCK)
            {
                BlockHitResult result = (BlockHitResult) Minecraft.getInstance().hitResult;
                if(result.getBlockPos().equals(fuelDrumTileEntity.getBlockPos()))
                {
                    this.drawFluidLabel(Minecraft.getInstance().font, fuelDrumTileEntity.getFluidTank(), matrixStack, renderTypeBuffer);
                }
            }
        }
    }

    private void drawFluidLabel(Font fontRendererIn, FluidTank tank, PoseStack matrixStack, MultiBufferSource renderTypeBuffer)
    {
        if(tank.getFluid().isEmpty())
            return;

        FluidStack stack = tank.getFluid();
        TextureAtlasSprite sprite = Minecraft.getInstance().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS).getSprite(IClientFluidTypeExtensions.of(tank.getFluid().getFluid()).getStillTexture(tank.getFluid()));
        if(sprite != null)
        {
            float level = tank.getFluidAmount() / (float) tank.getCapacity();
            float width = 30F;
            float fuelWidth = width * level;
            float remainingWidth = width - fuelWidth;
            float offsetWidth = width / 2.0F;

            matrixStack.pushPose();
            matrixStack.translate(0.5, 1.25, 0.5);
            matrixStack.mulPose(Minecraft.getInstance().gameRenderer.getMainCamera().rotation());
            matrixStack.scale(-0.025F, -0.025F, 0.025F);

            VertexConsumer backgroundBuilder = renderTypeBuffer.getBuffer(LABEL_BACKGROUND);

            /* Background */
            Matrix4f matrix = matrixStack.last().pose();
            backgroundBuilder.addVertex(matrix, -offsetWidth - 1.0F, -2.0F, -0.01F).setColor(0.5F, 0.5F, 0.5F, 1.0F);
            backgroundBuilder.addVertex(matrix, -offsetWidth - 1.0F, 5.0F, -0.01F).setColor(0.5F, 0.5F, 0.5F, 1.0F);
            backgroundBuilder.addVertex(matrix, -offsetWidth + width + 1.0F, 5.0F, -0.01F).setColor(0.5F, 0.5F, 0.5F, 1.0F);
            backgroundBuilder.addVertex(matrix, -offsetWidth + width + 1.0F, -2.0F, -0.01F).setColor(0.5F, 0.5F, 0.5F, 1.0F);

            matrixStack.translate(0, 0, -0.05);

            /* Remaining */
            matrix = matrixStack.last().pose();
            backgroundBuilder.addVertex(matrix, -offsetWidth + fuelWidth, -1.0F, 0.0F).setColor(0.4F, 0.4F, 0.4F, 1.0F);
            backgroundBuilder.addVertex(matrix, -offsetWidth + fuelWidth, 4.0F, 0.0F).setColor(0.4F, 0.4F, 0.4F, 1.0F);
            backgroundBuilder.addVertex(matrix, -offsetWidth + fuelWidth + remainingWidth, 4.0F, 0.0F).setColor(0.4F, 0.4F, 0.4F, 1.0F);
            backgroundBuilder.addVertex(matrix, -offsetWidth + fuelWidth + remainingWidth, -1.0F, 0.0F).setColor(0.4F, 0.4F, 0.4F, 1.0F);

            float minU = sprite.getU0();
            float maxU = minU + (sprite.getU1() - minU) * level;
            float minV = sprite.getV0();
            float maxV = minV + (sprite.getV1() - minV) * 4 * 0.0625F;

            /* Fluid Texture */
            VertexConsumer fluidBuilder = renderTypeBuffer.getBuffer(LABEL_FLUID);
            fluidBuilder.addVertex(matrix, -offsetWidth, -1.0F, 0.0F).setUv(minU, maxV);
            fluidBuilder.addVertex(matrix, -offsetWidth, 4.0F, 0.0F).setUv(minU, minV);
            fluidBuilder.addVertex(matrix, -offsetWidth + fuelWidth, 4.0F, 0.0F).setUv(maxU, minV);
            fluidBuilder.addVertex(matrix, -offsetWidth + fuelWidth, -1.0F, 0.0F).setUv(maxU, maxV);

            /* Fluid Name */
            matrixStack.scale(0.5F, 0.5F, 0.5F);
            String name = stack.getDisplayName().getString();
            int nameWidth = fontRendererIn.width(name) / 2;
            fontRendererIn.drawInBatch(name, -nameWidth, -14, -1, false, matrixStack.last().pose(), renderTypeBuffer, Font.DisplayMode.NORMAL, 0, 15728880);

            matrixStack.popPose();
        }
    }
}
