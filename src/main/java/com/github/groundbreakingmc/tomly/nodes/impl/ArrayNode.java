package com.github.groundbreakingmc.tomly.nodes.impl;

import com.github.groundbreakingmc.tomly.nodes.Node;
import com.github.groundbreakingmc.tomly.options.WriterOptions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class ArrayNode extends AbstractNode {

    private final @NotNull List<Node> elements;

    public ArrayNode(@NotNull List<Node> elements, @Nullable List<String> headerComments, @Nullable String inlineComment) {
        this(elements, -1, -1, headerComments, inlineComment);
    }

    public ArrayNode(@NotNull List<Node> elements, int line, int column,
                     @Nullable List<String> headerComments, @Nullable String inlineComment) {
        super(line, column, headerComments, inlineComment);
        this.elements = new ArrayList<>(elements);
    }

    @Override
    public @NotNull List<Node> value() {
        return this.elements;
    }

    @Override
    public @NotNull String saveToString(@NotNull WriterOptions options) {
        if (options.maxLineLength() <= 0) {
            throw new IllegalArgumentException("maxLineLength must be positive");
        }

        if (this.elements.isEmpty()) {
            return "[]";
        }

        final List<String> formatted = new ArrayList<>(this.elements.size());

        final WriterOptions elementOptions = WriterOptions.builder()
                .writeBlankLines(false)
                .writeHeaderComments(false)
                .writeInlineComments(false)
                .maxLineLength(Integer.MAX_VALUE)
                .build();

        int totalLength = 2;
        for (int i = 0; i < this.elements.size(); i++) {
            final String str = this.elements.get(i).saveToString(elementOptions);
            formatted.add(str);
            totalLength += str.length() + 2;
        }

        boolean multiLine = totalLength > options.maxLineLength();

        final StringBuilder result = new StringBuilder(totalLength);
        if (!multiLine) {
            // Inline format: ["value1", "value2", "value3"]
            result.append('[');
            for (int i = 0; i < formatted.size(); i++) {
                if (i > 0) result.append(", ");
                result.append(formatted.get(i));
            }
            result.append(']');
        } else {
            // Inline format:
            // [
            //     "value1",
            //     "value2",
            //     "value3"
            // ]
            result.append('[').append('\n');

            for (int i = 0; i < formatted.size(); i++) {
                result.append("    ");
                result.append(formatted.get(i));
                result.append(',').append('\n');
            }
            // replacing last command with space
            result.setCharAt(result.length() - 2, ' ');
            result.append(']');
        }
        return result.toString();
    }

    public List<Object> raw() {
        final List<Object> result = new ArrayList<>(this.elements.size());
        for (int i = 0, len = this.elements.size(); i < len; i++) {
            final Node value = this.elements.get(i);
            if (value instanceof ArrayNode arrayNode) {
                result.add(arrayNode.raw());
            } else if (value instanceof TableNode tableNode) {
                result.add(tableNode.raw());
            } else {
                result.add(value.value());
            }
        }
        return result;
    }
}
