package com.mrcrayfish.vehicle.client.screen.toolbar.widget;

import com.google.common.collect.ImmutableMap;
import com.mrcrayfish.vehicle.Reference;
import com.mrcrayfish.vehicle.client.screen.DashboardScreen;
import com.mrcrayfish.vehicle.common.cosmetic.CosmeticProperties;
import com.mrcrayfish.vehicle.common.cosmetic.actions.OpenableAction;
import com.mrcrayfish.vehicle.entity.VehicleEntity;
import com.mrcrayfish.vehicle.network.PacketHandler;
import com.mrcrayfish.vehicle.network.message.MessageInteractCosmetic;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.PacketDistributor;

public class DoorButton extends IconButton
{
    private static final ImmutableMap<ResourceLocation, DashboardScreen.Icons> ICON_MAP = Util.make(() -> {
        ImmutableMap.Builder<ResourceLocation, DashboardScreen.Icons> builder = ImmutableMap.builder();
        builder.put(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "left_door"), DashboardScreen.Icons.LEFT_DOOR);
        builder.put(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "right_door"), DashboardScreen.Icons.RIGHT_DOOR);
        builder.put(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "hood"), DashboardScreen.Icons.HOOD);
        builder.put(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "trunk"), DashboardScreen.Icons.TRUNK);
        builder.put(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "spoiler"), DashboardScreen.Icons.TRUNK);
        return builder.build();
    });

    private final OpenableAction action;

    public DoorButton(VehicleEntity entity, CosmeticProperties properties, OpenableAction action)
    {
        super(20, 20, ICON_MAP.getOrDefault(properties.getId(), DashboardScreen.Icons.LEFT_DOOR), Component.translatable(properties.getId().getNamespace() + ".toolbar.label." + properties.getId().getPath()), onPress -> {
            PacketHandler.getPlayChannel().send(new MessageInteractCosmetic(entity.getId(), properties.getId()), PacketDistributor.SERVER.noArg());
        });
        this.action = action;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks)
    {
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTicks);
        int backgroundColor = this.action.isOpen() ? 0xFFFFB64C : 0xFF941400;
        int foregroundColor = this.action.isOpen() ? 0xFFFFC54C : 0xFFBD2008;
        guiGraphics.fill(this.getX() + 3, this.getY() + this.height - 6, this.getX() + this.width - 3, this.getY() + this.height - 3, backgroundColor);
        guiGraphics.fill(this.getX() + 3, this.getY() + this.height - 6, this.getX() + this.width - 4, this.getY() + this.height - 4, foregroundColor);
    }

    @Override
    protected void drawIcon(GuiGraphics guiGraphics, int x, int y, int u, int v)
    {
        super.drawIcon(guiGraphics, x, y - 2, u, v);
    }
}
