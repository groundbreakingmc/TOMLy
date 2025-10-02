package com.github.groundbreakingmc.tomly.nodes.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class BooleanNode extends AbstractNode {

    private final boolean value;

    public BooleanNode(boolean value, @Nullable List<String> headerComments, @Nullable String inlineComment) {
        this(value, -1, -1, headerComments, inlineComment);
    }

    public BooleanNode(boolean value, int line, int column,
                       @Nullable List<String> headerComments, @Nullable String inlineComment) {
        super(line, column, headerComments, inlineComment);
        this.value = value;
    }

    @Override
    public @NotNull Boolean value() {
        return this.value;
    }
}
