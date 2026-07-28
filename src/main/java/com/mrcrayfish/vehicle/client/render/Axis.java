package com.mrcrayfish.vehicle.client.render;

import org.joml.Quaternionf;
import org.joml.Vector3f;

public class Axis
{
    public static final Axis NEGATIVE_X = new Axis(-1, 0, 0);
    public static final Axis POSITIVE_X = new Axis(1, 0, 0);
    public static final Axis NEGATIVE_Y = new Axis(0, -1, 0);
    public static final Axis POSITIVE_Y = new Axis(0, 1, 0);
    public static final Axis NEGATIVE_Z = new Axis(0, 0, -1);
    public static final Axis POSITIVE_Z = new Axis(0, 0, 1);

    public static final Axis XN = NEGATIVE_X;
    public static final Axis XP = POSITIVE_X;
    public static final Axis YN = NEGATIVE_Y;
    public static final Axis YP = POSITIVE_Y;
    public static final Axis ZN = NEGATIVE_Z;
    public static final Axis ZP = POSITIVE_Z;

    private final Vector3f axis;

    private Axis(float x, float y, float z)
    {
        this.axis = new Vector3f(x, y, z);
    }

    public Quaternionf rotationDegrees(float degrees)
    {
        return new Quaternionf().rotationAxis((float) Math.toRadians(degrees), this.axis);
    }
}
