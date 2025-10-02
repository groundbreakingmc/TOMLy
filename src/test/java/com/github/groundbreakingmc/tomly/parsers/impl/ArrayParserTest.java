package com.github.groundbreakingmc.tomly.parsers.impl;

import com.github.groundbreakingmc.fission.source.impl.StringCharSource;
import com.github.groundbreakingmc.tomly.contexts.ParsingContext;
import com.github.groundbreakingmc.tomly.dispatchers.ValueDispatcher;
import com.github.groundbreakingmc.tomly.exceptions.TomlParsingException;
import com.github.groundbreakingmc.tomly.nodes.Node;
import com.github.groundbreakingmc.tomly.nodes.impl.ArrayNode;
import com.github.groundbreakingmc.tomly.options.PreserveOptions;
import com.github.groundbreakingmc.tomly.parsers.NodeParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for ArrayParser.
 * Tests parsing of TOML arrays including:
 * empty arrays, arrays with different primitive types,
 * nested arrays, arrays with whitespace and comments,
 * and proper error handling for invalid array syntax.
 */
@DisplayName("Array Parser Tests")
class ArrayParserTest {

    private NodeParser parser;

    @BeforeEach
    void setUp() {
        final ValueDispatcher dispatcher = new ValueDispatcher();
        dispatcher.register(new StringParser());
        dispatcher.register(new BooleanParser());
        dispatcher.register(new ArrayParser(dispatcher));
        dispatcher.register(new DatetimeParser());
        dispatcher.register(new NumberParser());
        dispatcher.register(new InlineTableParser(dispatcher));

        this.parser = new ArrayParser(dispatcher);
    }

    @Nested
    @DisplayName("canRead() Method Tests")
    class CanReadTests {

        @Test
        @DisplayName("Should return true for '[' start character")
        void testCanReadArrayStart() {
            final ParsingContext context = contextFromString("[1, 2, 3]");
            assertTrue(ArrayParserTest.this.parser.canRead(context));
        }

        @Test
        @DisplayName("Should return false for non-array start character")
        void testCannotReadNonArrayStart() {
            final ParsingContext context = contextFromString("notAnArray");
            assertFalse(ArrayParserTest.this.parser.canRead(context));
        }
    }

    @Nested
    @DisplayName("Basic Array Tests")
    class BasicArrayTests {

        @Test
        @DisplayName("Parse empty array")
        void testParseEmptyArray() {
            final ParsingContext context = contextFromString("[]");
            final Node result = ArrayParserTest.this.parser.read(context);
            assertEquals(0, ((List<?>) result.value()).size());
        }

        @Test
        @DisplayName("Parse array with integers")
        void testParseIntegerArray() {
            final ParsingContext context = contextFromString("[1, 2, 3, 4]");
            final Node result = ArrayParserTest.this.parser.read(context);
            assertInstanceOf(ArrayNode.class, result);
            assertEquals(List.of(1, 2, 3, 4), ((ArrayNode) result).raw());
        }

        @Test
        @DisplayName("Parse array with mixed primitive types")
        void testParseMixedArray() {
            final ParsingContext context = contextFromString("[1, true, \"hello\"]");
            final Node result = ArrayParserTest.this.parser.read(context);
            assertInstanceOf(ArrayNode.class, result);
            assertEquals(List.of(1, true, "hello"), ((ArrayNode) result).raw());
        }

        @Test
        @DisplayName("Parse array with whitespace")
        void testArrayWithWhitespace() {
            final ParsingContext context = contextFromString("[ 1 , 2 , 3 ]");
            final Node result = ArrayParserTest.this.parser.read(context);
            assertInstanceOf(ArrayNode.class, result);
            assertEquals(List.of(1, 2, 3), ((ArrayNode) result).raw());
        }
    }

    @Nested
    @DisplayName("Nested Array Tests")
    class NestedArrayTests {

        @Test
        @DisplayName("Parse nested arrays")
        void testParseNestedArrays() {
            final ParsingContext context = contextFromString("[[1, 2], [3, 4]]");
            final Node result = ArrayParserTest.this.parser.read(context);
            assertInstanceOf(ArrayNode.class, result);
            assertEquals(List.of(List.of(1, 2), List.of(3, 4)), ((ArrayNode) result).raw());
        }

        @Test
        @DisplayName("Parse deeply nested arrays")
        void testParseDeeplyNestedArrays() {
            final ParsingContext context = contextFromString("[[[1]], [[2]]]");
            final Node result = ArrayParserTest.this.parser.read(context);
            assertInstanceOf(ArrayNode.class, result);
            assertEquals(List.of(List.of(List.of(1)), List.of(List.of(2))), ((ArrayNode) result).raw());
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should throw error for unterminated array")
        void testUnterminatedArray() {
            final ParsingContext context = contextFromString("[1, 2, 3");
            assertThrows(TomlParsingException.class, () -> ArrayParserTest.this.parser.read(context));
        }

        @Test
        @DisplayName("Should throw error for invalid array syntax")
        void testInvalidArraySyntax() {
            final ParsingContext context = contextFromString("[1, 2,, 3]");
            assertThrows(TomlParsingException.class, () -> ArrayParserTest.this.parser.read(context));
        }

        @Test
        @DisplayName("Should throw error for invalid array element")
        void testInvalidArrayElement() {
            final ParsingContext context = contextFromString("[1, unknownValue, 3]");
            assertThrows(TomlParsingException.class, () -> ArrayParserTest.this.parser.read(context));
        }
    }

    // Helper
    private static ParsingContext contextFromString(String input) {
        return new ParsingContext(new StringCharSource(input), true, PreserveOptions.defaultOptions());
    }
}