package com.mrcrayfish.vehicle.util;

import org.joml.Vector3f;

import java.util.Arrays;

/**
 * Author: MrCrayfish
 */
public enum Axis
{
    X(new Vector3f(1, 0, 0), "x"),
    Y(new Vector3f(0, 1, 0), "y"),
    Z(new Vector3f(0, 0, 1), "z");

    private final Vector3f axis;
    private final String key;

    Axis(Vector3f axis, String key)
    {
        this.axis = axis;
        this.key = key;
    }

    public Vector3f getAxis()
    {
        return this.axis;
    }

    public String getKey()
    {
        return this.key;
    }

    public com.mojang.math.Axis getMojangAxis()
    {
        return switch(this) {
            case X -> com.mojang.math.Axis.XP;
            case Y -> com.mojang.math.Axis.YP;
            case Z -> com.mojang.math.Axis.ZP;
        };
    }

    public static Axis fromKey(String key)
    {
        return Arrays.stream(values()).filter(axis -> axis.key.equals(key)).findFirst().orElse(Axis.X);
    }
}
