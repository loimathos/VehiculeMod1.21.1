package com.mrcrayfish.vehicle.client;


import com.mojang.math.Axis;
import com.mrcrayfish.vehicle.Config;
import com.mrcrayfish.vehicle.client.util.MathUtil;
import com.mrcrayfish.vehicle.common.Seat;
import com.mrcrayfish.vehicle.entity.VehicleEntity;
import com.mrcrayfish.vehicle.entity.properties.VehicleProperties;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import com.mrcrayfish.vehicle.client.VehicleHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * A helper class that manages the camera rotations for vehicles
 *
 * Author: MrCrayfish
 */
@OnlyIn(Dist.CLIENT)
public class CameraHelper
{
    private VehicleProperties properties;
    private Quaternionf currentRotation;
    private Quaternionf prevRotation;
    private float pitchOffset;
    private float yawOffset;

    // Debug properties
    public float debugOffsetX;
    public float debugOffsetY;
    public float debugOffsetZ;
    public float debugOffsetPitch;
    public float debugOffsetYaw;
    public float debugOffsetRoll;
    public boolean debugEnableStrength = true;

    public void load(VehicleEntity vehicle)
    {
        this.properties = vehicle.getProperties();
        this.pitchOffset = 0F;
        this.yawOffset = 0F;
        this.currentRotation = new Quaternionf().rotationXYZ((float) Math.toRadians(vehicle.getViewPitch(1F)), (float) Math.toRadians(-vehicle.getViewYaw(1F)), (float) Math.toRadians(vehicle.getViewRoll(1F)));
        this.prevRotation = new Quaternionf(this.currentRotation);
    }

    public void tick(VehicleEntity vehicle, CameraType pov)
    {
        float strength = this.getStrength(pov);
        this.prevRotation = this.currentRotation;
        Quaternionf quaternion = new Quaternionf(0.0F, 0.0F, 0.0F, 1.0F);
        quaternion.mul(Axis.YP.rotationDegrees(-vehicle.getViewYaw(1F) + (Config.CLIENT.debugCamera.get() ? this.debugOffsetYaw : 0F)));
        quaternion.mul(Axis.XP.rotationDegrees(vehicle.getViewPitch(1F) + (Config.CLIENT.debugCamera.get() ? this.debugOffsetPitch : 0F)));
        quaternion.mul(Axis.ZP.rotationDegrees(vehicle.getViewRoll(1F) + (Config.CLIENT.debugCamera.get() ? this.debugOffsetRoll : 0F)));
        this.currentRotation = MathUtil.slerp(this.currentRotation, quaternion, strength);
    }

    private float getStrength(CameraType pov)
    {
        return (!Config.CLIENT.debugCamera.get() || this.debugEnableStrength) && pov == CameraType.THIRD_PERSON_BACK && this.properties.getCamera().getType() != CameraProperties.Type.LOCKED ? this.properties.getCamera().getStrength() : 1.0F;
    }

    public void setupVanillaCamera(Camera info, CameraType pov, VehicleEntity vehicle, LocalPlayer player, float partialTicks)
    {
        switch(pov)
        {
            case FIRST_PERSON:
                this.setupFirstPersonCamera(info, vehicle, player, partialTicks);
                break;
            case THIRD_PERSON_BACK:
                this.setupThirdPersonCamera(info, vehicle, player, partialTicks, false);
                break;
            case THIRD_PERSON_FRONT:
                this.setupThirdPersonCamera(info, vehicle, player, partialTicks, true);
                break;
        }
    }

    private void setupFirstPersonCamera(Camera info, VehicleEntity vehicle, LocalPlayer player, float partialTicks)
    {
        int index = vehicle.getSeatTracker().getSeatIndex(player.getUUID());
        if(index != -1)
        {
            if(Config.CLIENT.followVehicleOrientation.get())
            {
                this.setVehicleRotation(info, vehicle, player, partialTicks);
            }

            Seat seat = this.properties.getSeats().get(index);
            Vec3 eyePos = seat.getPosition().add(0, this.properties.getAxleOffset() + this.properties.getWheelOffset(), 0).scale(this.properties.getBodyTransform().getScale()).multiply(-1, 1, 1).add(this.properties.getBodyTransform().getTranslate()).scale(0.0625);
            eyePos = eyePos.add(0, player.getEyeHeight(), 0);
            Vector3f rotatedEyePos = new Vector3f((float) eyePos.x, (float) eyePos.y, (float) eyePos.z);
            rotatedEyePos.rotate(MathUtil.slerp(this.prevRotation, this.currentRotation, partialTicks));
            float cameraX = (float) (Mth.lerp(partialTicks, vehicle.xo, vehicle.getX()) + rotatedEyePos.x());
            float cameraY = (float) (Mth.lerp(partialTicks, vehicle.yo, vehicle.getY()) + rotatedEyePos.y());
            float cameraZ = (float) (Mth.lerp(partialTicks, vehicle.zo, vehicle.getZ()) + rotatedEyePos.z());
            info.setPosition(cameraX, cameraY, cameraZ);
        }
    }

