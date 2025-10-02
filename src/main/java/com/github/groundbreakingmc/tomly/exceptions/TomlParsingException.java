package com.github.groundbreakingmc.tomly.exceptions;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Exception thrown when parsing a TOML document fails due to invalid syntax or structure.
 * <p>
 * This exception indicates errors that occur during the parsing phase,
 * such as malformed keys, unclosed strings, or other syntax violations.
 *
 * @author GroundbreakingMC
 * @version 1.0.0
 * @since 1.0.0
 */
public final class TomlParsingException extends TomlException {

    /**
     * Constructs a new TomlParsingException with the specified message, line, and column.
     *
     * @param message the error message describing the parsing failure
     * @param line    the 1-based line number where the parsing error occurred
     * @param column  the 0-based column number where the parsing error occurred
     * @since 1.0.0
     */
    public TomlParsingException(@NotNull String message, int line, int column) {
        super(message, line, column, "");
    }

    /**
     * Constructs a new TomlParsingException with the specified message, line, column, and context.
     *
     * @param message the error message describing the parsing failure
     * @param line    the 1-based line number where the parsing error occurred
     * @param column  the 0-based column number where the parsing error occurred
     * @param context additional context information about the parsing operation
     * @since 1.0.0
     */
    public TomlParsingException(@NotNull String message, int line, int column, @Nullable String context) {
        super(message, line, column, context);
    }
}
