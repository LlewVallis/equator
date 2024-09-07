package com.llewvallis.equator.properties;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Property<T> {

    private static final Logger LOG = LoggerFactory.getLogger(Property.class);

    private final String name;
    private final T defaultValue;
    private final PropertyType<T> type;

    @Nullable private String cachedOverrideUnparsed;
    private T cachedOverrideParsed;

    private Property(String name, T defaultValue, PropertyType<T> type) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.type = type;

        cachedOverrideUnparsed = null;
        cachedOverrideParsed = defaultValue;
    }

    public static Property<Integer> integer(String name, int defaultValue) {
        return new Property<>(name, defaultValue, PropertyType.INT);
    }

    public T get(PropertyOverrides overrides) {
        var override = overrides.getOverride(name);

        if (!isCached(override)) {
            cachedOverrideUnparsed = override;
            cachedOverrideParsed = parseFromOverride(override);
        }

        return cachedOverrideParsed;
    }

    @SuppressWarnings("ReferenceEquality")
    private boolean isCached(@Nullable String override) {
        // Use of reference == is intentional (for performance)
        return override == cachedOverrideUnparsed;
    }

    private T parseFromOverride(@Nullable String override) {
        if (override == null) {
            return defaultValue;
        }

        try {
            return type.parse(override);
        } catch (Exception e) {
            LOG.error("Error parsing override for property {}: {}", name, override, e);
            return defaultValue;
        }
    }

    public String name() {
        return name;
    }

    public PropertyType<T> type() {
        return type;
    }
}
