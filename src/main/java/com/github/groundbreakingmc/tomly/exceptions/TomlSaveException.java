package com.github.groundbreakingmc.tomly.exceptions;

import org.jetbrains.annotations.NotNull;

/**
 * Exception thrown when saving a TOML document fails due to serialization errors.
 * <p>
 * This exception indicates errors that occur during the saving/serialization phase,
 * such as invalid node values, unsupported data types, or formatting constraints
 * that cannot be satisfied.
 * <p>
 * Common scenarios include:
 * <ul>
 *   <li>Attempting to serialize unsupported Java types</li>
 *   <li>Invalid string content that cannot be properly escaped</li>
 *   <li>Circular references in table structures</li>
 *   <li>Values that exceed reasonable formatting limits</li>
 * </ul>
 *
 * @author GroundbreakingMC
 * @version 1.0.0
 * @since 1.0.0
 */
public final class TomlSaveException extends RuntimeException {

    /**
     * Constructs a new TomlSaveException with the specified message.
     *
     * @param message the error message describing the save failure (which is saved for later retrieval by the getMessage() method).
     * @since 1.0.0
     */
    public TomlSaveException(@NotNull String message) {
        super(message);
    }

    /**
     * Constructs a new TomlSaveException with the specified message.
     *
     * @param message the error message describing the save failure (which is saved for later retrieval by the getMessage() method).
     * @param cause   the cause (which is saved for later retrieval by the getCause() method). (A null value is permitted, and indicates that the cause is nonexistent or unknown.)
     * @since 1.0.0
     */
    public TomlSaveException(@NotNull String message, @NotNull Throwable cause) {
        super(message, cause);
    }
}
