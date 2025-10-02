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
 * Comprehensive test suite for StringParser.
 * Tests all TOML string formats: basic strings, literal strings, multi-line basic strings,
 * multi-line literal strings, and escape sequences.
 */
@DisplayName("String Parser Tests")
class StringParserTest {

    private NodeParser parser;

    @BeforeEach
    void setUp() {
        this.parser = new StringParser();
    }

    @Nested
    @DisplayName("canRead() Method Tests")
    class CanReadTests {

        @ParameterizedTest
        @ValueSource(strings = {"\"", "'", "\"\"\"", "'''"})
        @DisplayName("Should return true for valid string start characters")
        void testCanReadValidStartChars(String input) {
            final ParsingContext context = contextFromString(input);
            assertTrue(StringParserTest.this.parser.canRead(context));
        }

        @ParameterizedTest
        @ValueSource(strings = {"a", "z", "A", "Z", "0", "9", "#", "[", "]", "{", "}", "=", "-", "+"})
        @DisplayName("Should return false for invalid start characters")
        void testCanReadInvalidStartChars(String input) {
            final ParsingContext context = contextFromString(input);
            assertFalse(StringParserTest.this.parser.canRead(context));
        }
    }

    @Nested
    @DisplayName("Basic String Tests")
    class BasicStringTests {

        @ParameterizedTest
        @ValueSource(strings = {"", "hello world", "  hello  world  ", "hello123!@#$%", "Hëllö Wörld"})
        @DisplayName("Parse simple basic strings")
        void testBasicStrings(String input) {
            final ParsingContext context = contextFromString("\"" + input + "\"");
            final Node result = StringParserTest.this.parser.read(context);
            assertEquals(input, result.value());
        }

        @Test
        @DisplayName("Parse basic string with escape sequences")
        void testBasicStringEscapes() {
            final ParsingContext context = contextFromString("\"\\n\\t\\r\\b\\f\\\\\\\"\"");
            final Node result = StringParserTest.this.parser.read(context);
            assertEquals("\n\t\r\b\f\\\"", result.value());
        }
    }

    @Nested
    @DisplayName("Multi-line String Tests")
    class MultiLineStringTests {

        @Test
        @DisplayName("Parse multi-line basic string")
        void testMultiLineBasicString() {
            final ParsingContext context = contextFromString("\"\"\"\nhello\nworld\"\"\"");
            final Node result = StringParserTest.this.parser.read(context);
            assertEquals("hello\nworld", result.value());
        }

        @Test
        @DisplayName("Parse multi-line literal string")
        void testMultiLineLiteralString() {
            final ParsingContext context = contextFromString("'''\nhello\nworld'''");
            final Node result = StringParserTest.this.parser.read(context);
            assertEquals("hello\nworld", result.value());
        }
    }

    @Nested
    @DisplayName("Unicode Tests")
    class UnicodeTests {

        @Test
        @DisplayName("Parse basic unicode escape")
        void testUnicodeEscape() {
            final ParsingContext context = contextFromString("\"\\u0048\\u0065\\u006C\\u006C\\u006F\"");
            final Node result = StringParserTest.this.parser.read(context);
            assertEquals("Hello", result.value());
        }

        @Test
        @DisplayName("Parse long unicode escape")
        void testLongUnicodeEscape() {
            final ParsingContext context = contextFromString("\"\\U00000048\\U00000065\\U0000006C\\U0000006C\\U0000006F\"");
            final Node result = StringParserTest.this.parser.read(context);
            assertEquals("Hello", result.value());
        }

        @Test
        @DisplayName("Parse emoji unicode escape")
        void testEmojiUnicodeEscape() {
            final ParsingContext context = contextFromString("\"\\U0001F600\"");
            final Node result = StringParserTest.this.parser.read(context);
            assertEquals("😀", result.value());
        }

        @Test
        @DisplayName("Parse max unicode value")
        void testMaxUnicodeValue() {
            final ParsingContext context = contextFromString("\"\\U0010FFFF\"");
            final Node result = StringParserTest.this.parser.read(context);
            assertEquals("\uDBFF\uDFFF", result.value());
        }

        @Test
        @DisplayName("Parse zero width characters")
        void testZeroWidthCharacters() {
            final ParsingContext context = contextFromString("\"\\u200B\\u200C\\u200D\"");
            final Node result = StringParserTest.this.parser.read(context);
            assertEquals("\u200B\u200C\u200D", result.value());
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @ParameterizedTest
        @ValueSource(strings = {"\"hello world", "'hello world", "\"\"\"hello\nworld", "'''hello\nworld"})
        @DisplayName("Should throw error for unterminated strings")
        void testUnterminatedStrings(String input) {
            final ParsingContext context = contextFromString(input);
            assertThrows(TomlParsingException.class, () -> StringParserTest.this.parser.read(context));
        }

        @ParameterizedTest
        @ValueSource(strings = {"\"\\x\"", "\"hello\nworld\"", "'hello\nworld'", "\"hello\u0001world\""})
        @DisplayName("Should throw error for invalid characters or sequences")
        void testInvalidSequences(String input) {
            final ParsingContext context = contextFromString(input);
            assertThrows(TomlParsingException.class, () -> StringParserTest.this.parser.read(context));
        }
    }

    // Helper
    private static ParsingContext contextFromString(String input) {
        return new ParsingContext(new StringCharSource(input), true, PreserveOptions.defaultOptions());
    }
}