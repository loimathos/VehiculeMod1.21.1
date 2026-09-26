package com.mrcrayfish.vehicle.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Sheets;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.RenderType;
//import net.minecraft.client.renderer.ItemBlockRenderTypes; // Removed in 1.21.1
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import com.mojang.blaze3d.vertex.BufferUploader;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.network.chat.Style;
import net.minecraft.ChatFormatting;
import org.lwjgl.opengl.GL11;

import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.item.ItemDisplayContext;

/**
 * Author: MrCrayfish
 */
public class RenderUtil
{
    /**
     * Draws a textured modal rectangle with more precision than GuiScreen's methods. This will only
     * work correctly if the bound texture is 256x256.
     */
    public static void drawTexturedModalRect(int x, int y, int textureX, int textureY, int width, int height)
    {
        RenderSystem.setShader(net.minecraft.client.renderer.GameRenderer::getPositionTexShader);
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tessellator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.addVertex((float) x, (float) (y + height), 0.0F).setUv(((float) textureX * 0.00390625F), ((float) (textureY + height) * 0.00390625F));
        bufferbuilder.addVertex((float) (x + width), (float) (y + height), 0.0F).setUv(((float) (textureX + width) * 0.00390625F), ((float) (textureY + height) * 0.00390625F));
        bufferbuilder.addVertex((float) (x + width), (float) y, 0.0F).setUv(((float) (textureX + width) * 0.00390625F), ((float) textureY * 0.00390625F));
        bufferbuilder.addVertex((float) x, (float) y, 0.0F).setUv(((float) textureX * 0.00390625F), ((float) textureY * 0.00390625F));
        BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
    }

