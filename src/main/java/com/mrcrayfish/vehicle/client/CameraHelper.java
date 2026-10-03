package com.mrcrayfish.vehicle.client;

import com.mrcrayfish.vehicle.Config;
import com.mrcrayfish.vehicle.client.util.MathUtil;
import com.mrcrayfish.vehicle.common.Seat;
import com.mrcrayfish.vehicle.entity.VehicleEntity;
import com.mrcrayfish.vehicle.entity.properties.VehicleProperties;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ViewportEvent;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import javax.annotation.Nullable;

/**
 * A helper class that manages camera rotations and positioning for vehicles
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
        float yaw = vehicle.getViewYaw(1.0F);
        float pitch = vehicle.getViewPitch(1.0F);
        float roll = vehicle.getViewRoll(1.0F);
        this.currentRotation = new Quaternionf().rotationYXZ(
            (float) Math.PI - yaw * ((float) Math.PI / 180.0F),
            -pitch * ((float) Math.PI / 180.0F),
            roll * ((float) Math.PI / 180.0F)
        );
        this.prevRotation = new Quaternionf(this.currentRotation);
    }

    public void tick(VehicleEntity vehicle, CameraType pov)
    {
        if(this.currentRotation == null)
        {
            this.load(vehicle);
            return;
        }

        float strength = this.getStrength(pov);
        this.prevRotation = new Quaternionf(this.currentRotation);
        float yaw = vehicle.getViewYaw(1.0F) + (Config.CLIENT.debugCamera.get() ? this.debugOffsetYaw : 0.0F);
        float pitch = vehicle.getViewPitch(1.0F) + (Config.CLIENT.debugCamera.get() ? this.debugOffsetPitch : 0.0F);
        float roll = vehicle.getViewRoll(1.0F) + (Config.CLIENT.debugCamera.get() ? this.debugOffsetRoll : 0.0F);

        Quaternionf targetRotation = new Quaternionf().rotationYXZ(
            (float) Math.PI - yaw * ((float) Math.PI / 180.0F),
            -pitch * ((float) Math.PI / 180.0F),
            roll * ((float) Math.PI / 180.0F)
        );
        this.currentRotation = MathUtil.slerp(this.currentRotation, targetRotation, strength);
    }

    private float getStrength(CameraType pov)
    {
        return (!Config.CLIENT.debugCamera.get() || this.debugEnableStrength)
            && pov == CameraType.THIRD_PERSON_BACK
            && this.properties != null
            && this.properties.getCamera().getType() != CameraProperties.Type.LOCKED
            ? this.properties.getCamera().getStrength()
            : 1.0F;
    }

    public void setupVanillaCamera(ViewportEvent.ComputeCameraAngles event, CameraType pov, VehicleEntity vehicle, LocalPlayer player, float partialTicks)
    {
        if(this.currentRotation == null || this.prevRotation == null)
        {
            this.load(vehicle);
        }

        Camera info = event.getCamera();
        switch(pov)
        {
            case FIRST_PERSON:
                this.setupFirstPersonCamera(event, info, vehicle, player, partialTicks);
                break;
            case THIRD_PERSON_BACK:
                this.setupThirdPersonCamera(event, info, vehicle, player, partialTicks, false);
                break;
            case THIRD_PERSON_FRONT:
                this.setupThirdPersonCamera(event, info, vehicle, player, partialTicks, true);
                break;
        }
    }

    public void setupVanillaCameraLegacy(Camera info, CameraType pov, VehicleEntity vehicle, LocalPlayer player, float partialTicks)
    {
        if(this.currentRotation == null || this.prevRotation == null)
        {
            this.load(vehicle);
        }

        switch(pov)
        {
            case FIRST_PERSON:
                this.setupFirstPersonCamera(null, info, vehicle, player, partialTicks);
                break;
            case THIRD_PERSON_BACK:
                this.setupThirdPersonCamera(null, info, vehicle, player, partialTicks, false);
                break;
            case THIRD_PERSON_FRONT:
                this.setupThirdPersonCamera(null, info, vehicle, player, partialTicks, true);
                break;
        }
    }

    private void setupFirstPersonCamera(@Nullable ViewportEvent.ComputeCameraAngles event, Camera info, VehicleEntity vehicle, LocalPlayer player, float partialTicks)
    {
        int index = vehicle.getSeatTracker().getSeatIndex(player.getUUID());
        if(index == -1 || index >= vehicle.getProperties().getSeats().size())
        {
            return;
        }

        VehicleProperties properties = vehicle.getProperties();
        Seat seat = properties.getSeats().get(index);

        double vehicleX = Mth.lerp(partialTicks, vehicle.xo, vehicle.getX());
        double vehicleY = Mth.lerp(partialTicks, vehicle.yo, vehicle.getY());
        double vehicleZ = Mth.lerp(partialTicks, vehicle.zo, vehicle.getZ());
        float vehicleYaw = vehicle.getViewYaw(partialTicks);
        float vehiclePitch = vehicle.getViewPitch(partialTicks);
        float vehicleRoll = vehicle.getViewRoll(partialTicks);

        Vec3 seatVec = seat.getPosition()
            .add(0, properties.getAxleOffset() + properties.getWheelOffset(), 0)
            .scale(properties.getBodyTransform().getScale())
            .multiply(-1, 1, 1)
            .add(properties.getBodyTransform().getTranslate())
            .scale(0.0625);

        double eyeHeight = player.getEyeHeight() - 0.35D;
        Vector3f headPos = new Vector3f((float) -seatVec.x, (float) (seatVec.y + eyeHeight), (float) -seatVec.z);

        Quaternionf vehicleOrientation = MathUtil.slerp(this.prevRotation, this.currentRotation, partialTicks);
        headPos.rotate(vehicleOrientation);

        double camX = vehicleX + headPos.x();
        double camY = vehicleY + headPos.y();
        double camZ = vehicleZ + headPos.z();

        float yaw = player.getViewYRot(partialTicks);
        float pitch = player.getViewXRot(partialTicks);
        float roll = Config.CLIENT.followVehicleOrientation.get() && Config.CLIENT.shouldFollowRoll.get() ? vehicleRoll : 0.0F;

        info.setPosition(camX, camY, camZ);
        info.setRotation(yaw, pitch, roll);

        if(event != null)
        {
            event.setYaw(yaw);
            event.setPitch(pitch);
            event.setRoll(roll);
        }
    }

    private void setupThirdPersonCamera(@Nullable ViewportEvent.ComputeCameraAngles event, Camera info, VehicleEntity vehicle, LocalPlayer player, float partialTicks, boolean front)
    {
        CameraProperties camProps = vehicle.getProperties().getCamera();

        double vehicleX = Mth.lerp(partialTicks, vehicle.xo, vehicle.getX());
        double vehicleY = Mth.lerp(partialTicks, vehicle.yo, vehicle.getY());
        double vehicleZ = Mth.lerp(partialTicks, vehicle.zo, vehicle.getZ());

        Quaternionf vehicleOrientation = MathUtil.slerp(this.prevRotation, this.currentRotation, partialTicks);

        double focusX;
        double focusY;
        double focusZ;

        if(Config.CLIENT.useVehicleAsFocusPoint.get() && !front)
        {
            Vec3 camPos = camProps.getPosition();
            double focusHeight = camPos.y > 0.0 ? camPos.y : Math.max(vehicle.getBbHeight() * 0.75, 1.2);

            Vector3f focusOffset = new Vector3f((float) camPos.x, (float) focusHeight, (float) camPos.z);
            if(Config.CLIENT.debugCamera.get())
            {
                focusOffset.add(this.debugOffsetX, this.debugOffsetY, this.debugOffsetZ);
            }
            focusOffset.rotate(vehicleOrientation);

            focusX = vehicleX + focusOffset.x();
            focusY = vehicleY + focusOffset.y();
            focusZ = vehicleZ + focusOffset.z();
        }
        else
        {
            int index = vehicle.getSeatTracker().getSeatIndex(player.getUUID());
            if(index != -1 && index < vehicle.getProperties().getSeats().size())
            {
                VehicleProperties properties = vehicle.getProperties();
                Seat seat = properties.getSeats().get(index);
                Vec3 seatVec = seat.getPosition()
                    .add(0, properties.getAxleOffset() + properties.getWheelOffset(), 0)
                    .scale(properties.getBodyTransform().getScale())
                    .multiply(-1, 1, 1)
                    .add(properties.getBodyTransform().getTranslate())
                    .scale(0.0625);
                double eyeHeight = player.getEyeHeight() - 0.35D;
                Vector3f headPos = new Vector3f((float) -seatVec.x, (float) (seatVec.y + eyeHeight), (float) -seatVec.z);
                headPos.rotate(vehicleOrientation);
                focusX = vehicleX + headPos.x();
                focusY = vehicleY + headPos.y();
                focusZ = vehicleZ + headPos.z();
            }
            else
            {
                focusX = vehicleX;
                focusY = vehicleY + Math.max(vehicle.getBbHeight() * 0.75, 1.2);
                focusZ = vehicleZ;
            }
        }

        float yaw = player.getViewYRot(partialTicks);
        float pitch = player.getViewXRot(partialTicks);
        float roll = 0.0F;

        if(Config.CLIENT.followVehicleOrientation.get() && Config.CLIENT.shouldFollowRoll.get())
        {
            roll = vehicle.getViewRoll(partialTicks);
        }

        if(front)
        {
            yaw += 180.0F;
            pitch = -pitch;
            roll = -roll;
        }
        else
        {
            Vec3 camRot = camProps.getRotation();
            if(camRot.x != 0.0)
            {
                pitch += (float) camRot.x;
            }
        }
        pitch = Mth.clamp(pitch, -89.9F, 89.9F);

        float targetDistance = front ? 4.0F : (float) camProps.getDistance();
        if(targetDistance <= 0.0F)
        {
            targetDistance = 5.0F;
        }

        Vec3 lookVec = Vec3.directionFromRotation(pitch, yaw);
        Vec3 backVec = new Vec3(-lookVec.x, -lookVec.y, -lookVec.z);

        info.setRotation(yaw, pitch, roll);
        info.setPosition(focusX, focusY, focusZ);
        float actualDistance = info.getMaxZoom(targetDistance);
        actualDistance = Math.max(actualDistance, 0.75F);

        double camX = focusX + backVec.x * actualDistance;
        double camY = focusY + backVec.y * actualDistance;
        double camZ = focusZ + backVec.z * actualDistance;

        info.setPosition(camX, camY, camZ);

        if(event != null)
        {
            event.setYaw(yaw);
            event.setPitch(pitch);
            event.setRoll(roll);
        }
    }

    public void turnPlayerView(double x, double y)
    {
        this.pitchOffset += y * 0.15F;
        this.yawOffset += x * 0.15F;
        this.pitchOffset = Mth.clamp(this.pitchOffset, -90F, 90F);
        this.yawOffset = Mth.clamp(this.yawOffset, -120F, 120F);
    }
}
