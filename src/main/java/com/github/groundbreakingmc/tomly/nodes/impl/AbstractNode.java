package com.github.groundbreakingmc.tomly.nodes.impl;

import com.github.groundbreakingmc.tomly.nodes.Node;
import com.github.groundbreakingmc.tomly.options.WriterOptions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static java.util.Objects.requireNonNullElse;

public sealed abstract class AbstractNode implements Node permits ArrayNode, BooleanNode, DatetimeNode, NumberNode, StringNode, TableNode {

    private final int line;
    private final int column;
    private final @NotNull List<String> headerComments;
    private final @Nullable String inlineComment;

    protected AbstractNode(int line, int column,
                           @Nullable List<String> headerComments,
                           @Nullable String inlineComment) {
        this.line = line;
        this.column = column;
        this.headerComments = requireNonNullElse(headerComments, List.of());
        this.inlineComment = inlineComment;
    }

    @Override
    public final int line() {
        return this.line;
    }

    @Override
    public final int column() {
        return this.column;
    }

    @Override
    public final @NotNull List<String> headerComments() {
        return this.headerComments;
    }

    @Override
    public final @Nullable String inlineComment() {
        return this.inlineComment;
    }

    public String toString() {
        return this.getClass().getName()
                + "(value=" + this.value()
                + ", line=" + this.line
                + ", column=" + this.column
                + ", headerComments=" + this.headerComments
                + ", inlineComment=" + this.inlineComment
                + ")";
    }

    @Override
    public @NotNull String saveToString(@NotNull WriterOptions options) {
        return this.value().toString();
    }
}