    private void setupThirdPersonCamera(Camera info, VehicleEntity vehicle, LocalPlayer player, float partialTicks, boolean front)
    {
        if(Config.CLIENT.followVehicleOrientation.get())
        {
            this.setVehicleRotation(info, vehicle, player, partialTicks);
        }

        if(Config.CLIENT.useVehicleAsFocusPoint.get() && !front)
        {
            Vec3 position = this.properties.getCamera().getPosition();
            Vector3f rotatedPosition = new Vector3f((float) position.x, (float) position.y, (float) position.z);
            if(Config.CLIENT.debugCamera.get()) rotatedPosition.add(this.debugOffsetX, this.debugOffsetY, this.debugOffsetZ);
            rotatedPosition.rotate(MathUtil.slerp(this.prevRotation, this.currentRotation, partialTicks));
            float cameraX = (float) (Mth.lerp(partialTicks, vehicle.xo, vehicle.getX()) + rotatedPosition.x());
            float cameraY = (float) (Mth.lerp(partialTicks, vehicle.yo, vehicle.getY()) + rotatedPosition.y());
            float cameraZ = (float) (Mth.lerp(partialTicks, vehicle.zo, vehicle.getZ()) + rotatedPosition.z());
            info.setPosition(cameraX, cameraY, cameraZ);
        }
        else
        {
            int index = vehicle.getSeatTracker().getSeatIndex(player.getUUID());
            if(index != -1)
            {
                Seat seat = this.properties.getSeats().get(index);
                Vec3 eyePos = seat.getPosition().add(0, this.properties.getAxleOffset() + this.properties.getWheelOffset(), 0).scale(this.properties.getBodyTransform().getScale()).multiply(-1, 1, 1).add(this.properties.getBodyTransform().getTranslate()).scale(0.0625);
                eyePos = eyePos.add(0, player.getEyeHeight(), 0);
                Vector3f rotatedEyePos = new Vector3f((float) eyePos.x, (float) eyePos.y, (float) eyePos.z);
                rotatedEyePos.rotate(MathUtil.slerp(this.prevRotation, this.currentRotation, partialTicks));
                float cameraX = (float) (Mth.lerp(partialTicks, vehicle.xo, vehicle.getX()) + rotatedEyePos.x());
                float cameraY = (float) (Mth.lerp(partialTicks, vehicle.yo, vehicle.getY()) + rotatedEyePos.y());
                float cameraZ = (float) (Mth.lerp(partialTicks, vehicle.zo, vehicle.getZ()) + rotatedEyePos.z());
                info.setPosition(cameraX, cameraY, cameraZ);
            }
        }

        // 1.21.1 changed these from double to float
        float distance = front ? 4.0F : (float) this.properties.getCamera().getDistance();
        info.move(-info.getMaxZoom(distance), 0.0F, 0.0F);
    }

    private void setVehicleRotation(Camera info, VehicleEntity vehicle, LocalPlayer player, float partialTicks)
    {
        Quaternionf rotation = info.rotation();
        rotation.set(0.0F, 0.0F, 0.0F, 1.0F);

        // Applies the vehicle's body rotations to the camera
        //TODO add this back
        /*if(Config.CLIENT.shouldFollowYaw.get())
        {
            rotation.mul(Axis.YP.rotationDegrees(-this.getYaw(partialTicks)));
        }
        if(Config.CLIENT.shouldFollowPitch.get())
        {
            rotation.mul(Axis.XP.rotationDegrees(this.getPitch(partialTicks)));
        }
        if(Config.CLIENT.shouldFollowRoll.get())
        {
            rotation.mul(Axis.ZP.rotationDegrees(this.getRoll(partialTicks)));
        }*/

        rotation.mul(MathUtil.slerp(this.prevRotation, this.currentRotation, partialTicks));

        // Applies the player's pitch and yaw offset
        Quaternionf quaternion = new Quaternionf(0.0F, 0.0F, 0.0F, 1.0F);

        if(VehicleHelper.isThirdPersonFront())
        {
            quaternion.mul(Axis.YP.rotationDegrees(180F));
        }

        if(vehicle.canApplyYawOffset(player) && Config.CLIENT.shouldFollowYaw.get())
        {
            quaternion.mul(Axis.YP.rotationDegrees(-this.yawOffset));
        }
        else
        {
            quaternion.mul(Axis.YP.rotationDegrees(-player.getViewYRot(partialTicks)));
            if(Config.CLIENT.shouldFollowYaw.get())
            {
                quaternion.mul(Axis.YP.rotationDegrees(vehicle.getViewYaw(partialTicks)));
            }
        }

        if(Config.CLIENT.shouldFollowPitch.get())
        {
            quaternion.mul(Axis.XP.rotationDegrees(VehicleHelper.isThirdPersonFront() ? -this.pitchOffset : this.pitchOffset));
        }
        else
        {
            quaternion.mul(Axis.XP.rotationDegrees(Mth.lerp(partialTicks, player.xRotO, player.getXRot())));
        }

        // If the player is in third person, applies additional vehicle specific camera rotations
        if(Config.CLIENT.useVehicleAsFocusPoint.get() && VehicleHelper.isThirdPersonBack())
        {
            CameraProperties camera = vehicle.getProperties().getCamera();
            Vec3 cameraRotation = camera.getRotation();
            quaternion.mul(Axis.YP.rotationDegrees((float) cameraRotation.y));
            quaternion.mul(Axis.XP.rotationDegrees((float) cameraRotation.x));
            quaternion.mul(Axis.ZP.rotationDegrees((float) cameraRotation.z));
        }

        // Finally applies local rotations to the camera
        rotation.mul(quaternion);

        Vector3f forward = info.getLookVector();
        forward.set(0.0F, 0.0F, 1.0F);
        forward.rotate(rotation);

        Vector3f up = info.getUpVector();
        up.set(0.0F, 1.0F, 0.0F);
        up.rotate(rotation);

        // Camera#getLeftVector() is public - the old reflection on the private 'left' field was never needed
        Vector3f left = info.getLeftVector();
        left.set(1.0F, 0.0F, 0.0F);
        left.rotate(rotation);
    }

    public void turnPlayerView(double x, double y)
    {
        this.pitchOffset += y * 0.15F;
        this.yawOffset += x * 0.15F;
        this.pitchOffset = Mth.clamp(this.pitchOffset, -90F, 90F);
        this.yawOffset = Mth.clamp(this.yawOffset, -120F, 120F);
    }
}
