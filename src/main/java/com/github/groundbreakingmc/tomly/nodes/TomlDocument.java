package com.github.groundbreakingmc.tomly.nodes;

import com.github.groundbreakingmc.tomly.exceptions.TomlSaveException;
import com.github.groundbreakingmc.tomly.options.WriterOptions;
import com.github.groundbreakingmc.tomly.writer.TomlWriter;
import it.unimi.dsi.fastutil.booleans.BooleanList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.LongList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.*;

/**
 * Defines a typed interface for accessing and navigating TOML document data.
 * <p>
 * The {@code TomlDocument} interface provides methods to retrieve values from a parsed TOML document using dot-separated paths, with support for automatic type conversion and default values. It supports a variety of data types, including strings, integers, longs, doubles, booleans, dates, times, and their respective lists, as well as low-level access to raw nodes and tables. This API simplifies interaction with TOML data by reducing the need for manual type casting and null checks.
 * <p>
 * <strong>Warning:</strong> When retrieving multiple values from the same section, it is more efficient to use {@link #getSection(String)} to access the section once and then retrieve values from it, rather than repeatedly using dot-separated paths. For example:
 * <pre>{@code
 * // Less efficient
 * String one = doc.getStr("section.one");
 * String two = doc.getStr("section.two");
 *
 * // More efficient
 * TomlDocument section = doc.getSection("section");
 * String one = section.getStr("one");
 * String two = section.getStr("two");
 * }</pre>
 *
 * <h3>Usage Examples:</h3>
 * <pre>{@code
 * TomlDocument doc = Tomly.parse(tomlString, false);
 *
 * // Simple value access with defaults
 * String appName = doc.getStr("name", "MyApp");
 * int port = doc.getInt("server.port", 8080);
 * boolean debug = doc.getBool("debug", false);
 *
 * // List access with defaults
 * List<String> hosts = doc.getStrList("servers.hosts", Collections.emptyList());
 * IntList ports = doc.getIntList("servers.ports", IntLists.emptyList());
 *
 * // Date and time access
 * LocalDate release = doc.getLocalDate("release_date", () -> LocalDate.now());
 * OffsetDateTime timestamp = doc.getOffsetDateTime("timestamp", () -> OffsetDateTime.now());
 *
 * // Low-level access for complex structures
 * TableNode config = doc.getSection("config");
 * Map<String, Object> rawData = doc.raw();
 *
 * // Setting a value
 * doc.set("config.version", new StringNode("1.2.3", List.of("This is a header comment!"), "this is inline comment"));
 *
 * // Saving to file
 * doc.save(new File("config.toml"));
 * }</pre>
 *
 * @author GroundbreakingMC
 * @version 1.0.0
 * @since 1.0.0
 */
public interface TomlDocument extends Node {

    // String methods

    /**
     * Gets a string value at the given path.
     *
     * @param path the path to look up
     * @return the string value, or {@code null} if not present
     * @see #getStr(String, String)
     * @see #getStr(String, Supplier)
     * @since 1.0.0
     */
    @Nullable String getStr(@NotNull String path);

    /**
     * Gets a string value at the given path,
     * or returns {@code defaultValue} if the path is missing.
     *
     * @param path         the path to look up
     * @param defaultValue the default value to return if not found
     * @return the string value, or {@code defaultValue} if not present
     * @see #getStr(String, Supplier)
     * @since 1.0.0
     */
    @NotNull String getStr(@NotNull String path, @NotNull String defaultValue);

    /**
     * Gets a string value at the given path,
     * or supplies a default using the given {@link Supplier} if the path is missing.
     *
     * @param path         the path to look up
     * @param defaultValue the supplier of the default value
     * @return the string value, or the supplied value if not present
     * @see #getStr(String, String)
     * @since 1.0.0
     */
    @NotNull String getStr(@NotNull String path, @NotNull Supplier<@NotNull String> defaultValue);

