package com.github.groundbreakingmc.tomly.exceptions;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Exception thrown when a TOML value is accessed with the wrong type.
 * <p>
 * This exception occurs when attempting to retrieve a value as a type
 * that does not match its actual representation in the document.
 * <p>
 * Example: calling {@code getInt("foo")} when {@code "foo"} is a string.
 *
 * @author GroundbreakingMC
 * @version 1.0.0
 * @since 1.0.0
 */
public final class TomlTypeMismatchException extends TomlException {

    private final String expectedType;
    private final String actualType;

    /**
     * Constructs a new TomlTypeMismatchException with type information and location details.
     *
     * @param expectedType the type that was expected for the value
     * @param actualType   the actual type of the value found in the document
     * @param path         the path to the value in the TOML document (e.g., "database.host")
     * @param line         the 1-based line number where the type mismatch occurred
     * @param column       the 0-based column number where the type mismatch occurred
     * @param context      additional context information about the type access operation
     * @since 1.0.0
     */
    public TomlTypeMismatchException(@NotNull String expectedType,
                                     @NotNull String actualType,
                                     @NotNull String path,
                                     int line,
                                     int column,
                                     @Nullable String context) {
        super(("Type mismatch: expected " + expectedType + " at path " + path + " but found " + actualType), line, column, context);
        this.expectedType = expectedType;
        this.actualType = actualType;
    }

    public String expectedType() {
        return this.expectedType;
    }

    public String actualType() {
        return this.actualType;
    }
}
