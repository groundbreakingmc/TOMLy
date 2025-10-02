package com.github.groundbreakingmc.tomly.exceptions;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Base exception for all TOML-related errors.
 * <p>
 * This class provides common fields such as line, column, and context,
 * allowing subclasses to represent specific categories of TOML errors.
 *
 * @author GroundbreakingMC
 * @version 1.0.0
 * @since 1.0.0
 */
public abstract class TomlException extends RuntimeException {

    private final int line;
    private final int column;
    private final String context;

    /**
     * Constructs a new TomlException with the specified message, line, column, and context.
     *
     * @param message the error message
     * @param line    the 1-based line number where the error occurred
     * @param column  the 0-based column number where the error occurred
     * @param context additional context for the error (can be empty)
     * @since 1.0.0
     */
    protected TomlException(@NotNull String message, int line, int column, @Nullable String context) {
        super(message + " at line " + line + ", column " + column +
                (context != null && !context.isEmpty() ? "\nContext: " + context : ""));
        this.line = line;
        this.column = column;
        this.context = context != null ? context : "";
    }

    public int getLine() {
        return this.line;
    }

    public int getColumn() {
        return this.column;
    }

    public String getContext() {
        return this.context;
    }
}
