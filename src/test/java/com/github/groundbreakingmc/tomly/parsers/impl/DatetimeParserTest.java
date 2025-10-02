package com.github.groundbreakingmc.tomly.parsers.impl;

import com.github.groundbreakingmc.fission.Fission;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for DatetimeParser.
 * Tests parsing of all valid TOML date and time formats, including:
 * local date, local time, local datetime, and offset datetime.
 * Also verifies proper error handling for invalid or malformed datetime strings.
 */
@DisplayName("Datetime Parser Tests")
class DatetimeParserTest {

    private NodeParser parser;

    @BeforeEach
    void setUp() {
        this.parser = new DatetimeParser();
    }

    @Nested
    @DisplayName("canRead() Method Tests")
    class CanReadTests {

        @ParameterizedTest
        @ValueSource(strings = {
                "1979-05-27",
                "07:32:00",
                "1979-05-27T07:32:00",
                "1979-05-27T07:32:00Z",
                "1979-05-27T07:32:00+01:00"
        })
        @DisplayName("Should return true for valid datetime strings")
        void testCanReadValidDatetimes(String input) {
            final ParsingContext context = contextFromString(input);
            assertTrue(DatetimeParserTest.this.parser.canRead(context));
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "1979/05/27",
                "1979:05:27",
                "9-05-27",
                "notadate",
                ""
        })
        @DisplayName("Should return false for invalid datetime strings")
        void testCanReadInvalidDatetimes(String input) {
            final ParsingContext context = contextFromString(input);
            assertFalse(DatetimeParserTest.this.parser.canRead(context));
        }
    }

    @Nested
    @DisplayName("Local Date Tests")
    class LocalDateTests {

        @ParameterizedTest
        @ValueSource(strings = {
                "1979-05-27",
                "2000-01-01",
                "1999-12-31"
        })
        @DisplayName("Parse local date")
        void testParseLocalDate(String input) {
            final ParsingContext context = contextFromString(input);
            final Node result = DatetimeParserTest.this.parser.read(context);
            assertEquals(LocalDate.parse(input), result.value());
        }
    }

    @Nested
    @DisplayName("Local Time Tests")
    class LocalTimeTests {

        @ParameterizedTest
        @ValueSource(strings = {
                "07:32:00",
                "23:59:59",
                "00:00:00"
        })
        @DisplayName("Parse local time")
        void testParseLocalTime(String input) {
            final ParsingContext context = contextFromString(input);
            final Node result = DatetimeParserTest.this.parser.read(context);
            assertEquals(LocalTime.parse(input), result.value());
        }
    }

    @Nested
    @DisplayName("Local Datetime Tests")
    class LocalDatetimeTests {

        @ParameterizedTest
        @ValueSource(strings = {
                "1979-05-27T07:32:00",
                "2000-01-01T00:00:00",
                "1999-12-31T23:59:59"
        })
        @DisplayName("Parse local datetime")
        void testParseLocalDatetime(String input) {
            final ParsingContext context = contextFromString(input);
            final Node result = DatetimeParserTest.this.parser.read(context);
            assertEquals(LocalDateTime.parse(input), result.value());
        }
    }

    @Nested
    @DisplayName("Offset Datetime Tests")
    class OffsetDatetimeTests {

        @ParameterizedTest
        @ValueSource(strings = {
                "1979-05-27T07:32:00Z",
                "1979-05-27T07:32:00+01:00",
                "2000-01-01T00:00:00-05:00"
        })
        @DisplayName("Parse offset datetime")
        void testParseOffsetDatetime(String input) {
            final ParsingContext context = contextFromString(input);
            final Node result = DatetimeParserTest.this.parser.read(context);
            assertEquals(OffsetDateTime.parse(input), result.value());
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @ParameterizedTest
        @ValueSource(strings = {
                "1979/05/27",
                "07-32-00",
                "1979-05-27 07:32:00",
                "notadate",
                "",
                "1979-13-01", // invalid month
                "1999-12-32", // invalid day
                "25:00:00",   // invalid hour
                "07:61:00",   // invalid minute
                "07:32:61"    // invalid second
        })
        @DisplayName("Should throw error for invalid datetime strings")
        void testInvalidDatetimes(String input) {
            final ParsingContext context = contextFromString(input);
            assertThrows(TomlParsingException.class, () -> DatetimeParserTest.this.parser.read(context));
        }
    }

    // Helper
    private static ParsingContext contextFromString(String input) {
        return new ParsingContext(new StringCharSource(input), true, PreserveOptions.defaultOptions());
    }
}