package com.mrcrayfish.vehicle.client.render;


import com.mojang.math.Axis;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mrcrayfish.vehicle.Config;
import com.mrcrayfish.vehicle.client.raytrace.EntityRayTracer;
import com.mrcrayfish.vehicle.common.entity.Transform;
import com.mrcrayfish.vehicle.entity.EntityJack;
import com.mrcrayfish.vehicle.entity.PoweredVehicleEntity;
import com.mrcrayfish.vehicle.entity.VehicleEntity;
import com.mrcrayfish.vehicle.entity.properties.VehicleProperties;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.MultiBufferSource;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import javax.annotation.Nullable;

/**
 * Author: MrCrayfish
 */
public class EntityVehicleRenderer<T extends VehicleEntity> extends EntityRenderer<T>
{
    private final AbstractVehicleRenderer<T> wrapper;

    public EntityVehicleRenderer(EntityRendererProvider.Context renderManager, AbstractVehicleRenderer<T> wrapper)
    {
        super(renderManager);
        this.wrapper = wrapper;
    }

    @Override
    public ResourceLocation getTextureLocation(T entity)
    {
        return null;
    }

    @Override
    public void render(T entity, float entityYaw, float partialTicks, PoseStack matrixStack, MultiBufferSource renderTypeBuffer, int light)
    {
        if(!entity.isAlive())
            return;

        if(entity.getVehicle() instanceof EntityJack)
            return;

        matrixStack.pushPose();
        this.wrapper.applyPreRotations(entity, matrixStack, partialTicks);
        matrixStack.mulPose(Axis.YP.rotationDegrees(-entityYaw));
        this.setupBreakAnimation(entity, matrixStack, partialTicks);
        this.wrapper.setupTransformsAndRender(entity, matrixStack, renderTypeBuffer, partialTicks, light);
        this.drawDebugging(entity, matrixStack);
        matrixStack.popPose();

        EntityRayTracer.instance().renderRayTraceElements(entity, matrixStack, renderTypeBuffer, entityYaw);
    }

    private void setupBreakAnimation(VehicleEntity vehicle, PoseStack matrixStack, float partialTicks)
    {
        float timeSinceHit = (float) vehicle.getTimeSinceHit() - partialTicks;
        if(timeSinceHit > 0.0F)
        {
            matrixStack.mulPose(Axis.ZP.rotationDegrees(Mth.sin(timeSinceHit) * timeSinceHit));
        }
    }

    private void drawDebugging(T entity, PoseStack stack)
    {
        if(!Config.CLIENT.renderDebugging.get())
            return;

        if(entity instanceof PoweredVehicleEntity)
        {
            PoweredVehicleEntity poweredVehicle = (PoweredVehicleEntity) entity;
            VehicleProperties properties = entity.getProperties();
            this.drawAxle(poweredVehicle.getFrontAxleOffset(), properties, stack);
            this.drawAxle(poweredVehicle.getRearAxleOffset(), properties, stack);
        }
    }

    private void drawAxle(@Nullable Vec3 position, VehicleProperties properties, PoseStack stack)
    {
        if(position != null)
        {
            Transform body = properties.getBodyTransform();
            double offset = properties.getWheels().stream().findFirst().map(wheel -> wheel.getOffset().y).orElse(0.0);
            Vec3 wheelOffset = new Vec3(0, properties.getWheelOffset(), 0).add(0, offset, 0);
            Vec3 axle = position.add(wheelOffset).add(body.getTranslate()).scale(0.0625).scale(body.getScale());
            this.drawLine(stack, axle.add(-1, 0, 0), axle.add(1, 0, 0), 0xC300FF);
        }
    }

    private void drawLine(PoseStack stack, Vec3 from, Vec3 to, int color)
    {
        float red = (float) (color >> 16 & 255) / 255.0F;
        float green = (float) (color >> 8 & 255) / 255.0F;
        float blue = (float) (color & 255) / 255.0F;
        RenderSystem.lineWidth(Math.max(2.0F, (float) Minecraft.getInstance().getWindow().getWidth() / 1920.0F * 2.0F));
        RenderSystem.enableDepthTest();
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR);
        buffer.addVertex(stack.last().pose(), (float) from.x, (float) from.y, (float) from.z).setColor(red, green, blue, 1.0F);
        buffer.addVertex(stack.last().pose(), (float) to.x, (float) to.y, (float) to.z).setColor(red, green, blue, 1.0F);
        BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.disableDepthTest();
    }
}
