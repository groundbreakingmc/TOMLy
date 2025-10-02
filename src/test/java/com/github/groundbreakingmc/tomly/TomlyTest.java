package com.github.groundbreakingmc.tomly;

import com.github.groundbreakingmc.tomly.nodes.TomlDocument;
import com.github.groundbreakingmc.tomly.options.PreserveOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.time.*;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Comprehensive test suite for Tomly and TomlDocument.
 * Tests parsing of a full-feature TOML file with all supported types:
 * strings, numbers, booleans, dates, times, arrays, nested tables, inline tables,
 * array of tables, and edge-case values. Also verifies comments preservation.
 */
@DisplayName("Tomly/TomlDocument Full Feature Tests")
class TomlyTest {

    private TomlDocument document;

    @BeforeEach
    void setUp() throws URISyntaxException {
        final Path config = Path.of(Objects.requireNonNull(
                getClass().getResource("/test-data.toml")
        ).toURI());
        this.document = Tomly.parse(config, true, PreserveOptions.defaultOptions());
    }

    @Test
    @DisplayName("Test basic strings")
    void testStrings() {
        assertEquals("Hello, World!", this.document.getStr("basic_string"));
        assertEquals("This is a literal string with \\n escaped characters", this.document.getStr("literal_string"));
        assertEquals("This is a multi-line\nbasic string with \"quotes\"\nand line breaks.\n", this.document.getStr("multiline_basic"));
        assertEquals("This is a multi-line\nliteral string with \\n\nno escape processing.\n", this.document.getStr("multiline_literal"));
        assertEquals("This is a multi-line\nbasic string with \"quotes\"\nand line breaks.", this.document.getStr("multiline_basic_inline"));
        assertEquals("This is a multi-line literal string with \\nno escape processing.", this.document.getStr("multiline_literal_inline"));
    }

    @Test
    @DisplayName("Test numbers")
    void testNumbers() {
        assertEquals(42, this.document.getInt("decimal_int"));
        assertEquals(0x2A, this.document.getInt("hex_int"));
        assertEquals(42, this.document.getInt("octal_int"));
        assertEquals(0b101010, this.document.getInt("binary_int"));
        assertEquals(-123, this.document.getInt("negative_int"));
        assertEquals(9223372036854775807L, this.document.getLong("large_int"));

        assertEquals(3.14159, this.document.getDbl("basic_float"));
        assertEquals(1.5e-10, this.document.getDbl("exponent_float"));
        assertEquals(-0.001, this.document.getDbl("negative_float"));
        assertEquals(Double.POSITIVE_INFINITY, this.document.getDbl("infinity"));
        assertEquals(Double.NEGATIVE_INFINITY, this.document.getDbl("neg_infinity"));
        assertEquals(Double.NaN, this.document.getDbl("not_a_number"));
    }

    @Test
    @DisplayName("Test booleans")
    void testBooleans() {
        assertEquals(Boolean.TRUE, this.document.getBool("bool_true"));
        assertEquals(Boolean.FALSE, this.document.getBool("bool_false"));
    }

    @Test
    @DisplayName("Test dates and times")
    void testDatesAndTimes() {
        assertEquals(LocalDate.of(1979, 5, 27), document.getLocalDate("local_date"));
        assertEquals(LocalTime.of(7, 32, 0, 999_999_000), document.getLocalTime("local_time"));
        assertEquals(LocalDateTime.of(1979, 5, 27, 7, 32, 0, 999_999_000), document.getLocalDateTime("local_datetime"));
        assertEquals(OffsetDateTime.of(1979, 5, 27, 0, 32, 0, 999_999_000, ZoneOffset.ofHours(-7)),
                this.document.getOffsetDateTime("offset_datetime"));
    }

    @Test
    @DisplayName("Test primitive arrays")
    void testPrimitiveArrays() {
        assertEquals(List.of("red", "yellow", "green"), this.document.getStrList("string_array"));
        assertEquals(List.of(1, 2, 3, 4, 5), this.document.getIntList("integer_array"));
        assertEquals(List.of(1.1, 2.2, 3.3), this.document.getDblList("float_array"));
        assertEquals(List.of(true, false, true), this.document.getBoolList("boolean_array"));
    }

    @Test
    @DisplayName("Test date/time arrays")
    void testDateTimeArrays() {
        assertEquals(List.of(
                LocalDate.of(1979, 5, 27),
                LocalDate.of(1980, 6, 15),
                LocalDate.of(1981, 7, 20)
        ), this.document.getLocalDateList("date_array"));

        assertEquals(List.of(
                LocalTime.of(7, 32, 0),
                LocalTime.of(12, 45, 30),
                LocalTime.of(18, 20, 15)
        ), this.document.getLocalTimeList("time_array"));

        assertEquals(List.of(
                LocalDateTime.of(1979, 5, 27, 7, 32, 0),
                LocalDateTime.of(1980, 6, 15, 12, 45, 30)
        ), this.document.getLocalDateTimeList("datetime_array"));

        assertEquals(List.of(
                OffsetDateTime.of(1979, 5, 27, 0, 32, 0, 0, ZoneOffset.ofHours(-7)),
                OffsetDateTime.of(1980, 6, 15, 12, 45, 30, 0, ZoneOffset.ofHours(2)),
                OffsetDateTime.of(1981, 7, 20, 18, 20, 15, 0, ZoneOffset.UTC)
        ), this.document.getOffsetDateTimeList("offset_datetime_array"));
    }

