package com.mrcrayfish.vehicle.client.raytrace;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;

/**
 * Author: MrCrayfish
 */
public class Triangle
{
    private final float[] data;

    public Triangle(float[] data)
    {
        this.data = data;
    }

    public float[] getVertices()
    {
        return this.data;
    }

    public void draw(PoseStack matrixStack, VertexConsumer builder, float red, float green, float blue, float alpha)
    {
        Matrix4f matrix = matrixStack.last().pose();
        builder.addVertex(matrix, this.data[6], this.data[7], this.data[8]).setColor(red, green, blue, alpha);
        builder.addVertex(matrix, this.data[0], this.data[1], this.data[2]).setColor(red, green, blue, alpha);
        builder.addVertex(matrix, this.data[0], this.data[1], this.data[2]).setColor(red, green, blue, alpha);
        builder.addVertex(matrix, this.data[3], this.data[4], this.data[5]).setColor(red, green, blue, alpha);
        builder.addVertex(matrix, this.data[3], this.data[4], this.data[5]).setColor(red, green, blue, alpha);
        builder.addVertex(matrix, this.data[6], this.data[7], this.data[8]).setColor(red, green, blue, alpha);
    }
}