    // List<String> methods

    /**
     * Gets a list of strings at the given path.
     *
     * @param path the path to look up
     * @return the list of strings, or {@code null} if not present
     * @see #getStrList(String, List)
     * @see #getStrList(String, Supplier)
     * @since 1.0.0
     */
    @Nullable List<String> getStrList(@NotNull String path);

    /**
     * Gets a list of strings at the given path,
     * or returns {@code defaultValue} if the path is missing.
     *
     * @param path         the path to look up
     * @param defaultValue the default list to return if not found
     * @return the list of strings, or {@code defaultValue} if not present
     * @see #getStrList(String, Supplier)
     * @since 1.0.0
     */
    @NotNull List<String> getStrList(@NotNull String path, @NotNull List<String> defaultValue);

    /**
     * Gets a list of strings at the given path,
     * or supplies a default using the given {@link Supplier} if the path is missing.
     *
     * @param path         the path to look up
     * @param defaultValue the supplier of the default list
     * @return the list of strings, or the supplied list if not present
     * @see #getStrList(String, List)
     * @since 1.0.0
     */
    @NotNull List<String> getStrList(@NotNull String path, @NotNull Supplier<@NotNull List<String>> defaultValue);

    // Integer methods

    /**
     * Gets an integer value at the given path.
     *
     * @param path the dot-separated path to look up
     * @return the integer value, or {@code null} if not present or not a number
     * @see #getInt(String, int)
     * @see #getInt(String, IntSupplier)
     * @since 1.0.0
     */
    @Nullable Integer getInt(@NotNull String path);

    /**
     * Gets an integer value at the given path,
     * or returns {@code defaultValue} if the path is missing or not a number.
     *
     * @param path         the dot-separated path to look up
     * @param defaultValue the default value to return if not found
     * @return the integer value, or {@code defaultValue} if not present
     * @see #getInt(String, IntSupplier)
     * @since 1.0.0
     */
    int getInt(@NotNull String path, int defaultValue);

    /**
     * Gets an integer value at the given path,
     * or supplies a default using the given {@link IntSupplier} if the path is missing or not a number.
     *
     * @param path         the dot-separated path to look up
     * @param defaultValue the supplier of the default value
     * @return the integer value, or the supplied value if not present
     * @see #getInt(String, int)
     * @since 1.0.0
     */
    int getInt(@NotNull String path, @NotNull IntSupplier defaultValue);

    // List<Integer> methods

    /**
     * Gets a list of integers at the given path.
     *
     * @param path the dot-separated path to look up
     * @return the list of integers, or {@code null} if not present or not an array
     * @see #getIntList(String, IntList)
     * @see #getIntList(String, Supplier)
     * @since 1.0.0
     */
    @Nullable IntList getIntList(@NotNull String path);

    /**
     * Gets a list of integers at the given path,
     * or returns {@code defaultValue} if the path is missing or not an array.
     *
     * @param path         the dot-separated path to look up
     * @param defaultValue the default list to return if not found
     * @return the list of integers, or {@code defaultValue} if not present
     * @see #getIntList(String, Supplier)
     * @since 1.0.0
     */
    @NotNull IntList getIntList(@NotNull String path, @NotNull IntList defaultValue);

    /**
     * Gets a list of integers at the given path,
     * or supplies a default using the given {@link Supplier} if the path is missing or not an array.
     *
     * @param path         the dot-separated path to look up
     * @param defaultValue the supplier of the default list
     * @return the list of integers, or the supplied list if not present
     * @see #getIntList(String, IntList)
     * @since 1.0.0
     */
    @NotNull IntList getIntList(@NotNull String path, @NotNull Supplier<@NotNull IntList> defaultValue);

    // Long methods

    /**
     * Gets a long value at the given path.
     *
     * @param path the dot-separated path to look up
     * @return the long value, or {@code null} if not present or not a number
     * @see #getLong(String, long)
     * @see #getLong(String, LongSupplier)
     * @since 1.0.0
     */
    @Nullable Long getLong(@NotNull String path);

