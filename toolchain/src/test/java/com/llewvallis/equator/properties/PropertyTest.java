package com.llewvallis.equator.properties;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PropertyTest {

    private final Property<Integer> property = Property.integer("test", 1);
    private final PropertyOverrides overrides = new PropertyOverrides();

    @Test
    void defaultValueIsTheDefault() {
        assertThat(property.get(overrides)).isEqualTo(1);
    }

    @Test
    void overridesAreParsedAndUsed() {
        overrides.setOverride("test", "2");
        assertThat(property.get(overrides)).isEqualTo(2);
    }

    @Test
    void canOverwriteAnOverride() {
        overrides.setOverride("test", "2");
        property.get(overrides);
        overrides.setOverride("test", "3");
        assertThat(property.get(overrides)).isEqualTo(3);
    }

    @Test
    void defaultIsUsedWhenOverrideIsMalformed() {
        overrides.setOverride("test", "foo");
        assertThat(property.get(overrides)).isEqualTo(1);
    }
}
