package com.mrcrayfish.vehicle.client.handler;

import com.mrcrayfish.vehicle.Config;
import com.mrcrayfish.vehicle.entity.LandVehicleEntity;
import com.mrcrayfish.vehicle.entity.PoweredVehicleEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.loading.FMLLoader;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: MrCrayfish
 */
public class OverlayHandler
{
    private static final DecimalFormat FORMAT = new DecimalFormat("0.00");

    public static void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker)
    {
        if(!Config.CLIENT.enabledSpeedometer.get())
            return;

        Minecraft mc = Minecraft.getInstance();
        if(mc.options.hideGui)
            return;

        Player player = mc.player;
        if(player == null)
            return;

        Entity entity = player.getVehicle();
        if(!(entity instanceof PoweredVehicleEntity vehicle))
            return;

        List<Component> stats = new ArrayList<>();

        stats.add(Component.literal("BPS: ").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.RESET)
            .append(Component.literal(FORMAT.format(vehicle.getSpeed())).withStyle(ChatFormatting.YELLOW)));

        if(vehicle.requiresEnergy())
        {
            String fuel = FORMAT.format(vehicle.getCurrentEnergy()) + "/" + FORMAT.format(vehicle.getEnergyCapacity());
            stats.add(Component.literal("Fuel: ").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.RESET)
                .append(Component.literal(fuel).withStyle(ChatFormatting.YELLOW)));
        }

        if(!FMLLoader.isProduction() || Config.CLIENT.renderDebugging.get())
        {
            if(vehicle instanceof LandVehicleEntity landVehicle)
            {
                String traction = FORMAT.format(landVehicle.getTraction());
                stats.add(Component.literal("Traction: ").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.RESET)
                    .append(Component.literal(traction).withStyle(ChatFormatting.YELLOW)));

                Vec3 forward = Vec3.directionFromRotation(landVehicle.getRotationVector());
                float side = (float) landVehicle.getVelocity().normalize().cross(forward.normalize()).length();
                String sideString = FORMAT.format(side);
                stats.add(Component.literal("Side: ").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.RESET)
                    .append(Component.literal(sideString).withStyle(ChatFormatting.YELLOW)));
            }
        }

        for(int i = 0; i < stats.size(); i++)
        {
            guiGraphics.drawString(mc.font, stats.get(i), 10, 10 + 12 * i, 0xFFFFFF, true);
        }
    }
}