    /**
     * Gets a long value at the given path,
     * or returns {@code defaultValue} if the path is missing or not a number.
     *
     * @param path         the dot-separated path to look up
     * @param defaultValue the default value to return if not found
     * @return the long value, or {@code defaultValue} if not present
     * @see #getLong(String, LongSupplier)
     * @since 1.0.0
     */
    long getLong(@NotNull String path, long defaultValue);

    /**
     * Gets a long value at the given path,
     * or supplies a default using the given {@link LongSupplier} if the path is missing or not a number.
     *
     * @param path         the dot-separated path to look up
     * @param defaultValue the supplier of the default value
     * @return the long value, or the supplied value if not present
     * @see #getLong(String, long)
     * @since 1.0.0
     */
    long getLong(@NotNull String path, @NotNull LongSupplier defaultValue);

    // List<Long> methods

    /**
     * Gets a list of longs at the given path.
     *
     * @param path the dot-separated path to look up
     * @return the list of longs, or {@code null} if not present or not an array
     * @see #getLongList(String, LongList)
     * @see #getLongList(String, Supplier)
     * @since 1.0.0
     */
    @Nullable LongList getLongList(@NotNull String path);

    /**
     * Gets a list of longs at the given path,
     * or returns {@code defaultValue} if the path is missing or not an array.
     *
     * @param path         the dot-separated path to look up
     * @param defaultValue the default list to return if not found
     * @return the list of longs, or {@code defaultValue} if not present
     * @see #getLongList(String, Supplier)
     * @since 1.0.0
     */
    @NotNull LongList getLongList(@NotNull String path, @NotNull LongList defaultValue);

    /**
     * Gets a list of longs at the given path,
     * or supplies a default using the given {@link Supplier} if the path is missing or not an array.
     *
     * @param path         the dot-separated path to look up
     * @param defaultValue the supplier of the default list
     * @return the list of longs, or the supplied list if not present
     * @see #getLongList(String, LongList)
     * @since 1.0.0
     */
    @NotNull LongList getLongList(@NotNull String path, @NotNull Supplier<@NotNull LongList> defaultValue);

    // Double methods

    /**
     * Gets a double value at the given path.
     *
     * @param path the dot-separated path to look up
     * @return the double value, or {@code null} if not present or not a number
     * @see #getDbl(String, double)
     * @see #getDbl(String, DoubleSupplier)
     * @since 1.0.0
     */
    @Nullable Double getDbl(@NotNull String path);

    /**
     * Gets a double value at the given path,
     * or returns {@code defaultValue} if the path is missing or not a number.
     *
     * @param path         the dot-separated path to look up
     * @param defaultValue the default value to return if not found
     * @return the double value, or {@code defaultValue} if not present
     * @see #getDbl(String, DoubleSupplier)
     * @since 1.0.0
     */
    double getDbl(@NotNull String path, double defaultValue);

    /**
     * Gets a double value at the given path,
     * or supplies a default using the given {@link DoubleSupplier} if the path is missing or not a number.
     *
     * @param path         the dot-separated path to look up
     * @param defaultValue the supplier of the default value
     * @return the double value, or the supplied value if not present
     * @see #getDbl(String, double)
     * @since 1.0.0
     */
    double getDbl(@NotNull String path, @NotNull DoubleSupplier defaultValue);

    // List<Double> methods

    /**
     * Gets a list of doubles at the given path.
     *
     * @param path the dot-separated path to look up
     * @return the list of doubles, or {@code null} if not present or not an array
     * @see #getDblList(String, DoubleList)
     * @see #getDblList(String, Supplier)
     * @since 1.0.0
     */
    @Nullable DoubleList getDblList(@NotNull String path);

