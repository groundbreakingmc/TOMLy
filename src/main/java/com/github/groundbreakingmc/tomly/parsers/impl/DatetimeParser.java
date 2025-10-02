package com.github.groundbreakingmc.tomly.parsers.impl;

import com.github.groundbreakingmc.tomly.contexts.ParsingContext;
import com.github.groundbreakingmc.tomly.nodes.impl.DatetimeNode;
import com.github.groundbreakingmc.tomly.parsers.NodeParser;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.Temporal;
import java.util.List;

public class DatetimeParser implements NodeParser {

    @Override
    public boolean canRead(@NotNull ParsingContext context) {
        if (!Character.isDigit(context.current())) {
            return false;
        }

        final int[] chars = context.peekAhead(4);
        if (!Character.isDigit(chars[0]) || !Character.isDigit(chars[1])) {
            return chars[1] == ':'; // starts with "numberNumber:" (Time)
        }
        if (!Character.isDigit(chars[2])) return false;
        return chars[3] == '-'; // starts with "numberNumberNumberNumber:" (Date)
    }

    @Override
    public @NotNull DatetimeNode read(@NotNull ParsingContext context) {
        final int startLine = context.getLine();
        final int startCol = context.getColumn();
        final StringBuilder buffer = new StringBuilder();

        // read datetime string
        while (!context.eof()) {
            int ch = context.current();

            if (Character.isWhitespace(ch) || ch == ',' || ch == ']' || ch == '}' || ch == '#') {
                break;
            }

            buffer.append((char) ch);
            context.advance();
        }

        String datetimeStr = buffer.toString();
        Temporal result;

        try {
            // determine type and parse
            if (datetimeStr.contains("T")) {
                // datetime with T separator
                if (datetimeStr.contains("+") || datetimeStr.contains("Z") ||
                        (datetimeStr.contains("-") && datetimeStr.lastIndexOf('-') > datetimeStr.indexOf('T'))) {
                    result = OffsetDateTime.parse(datetimeStr);
                } else {
                    result = LocalDateTime.parse(datetimeStr);
                }
            } else if (datetimeStr.contains(":")) {
                // time only
                result = LocalTime.parse(datetimeStr);
            } else {
                // date only
                result = LocalDate.parse(datetimeStr);
            }
        } catch (DateTimeParseException e) {
            throw context.error("Invalid datetime format: " + datetimeStr);
        }

        final List<String> headerComments = context.takeHeaderComments();
        final String inlineComment = context.readInlineCommentIfAny();

        return new DatetimeNode(result, startLine, startCol, headerComments, inlineComment);
    }
}
