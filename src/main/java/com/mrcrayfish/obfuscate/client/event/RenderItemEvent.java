package com.mrcrayfish.obfuscate.client.event;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.HumanoidArm;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraftforge.eventbus.api.Event;

public class RenderItemEvent extends Event {
    public static class Held extends RenderItemEvent {
        private final LivingEntity entity;
        private final ItemStack stack;
        private final HumanoidArm arm;
        private final PoseStack matrixStack;
        private final MultiBufferSource renderTypeBuffer;

        public Held(LivingEntity entity, ItemStack stack, HumanoidArm arm, PoseStack matrixStack, MultiBufferSource renderTypeBuffer) {
            this.entity = entity;
            this.stack = stack;
            this.arm = arm;
            this.matrixStack = matrixStack;
            this.renderTypeBuffer = renderTypeBuffer;
        }

        public LivingEntity getEntity() {
            return this.entity;
        }

        public ItemStack getItem() {
            return this.stack;
        }

        public HumanoidArm getArm() {
            return this.arm;
        }

        public PoseStack getMatrixStack() {
            return this.matrixStack;
        }

        public MultiBufferSource getRenderTypeBuffer() {
            return this.renderTypeBuffer;
        }

        public static class Pre extends Held {
            public Pre(LivingEntity entity, ItemStack stack, HumanoidArm arm, PoseStack matrixStack, MultiBufferSource renderTypeBuffer) {
                super(entity, stack, arm, matrixStack, renderTypeBuffer);
            }
        }
    }
}
