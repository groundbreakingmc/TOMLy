package com.github.groundbreakingmc.tomly.options;

/**
 * Configuration options for preserving formatting elements during TOML parsing.
 * <p>
 * This class controls which formatting details (blank lines, comments) are retained
 * in the parsed document structure. Preserving these elements enables round-trip
 * serialization where the output TOML maintains the original document's layout and
 * documentation.
 * <p>
 * <h3>Preservation Options:</h3>
 * <ul>
 *   <li><strong>Blank lines:</strong> Empty lines between key-value pairs and tables</li>
 *   <li><strong>Header comments:</strong> Comments appearing before a key or table declaration</li>
 *   <li><strong>Inline comments:</strong> Comments at the end of a line with a value</li>
 * </ul>
 * <p>
 * All options are disabled by default for optimal parsing performance. Enable only
 * the preservation features needed for your use case.
 * <p>
 * <h3>Usage Examples:</h3>
 * <pre>{@code
 * // Use defaults (no preservation)
 * PreserveOptions options = PreserveOptions.defaultOptions();
 *
 * // Preserve only comments
 * PreserveOptions options = PreserveOptions.builder()
 *     .preserveHeaderComments(true)
 *     .preserveInlineComments(true)
 *     .build();
 *
 * // Preserve everything
 * PreserveOptions options = PreserveOptions.builder()
 *     .preserveBlankLines(true)
 *     .preserveHeaderComments(true)
 *     .preserveInlineComments(true)
 *     .build();
 * }</pre>
 *
 * @author GroundbreakingMC
 * @version 1.0.0
 * @see com.github.groundbreakingmc.tomly.Tomly#parse(String, boolean, PreserveOptions)
 * @since 1.0.0
 */
public class PreserveOptions {

    private final boolean preserveBlankLines;
    private final boolean preserveHeaderComments;
    private final boolean preserveInlineComments;

    private PreserveOptions(Builder builder) {
        this.preserveBlankLines = builder.preserveBlankLines;
        this.preserveHeaderComments = builder.preserveHeaderComments;
        this.preserveInlineComments = builder.preserveInlineComments;
    }

    public boolean preserveBlankLines() {
        return this.preserveBlankLines;
    }

    public boolean preserveHeaderComments() {
        return this.preserveHeaderComments;
    }

    public boolean preserveInlineComments() {
        return this.preserveInlineComments;
    }

    public static PreserveOptions defaultOptions() {
        return new Builder().build();
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for constructing {@link PreserveOptions} instances.
     * <p>
     * Provides a fluent API for configuring preservation options. All options
     * default to {@code false} (disabled).
     *
     * @since 1.0.0
     */
    public static class Builder {
        private boolean preserveBlankLines = false;
        private boolean preserveHeaderComments = false;
        private boolean preserveInlineComments = false;

        private Builder() {

        }

        /**
         * Sets whether to preserve blank lines in the parsed document.
         *
         * @param preserveBlankLines {@code true} to preserve blank lines
         * @return this builder for method chaining
         */
        public Builder preserveBlankLines(boolean preserveBlankLines) {
            this.preserveBlankLines = preserveBlankLines;
            return this;
        }

        public boolean preserveBlankLines() {
            return this.preserveBlankLines;
        }

        /**
         * Sets whether to preserve header comments (comments before declarations).
         *
         * @param preserveHeaderComments {@code true} to preserve header comments
         * @return this builder for method chaining
         */
        public Builder preserveHeaderComments(boolean preserveHeaderComments) {
            this.preserveHeaderComments = preserveHeaderComments;
            return this;
        }

        public boolean preserveHeaderComments() {
            return this.preserveHeaderComments;
        }

        /**
         * Sets whether to preserve inline comments (comments at end of lines).
         *
         * @param preserveInlineComments {@code true} to preserve inline comments
         * @return this builder for method chaining
         */
        public Builder preserveInlineComments(boolean preserveInlineComments) {
            this.preserveInlineComments = preserveInlineComments;
            return this;
        }

        public boolean preserveInlineComments() {
            return this.preserveInlineComments;
        }

        /**
         * Builds the {@link PreserveOptions} instance with configured settings.
         *
         * @return a new PreserveOptions instance
         */
        public PreserveOptions build() {
            return new PreserveOptions(this);
        }
    }
}
