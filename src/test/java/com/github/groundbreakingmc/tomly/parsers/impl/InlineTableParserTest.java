package com.github.groundbreakingmc.tomly.parsers.impl;

import com.github.groundbreakingmc.fission.source.impl.StringCharSource;
import com.github.groundbreakingmc.tomly.contexts.ParsingContext;
import com.github.groundbreakingmc.tomly.dispatchers.ValueDispatcher;
import com.github.groundbreakingmc.tomly.exceptions.TomlParsingException;
import com.github.groundbreakingmc.tomly.nodes.Node;
import com.github.groundbreakingmc.tomly.nodes.impl.TableNode;
import com.github.groundbreakingmc.tomly.options.PreserveOptions;
import com.github.groundbreakingmc.tomly.parsers.NodeParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for InlineTableParser.
 * Tests parsing of TOML inline tables including:
 * empty tables, tables with primitive key-value pairs,
 * nested inline tables, whitespace and comment handling,
 * and proper error handling for invalid table syntax.
 */
@DisplayName("Inline Table Parser Tests")
class InlineTableParserTest {

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

        this.parser = new InlineTableParser(dispatcher);
    }

    @Nested
    @DisplayName("canRead() Method Tests")
    class CanReadTests {

        @Test
        @DisplayName("Should return true for '{' start character")
        void testCanReadInlineTableStart() {
            final ParsingContext context = contextFromString("{ key = 1 }");
            assertTrue(InlineTableParserTest.this.parser.canRead(context));
        }

        @Test
        @DisplayName("Should return false for non-table start character")
        void testCannotReadNonTableStart() {
            final ParsingContext context = contextFromString("notATable");
            assertFalse(InlineTableParserTest.this.parser.canRead(context));
        }
    }

    @Nested
    @DisplayName("Basic Inline Table Tests")
    class BasicTableTests {

        @Test
        @DisplayName("Parse empty inline table")
        void testParseEmptyTable() {
            final ParsingContext context = contextFromString("{}");
            final Node result = InlineTableParserTest.this.parser.read(context);
            assertEquals(Map.of(), result.value());
        }

        @Test
        @DisplayName("Parse inline table with primitives")
        void testParsePrimitiveTable() {
            final ParsingContext context = contextFromString("{ a = 1, b = true, c = \"text\" }");
            final Node result = InlineTableParserTest.this.parser.read(context);
            assertInstanceOf(TableNode.class, result);
            assertEquals(Map.of(
                    "a", 1,
                    "b", true,
                    "c", "text"
            ), ((TableNode) result).raw());
        }
    }

    @Nested
    @DisplayName("Nested Inline Table Tests")
    class NestedTableTests {

        @Test
        @DisplayName("Parse nested inline tables")
        void testParseNestedTables() {
            final ParsingContext context = contextFromString("{ outer = { inner = 1 } }");
            final Node result = InlineTableParserTest.this.parser.read(context);
            assertInstanceOf(TableNode.class, result);
            assertEquals(Map.of(
                    "outer", Map.of("inner", 1)
            ), ((TableNode) result).raw());
        }

        @Test
        @DisplayName("Parse deeply nested inline tables")
        void testParseDeeplyNestedTables() {
            final ParsingContext context = contextFromString("{ a = { b = { c = 42 } } }");
            final Node result = InlineTableParserTest.this.parser.read(context);
            assertInstanceOf(TableNode.class, result);
            assertEquals(Map.of(
                    "a", Map.of("b", Map.of("c", 42))
            ), ((TableNode) result).raw());
        }
    }

    @Nested
    @DisplayName("Whitespace and Comment Handling Tests")
    class WhitespaceAndCommentsTests {

        @Test
        @DisplayName("Parse table with whitespace")
        void testTableWithWhitespace() {
            final ParsingContext context = contextFromString("{  a  = 1 ,  b=2 }");
            final Node result = InlineTableParserTest.this.parser.read(context);
            assertInstanceOf(TableNode.class, result);
            assertEquals(Map.of("a", 1, "b", 2), ((TableNode) result).raw());
        }

        @Test
        @DisplayName("Parse table with inline comment")
        void testTableWithComment() {
            final ParsingContext context = contextFromString("{ a = 1, b = 2 } # comment");
            final Node result = InlineTableParserTest.this.parser.read(context);
            assertInstanceOf(TableNode.class, result);
            assertEquals(Map.of("a", 1, "b", 2), ((TableNode) result).raw());
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should throw error for unterminated table")
        void testUnterminatedTable() {
            final ParsingContext context = contextFromString("{ a = 1, b = 2");
            assertThrows(TomlParsingException.class, () -> InlineTableParserTest.this.parser.read(context));
        }

        @Test
        @DisplayName("Should throw error for invalid table syntax")
        void testInvalidTableSyntax() {
            final ParsingContext context = contextFromString("{ a = 1,, b = 2 }");
            assertThrows(TomlParsingException.class, () -> InlineTableParserTest.this.parser.read(context));
        }

        @Test
        @DisplayName("Should throw error for duplicate keys")
        void testDuplicateKeys() {
            final ParsingContext context = contextFromString("{ a = 1, a = 2 }");
            assertThrows(TomlParsingException.class, () -> InlineTableParserTest.this.parser.read(context));
        }

        @Test
        @DisplayName("Should throw error for invalid element")
        void testInvalidElement() {
            final ParsingContext context = contextFromString("{ a = unknownValue }");
            assertThrows(TomlParsingException.class, () -> InlineTableParserTest.this.parser.read(context));
        }
    }

    // Helper
    private static ParsingContext contextFromString(String input) {
        return new ParsingContext(new StringCharSource(input), true, PreserveOptions.defaultOptions());
    }
}