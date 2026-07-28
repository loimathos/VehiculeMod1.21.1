package com.mrcrayfish.vehicle.client.handler;

import com.mrcrayfish.vehicle.Config;
import com.mrcrayfish.vehicle.client.ClientHandler;
import com.mrcrayfish.vehicle.entity.VehicleEntity;
import com.mrcrayfish.vehicle.network.PacketHandler;
import com.mrcrayfish.vehicle.network.message.MessageCycleSeats;
import com.mrcrayfish.vehicle.network.message.MessageHitchTrailer;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

/**
 * Manages controller input
 *
 * Author: MrCrayfish
 */
@OnlyIn(Dist.CLIENT)
public class ControllerHandler
{
    public static final IKeyConflictContext VEHICLE_KEY_CONFLICT = new VehicleKeyConflict();
    public static final IKeyConflictContext AIR_VEHICLE_KEY_CONFLICT = new VehicleKeyConflict();

    public static void init()
    {
    }

    public static boolean isRightClicking()
    {
        boolean isRightClicking = GLFW.glfwGetMouseButton(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
        if(ClientHandler.isControllableLoaded())
        {
        }
        return isRightClicking;
    }

    public static class VehicleKeyConflict implements IKeyConflictContext
    {
        @Override
        public boolean isActive()
        {
            return !KeyConflictContext.GUI.isActive();
        }

        @Override
        public boolean conflicts(IKeyConflictContext other)
        {
            return this == other;
        }
    }
}