    @Test
    @DisplayName("Test mixed arrays")
    void testMixedArray() {
        assertEquals(List.of(1, "two", 3.0, true), this.document.getList("mixed_array"));
    }

    @Test
    @DisplayName("Test inline tables")
    void testInlineTables() {
        final TomlDocument database = this.document.getSection("database");
        assertNotNull(database);
        assertEquals("192.168.1.1", database.getStr("server"));
        assertEquals(List.of(8001, 8001, 8002), database.getIntList("ports"));
        assertEquals(5000, database.getInt("connection_max"));
        assertEquals(Boolean.TRUE, database.getBool("enabled"));

        final TomlDocument credentials = database.getSection("credentials");
        assertNotNull(credentials);
        assertEquals("admin", credentials.getStr("username"));
        assertEquals("secret123", credentials.getStr("password"));
    }

    @Test
    @DisplayName("Test nested tables")
    void testNestedTables() {
        final TomlDocument servers = this.document.getSection("servers");
        assertNotNull(servers);

        final TomlDocument dev = servers.getSection("development");
        assertNotNull(dev);
        assertEquals("127.0.0.1", dev.getStr("ip"));
        assertEquals(3000, dev.getInt("port"));
        assertEquals(Boolean.TRUE, dev.getBool("debug"));

        final TomlDocument prod = servers.getSection("production");
        assertNotNull(prod);
        assertEquals("192.168.1.100", prod.getStr("ip"));
        assertEquals(80, prod.getInt("port"));
        assertEquals(Boolean.FALSE, prod.getBool("debug"));
    }

    @Test
    @DisplayName("Test array of tables")
    void testArrayOfTables() {
        List<TomlDocument> products = this.document.getSectionList("products");
        assertNotNull(products);
        assertEquals(3, products.size());

        assertEquals("Hammer", products.get(0).getStr("name"));
        assertEquals(738594937, products.get(0).getInt("sku"));
        assertEquals(15.99, products.get(0).getDbl("price"));
        assertEquals(Boolean.TRUE, products.get(0).getBool("in_stock"));

        assertEquals("Screwdriver", products.get(1).getStr("name"));
        assertEquals(284758393, products.get(1).getInt("sku"));
        assertEquals(8.50, products.get(1).getDbl("price"));
        assertEquals(Boolean.FALSE, products.get(1).getBool("in_stock"));

        assertEquals("Wrench", products.get(2).getStr("name"));
        assertEquals(947382910, products.get(2).getInt("sku"));
        assertEquals(12.75, products.get(2).getDbl("price"));
        assertEquals(Boolean.TRUE, products.get(2).getBool("in_stock"));
    }

    @Test
    @DisplayName("Test nested config tables")
    void testNestedConfigTables() {
        final TomlDocument app = this.document.getSection("application");
        assertNotNull(app);
        assertEquals("MyApp", app.getStr("name"));
        assertEquals("1.2.3", app.getStr("version"));
        assertEquals(LocalDate.of(2024, 1, 15), app.getLocalDate("release_date"));

        final TomlDocument config = app.getSection("config");
        assertNotNull(config);
        assertEquals(30, config.getInt("timeout"));
        assertEquals(3, config.getInt("retry_attempts"));
        assertEquals("INFO", config.getStr("log_level"));

        final TomlDocument dbConfig = config.getSection("database");
        assertNotNull(dbConfig);
        assertEquals("localhost", dbConfig.getStr("host"));
        assertEquals(5432, dbConfig.getInt("port"));
        assertEquals(Boolean.TRUE, dbConfig.getBool("ssl"));
    }

    @Test
    @DisplayName("Test edge cases")
    void testEdgeCases() {
        final TomlDocument edge = this.document.getSection("edge_cases");
        assertNotNull(edge);
        assertEquals("", edge.getStr("empty_string"));
        assertEquals(0, edge.getInt("zero_int"));
        assertEquals(0.0, edge.getDbl("zero_float"));
        assertEquals(-0.0, edge.getDbl("negative_zero"));
        assertEquals(1.7976931348623157e+308, edge.getDbl("very_large_number"));
        assertEquals(2.2250738585072014e-308, edge.getDbl("very_small_number"));
        assertEquals("Hello 🌍 World! Привет мир! こんにちは世界！", edge.getStr("unicode_string"));
    }
}
