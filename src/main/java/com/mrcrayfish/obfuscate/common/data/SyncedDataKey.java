package com.mrcrayfish.obfuscate.common.data;

import net.minecraft.resources.ResourceLocation;
import java.util.function.Supplier;

public class SyncedDataKey<T> {
    private Supplier<T> defaultValueSupplier;

    public Supplier<T> getDefaultValueSupplier() {
        return defaultValueSupplier;
    }

    public static <T> Builder<T> builder(Object serializer) {
        return new Builder<>();
    }

    public static class Builder<T> {
        private Supplier<T> defaultValueSupplier;

        public Builder<T> id(ResourceLocation id) { return this; }
        public Builder<T> defaultValueSupplier(Supplier<T> supplier) {
            this.defaultValueSupplier = supplier;
            return this;
        }
        public Builder<T> resetOnDeath() { return this; }
        public SyncedDataKey<T> build() {
            SyncedDataKey<T> key = new SyncedDataKey<>();
            key.defaultValueSupplier = this.defaultValueSupplier;
            return key;
        }
    }
}
