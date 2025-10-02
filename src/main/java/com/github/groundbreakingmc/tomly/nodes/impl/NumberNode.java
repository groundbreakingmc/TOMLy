package com.github.groundbreakingmc.tomly.nodes.impl;

import com.github.groundbreakingmc.tomly.options.WriterOptions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class NumberNode extends AbstractNode {

    private final Number value;

    public NumberNode(@NotNull Number value, @Nullable List<String> headerComments, @Nullable String inlineComment) {
        this(value, -1, -1, headerComments, inlineComment);
    }

    public NumberNode(@NotNull Number value, int line, int column,
                      @Nullable List<String> headerComments, @Nullable String inlineComment) {
        super(line, column, headerComments, inlineComment);
        this.value = value;
    }

    @Override
    public @NotNull Number value() {
        return this.value;
    }

    public int intValue() {
        return this.value.intValue();
    }

    public long longValue() {
        return this.value.longValue();
    }

    public float floatValue() {
        return this.value.floatValue();
    }

    public double doubleValue() {
        return this.value.doubleValue();
    }

    @Override
    public @NotNull String saveToString(@NotNull WriterOptions options) {
        if (this.value instanceof Double dbl) {
            if (Double.isNaN(dbl)) return "nan";
            if (dbl == Double.POSITIVE_INFINITY) return "+inf";
            if (dbl == Double.NEGATIVE_INFINITY) return "-inf";
        }
        return this.value.toString();
    }
}
