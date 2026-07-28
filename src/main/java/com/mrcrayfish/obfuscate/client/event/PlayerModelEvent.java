package com.mrcrayfish.obfuscate.client.event;

import net.minecraft.world.entity.player.Player;
import net.minecraft.client.model.PlayerModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraftforge.eventbus.api.Event;

public class PlayerModelEvent extends Event {
    private final Player player;
    private final PlayerModel<?> model;

    public PlayerModelEvent(Player player, PlayerModel<?> model) {
        this.player = player;
        this.model = model;
    }

    public Player getPlayer() {
        return this.player;
    }

    public PlayerModel<?> getModel() {
        return this.model;
    }

    public static class Render extends PlayerModelEvent {
        private final PoseStack matrixStack;
        private final VertexConsumer builder;
        private final float partialTicks;

        public Render(Player player, PlayerModel<?> model, PoseStack matrixStack, VertexConsumer builder, float partialTicks) {
            super(player, model);
            this.matrixStack = matrixStack;
            this.builder = builder;
            this.partialTicks = partialTicks;
        }

        public PoseStack getMatrixStack() {
            return this.matrixStack;
        }

        public VertexConsumer getBuilder() {
            return this.builder;
        }

        public float getPartialTicks() {
            return this.partialTicks;
        }

        public static class Pre extends Render {
            public Pre(Player player, PlayerModel<?> model, PoseStack matrixStack, VertexConsumer builder, float partialTicks) {
                super(player, model, matrixStack, builder, partialTicks);
            }
        }

        public static class Post extends Render {
            public Post(Player player, PlayerModel<?> model, PoseStack matrixStack, VertexConsumer builder, float partialTicks) {
                super(player, model, matrixStack, builder, partialTicks);
            }
        }
    }

    public static class SetupAngles extends PlayerModelEvent {
        public SetupAngles(Player player, PlayerModel<?> model) {
            super(player, model);
        }

        public static class Post extends SetupAngles {
            public Post(Player player, PlayerModel<?> model) {
                super(player, model);
            }
        }
    }
}
