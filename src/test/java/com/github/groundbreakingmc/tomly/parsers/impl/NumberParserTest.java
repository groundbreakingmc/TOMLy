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
 * Comprehensive test suite for NumberParser.
 * Tests all TOML number formats: decimal, hexadecimal, octal, binary, floats, and special values.
 */
@DisplayName("Number Parser Tests")
class NumberParserTest {

    private NodeParser parser;

    @BeforeEach
    void setUp() {
        parser = new NumberParser();
    }

    @Nested
    @DisplayName("canRead() Method Tests")
    class CanReadTests {

        @ParameterizedTest
        @ValueSource(strings = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "+9", "-9", "nan", "inf"})
        @DisplayName("Should return true for valid number start characters")
        void testCanReadValidStartChars(String input) {
            final ParsingContext context = NumberParserTest.contextFromString(input);
            assertTrue(NumberParserTest.this.parser.canRead(context));
        }

        @ParameterizedTest
        @ValueSource(strings = {"n", "i", "+", "-", "#", "[", "]", "{", "}", "="})
        @DisplayName("Should return false for invalid start characters")
        void testCanReadInvalidStartChars(String input) {
            final ParsingContext context = NumberParserTest.contextFromString(input);
            assertFalse(NumberParserTest.this.parser.canRead(context));
        }

        @Test
        @DisplayName("Should recognize hexadecimal numbers")
        void testCanReadHexadecimal() {
            final ParsingContext context = NumberParserTest.contextFromString("0x2A");
            assertTrue(NumberParserTest.this.parser.canRead(context));
        }

        @Test
        @DisplayName("Should recognize octal numbers")
        void testCanReadOctal() {
            final ParsingContext context = NumberParserTest.contextFromString("0o52");
            assertTrue(NumberParserTest.this.parser.canRead(context));
        }

        @Test
        @DisplayName("Should recognize binary numbers")
        void testCanReadBinary() {
            final ParsingContext context = NumberParserTest.contextFromString("0b1010");
            assertTrue(NumberParserTest.this.parser.canRead(context));
        }

        @Test
        @DisplayName("Should recognize infinity")
        void testCanReadInfinity() {
            final ParsingContext context = NumberParserTest.contextFromString("inf");
            assertTrue(NumberParserTest.this.parser.canRead(context));
        }

