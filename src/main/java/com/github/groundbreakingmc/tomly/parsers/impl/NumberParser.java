package com.github.groundbreakingmc.tomly.parsers.impl;

import com.github.groundbreakingmc.tomly.contexts.ParsingContext;
import com.github.groundbreakingmc.tomly.nodes.impl.NumberNode;
import com.github.groundbreakingmc.tomly.parsers.NodeParser;
import com.github.groundbreakingmc.tomly.utils.NumberUtils;
import org.jetbrains.annotations.NotNull;

public final class NumberParser implements NodeParser {

    @Override
    public boolean canRead(@NotNull ParsingContext context) {
        int ch = context.current();

        if (!Character.isDigit(ch) && ch != '+' && ch != '-' && ch != 'n' && ch != 'i') {
            return false;
        }

        context.saveState();
        try {
            if (ch == '+' || ch == '-') {
                context.advance();
                ch = context.current();
            }

            if (ch == 'n') {
                return matchWord(context, "nan");
            } else if (ch == 'i') {
                return matchWord(context, "inf");
            }

            if (ch == '0' && !context.eof()) {
                int nextCh = context.peek();
                if (nextCh == 'x' || nextCh == 'X') {
                    context.advance();
                    context.advance();
                    return hasHexDigits(context);
                } else if (nextCh == 'o' || nextCh == 'O') {
                    context.advance();
                    context.advance();
                    return hasOctalDigits(context);
                } else if (nextCh == 'b' || nextCh == 'B') {
                    context.advance();
                    context.advance();
                    return hasBinaryDigits(context);
                }
            }

            return hasDecimalNumber(context);
        } finally {
            context.rollback();
        }
    }

    @Override
    public @NotNull NumberNode read(@NotNull ParsingContext context) {
        final int startLine = context.getLine();
        final int startCol = context.getColumn();
        boolean isNegative = false;

        if (context.current() == '+' || context.current() == '-') {
            isNegative = context.current() == '-';
            context.advance();
        }

        final int c1 = context.current();
        if (c1 == 'n') {
            readWord(context, "nan");
            return new NumberNode(Double.NaN, startLine, startCol, context.takeHeaderComments(), context.readInlineCommentIfAny());
        } else if (c1 == 'i') {
            readWord(context, "inf");
            final double value = isNegative ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
            return new NumberNode(value, startLine, startCol, context.takeHeaderComments(), context.readInlineCommentIfAny());
        }

        if (context.current() == '0' && context.peek() != -1) {
            context.advance();
            final int nextCh = context.current();

            if (nextCh == 'x' || nextCh == 'X') {
                // hex number
                context.advance(); // consume 'x'
                return this.readHexadecimal(context, isNegative, startLine, startCol);
            } else if (nextCh == 'o' || nextCh == 'O') {
                // octal number
                context.advance(); // consume 'o'
                return this.readOctal(context, isNegative, startLine, startCol);
            } else if (nextCh == 'b' || nextCh == 'B') {
                // binary number
                context.advance(); // consume 'b'
                return this.readBinary(context, isNegative, startLine, startCol);
            } else {
                final StringBuilder numberBuilder = new StringBuilder(32);
                numberBuilder.append(isNegative ? '-' : '+');
                // normal number starting with 0
                numberBuilder.append('0');
                return this.readDecimal(context, numberBuilder, startLine, startCol);
            }
        }

        // default decimal number
        final StringBuilder numberBuilder = new StringBuilder(32);
        numberBuilder.append(isNegative ? '-' : '+');
        return this.readDecimal(context, numberBuilder, startLine, startCol);
    }

    private NumberNode readHexadecimal(ParsingContext context, boolean isNegative, int startLine, int startCol) {
        final StringBuilder hexBuilder = new StringBuilder();
        boolean hasDigit = false;

        while (!context.eof()) {
            int ch = context.current();
            if ((ch >= '0' && ch <= '9') || (ch >= 'A' && ch <= 'F') || (ch >= 'a' && ch <= 'f')) {
                hexBuilder.append((char) ch);
                context.advance();
                hasDigit = true;
            } else if (ch == '_') {
                context.advance(); // consume underlines
            } else {
                break;
            }
        }

        if (!hasDigit) {
            throw context.error("Expected hexadecimal digits after '0x'");
        }

        try {
            final String cleaned = NumberUtils.stripUnderscores(hexBuilder);
            final long value = Long.parseUnsignedLong(cleaned, 16);
            return this.createNumberNode(value, isNegative, startLine, startCol, context);
        } catch (final NumberFormatException ex) {
            throw context.error("Invalid hexadecimal number: " + hexBuilder);
        }
    }

    private NumberNode readOctal(ParsingContext context, boolean isNegative, int startLine, int startCol) {
        final StringBuilder octalBuilder = new StringBuilder();
        boolean hasDigit = false;

        while (!context.eof()) {
            int ch = context.current();
            if (ch >= '0' && ch <= '7') {
                octalBuilder.append((char) ch);
                context.advance();
                hasDigit = true;
            } else if (ch == '_') {
                context.advance(); // consume underlines
            } else {
                break;
            }
        }

        if (!hasDigit) {
            throw context.error("Expected octal digits after '0o'");
        }

        try {
            final String cleaned = NumberUtils.stripUnderscores(octalBuilder);
            final long value = Long.parseUnsignedLong(cleaned, 8);
            return this.createNumberNode(value, isNegative, startLine, startCol, context);
        } catch (final NumberFormatException ex) {
            throw context.error("Invalid octal number: " + octalBuilder);
        }
    }