    /**
     * Gets a list of doubles at the given path,
     * or returns {@code defaultValue} if the path is missing or not an array.
     *
     * @param path         the dot-separated path to look up
     * @param defaultValue the default list to return if not found
     * @return the list of doubles, or {@code defaultValue} if not present
     * @see #getDblList(String, Supplier)
     * @since 1.0.0
     */
    @NotNull DoubleList getDblList(@NotNull String path, @NotNull DoubleList defaultValue);

    /**
     * Gets a list of doubles at the given path,
     * or supplies a default using the given {@link Supplier} if the path is missing or not an array.
     *
     * @param path         the dot-separated path to look up
     * @param defaultValue the supplier of the default list
     * @return the list of doubles, or the supplied list if not present
     * @see #getDblList(String, DoubleList)
     * @since 1.0.0
     */
    @NotNull DoubleList getDblList(@NotNull String path, @NotNull Supplier<@NotNull DoubleList> defaultValue);

    // Boolean methods

    /**
     * Gets a boolean value at the given path.
     *
     * @param path the dot-separated path to look up
     * @return the boolean value, or {@code null} if not present or not a boolean
     * @see #getBool(String, boolean)
     * @see #getBool(String, BooleanSupplier)
     * @since 1.0.0
     */
    @Nullable Boolean getBool(@NotNull String path);

    /**
     * Gets a boolean value at the given path,
     * or returns {@code defaultValue} if the path is missing or not a boolean.
     *
     * @param path         the dot-separated path to look up
     * @param defaultValue the default value to return if not found
     * @return the boolean value, or {@code defaultValue} if not present
     * @see #getBool(String, BooleanSupplier)
     * @since 1.0.0
     */
    boolean getBool(@NotNull String path, boolean defaultValue);

    /**
     * Gets a boolean value at the given path,
     * or supplies a default using the given {@link BooleanSupplier} if the path is missing or not a boolean.
     *
     * @param path         the dot-separated path to look up
     * @param defaultValue the supplier of the default value
     * @return the boolean value, or the supplied value if not present
     * @see #getBool(String, boolean)
     * @since 1.0.0
     */
    boolean getBool(@NotNull String path, @NotNull BooleanSupplier defaultValue);

    // List<Boolean> methods

    /**
     * Gets a list of booleans at the given path.
     *
     * @param path the dot-separated path to look up
     * @return the list of booleans, or {@code null} if not present or not an array
     * @see #getBoolList(String, BooleanList)
     * @see #getBoolList(String, Supplier)
     * @since 1.0.0
     */
    @Nullable BooleanList getBoolList(@NotNull String path);

    /**
     * Gets a list of booleans at the given path,
     * or returns {@code defaultValue} if the path is missing or not an array.
     *
     * @param path         the dot-separated path to look up
     * @param defaultValue the default list to return if not found
     * @return the list of booleans, or {@code defaultValue} if not present
     * @see #getBoolList(String, Supplier)
     * @since 1.0.0
     */
    @NotNull BooleanList getBoolList(@NotNull String path, @NotNull BooleanList defaultValue);

    /**
     * Gets a list of booleans at the given path,
     * or supplies a default using the given {@link Supplier} if the path is missing or not an array.
     *
     * @param path         the dot-separated path to look up
     * @param defaultValue the supplier of the default list
     * @return the list of booleans, or the supplied list if not present
     * @see #getBoolList(String, BooleanList)
     * @since 1.0.0
     */
    @NotNull BooleanList getBoolList(@NotNull String path, @NotNull Supplier<@NotNull BooleanList> defaultValue);

    // LocalDate methods

    /**
     * Gets a LocalDate value at the given path.
     *
     * @param path the dot-separated path to look up
     * @return the LocalDate value, or {@code null} if not present or not a date
     * @see #getLocalDate(String, LocalDate)
     * @see #getLocalDate(String, Supplier)
     * @since 1.0.0
     */
    @Nullable LocalDate getLocalDate(@NotNull String path);

