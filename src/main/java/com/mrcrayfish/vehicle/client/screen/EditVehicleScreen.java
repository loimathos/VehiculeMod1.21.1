package com.mrcrayfish.vehicle.client.screen;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mrcrayfish.vehicle.client.render.AbstractVehicleRenderer;
import com.mrcrayfish.vehicle.client.render.Axis;
import com.mrcrayfish.vehicle.client.render.CachedVehicle;
import com.mrcrayfish.vehicle.common.entity.Transform;
import com.mrcrayfish.vehicle.entity.EngineType;
import com.mrcrayfish.vehicle.entity.properties.PoweredProperties;
import com.mrcrayfish.vehicle.inventory.container.EditVehicleContainer;
import com.mrcrayfish.vehicle.util.CommonUtils;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.renderer.MultiBufferSource;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.client.resources.language.I18n;
import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.Container;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Quaternionf;
import net.minecraft.network.chat.Component;

import net.minecraft.ChatFormatting;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.util.Arrays;
import java.util.Collections;

public class EditVehicleScreen extends AbstractContainerScreen<EditVehicleContainer>
{
    private static final ResourceLocation GUI_TEXTURES = ResourceLocation.parse("vehicle:textures/gui/edit_vehicle.png");

    private final Inventory playerInventory;
    private final Container vehicleInventory;
    private final CachedVehicle cachedVehicle;

    private RenderTarget framebuffer;
    private boolean showHelp = true;
    private int windowZoom = 10;
    private int windowX, windowY;
    private float windowRotationX, windowRotationY;
    private boolean mouseGrabbed;
    private int mouseGrabbedButton;
    private int mouseClickedX, mouseClickedY;