    private NumberNode readBinary(ParsingContext context, boolean isNegative, int startLine, int startCol) {
        final StringBuilder binaryBuilder = new StringBuilder();
        boolean hasDigit = false;

        while (!context.eof()) {
            int ch = context.current();
            if (ch == '0' || ch == '1') {
                binaryBuilder.append((char) ch);
                context.advance();
                hasDigit = true;
            } else if (ch == '_') {
                context.advance(); // consume underlines
            } else if (Character.isDigit(ch)) {
                throw context.error("Expected binary digits after '0b'");
            } else {
                break;
            }
        }

        if (!hasDigit) {
            throw context.error("Expected binary digits after '0b'");
        }

        try {
            final String cleaned = NumberUtils.stripUnderscores(binaryBuilder);
            final long value = Long.parseUnsignedLong(cleaned, 2);
            return this.createNumberNode(value, isNegative, startLine, startCol, context);
        } catch (final NumberFormatException ex) {
            throw context.error("Invalid binary number: " + binaryBuilder);
        }
    }

    private NumberNode readDecimal(ParsingContext context, StringBuilder numberBuilder, int startLine, int startCol) {
        boolean hasDigit = numberBuilder.length() > 1;
        boolean hasDot = false;
        boolean hasExp = false;

        while (!context.eof()) {
            final int ch = context.current();
            if (ch >= '0' && ch <= '9') {
                numberBuilder.append((char) ch);
                context.advance();
                hasDigit = true;
            } else if (ch == '_') {
                context.advance(); // consume underlines
            } else if (ch == '.' && !hasDot && !hasExp) {
                numberBuilder.append('.');
                context.advance();
                hasDot = true;
            } else if ((ch == 'e' || ch == 'E') && !hasExp && hasDigit) {
                numberBuilder.append('e');
                context.advance();
                hasExp = true;
                if (context.current() == '+' || context.current() == '-') {
                    numberBuilder.append((char) context.current());
                    context.advance();
                }
            } else {
                break;
            }
        }

        if (!hasDigit) {
            throw context.error("Expected number");
        }

        final String cleaned = NumberUtils.stripUnderscores(numberBuilder);
        final Number value;
        try {
            if (hasDot || hasExp) {
                value = Double.parseDouble(cleaned);
            } else {
                final long temp = Long.parseLong(cleaned);
                if (temp > Integer.MAX_VALUE || temp < Integer.MIN_VALUE) {
                    value = temp;
                } else {
                    value = (int) temp;
                }
            }
        } catch (final NumberFormatException ex) {
            throw context.error("Invalid number: " + cleaned);
        }

        return new NumberNode(value, startLine, startCol, context.takeHeaderComments(), context.readInlineCommentIfAny());
    }

    private NumberNode createNumberNode(long value, boolean isNegative,
                                        int startLine, int startCol, ParsingContext context) {
        if (isNegative) value = -value;

        if (value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE) {
            return new NumberNode((int) value, startLine, startCol, context.takeHeaderComments(), context.readInlineCommentIfAny());
        } else {
            return new NumberNode(value, startLine, startCol, context.takeHeaderComments(), context.readInlineCommentIfAny());
        }
    }

    // Helper methods for canRead()
    private static boolean hasHexDigits(ParsingContext context) {
        boolean hasDigit = false;
        while (!context.eof()) {
            int ch = context.current();
            if ((ch >= '0' && ch <= '9') || (ch >= 'A' && ch <= 'F') || (ch >= 'a' && ch <= 'f')) {
                hasDigit = true;
                context.advance();
            } else if (ch == '_') {
                context.advance();
            } else {
                break;
            }
        }
        return hasDigit;
    }

    private static boolean hasOctalDigits(ParsingContext context) {
        boolean hasDigit = false;
        while (!context.eof()) {
            int ch = context.current();
            if (ch >= '0' && ch <= '7') {
                hasDigit = true;
                context.advance();
            } else if (ch == '_') {
                context.advance();
            } else {
                break;
            }
        }
        return hasDigit;
    }

    private static boolean hasBinaryDigits(ParsingContext context) {
        boolean hasDigit = false;
        while (!context.eof()) {
            int ch = context.current();
            if (ch == '0' || ch == '1') {
                hasDigit = true;
                context.advance();
            } else if (ch == '_') {
                context.advance();
            } else {
                break;
            }
        }
        return hasDigit;
    }

    private static boolean hasDecimalNumber(ParsingContext context) {
        boolean hasDigit = false;
        boolean seenDot = false;
        boolean seenExp = false;

        while (!context.eof()) {
            final int ch = context.current();
            if (ch >= '0' && ch <= '9') {
                hasDigit = true;
                context.advance();
            } else if (ch == '_') {
                context.advance();
            } else if (ch == '.' && !seenDot && !seenExp) {
                seenDot = true;
                context.advance();
            } else if ((ch == 'e' || ch == 'E') && !seenExp && hasDigit) {
                seenExp = true;
                context.advance();
                if (context.current() == '+' || context.current() == '-') {
                    context.advance();
                }
                hasDigit = false; // at least one digit after the exponent is required
            } else if (Character.isWhitespace(ch) || ch == '#' || ch == ',' || ch == ']' || ch == '}') {
                break;
            } else {
                break;
            }
        }

        return hasDigit;
    }

    private static boolean matchWord(ParsingContext context, String word) {
        for (int i = 0; i < word.length(); i++) {
            if (context.current() != word.charAt(i)) return false;
            context.advance();
        }
        return true;
    }

    private static void readWord(ParsingContext context, String word) {
        for (int i = 0; i < word.length(); i++) {
            if (context.current() != word.charAt(i)) {
                throw context.error("Expected '" + word + "'");
            }
            context.advance();
        }
    }
}