    /**
     * Gets a LocalDate value at the given path,
     * or returns {@code defaultValue} if the path is missing or not a date.
     *
     * @param path         the dot-separated path to look up
     * @param defaultValue the default value to return if not found
     * @return the LocalDate value, or {@code defaultValue} if not present
     * @see #getLocalDate(String, Supplier)
     * @since 1.0.0
     */
    @NotNull LocalDate getLocalDate(@NotNull String path, @NotNull LocalDate defaultValue);

    /**
     * Gets a LocalDate value at the given path,
     * or supplies a default using the given {@link Supplier} if the path is missing or not a date.
     *
     * @param path         the dot-separated path to look up
     * @param defaultValue the supplier of the default value
     * @return the LocalDate value, or the supplied value if not present
     * @see #getLocalDate(String, LocalDate)
     * @since 1.0.0
     */
    @NotNull LocalDate getLocalDate(@NotNull String path, @NotNull Supplier<@NotNull LocalDate> defaultValue);

    // List<LocalDate> methods

    /**
     * Gets a list of LocalDate values at the given path.
     *
     * @param path the dot-separated path to look up
     * @return the list of LocalDate values, or {@code null} if not present or not an array
     * @see #getLocalDateList(String, List)
     * @see #getLocalDateList(String, Supplier)
     * @since 1.0.0
     */
    @Nullable List<LocalDate> getLocalDateList(@NotNull String path);

    /**
     * Gets a list of LocalDate values at the given path,
     * or returns {@code defaultValue} if the path is missing or not an array.
     *
     * @param path         the dot-separated path to look up
     * @param defaultValue the default list to return if not found
     * @return the list of LocalDate values, or {@code defaultValue} if not present
     * @see #getLocalDateList(String, Supplier)
     * @since 1.0.0
     */
    @NotNull List<LocalDate> getLocalDateList(@NotNull String path, @NotNull List<LocalDate> defaultValue);

    /**
     * Gets a list of LocalDate values at the given path,
     * or supplies a default using the given {@link Supplier} if the path is missing or not an array.
     *
     * @param path         the dot-separated path to look up
     * @param defaultValue the supplier of the default list
     * @return the list of LocalDate values, or the supplied list if not present
     * @see #getLocalDateList(String, List)
     * @since 1.0.0
     */
    @NotNull List<LocalDate> getLocalDateList(@NotNull String path, @NotNull Supplier<@NotNull List<LocalDate>> defaultValue);

    // LocalTime methods

    /**
     * Gets a LocalTime value at the given path.
     *
     * @param path the dot-separated path to look up
     * @return the LocalTime value, or {@code null} if not present or not a time
     * @see #getLocalTime(String, LocalTime)
     * @see #getLocalTime(String, Supplier)
     * @since 1.0.0
     */
    @Nullable LocalTime getLocalTime(@NotNull String path);

    /**
     * Gets a LocalTime value at the given path,
     * or returns {@code defaultValue} if the path is missing or not a time.
     *
     * @param path         the dot-separated path to look up
     * @param defaultValue the default value to return if not found
     * @return the LocalTime value, or {@code defaultValue} if not present
     * @see #getLocalTime(String, Supplier)
     * @since 1.0.0
     */
    @NotNull LocalTime getLocalTime(@NotNull String path, @NotNull LocalTime defaultValue);

    /**
     * Gets a LocalTime value at the given path,
     * or supplies a default using the given {@link Supplier} if the path is missing or not a time.
     *
     * @param path         the dot-separated path to look up
     * @param defaultValue the supplier of the default value
     * @return the LocalTime value, or the supplied value if not present
     * @see #getLocalTime(String, LocalTime)
     * @since 1.0.0
     */
    @NotNull LocalTime getLocalTime(@NotNull String path, @NotNull Supplier<@NotNull LocalTime> defaultValue);

    // List<LocalTime> methods

