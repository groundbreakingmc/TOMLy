package com.github.groundbreakingmc.tomly.parsers.impl;

import com.github.groundbreakingmc.tomly.contexts.ParsingContext;
import com.github.groundbreakingmc.tomly.nodes.impl.StringNode;
import com.github.groundbreakingmc.tomly.parsers.NodeParser;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class StringParser implements NodeParser {

    @Override
    public boolean canRead(@NotNull ParsingContext context) {
        return context.current() == '"' || context.current() == '\'';
    }

    @Override
    public @NotNull StringNode read(@NotNull ParsingContext context) {
        final int startLine = context.getLine();
        final int startCol = context.getColumn();
        final int quote = context.current(); // ' or "
        boolean closed = false;
        context.advance();

        final boolean isMultiline = (context.current() == quote && context.peek() == quote);
        if (isMultiline) {
            context.advance(); // skipping second quote
            context.advance(); // skipping third quote
            if (context.current() == '\n') context.advance(); // skipping first newline
        }

        final boolean literal = (quote == '\'');
        final StringBuilder stringBuilder = new StringBuilder(64);

        int ch;
        while ((ch = context.current()) != -1) {
            if (!isMultiline && ch == '\n') {
                throw context.error("Unexpected line break in string");
            }
            if (!isMultiline && Character.isISOControl(ch)) {
                throw context.error("Unexpected control character");
            }
            if (ch == quote) { // end
                if (isMultiline) {
                    // end if next two also quote
                    final int[] p1 = context.peekAhead(3);
                    if (p1[0] == quote && p1[1] == quote && p1[2] != quote) {
                        context.advance(); // consume current quote
                        context.advance(); // consume second
                        context.advance(); // consume third
                        closed = true;
                        break;
                    } else {
                        // single quote inside triple string
                        stringBuilder.append((char) ch);
                        context.advance();
                        continue;
                    }
                } else {
                    context.advance();
                    closed = true;
                    break;
                }
            }
            if (!literal && ch == '\\') { // escape sequences
                ch = context.advance();
                if (ch == 'n') {
                    stringBuilder.append('\n');
                    context.advance();
                    continue;
                }
                if (ch == 't') {
                    stringBuilder.append('\t');
                    context.advance();
                    continue;
                }
                if (ch == 'f') {
                    stringBuilder.append('\f');
                    context.advance();
                    continue;
                }
                if (ch == 'r') {
                    stringBuilder.append('\r');
                    context.advance();
                    continue;
                }
                if (ch == '"') {
                    stringBuilder.append('"');
                    context.advance();
                    continue;
                }
                if (ch == 'b') {
                    stringBuilder.append('\b');
                    context.advance();
                    continue;
                }
                if (ch == '\\') {
                    stringBuilder.append('\\');
                    context.advance();
                    continue;
                }
                // unicode \\uXXXX
                if (ch == 'u') {
                    readUnicode(context, 4, stringBuilder);
                    continue;
                }
                if (ch == 'U') {
                    readUnicode(context, 8, stringBuilder);
                    continue;
                }
                throw context.error("Unknown escape sequence");
            } else {
                stringBuilder.append((char) ch);
                context.advance();
            }
        }

        if (!closed) {
            throw context.error("Unclosed string literal");
        }

        final String value = stringBuilder.toString();
        final List<String> headerComments = context.takeHeaderComments();
        final String inlineComment = context.readInlineCommentIfAny();
        return new StringNode(value, startLine, startCol, headerComments, inlineComment);
    }

    private static void readUnicode(@NotNull ParsingContext context, int count, StringBuilder sb) {
        int codePoint = 0;
        context.advance(); // consume 'u' or 'U'
        for (int i = 0; i < count; i++) {
            final int ch = context.current();
            final int digit = hexDigit(ch);
            if (digit < 0) {
                throw context.error("Invalid unicode escape");
            }
            codePoint = (codePoint << 4) | digit;
            context.advance();
        }
        sb.append(Character.toChars(codePoint));
    }

    private static int hexDigit(int ch) {
        if (ch >= '0' && ch <= '9') return ch - '0';
        if (ch >= 'a' && ch <= 'f') return 10 + (ch - 'a');
        if (ch >= 'A' && ch <= 'F') return 10 + (ch - 'A');
        return -1;
    }
}
