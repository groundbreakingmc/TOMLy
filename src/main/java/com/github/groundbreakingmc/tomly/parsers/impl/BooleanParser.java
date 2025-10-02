package com.github.groundbreakingmc.tomly.parsers.impl;

import com.github.groundbreakingmc.tomly.contexts.ParsingContext;
import com.github.groundbreakingmc.tomly.nodes.impl.BooleanNode;
import com.github.groundbreakingmc.tomly.parsers.NodeParser;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class BooleanParser implements NodeParser {

    @Override
    public boolean canRead(@NotNull ParsingContext context) {
        int ch = context.current();
        return ch == 't' || ch == 'f';
    }

    @Override
    public @NotNull BooleanNode read(@NotNull ParsingContext context) {
        final int startLine = context.getLine();
        final int startCol = context.getColumn();

        final boolean value;
        if (context.current() == 't') {
            readWord(context, "true");
            value = true;
        } else {
            readWord(context, "false");
            value = false;
        }

        final List<String> headerComments = context.takeHeaderComments();
        final String inlineComment = context.readInlineCommentIfAny();

        return new BooleanNode(value, startLine, startCol, headerComments, inlineComment);
    }

    private static void readWord(@NotNull ParsingContext context, @NotNull String word) {
        for (int i = 0; i < word.length(); i++) {
            if (context.current() != word.charAt(i)) {
                throw context.error("Expected '" + word + "'");
            }
            context.advance();
        }
    }
}