    /**
     * Gets a list of LocalTime values at the given path.
     *
     * @param path the dot-separated path to look up
     * @return the list of LocalTime values, or {@code null} if not present or not an array
     * @see #getLocalTimeList(String, List)
     * @see #getLocalTimeList(String, Supplier)
     * @since 1.0.0
     */
    @Nullable List<LocalTime> getLocalTimeList(@NotNull String path);

    /**
     * Gets a list of LocalTime values at the given path,
     * or returns {@code defaultValue} if the path is missing or not an array.
     *
     * @param path         the dot-separated path to look up
     * @param defaultValue the default list to return if not found
     * @return the list of LocalTime values, or {@code defaultValue} if not present
     * @see #getLocalTimeList(String, Supplier)
     * @since 1.0.0
     */
    @NotNull List<LocalTime> getLocalTimeList(@NotNull String path, @NotNull List<LocalTime> defaultValue);

    /**
     * Gets a list of LocalTime values at the given path,
     * or supplies a default using the given {@link Supplier} if the path is missing or not an array.
     *
     * @param path         the dot-separated path to look up
     * @param defaultValue the supplier of the default list
     * @return the list of LocalTime values, or the supplied list if not present
     * @see #getLocalTimeList(String, List)
     * @since 1.0.0
     */
    @NotNull List<LocalTime> getLocalTimeList(@NotNull String path, @NotNull Supplier<@NotNull List<LocalTime>> defaultValue);

    // LocalDateTime methods

    /**
     * Gets a LocalDateTime value at the given path.
     *
     * @param path the dot-separated path to look up
     * @return the LocalDateTime value, or {@code null} if not present or not a datetime
     * @see #getLocalDateTime(String, LocalDateTime)
     * @see #getLocalDateTime(String, Supplier)
     * @since 1.0.0
     */
    @Nullable LocalDateTime getLocalDateTime(@NotNull String path);

    /**
     * Gets a LocalDateTime value at the given path,
     * or returns {@code defaultValue} if the path is missing or not a datetime.
     *
     * @param path         the dot-separated path to look up
     * @param defaultValue the default value to return if not found
     * @return the LocalDateTime value, or {@code defaultValue} if not present
     * @see #getLocalDateTime(String, Supplier)
     * @since 1.0.0
     */
    @NotNull LocalDateTime getLocalDateTime(@NotNull String path, @NotNull LocalDateTime defaultValue);

    /**
     * Gets a LocalDateTime value at the given path,
     * or supplies a default using the given {@link Supplier} if the path is missing or not a datetime.
     *
     * @param path         the dot-separated path to look up
     * @param defaultValue the supplier of the default value
     * @return the LocalDateTime value, or the supplied value if not present
     * @see #getLocalDateTime(String, LocalDateTime)
     * @since 1.0.0
     */
    @NotNull LocalDateTime getLocalDateTime(@NotNull String path, @NotNull Supplier<@NotNull LocalDateTime> defaultValue);

    // List<LocalDateTime> methods

    /**
     * Gets a list of LocalDateTime values at the given path.
     *
     * @param path the dot-separated path to look up
     * @return the list of LocalDateTime values, or {@code null} if not present or not an array
     * @see #getLocalDateTimeList(String, List)
     * @see #getLocalDateTimeList(String, Supplier)
     * @since 1.0.0
     */
    @Nullable List<LocalDateTime> getLocalDateTimeList(@NotNull String path);

    /**
     * Gets a list of LocalDateTime values at the given path,
     * or returns {@code defaultValue} if the path is missing or not an array.
     *
     * @param path         the dot-separated path to look up
     * @param defaultValue the default list to return if not found
     * @return the list of LocalDateTime values, or {@code defaultValue} if not present
     * @see #getLocalDateTimeList(String, Supplier)
     * @since 1.0.0
     */
    @NotNull List<LocalDateTime> getLocalDateTimeList(@NotNull String path, @NotNull List<LocalDateTime> defaultValue);

