package com.llewvallis.equator.properties;

import java.util.HashMap;
import java.util.Map;
import org.jspecify.annotations.Nullable;

public class PropertyOverrides {

    private final Map<String, String> overrides = new HashMap<>();

    public @Nullable String getOverride(String name) {
        return overrides.get(name);
    }

    public void setOverride(String name, String value) {
        overrides.put(name, value);
    }

    public <T> void setOverride(Property<T> property, T value) {
        setOverride(property.name(), property.type().serialize(value));
    }
}
