package com.github.groundbreakingmc.tomly.parsers.impl;

import com.github.groundbreakingmc.tomly.contexts.ParsingContext;
import com.github.groundbreakingmc.tomly.dispatchers.ValueDispatcher;
import com.github.groundbreakingmc.tomly.nodes.Node;
import com.github.groundbreakingmc.tomly.nodes.impl.TableNode;
import com.github.groundbreakingmc.tomly.parsers.NodeParser;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class InlineTableParser implements NodeParser {

    private final @NotNull ValueDispatcher dispatcher;

    public InlineTableParser(@NotNull ValueDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public boolean canRead(@NotNull ParsingContext context) {
        return context.current() == '{';
    }

    @Override
    public @NotNull TableNode read(@NotNull ParsingContext context) {
        if (context.current() == '{') context.advance(); // consume '{'

        final int startLine = context.getLine();
        final int startCol = context.getColumn();
        final Map<String, Node> map = new LinkedHashMap<>(8);

        while (true) {
            context.skipWhitespaces();
            if (context.current() == '}') {
                context.advance();
                break;
            }
            final String key = readKey(context);
            context.skipWhitespaces();
            if (context.current() != '=') {
                throw context.error("Expected '=' after key in inlineComment table");
            }
            context.advance();
            context.skipWhitespaces();
            final Node value = this.dispatcher.readValue(context);
            if (map.put(key, value) != null) {
                throw context.error("Redefine existing key");
            }
            context.skipWhitespaces();
            if (context.current() == ',') {
                context.advance();
                continue;
            }
            if (context.current() == '}') {
                context.advance();
                break;
            }
            throw context.error("Expected ',' or '}' in inlineComment table");
        }

        final List<String> headerComments = context.takeHeaderComments();
        final String inlineComment = context.readInlineCommentIfAny();

        return new TableNode("$inline-table", map, startLine, startCol, headerComments, inlineComment);
    }

    static @NotNull String readKey(@NotNull ParsingContext context) {
        context.skipWhitespaces();
        final StringBuilder keyBuilder = new StringBuilder(24);
        if (context.current() == '"' || context.current() == '\'') {
            // quoted key, reuse StringParser basics (no escapes for literal quotes, but keep simple)
            final int quote = context.current();
            context.advance();
            while (!context.eof() && context.current() != quote) {
                keyBuilder.append((char) context.current());
                context.advance();
            }
            if (context.current() != quote) {
                throw context.error("Unterminated quoted key");
            }
            context.advance();
            return keyBuilder.toString();
        }
        // bare key
        while (!context.eof()) {
            final int ch = context.current();
            if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || (ch >= '0' && ch <= '9') || ch == '_' || ch == '-') {
                keyBuilder.append((char) ch);
                context.advance();
            } else {
                break;
            }
        }
        if (keyBuilder.isEmpty()) {
            throw context.error("Expected key");
        }
        return keyBuilder.toString();
    }
}