    /**
     * Gets a list of LocalDateTime values at the given path,
     * or supplies a default using the given {@link Supplier} if the path is missing or not an array.
     *
     * @param path         the dot-separated path to look up
     * @param defaultValue the supplier of the default list
     * @return the list of LocalDateTime values, or the supplied list if not present
     * @see #getLocalDateTimeList(String, List)
     * @since 1.0.0
     */
    @NotNull List<LocalDateTime> getLocalDateTimeList(@NotNull String path, @NotNull Supplier<@NotNull List<LocalDateTime>> defaultValue);

    // OffsetDateTime methods

    /**
     * Gets an OffsetDateTime value at the given path.
     *
     * @param path the dot-separated path to look up
     * @return the OffsetDateTime value, or {@code null} if not present or not an offset datetime
     * @see #getOffsetDateTime(String, OffsetDateTime)
     * @see #getOffsetDateTime(String, Supplier)
     * @since 1.0.0
     */
    @Nullable OffsetDateTime getOffsetDateTime(@NotNull String path);

    /**
     * Gets an OffsetDateTime value at the given path,
     * or returns {@code defaultValue} if the path is missing or not an offset datetime.
     *
     * @param path         the dot-separated path to look up
     * @param defaultValue the default value to return if not found
     * @return the OffsetDateTime value, or {@code defaultValue} if not present
     * @see #getOffsetDateTime(String, Supplier)
     * @since 1.0.0
     */
    @NotNull OffsetDateTime getOffsetDateTime(@NotNull String path, @NotNull OffsetDateTime defaultValue);

    /**
     * Gets an OffsetDateTime value at the given path,
     * or supplies a default using the given {@link Supplier} if the path is missing or not an offset datetime.
     *
     * @param path         the dot-separated path to look up
     * @param defaultValue the supplier of the default value
     * @return the OffsetDateTime value, or the supplied value if not present
     * @see #getOffsetDateTime(String, OffsetDateTime)
     * @since 1.0.0
     */
    @NotNull OffsetDateTime getOffsetDateTime(@NotNull String path, @NotNull Supplier<@NotNull OffsetDateTime> defaultValue);

    // List<OffsetDateTime> methods

    /**
     * Gets a list of OffsetDateTime values at the given path.
     *
     * @param path the dot-separated path to look up
     * @return the list of OffsetDateTime values, or {@code null} if not present or not an array
     * @see #getOffsetDateTimeList(String, List)
     * @see #getOffsetDateTimeList(String, Supplier)
     * @since 1.0.0
     */
    @Nullable List<OffsetDateTime> getOffsetDateTimeList(@NotNull String path);

    /**
     * Gets a list of OffsetDateTime values at the given path,
     * or returns {@code defaultValue} if the path is missing or not an array.
     *
     * @param path         the dot-separated path to look up
     * @param defaultValue the default list to return if not found
     * @return the list of OffsetDateTime values, or {@code defaultValue} if not present
     * @see #getOffsetDateTimeList(String, Supplier)
     * @since 1.0.0
     */
    @NotNull List<OffsetDateTime> getOffsetDateTimeList(@NotNull String path, @NotNull List<OffsetDateTime> defaultValue);

    /**
     * Gets a list of OffsetDateTime values at the given path,
     * or supplies a default using the given {@link Supplier} if the path is missing or not an array.
     *
     * @param path         the dot-separated path to look up
     * @param defaultValue the supplier of the default list
     * @return the list of OffsetDateTime values, or the supplied list if not present
     * @see #getOffsetDateTimeList(String, List)
     * @since 1.0.0
     */
    @NotNull List<OffsetDateTime> getOffsetDateTimeList(@NotNull String path, @NotNull Supplier<@NotNull List<OffsetDateTime>> defaultValue);

    // List<TomlDocument>

