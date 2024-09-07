package com.llewvallis.equator.properties;

import java.util.function.Function;

public class PropertyType<T> {

    public static final PropertyType<Integer> INT =
            new PropertyType<>(Integer::parseInt, String::valueOf);

    private final Function<String, T> parser;
    private final Function<T, String> serializer;

    private PropertyType(Function<String, T> parser, Function<T, String> serializer) {
        this.parser = parser;
        this.serializer = serializer;
    }

    public T parse(String value) {
        return parser.apply(value);
    }

    public String serialize(T value) {
        return serializer.apply(value);
    }
}