        @Test
        @DisplayName("Should recognize NaN")
        void testCanReadNaN() {
            final ParsingContext context = NumberParserTest.contextFromString("nan");
            assertTrue(NumberParserTest.this.parser.canRead(context));
        }
    }

    @Nested
    @DisplayName("Decimal Numbers Tests")
    class DecimalNumberTests {

        @Test
        @DisplayName("Parse positive integer")
        void testParsePositiveInteger() {
            final ParsingContext context = NumberParserTest.contextFromString("42");
            final Node result = NumberParserTest.this.parser.read(context);
            assertEquals(42, result.value());
            assertTrue(result.value() instanceof Integer);
        }

        @Test
        @DisplayName("Parse negative integer")
        void testParseNegativeInteger() {
            final ParsingContext context = NumberParserTest.contextFromString("-123");
            final Node result = NumberParserTest.this.parser.read(context);
            assertEquals(-123, result.value());
            assertTrue(result.value() instanceof Integer);
        }

        @Test
        @DisplayName("Parse large integer as long")
        void testParseLargeInteger() {
            final ParsingContext context = NumberParserTest.contextFromString("9223372036854775807");
            final Node result = NumberParserTest.this.parser.read(context);
            assertEquals(9223372036854775807L, result.value());
            assertTrue(result.value() instanceof Long);
        }

        @Test
        @DisplayName("Parse integer with underscores")
        void testParseIntegerWithUnderscores() {
            final ParsingContext context = NumberParserTest.contextFromString("1_000_000");
            final Node result = NumberParserTest.this.parser.read(context);
            assertEquals(1000000, result.value());
        }

        @Test
        @DisplayName("Parse positive float")
        void testParsePositiveFloat() {
            final ParsingContext context = NumberParserTest.contextFromString("3.14159");
            final Node result = NumberParserTest.this.parser.read(context);
            assertEquals(3.14159, (Double) result.value(), 0.00001);
            assertTrue(result.value() instanceof Double);
        }

        @Test
        @DisplayName("Parse negative float")
        void testParseNegativeFloat() {
            final ParsingContext context = NumberParserTest.contextFromString("-2.718");
            final Node result = NumberParserTest.this.parser.read(context);
            assertEquals(-2.718, (Double) result.value(), 0.001);
        }

        @Test
        @DisplayName("Parse exponential notation")
        void testParseExponentialNotation() {
            final ParsingContext context = NumberParserTest.contextFromString("1.5e-10");
            final Node result = NumberParserTest.this.parser.read(context);
            assertEquals(1.5e-10, (Double) result.value(), 1e-15);
        }

        @Test
        @DisplayName("Parse exponential with positive exponent")
        void testParseExponentialPositive() {
            final ParsingContext context = NumberParserTest.contextFromString("1.2e+3");
            final Node result = NumberParserTest.this.parser.read(context);
            assertEquals(1200.0, (Double) result.value(), 0.1);
        }
    }

    @Nested
    @DisplayName("Hexadecimal Numbers Tests")
    class HexadecimalNumberTests {

        @Test
        @DisplayName("Parse simple hexadecimal")
        void testParseSimpleHex() {
            final ParsingContext context = NumberParserTest.contextFromString("0x2A");
            final Node result = NumberParserTest.this.parser.read(context);
            assertEquals(42, result.value());
        }

        @Test
        @DisplayName("Parse hexadecimal with uppercase X")
        void testParseHexUppercaseX() {
            final ParsingContext context = NumberParserTest.contextFromString("0X2A");
            final Node result = NumberParserTest.this.parser.read(context);
            assertEquals(42, result.value());
        }

        @Test
        @DisplayName("Parse hexadecimal with lowercase letters")
        void testParseHexLowercase() {
            final ParsingContext context = NumberParserTest.contextFromString("0xdeadbeef");
            final Node result = NumberParserTest.this.parser.read(context);
            assertEquals(0xdeadbeefL, result.value());
        }

        @Test
        @DisplayName("Parse hexadecimal with uppercase letters")
        void testParseHexUppercase() {
            final ParsingContext context = NumberParserTest.contextFromString("0xDEADBEEF");
            final Node result = NumberParserTest.this.parser.read(context);
            assertEquals(0xDEADBEEFL, result.value());
        }

        @Test
        @DisplayName("Parse hexadecimal with underscores")
        void testParseHexWithUnderscores() {
            final ParsingContext context = NumberParserTest.contextFromString("0xFF_FF");
            final Node result = NumberParserTest.this.parser.read(context);
            assertEquals(0xFFFF, result.value());
        }

        @Test
        @DisplayName("Parse negative hexadecimal")
        void testParseNegativeHex() {
            final ParsingContext context = NumberParserTest.contextFromString("-0x10");
            final Node result = NumberParserTest.this.parser.read(context);
            assertEquals(-16, result.value());
        }

        @Test
        @DisplayName("Should throw error for invalid hex digits")
        void testInvalidHexDigits() {
            final ParsingContext context = NumberParserTest.contextFromString("0xGH");
            assertThrows(TomlParsingException.class, () -> NumberParserTest.this.parser.read(context));
        }
    }

    @Nested
    @DisplayName("Octal Numbers Tests")
    class OctalNumberTests {

        @Test
        @DisplayName("Parse simple octal")
        void testParseSimpleOctal() {
            final ParsingContext context = NumberParserTest.contextFromString("0o52");
            final Node result = NumberParserTest.this.parser.read(context);
            assertEquals(42, result.value()); // 5*8 + 2 = 42
        }

        @Test
        @DisplayName("Parse octal with uppercase O")
        void testParseOctalUppercaseO() {
            final ParsingContext context = NumberParserTest.contextFromString("0O52");
            final Node result = NumberParserTest.this.parser.read(context);
            assertEquals(42, result.value());
        }

        @Test
        @DisplayName("Parse octal with underscores")
        void testParseOctalWithUnderscores() {
            final ParsingContext context = NumberParserTest.contextFromString("0o1_234");
            final Node result = NumberParserTest.this.parser.read(context);
            assertEquals(668, result.value()); // 1*512 + 2*64 + 3*8 + 4 = 668
        }

        @Test
        @DisplayName("Parse negative octal")
        void testParseNegativeOctal() {
            final ParsingContext context = NumberParserTest.contextFromString("-0o10");
            final Node result = NumberParserTest.this.parser.read(context);
            assertEquals(-8, result.value());
        }

        @Test
        @DisplayName("Should throw error for invalid octal digits")
        void testInvalidOctalDigits() {
            final ParsingContext context = NumberParserTest.contextFromString("0o89");
            assertThrows(TomlParsingException.class, () -> NumberParserTest.this.parser.read(context));
        }
    }

    @Nested
    @DisplayName("Binary Numbers Tests")
    class BinaryNumberTests {

        @Test
        @DisplayName("Parse simple binary")
        void testParseSimpleBinary() {
            final ParsingContext context = NumberParserTest.contextFromString("0b101010");
            final Node result = NumberParserTest.this.parser.read(context);
            assertEquals(42, result.value());
        }

        @Test
        @DisplayName("Parse binary with uppercase B")
        void testParseBinaryUppercaseB() {
            final ParsingContext context = NumberParserTest.contextFromString("0B101010");
            final Node result = NumberParserTest.this.parser.read(context);
            assertEquals(42, result.value());
        }

        @Test
        @DisplayName("Parse binary with underscores")
        void testParseBinaryWithUnderscores() {
            final ParsingContext context = NumberParserTest.contextFromString("0b1010_1010");
            final Node result = NumberParserTest.this.parser.read(context);
            assertEquals(170, result.value());
        }

        @Test
        @DisplayName("Parse negative binary")
        void testParseNegativeBinary() {
            final ParsingContext context = NumberParserTest.contextFromString("-0b1010");
            final Node result = NumberParserTest.this.parser.read(context);
            assertEquals(-10, result.value());
        }

        @Test
        @DisplayName("Should throw error for invalid binary digits")
        void testInvalidBinaryDigits() {
            final ParsingContext context = NumberParserTest.contextFromString("0b102");
            assertThrows(TomlParsingException.class, () -> NumberParserTest.this.parser.read(context));
        }
    }

    @Nested
    @DisplayName("Special Values Tests")
    class SpecialValuesTests {

        @Test
        @DisplayName("Parse positive infinity")
        void testParsePositiveInfinity() {
            final ParsingContext context = NumberParserTest.contextFromString("inf");
            final Node result = NumberParserTest.this.parser.read(context);
            assertEquals(Double.POSITIVE_INFINITY, result.value());
        }

        @Test
        @DisplayName("Parse negative infinity")
        void testParseNegativeInfinity() {
            final ParsingContext context = NumberParserTest.contextFromString("-inf");
            final Node result = NumberParserTest.this.parser.read(context);
            assertEquals(Double.NEGATIVE_INFINITY, result.value());
        }

        @Test
        @DisplayName("Parse positive infinity with plus sign")
        void testParsePositiveInfinityWithPlus() {
            final ParsingContext context = NumberParserTest.contextFromString("+inf");
            final Node result = NumberParserTest.this.parser.read(context);
            assertEquals(Double.POSITIVE_INFINITY, result.value());
        }

        @Test
        @DisplayName("Parse NaN")
        void testParseNaN() {
            final ParsingContext context = NumberParserTest.contextFromString("nan");
            final Node result = NumberParserTest.this.parser.read(context);
            assertTrue(Double.isNaN((Double) result.value()));
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should throw error for empty number")
        void testEmptyNumber() {
            final ParsingContext context = NumberParserTest.contextFromString("");
            assertThrows(TomlParsingException.class, () -> NumberParserTest.this.parser.read(context));
        }

        @Test
        @DisplayName("Should throw error for invalid exponential")
        void testInvalidExponential() {
            final ParsingContext context = NumberParserTest.contextFromString("1e");
            assertThrows(TomlParsingException.class, () -> NumberParserTest.this.parser.read(context));
        }

        @Test
        @DisplayName("Should throw error for multiple dots")
        void testMultipleDots() {
            final ParsingContext context = NumberParserTest.contextFromString("1.2.3");
            assertThrows(TomlParsingException.class, () -> NumberParserTest.this.parser.read(context));
        }

        @Test
        @DisplayName("Should throw error for invalid hex format")
        void testInvalidHexFormat() {
            final ParsingContext context = NumberParserTest.contextFromString("0x");
            assertThrows(TomlParsingException.class, () -> NumberParserTest.this.parser.read(context));
        }

        @Test
        @DisplayName("Should throw error for invalid octal format")
        void testInvalidOctalFormat() {
            final ParsingContext context = NumberParserTest.contextFromString("0o");
            assertThrows(TomlParsingException.class, () -> NumberParserTest.this.parser.read(context));
        }

        @Test
        @DisplayName("Should throw error for invalid binary format")
        void testInvalidBinaryFormat() {
            final ParsingContext context = NumberParserTest.contextFromString("0b");
            assertThrows(TomlParsingException.class, () -> NumberParserTest.this.parser.read(context));
        }

        @Test
        @DisplayName("Should handle number overflow gracefully")
        void testNumberOverflow() {
            final ParsingContext context = NumberParserTest.contextFromString("999999999999999999999999999999999999999");
            assertThrows(TomlParsingException.class, () -> NumberParserTest.this.parser.read(context));
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Parse number with leading plus")
        void testParseNumberWithLeadingPlus() {
            final ParsingContext context = NumberParserTest.contextFromString("+42");
            final Node result = NumberParserTest.this.parser.read(context);
            assertEquals(42, result.value());
        }

        @Test
        @DisplayName("Parse float with no decimal digits")
        void testParseFloatNoDecimalDigits() {
            final ParsingContext context = NumberParserTest.contextFromString("1.");
            final Node result = NumberParserTest.this.parser.read(context);
            assertEquals(1.0, (Double) result.value(), 0.001);
        }

        @Test
        @DisplayName("Parse float with no integer digits")
        void testParseFloatNoIntegerDigits() {
            final ParsingContext context = NumberParserTest.contextFromString(".5");
            final Node result = NumberParserTest.this.parser.read(context);
            assertEquals(0.5, (Double) result.value(), 0.001);
        }

        @Test
        @DisplayName("Parse number with trailing underscores")
        void testParseNumberWithTrailingUnderscores() {
            final ParsingContext context = NumberParserTest.contextFromString("123_");
            final Node result = NumberParserTest.this.parser.read(context);
            assertEquals(123, result.value());
        }

        @Test
        @DisplayName("Parse very small exponential")
        void testParseVerySmallExponential() {
            final ParsingContext context = NumberParserTest.contextFromString("1e-323");
            final Node result = NumberParserTest.this.parser.read(context);
            assertEquals(1e-323, (Double) result.value());
        }

        @Test
        @DisplayName("Parse very large exponential")
        void testParseVeryLargeExponential() {
            final ParsingContext context = NumberParserTest.contextFromString("1e308");
            final Node result = NumberParserTest.this.parser.read(context);
            assertEquals(1e308, (Double) result.value());
        }

        @Test
        @DisplayName("Parse zero with different formats")
        void testParseZeroDifferentFormats() {
            // Decimal
            ParsingContext context = NumberParserTest.contextFromString("0");
            Node result = NumberParserTest.this.parser.read(context);
            assertEquals(0, result.value());

            // Hex
            context = NumberParserTest.contextFromString("0x0");
            result = NumberParserTest.this.parser.read(context);
            assertEquals(0, result.value());

            // Octal
            context = NumberParserTest.contextFromString("0o0");
            result = NumberParserTest.this.parser.read(context);
            assertEquals(0, result.value());

            // Binary
            context = NumberParserTest.contextFromString("0b0");
            result = NumberParserTest.this.parser.read(context);
            assertEquals(0, result.value());
        }
    }

    // Helper methods

    private static ParsingContext contextFromString(String input) {
        return new ParsingContext(new StringCharSource(input), false, PreserveOptions.defaultOptions());
    }
}