    /**
     * Gets a list of sections at the specified path.
     *
     * @param path the dot-separated path to look up
     * @return the List at the path, or {@code null} if not found
     * @since 1.0.0
     */
    @Nullable List<TomlDocument> getSectionList(@NotNull String path);

    // List<Object> (mixed collection)

    /**
     * Gets a mixed list at the specified path.
     *
     * @param path the dot-separated path to look up
     * @return the List at the path, or {@code null} if not found
     * @since 1.0.0
     */
    @Nullable List<Object> getList(@NotNull String path);

    // Low level methods

    /**
     * Gets a node at the specified path.
     *
     * @param path the dot-separated path to the node
     * @return the node at the path, or {@code null} if not found
     * @see #raw(String)
     * @since 1.0.0
     */
    @Nullable Node get(@NotNull String path);

    /**
     * Gets a value at the specified path.
     *
     * @param path the dot-separated path to the node
     * @return the value at the path, or {@code null} if not found
     * @see #get(String)
     * @since 1.0.0
     */
    @Nullable Object raw(@NotNull String path);

    /**
     * Checks if a path exists in the document.
     *
     * @param path the dot-separated path to check
     * @return {@code true} if the path exists, {@code false} otherwise
     * @since 1.0.0
     */
    boolean hasPath(@NotNull String path);

    /**
     * Gets the name of the current section.
     *
     * @return the name of the section, or {@code null} if not applicable
     * @since 1.0.0
     */
    @Nullable String getName();

    /**
     * Gets the parent section of the current section.
     *
     * @return the parent section, or {@code null} if there is no parent
     * @since 1.0.0
     */
    @Nullable TomlDocument parent();

    /**
     * Converts the entire document to a raw Java object map.
     *
     * @return a map with plain Java objects as values
     * @see #getSection(String)
     * @since 1.0.0
     */
    @NotNull Map<String, Object> raw();

    /**
     * Gets a TableNode at the specified path.
     *
     * @param path the dot-separated path
     * @return the TableNode, or {@code null} if not found or not a table
     * @since 1.0.0
     */
    @Nullable TomlDocument getSection(@NotNull String path);

    /**
     * Sets a value at the specified path.
     *
     * @param path  the dot-separated path to set the value
     * @param value the node value to set
     * @since 1.0.0
     */
    void set(@NotNull String path, @NotNull Node value);

    // Save method

    /**
     * Saves the TOML document to the specified file.
     * <p>
     * This method serializes the entire document structure to TOML format and writes
     * it to the given path. The output preserves the hierarchical structure, comments
     * (when available), and proper TOML formatting conventions.
     * <p>
     * The method handles:
     * <ul>
     *   <li>Proper TOML syntax for all supported data types</li>
     *   <li>Nested tables and array of tables</li>
     *   <li>Inline tables when appropriate</li>
     *   <li>Header and inline comments (when preserved from original parsing)</li>
     *   <li>Proper escaping of strings and special characters</li>
     * </ul>
     *
     * <h3>Usage Examples:</h3>
     * <pre>{@code
     * // Save with Path
     * TomlDocument doc = Tomly.parse(tomlString, true);
     * doc.save(Path.of("config.toml"), WriterOptions.defaultOptions());
     *
     * // Modify and save
     * doc.set("version", new StringNode("2.0.0"));
     * doc.save(Path.of("updated-config.toml"), WriterOptions.builder()
     *     .writeHeaderComments(true)
     *     .writeInlineComments(true)
     *     .build());
     * }</pre>
     *
     * @param path    the path to save the TOML document to
     * @param options the formatting and serialization options controlling how the TOML
     *                document is written, including comment preservation and line wrapping
     * @throws TomlSaveException        if an I/O error occurs during writing
     * @throws IllegalArgumentException if the file is null or cannot be written
     * @see TomlWriter#write(Map, Path, WriterOptions)
     * @since 1.0.0
     */
    void save(@NotNull Path path, @NotNull WriterOptions options) throws TomlSaveException;
}
