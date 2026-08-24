package com.mrcrayfish.vehicle.client.render.vehicle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mrcrayfish.vehicle.client.model.VehicleModels;
import com.mrcrayfish.vehicle.client.raytrace.RayTraceTransforms;
import com.mrcrayfish.vehicle.client.raytrace.TransformHelper;
import com.mrcrayfish.vehicle.client.render.AbstractTrailerRenderer;
import com.mrcrayfish.vehicle.entity.properties.VehicleProperties;
import com.mrcrayfish.vehicle.entity.trailer.FluidTrailerEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.joml.Matrix4f;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import javax.annotation.Nullable;

/**
 * Author: MrCrayfish
 */
public class FluidTrailerRenderer extends AbstractTrailerRenderer<FluidTrailerEntity>
{
    protected final PropertyFunction<FluidTrailerEntity, FluidTank> fluidTankProperty = new PropertyFunction<>(FluidTrailerEntity::getTank, new FluidTank(1000));

    public FluidTrailerRenderer(EntityType<FluidTrailerEntity> type, VehicleProperties defaultProperties)
    {
        super(type, defaultProperties);
    }

    @Override
    public void render(@Nullable FluidTrailerEntity vehicle, PoseStack matrixStack, MultiBufferSource renderTypeBuffer, float partialTicks, int light)
    {
        this.renderDamagedPart(vehicle, VehicleModels.FLUID_TRAILER, matrixStack, renderTypeBuffer, light, partialTicks);

        FluidTank tank = this.fluidTankProperty.get();
        float height = 9.9F * (tank.getFluidAmount() / (float) tank.getCapacity()) * 0.0625F;
        this.drawFluid(vehicle, tank, matrixStack, renderTypeBuffer, -0.3875F, -0.1875F, -0.99F, 0.7625F, height, 1.67F, light);
    }

    private void drawFluid(@Nullable FluidTrailerEntity vehicle, FluidTank tank, PoseStack matrixStack, MultiBufferSource renderTypeBuffer, float x, float y, float z, float width, float height, float depth, int light)
    {
        if(tank.isEmpty()) return;
        FluidStack fluidStack = tank.getFluid();
        Fluid fluid = fluidStack.getFluid();
        if(fluid == Fluids.EMPTY)
            return;

        TextureAtlasSprite sprite = Minecraft.getInstance().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS).getSprite(IClientFluidTypeExtensions.of(fluid).getStillTexture());

        int fluidColor = vehicle != null ? IClientFluidTypeExtensions.of(fluid).getTintColor(fluidStack) : 0xFF3F76E4;
        float red = (float) (fluidColor >> 16 & 255) / 255.0F;
        float green = (float) (fluidColor >> 8 & 255) / 255.0F;
        float blue = (float) (fluidColor & 255) / 255.0F;
        float minU = sprite.getU0();
        float maxU = Math.min(minU + (sprite.getU1() - minU) * width, sprite.getU1());
        float minV = sprite.getV0();
        float maxV = Math.min(minV + (sprite.getV1() - minV) * height, sprite.getV1());

        VertexConsumer buffer = renderTypeBuffer.getBuffer(RenderType.translucent());
        Matrix4f matrix = matrixStack.last().pose();

        //left side
        buffer.addVertex(matrix, x + width, y, z).setColor(red - 0.25F, green - 0.25F, blue - 0.25F, 1.0F).setUv(maxU, minV).setUv2(light & 0xFFFF, light >> 16 & 0xFFFF).setNormal(0.0F, 1.0F, 0.0F);
        buffer.addVertex(matrix, x, y, z).setColor(red - 0.25F, green - 0.25F, blue - 0.25F, 1.0F).setUv(minU, minV).setUv2(light & 0xFFFF, light >> 16 & 0xFFFF).setNormal(0.0F, 1.0F, 0.0F);
        buffer.addVertex(matrix, x, y + height, z).setColor(red - 0.25F, green - 0.25F, blue - 0.25F, 1.0F).setUv(minU, maxV).setUv2(light & 0xFFFF, light >> 16 & 0xFFFF).setNormal(0.0F, 1.0F, 0.0F);
        buffer.addVertex(matrix, x + width, y + height, z).setColor(red - 0.25F, green - 0.25F, blue - 0.25F, 1.0F).setUv(maxU, maxV).setUv2(light & 0xFFFF, light >> 16 & 0xFFFF).setNormal(0.0F, 1.0F, 0.0F);

        buffer.addVertex(matrix, x, y, z + depth).setColor(red - 0.25F, green - 0.25F, blue - 0.25F, 1.0F).setUv(maxU, minV).setUv2(light & 0xFFFF, light >> 16 & 0xFFFF).setNormal(0.0F, 1.0F, 0.0F);
        buffer.addVertex(matrix, x + width, y, z + depth).setColor(red - 0.25F, green - 0.25F, blue - 0.25F, 1.0F).setUv(minU, minV).setUv2(light & 0xFFFF, light >> 16 & 0xFFFF).setNormal(0.0F, 1.0F, 0.0F);
        buffer.addVertex(matrix, x + width, y + height, z + depth).setColor(red - 0.25F, green - 0.25F, blue - 0.25F, 1.0F).setUv(minU, maxV).setUv2(light & 0xFFFF, light >> 16 & 0xFFFF).setNormal(0.0F, 1.0F, 0.0F);
        buffer.addVertex(matrix, x, y + height, z + depth).setColor(red - 0.25F, green - 0.25F, blue - 0.25F, 1.0F).setUv(maxU, maxV).setUv2(light & 0xFFFF, light >> 16 & 0xFFFF).setNormal(0.0F, 1.0F, 0.0F);

        maxU = Math.min(minU + (sprite.getU1() - minU) * depth, sprite.getU1());
        maxV = Math.min(minV + (sprite.getV1() - minV) * width, sprite.getV1());

        buffer.addVertex(matrix, x, y + height, z).setColor(red, green, blue, 1.0F).setUv(maxU, minV).setUv2(light & 0xFFFF, light >> 16 & 0xFFFF).setNormal(0.0F, 1.0F, 0.0F);
        buffer.addVertex(matrix, x, y + height, z + depth).setColor(red, green, blue, 1.0F).setUv(minU, minV).setUv2(light & 0xFFFF, light >> 16 & 0xFFFF).setNormal(0.0F, 1.0F, 0.0F);
        buffer.addVertex(matrix, x + width, y + height, z + depth).setColor(red, green, blue, 1.0F).setUv(minU, maxV).setUv2(light & 0xFFFF, light >> 16 & 0xFFFF).setNormal(0.0F, 1.0F, 0.0F);
        buffer.addVertex(matrix, x + width, y + height, z).setColor(red, green, blue, 1.0F).setUv(maxU, maxV).setUv2(light & 0xFFFF, light >> 16 & 0xFFFF).setNormal(0.0F, 1.0F, 0.0F);
    }

    @Nullable
    @Override
    public RayTraceTransforms getRayTraceTransforms()
    {
        return (tracer, transforms, parts) ->
        {
            TransformHelper.createTransformListForPart(VehicleModels.FLUID_TRAILER, parts, transforms);
        };
    }
}
