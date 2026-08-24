package com.mrcrayfish.vehicle.datagen;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mrcrayfish.vehicle.entity.VehicleEntity;
import com.mrcrayfish.vehicle.entity.properties.VehicleProperties;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import java.util.concurrent.CompletableFuture;
import java.util.ArrayList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;

/**
 * Author: MrCrayfish
 */
public abstract class VehiclePropertiesProvider implements DataProvider
{
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new GsonBuilder().registerTypeAdapter(VehicleProperties.class, new VehicleProperties.Serializer()).create();

    private final DataGenerator generator;
    private final Map<ResourceLocation, VehicleProperties> vehiclePropertiesMap = new HashMap<>();
    private boolean scaleWheels = false;

    protected VehiclePropertiesProvider(DataGenerator generator)
    {
        this.generator = generator;
    }

    protected final void scaleWheels()
    {
        this.scaleWheels = true;
    }

    public final void setScaleWheels(boolean scale)
    {
        this.scaleWheels = scale;
    }

    protected final void add(EntityType<? extends VehicleEntity> type, VehicleProperties.Builder builder)
    {
        this.add(BuiltInRegistries.ENTITY_TYPE.getKey(type), builder);
    }

    protected final void add(ResourceLocation id, VehicleProperties.Builder builder)
    {
        this.vehiclePropertiesMap.put(id, builder.build(this.scaleWheels));
    }

    public Map<ResourceLocation, VehicleProperties> getVehiclePropertiesMap()
    {
        return ImmutableMap.copyOf(this.vehiclePropertiesMap);
    }

    public abstract void registerProperties();

    @Override
    public CompletableFuture<?> run(CachedOutput cache)
    {
        this.vehiclePropertiesMap.clear();
        this.registerProperties();
        
        List<CompletableFuture<?>> futures = new ArrayList<>();
        
        this.vehiclePropertiesMap.forEach((id, properties) ->
        {
            String modId = id.getNamespace();
            String vehicleId = id.getPath();
            Path path = this.generator.getPackOutput().getOutputFolder().resolve("data/" + modId + "/vehicles/properties/" + vehicleId + ".json");
            JsonObject json = (JsonObject) GSON.toJsonTree(properties);
            futures.add(DataProvider.saveStable(cache, json, path));

            if(properties.getCosmetics().isEmpty())
                return;

            Path cosmeticsPath = this.generator.getPackOutput().getOutputFolder().resolve("data/" + modId + "/vehicles/cosmetics/" + vehicleId + ".json");
            JsonObject object = new JsonObject();
            object.addProperty("replace", false);
            JsonObject validModels = new JsonObject();
            properties.getCosmetics().forEach((cosmeticId, cosmeticProperties) ->
            {
                JsonArray array = new JsonArray();
                cosmeticProperties.getModelLocations().forEach(location ->
                {
                    List<ResourceLocation> disabledCosmetics = cosmeticProperties.getDisabledCosmetics().getOrDefault(location, Collections.emptyList());
                    if(disabledCosmetics.isEmpty())
                    {
                        array.add(location.toString());
                    }
                    else
                    {
                        JsonObject modelObject = new JsonObject();
                        modelObject.addProperty("model", location.toString());
                        JsonArray disables = new JsonArray();
                        disabledCosmetics.forEach(disabledCosmeticId -> disables.add(disabledCosmeticId.toString()));
                        modelObject.add("disables", disables);
                        array.add(modelObject);
                    }
                });
                validModels.add(cosmeticId.toString(), array);
            });
            object.add("valid_models", validModels);
            futures.add(DataProvider.saveStable(cache, object, cosmeticsPath));
        });
        
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    @Nonnull
    @Override
    public String getName()
    {
        return "VehicleProperties";
    }
}
