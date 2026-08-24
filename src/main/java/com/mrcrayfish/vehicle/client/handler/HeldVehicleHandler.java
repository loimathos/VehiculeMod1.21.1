package com.mrcrayfish.vehicle.client.handler;

import com.mrcrayfish.obfuscate.client.event.PlayerModelEvent;
import com.mrcrayfish.vehicle.client.render.layer.LayerHeldVehicle;
import com.mrcrayfish.vehicle.common.entity.HeldVehicleDataHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Author: MrCrayfish
 */
public class HeldVehicleHandler
{
    private static boolean setupExtraLayers = false;

    @SubscribeEvent
    public void onRenderPlayer(RenderPlayerEvent.Pre event)
    {
        if(!setupExtraLayers)
        {
            Map<PlayerSkin.Model, EntityRenderer<? extends Player>> skinMap = Minecraft.getInstance().getEntityRenderDispatcher().getSkinMap();
            this.patchPlayerRender((PlayerRenderer) skinMap.get(PlayerSkin.Model.WIDE));
            this.patchPlayerRender((PlayerRenderer) skinMap.get(PlayerSkin.Model.SLIM));
            setupExtraLayers = true;
        }
    }

    private void patchPlayerRender(PlayerRenderer player)
    {
        List<RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>>> layers = ObfuscationReflectionHelper.getPrivateValue(LivingEntityRenderer.class, player, "layers");
        if(layers != null)
        {
            layers.add(new LayerHeldVehicle(player));
        }
    }

    public static final Map<UUID, AnimationCounter> idToCounter = new HashMap<>();

    @SubscribeEvent
    public void onSetupAngles(PlayerModelEvent.SetupAngles.Post event)
    {
        PlayerModel model = event.getModel();
        Player player = event.getPlayer();

        boolean holdingVehicle = HeldVehicleDataHandler.isHoldingVehicle(player);
        if(holdingVehicle && !idToCounter.containsKey(player.getUUID()))
        {
            idToCounter.put(player.getUUID(), new AnimationCounter(40));
        }
        else if(idToCounter.containsKey(player.getUUID()))
        {
            float partialTicks = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
            if(idToCounter.get(player.getUUID()).getProgress(partialTicks) == 0F)
            {
                idToCounter.remove(player.getUUID());
                return;
            }
            if(!holdingVehicle)
            {
                AnimationCounter counter = idToCounter.get(player.getUUID());
                player.yBodyRot = player.getYHeadRot() - (player.getYHeadRot() - player.yBodyRotO) * counter.getProgress(partialTicks);
            }
        }
        else
        {
            return;
        }

        AnimationCounter counter = idToCounter.get(player.getUUID());
        counter.update(holdingVehicle);
        float partialTicks = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
        float progress = counter.getProgress(partialTicks);
        model.rightArm.xRot = (float) Math.toRadians(-180F * progress);
        model.rightArm.zRot = (float) Math.toRadians(-5F * progress);
        model.rightArm.y = (player.isCrouching() ? 3.0F : -0.5F) * progress;
        model.leftArm.xRot = (float) Math.toRadians(-180F * progress);
        model.leftArm.zRot = (float) Math.toRadians(5F * progress);
        model.leftArm.y = (player.isCrouching() ? 3.0F : -0.5F) * progress;
    }

    public static class AnimationCounter
    {
        private final int MAX_COUNT;
        private int prevCount;
        private int currentCount;

        private AnimationCounter(int maxCount)
        {
            this.MAX_COUNT = maxCount;
        }

        public int update(boolean increment)
        {
            prevCount = currentCount;
            if(increment)
            {
                if(currentCount < MAX_COUNT)
                {
                    currentCount++;
                }
            }
            else
            {
                if(currentCount > 0)
                {
                    currentCount = Math.max(0, currentCount - 2);
                }
            }
            return currentCount;
        }

        public int getMaxCount()
        {
            return MAX_COUNT;
        }

        public int getCurrentCount()
        {
            return currentCount;
        }

        public float getProgress(float partialTicks)
        {
            return (prevCount + (currentCount - prevCount) * partialTicks) / (float) MAX_COUNT;
        }
    }
}
