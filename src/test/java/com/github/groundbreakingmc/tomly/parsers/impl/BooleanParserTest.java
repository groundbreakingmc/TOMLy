package com.github.groundbreakingmc.tomly.parsers.impl;

import com.github.groundbreakingmc.fission.source.impl.StringCharSource;
import com.github.groundbreakingmc.tomly.contexts.ParsingContext;
import com.github.groundbreakingmc.tomly.exceptions.TomlParsingException;
import com.github.groundbreakingmc.tomly.nodes.Node;
import com.github.groundbreakingmc.tomly.options.PreserveOptions;
import com.github.groundbreakingmc.tomly.parsers.NodeParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for BooleanParser.
 * Tests parsing of valid TOML boolean values (`true` and `false`) and
 * ensures errors are thrown for invalid or incorrectly formatted inputs.
 */
@DisplayName("Boolean Parser Tests")
class BooleanParserTest {

    private NodeParser parser;

    @BeforeEach
    void setUp() {
        this.parser = new BooleanParser();
    }

    @Nested
    @DisplayName("canRead() Method Tests")
    class CanReadTests {

        @ParameterizedTest
        @ValueSource(strings = {"true", "false", "t", "f"})
        @DisplayName("Should return true for valid boolean values")
        void testCanReadValidBooleans(String input) {
            final ParsingContext context = contextFromString(input);
            assertTrue(BooleanParserTest.this.parser.canRead(context));
        }

        @ParameterizedTest
        @ValueSource(strings = {"True", "FALSE", "yes", "no", "1", "0", "abc"})
        @DisplayName("Should return false for invalid boolean values")
        void testCanReadInvalidBooleans(String input) {
            final ParsingContext context = contextFromString(input);
            assertFalse(BooleanParserTest.this.parser.canRead(context));
        }
    }

    @Nested
    @DisplayName("Boolean Value Tests")
    class BooleanValueTests {

        @ParameterizedTest
        @ValueSource(strings = {"true"})
        @DisplayName("Parse true value")
        void testParseTrue(String input) {
            final ParsingContext context = contextFromString(input);
            final Node result = BooleanParserTest.this.parser.read(context);
            assertEquals(Boolean.TRUE, result.value());
        }

        @ParameterizedTest
        @ValueSource(strings = {"false"})
        @DisplayName("Parse false value")
        void testParseFalse(String input) {
            final ParsingContext context = contextFromString(input);
            final Node result = BooleanParserTest.this.parser.read(context);
            assertEquals(Boolean.FALSE, result.value());
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @ParameterizedTest
        @ValueSource(strings = {"True", "FALSE", "yes", "no", "1", "0", "t", "f", "abc"})
        @DisplayName("Should throw error for invalid boolean values")
        void testInvalidBooleans(String input) {
            final ParsingContext context = contextFromString(input);
            assertThrows(TomlParsingException.class, () -> BooleanParserTest.this.parser.read(context));
        }

        @Test
        @DisplayName("Should throw error for empty input")
        void testEmptyInput() {
            final ParsingContext context = contextFromString("");
            assertThrows(TomlParsingException.class, () -> BooleanParserTest.this.parser.read(context));
        }
    }

    // Helper
    private static ParsingContext contextFromString(String input) {
        return new ParsingContext(new StringCharSource(input), true, PreserveOptions.defaultOptions());
    }
}