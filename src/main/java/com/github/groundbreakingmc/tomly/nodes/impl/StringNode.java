package com.github.groundbreakingmc.tomly.nodes.impl;

import com.github.groundbreakingmc.tomly.options.WriterOptions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class StringNode extends AbstractNode {

    private final @NotNull String value;

    public StringNode(@NotNull String value, @Nullable List<String> headerComments, @Nullable String inlineComment) {
        this(value, -1, -1, headerComments, inlineComment);
    }

    public StringNode(@NotNull String value, int line, int column,
                      @Nullable List<String> headerComments, @Nullable String inlineComment) {
        super(line, column, headerComments, inlineComment);
        this.value = value;
    }

    @Override
    public @NotNull String value() {
        return this.value;
    }

    @Override
    public @NotNull String saveToString(@NotNull WriterOptions options) {
        if (options.maxLineLength() <= 0) {
            throw new IllegalArgumentException("maxLineLength must be positive");
        }

        if (this.value.isEmpty()) {
            return "\"\"";
        }

        if (this.value.indexOf('\n') != -1) {
            return format2MultiLineString(this.value, options.maxLineLength());
        }

        final int escapedLength = calculateEscapedLength(this.value);
        final boolean needsMultiLine = escapedLength + 2 > options.maxLineLength(); // +2 for quotes

        if (!needsMultiLine) {
            return "\"" + escapeString(this.value) + "\"";
        } else {
            return format2MultiLineString(this.value, options.maxLineLength());
        }
    }

    private static String format2MultiLineString(String value, int maxLineLength) {
        final StringBuilder result = new StringBuilder();
        result.append("\"\"\"\n");

        int currentLineLength = 0;
        for (int i = 0, len = value.length(); i < len; i++) {
            final String ch = escapeChar(value.charAt(i), true);
            if (currentLineLength > maxLineLength && ch.length() == 1 && ch.charAt(0) == ' ') {
                result.append('\n');
                currentLineLength = 0;
            }
            result.append(ch);
            if (ch.length() == 1 && ch.charAt(0) == '\n') currentLineLength = 0;
            else ++currentLineLength;
        }

        result.append("\"\"\"");
        return result.toString();
    }

    private static String escapeString(String value) {
        final StringBuilder result = new StringBuilder(value.length() + 16);
        for (int i = 0; i < value.length(); i++) {
            result.append(escapeChar(value.charAt(i), false));
        }
        return result.toString();
    }

    private static int calculateEscapedLength(String value) {
        int length = 0;
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            length += getEscapedCharLength(ch);
        }
        return length;
    }

    private static int getEscapedCharLength(char ch) {
        return switch (ch) {
            case '"', '\\', '\t', '\n', '\r', '\b', '\f' -> 2; // escaped character
            default -> (ch < 32 || ch == 127) ? 6 : 1; // \\uXXXX or regular symbol
        };
    }

    private static String escapeChar(char ch, boolean isMultiLine) {
        return switch (ch) {
            case '"' -> "\\\""; // -> \"
            case '\\' -> "\\\\"; // -> \\
            case '\t' -> "\\t"; // -> \t
            case '\n' -> isMultiLine ? "\n" : "\\n"; // in multiline strings we don't need to escape newline character
            case '\r' -> "\\r"; // -> \r
            case '\b' -> "\\b"; // -> \b
            case '\f' -> "\\f"; // -> \f
            default -> (ch < 32 || ch == 127) ? String.format("\\u%04X", (int) ch) : Character.toString(ch);
        };
    }
}
