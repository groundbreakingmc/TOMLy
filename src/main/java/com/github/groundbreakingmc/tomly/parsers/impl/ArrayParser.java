package com.github.groundbreakingmc.tomly.parsers.impl;

import com.github.groundbreakingmc.tomly.contexts.ParsingContext;
import com.github.groundbreakingmc.tomly.dispatchers.ValueDispatcher;
import com.github.groundbreakingmc.tomly.nodes.Node;
import com.github.groundbreakingmc.tomly.nodes.impl.ArrayNode;
import com.github.groundbreakingmc.tomly.parsers.NodeParser;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class ArrayParser implements NodeParser {

    private final @NotNull ValueDispatcher dispatcher;

    public ArrayParser(@NotNull ValueDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public boolean canRead(@NotNull ParsingContext context) {
        return context.current() == '[';
    }

    @Override
    public @NotNull ArrayNode read(@NotNull ParsingContext context) {
        if (context.current() == '[') context.advance(); // consume '['
        if (context.lineEnd()) context.advance(); // consume '[' to support multiline arrays

        final int startLine = context.getLine();
        final int startCol = context.getColumn();
        final List<Node> items = new ArrayList<>(8);

        context.skipWhitespaces();
        // true if array not empty
        if (context.current() != ']') {
            do {
                context.skipWhitespaces();
                final Node item = this.dispatcher.readValue(context);
                items.add(item);
                context.skipWhitespaces();
                if (context.current() == ',') {
                    context.advance();
                }
                if (context.lineEnd()) {
                    context.advance();
                }
            } while (context.current() != ']');
        }

        context.advance();

        final List<String> headerComments = context.takeHeaderComments();
        final String inlineComment = context.readInlineCommentIfAny();

        return new ArrayNode(items, startLine, startCol, headerComments, inlineComment);
    }
}