    public EditVehicleScreen(EditVehicleContainer container, Inventory playerInventory, Component title)
    {
        super(container, playerInventory, title);
        this.playerInventory = playerInventory;
        this.vehicleInventory = container.getVehicleInventory();
        this.cachedVehicle = new CachedVehicle(container.getVehicle());
        this.imageHeight = 184;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY)
    {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int left = (this.width - this.imageWidth) / 2;
        int top = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(GUI_TEXTURES, left, top, 0, 0, this.imageWidth, this.imageHeight);

        if(this.cachedVehicle.getProperties().getExtended(PoweredProperties.class).getEngineType() != EngineType.NONE)
        {
            if(this.vehicleInventory.getItem(0).isEmpty())
            {
                guiGraphics.blit(GUI_TEXTURES, left + 8, top + 17, 176, 0, 16, 16);
            }
        }
        else if(this.vehicleInventory.getItem(0).isEmpty())
        {
            guiGraphics.blit(GUI_TEXTURES, left + 8, top + 17, 176, 32, 16, 16);
        }

        if(this.cachedVehicle.getProperties().canChangeWheels())
        {
            if(this.vehicleInventory.getItem(1).isEmpty())
            {
                guiGraphics.blit(GUI_TEXTURES, left + 8, top + 35, 176, 16, 16, 16);
            }
        }
        else if(this.vehicleInventory.getItem(1).isEmpty())
        {
            guiGraphics.blit(GUI_TEXTURES, left + 8, top + 35, 176, 32, 16, 16);
        }

        if(this.framebuffer != null)
        {
            this.framebuffer.bindRead();
            int startX = left + 26;
            int startY = top + 17;
            RenderSystem.disableCull();
            Matrix4f pose = new Matrix4f().identity();
            BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            builder.addVertex(pose, startX, startY, 0).setUv(0, 1);
            builder.addVertex(pose, startX, startY + 70, 0).setUv(0, 0);
            builder.addVertex(pose, startX + 142, startY + 70, 0).setUv(1, 0);
            builder.addVertex(pose, startX + 142, startY, 0).setUv(1, 1);
            BufferUploader.drawWithShader(builder.buildOrThrow());
        }
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY)
    {
        guiGraphics.drawString(this.minecraft.font, this.title.getString(), 8, 6, 4210752, false);
        guiGraphics.drawString(this.minecraft.font, this.playerInventory.getDisplayName().getString(), 8, this.imageHeight - 96 + 2, 4210752, false);

        if(this.showHelp)
        {
            PoseStack poseStack = guiGraphics.pose();
            poseStack.pushPose();
            poseStack.scale(0.5F, 0.5F, 0.5F);
            guiGraphics.drawString(this.minecraft.font, I18n.get("container.edit_vehicle.window_help"), 56, 38, 0xFFFFFF);
            poseStack.popPose();
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void renderVehicleToBuffer(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks)
    {
        Matrix4f oldProj = new Matrix4f(RenderSystem.getProjectionMatrix());

        Matrix4f projectionMatrix = new Matrix4f().perspective((float) Math.toRadians(30), 142.0F / 70.0F, 0.5F, 200.0F);
        RenderSystem.setProjectionMatrix(projectionMatrix, VertexSorting.DISTANCE_TO_ORIGIN);

        RenderSystem.setShaderLights(new Vector3f(0.2F, 1.0F, -0.7F).normalize(), new Vector3f(-0.2F, 1.0F, 0.7F).normalize());

        AbstractVehicleRenderer renderer = this.cachedVehicle.getRenderer();
        if(renderer != null)
        {
            this.bindFrameBuffer();

            PoseStack modelViewStack = new PoseStack();
            PoseStack.Pose last = modelViewStack.last();
            last.pose().identity();
            last.normal().identity();
            modelViewStack.translate(0, -20, -150);
            modelViewStack.translate(this.windowX + (this.mouseGrabbed && this.mouseGrabbedButton == 0 ? mouseX - this.mouseClickedX : 0), 0, 0);
            modelViewStack.translate(0, this.windowY - (this.mouseGrabbed && this.mouseGrabbedButton == 0 ? mouseY - this.mouseClickedY : 0), 0);

            Quaternionf quaternion = Axis.XP.rotationDegrees(20F);
            quaternion.mul(Axis.XN.rotationDegrees(this.windowRotationY - (this.mouseGrabbed && this.mouseGrabbedButton == 1 ? mouseY - this.mouseClickedY : 0)));
            quaternion.mul(Axis.YP.rotationDegrees(this.windowRotationX + (this.mouseGrabbed && this.mouseGrabbedButton == 1 ? mouseX - this.mouseClickedX : 0)));
            quaternion.mul(Axis.YP.rotationDegrees(45F));
            modelViewStack.mulPose(quaternion);

            modelViewStack.scale(this.windowZoom / 10F, this.windowZoom / 10F, this.windowZoom / 10F);
            modelViewStack.scale(22F, 22F, 22F);

            Transform position = this.cachedVehicle.getProperties().getDisplayTransform();
            modelViewStack.scale((float) position.getScale(), (float) position.getScale(), (float) position.getScale());
            modelViewStack.mulPose(Axis.XP.rotationDegrees((float) position.getRotX()));
            modelViewStack.mulPose(Axis.YP.rotationDegrees((float) position.getRotY()));
            modelViewStack.mulPose(Axis.ZP.rotationDegrees((float) position.getRotZ()));
            modelViewStack.translate(position.getX(), position.getY(), position.getZ());

            MultiBufferSource.BufferSource renderTypeBuffer = Minecraft.getInstance().renderBuffers().bufferSource();
            renderer.setupTransformsAndRender(this.menu.getVehicle(), modelViewStack, renderTypeBuffer, Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true), 15728880);
            renderTypeBuffer.endBatch();

            this.unbindFrameBuffer();
        }

        RenderSystem.setProjectionMatrix(oldProj, VertexSorting.DISTANCE_TO_ORIGIN);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks)
    {
        this.renderVehicleToBuffer(guiGraphics, mouseX, mouseY, partialTicks);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        int startX = (this.width - this.imageWidth) / 2;
        int startY = (this.height - this.imageHeight) / 2;

        if(this.vehicleInventory.getItem(0).isEmpty())
        {
            if(CommonUtils.isMouseWithin(mouseX, mouseY, startX + 7, startY + 16, 18, 18))
            {
                if(this.cachedVehicle.getProperties().getExtended(PoweredProperties.class).getEngineType() != EngineType.NONE)
                {
                    guiGraphics.renderTooltip(this.minecraft.font, Collections.singletonList(Component.literal("Engine")), java.util.Optional.empty(), mouseX, mouseY);
                }
                else
                {
                    guiGraphics.renderTooltip(this.minecraft.font, Arrays.asList(Component.literal("Engine"), Component.literal(ChatFormatting.GRAY + "Not applicable")), java.util.Optional.empty(), mouseX, mouseY);
                }
            }
        }

        if(this.vehicleInventory.getItem(1).isEmpty())
        {
            if(CommonUtils.isMouseWithin(mouseX, mouseY, startX + 7, startY + 34, 18, 18))
            {
                if(this.cachedVehicle.getProperties().canChangeWheels())
                {
                    guiGraphics.renderTooltip(this.minecraft.font, Collections.singletonList(Component.literal("Wheels")), java.util.Optional.empty(), mouseX, mouseY);
                }
                else
                {
                    guiGraphics.renderTooltip(this.minecraft.font, Arrays.asList(Component.literal("Wheels"), Component.literal(ChatFormatting.GRAY + "Not applicable")), java.util.Optional.empty(), mouseX, mouseY);
                }
            }
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY)
    {
        int startX = (this.width - this.imageWidth) / 2;
        int startY = (this.height - this.imageHeight) / 2;
        if(CommonUtils.isMouseWithin((int) mouseX, (int) mouseY, startX + 26, startY + 17, 142, 70))
        {
            if(scrollY < 0 && this.windowZoom > 0)
            {
                this.showHelp = false;
                this.windowZoom--;
            }
            else if(scrollY > 0)
            {
                this.showHelp = false;
                this.windowZoom++;
            }
        }
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        int startX = (this.width - this.imageWidth) / 2;
        int startY = (this.height - this.imageHeight) / 2;

        if(CommonUtils.isMouseWithin((int) mouseX, (int) mouseY, startX + 26, startY + 17, 142, 70))
        {
            if(!this.mouseGrabbed && (button == GLFW.GLFW_MOUSE_BUTTON_LEFT || button == GLFW.GLFW_MOUSE_BUTTON_RIGHT))
            {
                this.mouseGrabbed = true;
                this.mouseGrabbedButton = button == GLFW.GLFW_MOUSE_BUTTON_RIGHT ? 1 : 0;
                this.mouseClickedX = (int) mouseX;
                this.mouseClickedY = (int) mouseY;
                this.showHelp = false;
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button)
    {
        if(this.mouseGrabbed)
        {
            if(this.mouseGrabbedButton == 0 && button == GLFW.GLFW_MOUSE_BUTTON_LEFT)
            {
                this.mouseGrabbed = false;
                this.windowX += (mouseX - this.mouseClickedX);
                this.windowY -= (mouseY - this.mouseClickedY);
            }
            else if(mouseGrabbedButton == 1 && button == GLFW.GLFW_MOUSE_BUTTON_RIGHT)
            {
                this.mouseGrabbed = false;
                this.windowRotationX += (mouseX - this.mouseClickedX);
                this.windowRotationY -= (mouseY - this.mouseClickedY);
            }
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void bindFrameBuffer()
    {
        Minecraft minecraft = Minecraft.getInstance();
        Window window = minecraft.getWindow();
        int windowWidth = (int) (142 * window.getGuiScale());
        int windowHeight = (int) (70 * window.getGuiScale());
        if(this.framebuffer == null)
        {
            this.framebuffer = new com.mojang.blaze3d.pipeline.TextureTarget(windowWidth, windowHeight, true, Minecraft.ON_OSX);
            this.framebuffer.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
        }
        else if(this.framebuffer.width != windowWidth || this.framebuffer.height != windowHeight)
        {
            this.framebuffer.destroyBuffers();
            this.framebuffer.resize(windowWidth, windowHeight, Minecraft.ON_OSX);
        }
        this.framebuffer.clear(Minecraft.ON_OSX);
        this.framebuffer.bindWrite(true);
    }

    private void unbindFrameBuffer()
    {
        if(this.framebuffer != null)
        {
            this.framebuffer.unbindWrite();
        }
        this.minecraft.getMainRenderTarget().bindWrite(true);
    }

    @Override
    public void onClose()
    {
        super.onClose();
        if(this.framebuffer != null)
        {
            this.framebuffer.destroyBuffers();
        }
    }
}
