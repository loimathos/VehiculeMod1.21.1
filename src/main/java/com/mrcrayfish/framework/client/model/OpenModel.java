package com.mrcrayfish.framework.client.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mrcrayfish.vehicle.Reference;
import com.mrcrayfish.vehicle.util.ExtraJSONUtils;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementRotation;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.GsonHelper;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class OpenModel implements IUnbakedGeometry<OpenModel>
{
    private final BlockModel model;

    public OpenModel(BlockModel model)
    {
        this.model = model;
    }

    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides)
    {
        return this.model.bake(baker, this.model, spriteGetter, modelState, context.useBlockLight());
    }

    @Override
    public void resolveParents(Function<ResourceLocation, UnbakedModel> modelGetter, IGeometryBakingContext context)
    {
        this.model.resolveParents(modelGetter);
    }

    @Mod.EventBusSubscriber(modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class Loader implements IGeometryLoader<OpenModel>
    {
        @Override
        public OpenModel read(JsonObject object, JsonDeserializationContext context)
        {
            return new OpenModel(Deserializer.INSTANCE.deserialize(object, BlockModel.class, context));
        }

        @SubscribeEvent
        public static void onModelRegister(ModelEvent.RegisterGeometryLoaders event)
        {
            event.register("open_model", new Loader());
        }
    }

    public static class Deserializer extends BlockModel.Deserializer
    {
        private static final BlockElement.Deserializer BLOCK_PART_DESERIALIZER = new BlockElement.Deserializer();
        private static final Deserializer INSTANCE = new Deserializer();

        @Override
        protected List<BlockElement> getElements(JsonDeserializationContext context, JsonObject object)
        {
            try
            {
                List<BlockElement> list = new ArrayList<>();
                for(JsonElement element : Objects.requireNonNull(GsonHelper.getAsJsonArray(object, "components", new JsonArray())))
                {
                    list.add(this.readBlockElement(element, context));
                }
                return list;
            }
            catch(Exception e)
            {
                throw new JsonParseException(e);
            }
        }

        private BlockElement readBlockElement(JsonElement element, JsonDeserializationContext context)
        {
            JsonObject object = element.getAsJsonObject();

            Vector3f from = ExtraJSONUtils.getAsVector3f(object, "from");
            Vector3f to = ExtraJSONUtils.getAsVector3f(object, "to");
            JsonObject rotation = GsonHelper.getAsJsonObject(object, "rotation", new JsonObject());
            float angle = GsonHelper.getAsFloat(rotation, "angle", 0F);

            JsonArray zero = new JsonArray();
            zero.add(0F);
            zero.add(0F);
            zero.add(0F);
            object.add("from", zero);
            object.add("to", zero);
            rotation.addProperty("angle", 0F);

            BlockElement e = BLOCK_PART_DESERIALIZER.deserialize(element, BlockElement.class, context);
            BlockElementRotation r = e.rotation != null ? new BlockElementRotation(e.rotation.origin(), e.rotation.axis(), angle, e.rotation.rescale()) : null;
            return new BlockElement(from, to, e.faces, r, e.shade);
        }
    }
}