    /**
     * Draws a rectangle with a horizontal gradient between the specified colors (ARGB format).
     */
    public static void drawGradientRectHorizontal(int left, int top, int right, int bottom, int leftColor, int rightColor)
    {
        float redStart = (float)(leftColor >> 16 & 255) / 255.0F;
        float greenStart = (float)(leftColor >> 8 & 255) / 255.0F;
        float blueStart = (float)(leftColor & 255) / 255.0F;
        float alphaStart = (float)(leftColor >> 24 & 255) / 255.0F;
        float redEnd = (float)(rightColor >> 16 & 255) / 255.0F;
        float greenEnd = (float)(rightColor >> 8 & 255) / 255.0F;
        float blueEnd = (float)(rightColor & 255) / 255.0F;
        float alphaEnd = (float)(rightColor >> 24 & 255) / 255.0F;
        RenderSystem.setShader(net.minecraft.client.renderer.GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tessellator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferbuilder.addVertex((float) right, (float) top, 0.0F).setColor(redEnd, greenEnd, blueEnd, alphaEnd);
        bufferbuilder.addVertex((float) left, (float) top, 0.0F).setColor(redStart, greenStart, blueStart, alphaStart);
        bufferbuilder.addVertex((float) left, (float) bottom, 0.0F).setColor(redStart, greenStart, blueStart, alphaStart);
        bufferbuilder.addVertex((float) right, (float) bottom, 0.0F).setColor(redEnd, greenEnd, blueEnd, alphaEnd);
        BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
        RenderSystem.disableBlend();
    }

    public static void scissor(int x, int y, int width, int height) //TODO might need fixing. I believe I rewrote this in a another mod
    {
        Minecraft mc = Minecraft.getInstance();
        int scale = (int) mc.getWindow().getGuiScale();
        GL11.glScissor(x * scale, mc.getWindow().getScreenHeight() - y * scale - height * scale, Math.max(0, width * scale), Math.max(0, height * scale));
    }

    public static BakedModel getModel(ItemStack stack)
    {
        return Minecraft.getInstance().getItemRenderer().getItemModelShaper().getItemModel(stack);
    }

    public static void renderColoredModel(BakedModel model, ItemDisplayContext transformType, boolean leftHanded, PoseStack matrixStack, MultiBufferSource renderTypeBuffer, int color, int lightTexture, int overlayTexture)
    {
        matrixStack.pushPose();
        model.applyTransform(transformType, matrixStack, leftHanded);
        matrixStack.translate(-0.5, -0.5, -0.5);
        if(!model.isCustomRenderer())
        {
            VertexConsumer vertexBuilder = renderTypeBuffer.getBuffer(Sheets.cutoutBlockSheet());
            renderModel(model, ItemStack.EMPTY, color, lightTexture, overlayTexture, matrixStack, vertexBuilder);
        }
        matrixStack.popPose();
    }

    public static void renderDamagedVehicleModel(BakedModel model, ItemDisplayContext transformType, boolean leftHanded, PoseStack matrixStack, int stage, int color, int lightTexture, int overlayTexture)
    {
        matrixStack.pushPose();
        model.applyTransform(transformType, matrixStack, leftHanded);
        matrixStack.translate(-0.5, -0.5, -0.5);
        if(!model.isCustomRenderer())
        {
            Minecraft mc = Minecraft.getInstance();
            VertexConsumer vertexBuilder = mc.renderBuffers().crumblingBufferSource().getBuffer(ModelBakery.DESTROY_TYPES.get(stage));
            renderModel(model, ItemStack.EMPTY, color, lightTexture, overlayTexture, matrixStack, vertexBuilder);
        }
        matrixStack.popPose();
    }

    public static void renderModel(ItemStack stack, ItemDisplayContext transformType, boolean leftHanded, PoseStack matrixStack, MultiBufferSource renderTypeBuffer, int lightTexture, int overlayTexture, BakedModel model)
    {
        if(!stack.isEmpty())
        {
            matrixStack.pushPose();
            boolean isGui = transformType == ItemDisplayContext.GUI;
            boolean tridentFlag = isGui || transformType == ItemDisplayContext.GROUND || transformType == ItemDisplayContext.FIXED;
            if(stack.getItem() == Items.TRIDENT && tridentFlag)
            {
                model = Minecraft.getInstance().getModelManager().getModel(new net.minecraft.client.resources.model.ModelResourceLocation(net.minecraft.resources.ResourceLocation.parse("minecraft:trident"), "inventory"));
            }

            model.applyTransform(transformType, matrixStack, leftHanded);
            matrixStack.translate(-0.5, -0.5, -0.5);
            if(!model.isCustomRenderer() && (stack.getItem() != Items.TRIDENT || tridentFlag))
            {
                RenderType renderType = Sheets.cutoutBlockSheet();
                VertexConsumer vertexBuilder = ItemRenderer.getFoilBuffer(renderTypeBuffer, renderType, true, stack.hasFoil());
                renderModel(model, stack, -1, lightTexture, overlayTexture, matrixStack, vertexBuilder);
            }

            matrixStack.popPose();
        }
    }

    private static void renderModel(BakedModel model, ItemStack stack, int color, int lightTexture, int overlayTexture, PoseStack matrixStack, VertexConsumer vertexBuilder)
    {
        net.minecraft.util.RandomSource random = net.minecraft.util.RandomSource.create();
        for(Direction direction : Direction.values())
        {
            random.setSeed(42L);
            renderQuads(matrixStack, vertexBuilder, model.getQuads(null, direction, random), stack, color, lightTexture, overlayTexture);
        }
        random.setSeed(42L);
        renderQuads(matrixStack, vertexBuilder, model.getQuads(null, null, random), stack, color, lightTexture, overlayTexture);
    }

    private static void renderQuads(PoseStack matrixStack, VertexConsumer vertexBuilder, List<BakedQuad> quads, ItemStack stack, int color, int lightTexture, int overlayTexture)
    {
        boolean useItemColor = !stack.isEmpty() && color == -1;
        PoseStack.Pose entry = matrixStack.last();
        for(BakedQuad quad : quads)
        {
            int tintColor = 0xFFFFFF;
            if(quad.isTinted())
            {
                if(useItemColor)
                {
                    tintColor = Minecraft.getInstance().getItemColors().getColor(stack, quad.getTintIndex());
                }
                else
                {
                    tintColor = color;
                }
            }
            float red = (float) (tintColor >> 16 & 255) / 255.0F;
            float green = (float) (tintColor >> 8 & 255) / 255.0F;
            float blue = (float) (tintColor & 255) / 255.0F;
            vertexBuilder.putBulkData(entry, quad, red, green, blue, 1.0F, lightTexture, overlayTexture);
        }
    }

    public static List<Component> lines(FormattedText text, int maxWidth)
    {
        List<FormattedText> lines = Minecraft.getInstance().font.getSplitter().splitLines(text, maxWidth, Style.EMPTY);
        return lines.stream().map(t -> Component.literal(t.getString()).withStyle(ChatFormatting.GRAY)).collect(Collectors.toList());
    }
}
