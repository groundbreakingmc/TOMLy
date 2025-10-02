package com.github.groundbreakingmc.tomly.nodes.impl;

import com.github.groundbreakingmc.tomly.options.WriterOptions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.util.List;

public final class DatetimeNode extends AbstractNode {

    private final @NotNull Temporal datetime;

    public DatetimeNode(@NotNull Temporal value, @Nullable List<String> headerComments, @Nullable String inlineComment) {
        this(value, -1, -1, headerComments, inlineComment);
    }

    public DatetimeNode(@NotNull Temporal value, int line, int column,
                        @Nullable List<String> headerComments, @Nullable String inlineComment) {
        super(line, column, headerComments, inlineComment);
        this.datetime = value;
    }

    @Override
    public @NotNull Temporal value() {
        return this.datetime;
    }

    public boolean isLocalDate() {
        return this.datetime instanceof LocalDate;
    }

    public boolean isLocalTime() {
        return this.datetime instanceof LocalTime;
    }

    public boolean isLocalDateTime() {
        return this.datetime instanceof LocalDateTime;
    }

    public boolean isOffsetDateTime() {
        return this.datetime instanceof OffsetDateTime;
    }

    @Override
    public @NotNull String saveToString(@NotNull WriterOptions options) {
        if (this.isLocalDate()) {
            return ((LocalDate) this.datetime).format(DateTimeFormatter.ISO_LOCAL_DATE);
        } else if (this.isLocalTime()) {
            return ((LocalTime) this.datetime).format(DateTimeFormatter.ISO_LOCAL_TIME);
        } else if (this.isLocalDateTime()) {
            return ((LocalDateTime) this.datetime).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } else if (this.isOffsetDateTime()) {
            return ((OffsetDateTime) this.datetime).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        }
        return this.datetime.toString();
    }
}
