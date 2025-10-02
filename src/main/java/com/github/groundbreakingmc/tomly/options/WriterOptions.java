package com.github.groundbreakingmc.tomly.options;

import com.github.groundbreakingmc.tomly.writer.TomlWriter;

/**
 * Configuration options for controlling TOML serialization formatting.
 * <p>
 * This class provides fine-grained control over how TOML documents are written,
 * including comment preservation, blank line handling, and line length formatting.
 * These options allow customization of the output format while maintaining TOML
 * v1.0.0 specification compliance.
 * <p>
 * <h3>Configuration Options:</h3>
 * <ul>
 *   <li><strong>Blank lines:</strong> preserve empty lines between sections</li>
 *   <li><strong>Header comments:</strong> include comments appearing before key-value pairs</li>
 *   <li><strong>Inline comments:</strong> include comments at the end of value lines</li>
 *   <li><strong>Max line length:</strong> control when to break long values across multiple lines</li>
 * </ul>
 * <p>
 * All comment options are disabled by default. Default max line length is 100 characters.
 * <p>
 * <h3>Usage Examples:</h3>
 * <pre>{@code
 * // Use defaults (no comments, 100 char line length)
 * WriterOptions options = WriterOptions.defaultOptions();
 *
 * // Write with all comments preserved
 * WriterOptions options = WriterOptions.builder()
 *     .writeHeaderComments(true)
 *     .writeInlineComments(true)
 *     .writeBlankLines(true)
 *     .build();
 *
 * // Compact format with no line breaks
 * WriterOptions options = WriterOptions.builder()
 *     .maxLineLength(Integer.MAX_VALUE)
 *     .build();
 *
 * // Custom line length for readability
 * WriterOptions options = WriterOptions.builder()
 *     .maxLineLength(80)
 *     .writeHeaderComments(true)
 *     .build();
 * }</pre>
 *
 * @author GroundbreakingMC
 * @version 1.0.0
 * @see TomlWriter#write(Map, Path, WriterOptions)
 * @since 1.0.0
 */
public class WriterOptions {

    private final boolean writeBlankLines;
    private final boolean writeHeaderComments;
    private final boolean writeInlineComments;
    private final int maxLineLength;

    private WriterOptions(Builder builder) {
        this.writeBlankLines = builder.writeBlankLines;
        this.writeHeaderComments = builder.writeHeaderComments;
        this.writeInlineComments = builder.writeInlineComments;
        this.maxLineLength = builder.maxLineLength;
    }

    public boolean writeBlankLines() {
        return this.writeBlankLines;
    }

    public boolean writeHeaderComments() {
        return this.writeHeaderComments;
    }

    public boolean writeInlineComments() {
        return this.writeInlineComments;
    }

    public int maxLineLength() {
        return this.maxLineLength;
    }

    public static WriterOptions defaultOptions() {
        return new Builder().build();
    }

    public static Builder builder() {
        return new Builder();
    }


    /**
     * Builder for constructing {@link WriterOptions} instances.
     * <p>
     * Provides a fluent API for configuring serialization options. All comment
     * options default to {@code false}, and maxLineLength defaults to 100.
     *
     * @since 1.0.0
     */
    public static class Builder {

        private boolean writeBlankLines = false;
        private boolean writeHeaderComments = false;
        private boolean writeInlineComments = false;
        private int maxLineLength = 100;

        private Builder() {

        }

        /**
         * Sets whether to preserve blank lines in the output.
         *
         * @param writeBlankLines {@code true} to write blank lines
         * @return this builder for method chaining
         */
        public Builder writeBlankLines(boolean writeBlankLines) {
            this.writeBlankLines = writeBlankLines;
            return this;
        }

        public boolean writeBlankLines() {
            return this.writeBlankLines;
        }

        /**
         * Sets whether to write header comments (comments before declarations).
         *
         * @param writeHeaderComments {@code true} to write header comments
         * @return this builder for method chaining
         */
        public Builder writeHeaderComments(boolean writeHeaderComments) {
            this.writeHeaderComments = writeHeaderComments;
            return this;
        }

        public boolean writeHeaderComments() {
            return this.writeHeaderComments;
        }

        /**
         * Sets whether to write inline comments (comments at end of lines).
         *
         * @param writeInlineComments {@code true} to write inline comments
         * @return this builder for method chaining
         */
        public Builder writeInlineComments(boolean writeInlineComments) {
            this.writeInlineComments = writeInlineComments;
            return this;
        }

        public boolean writeInlineComments() {
            return this.writeInlineComments;
        }

        /**
         * Sets the maximum line length before breaking into multiple lines.
         *
         * @param maxLineLength maximum characters per line, must be positive
         * @return this builder for method chaining
         */
        public Builder maxLineLength(int maxLineLength) {
            this.maxLineLength = maxLineLength;
            return this;
        }

        public int maxLineLength() {
            return this.maxLineLength;
        }

        /**
         * Builds the {@link WriterOptions} instance with configured settings.
         *
         * @return a new WriterOptions instance
         */
        public WriterOptions build() {
            return new WriterOptions(this);
        }
    }
}
