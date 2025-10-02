package com.github.groundbreakingmc.tomly.nodes.impl;

import com.github.groundbreakingmc.tomly.exceptions.TomlSaveException;
import com.github.groundbreakingmc.tomly.exceptions.TomlTypeMismatchException;
import com.github.groundbreakingmc.tomly.nodes.Node;
import com.github.groundbreakingmc.tomly.nodes.TomlDocument;
import com.github.groundbreakingmc.tomly.options.WriterOptions;
import com.github.groundbreakingmc.tomly.writer.TomlWriter;
import it.unimi.dsi.fastutil.booleans.BooleanArrayList;
import it.unimi.dsi.fastutil.booleans.BooleanList;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.*;

/**
 * A node implementation that wraps a TableNode and provides a typed API for accessing TOML document data.
 * <p>
 * This class implements the {@link TomlDocument} interface by delegating most operations to an underlying
 * {@link TableNode}. It supports retrieving values with type safety, setting values, and navigating the
 * document structure. Type mismatches during value retrieval result in a {@link TomlTypeMismatchException}.
 *
 * @author GroundbreakingMC
 * @version 1.0.0
 * @since 1.0.0
 */
public final class DocumentNode extends TableNode implements TomlDocument {

    private final @Nullable String name;
    private final @Nullable DocumentNode parent;

    /**
     * Constructs a DocumentNode wrapping the given TableNode.
     *
     * @param tableNode the TableNode to wrap
     * @since 1.0.0
     */
    public DocumentNode(@NotNull TableNode tableNode) {
        this(tableNode, null, null);
    }

    /**
     * Constructs a DocumentNode wrapping the given TableNode with a name and parent.
     *
     * @param tableNode the TableNode to wrap
     * @param name      the name of this section, or null if not applicable
     * @param parent    the parent DocumentNode, or null if this is the root
     * @since 1.0.0
     */
    public DocumentNode(@NotNull TableNode tableNode, @Nullable String name, @Nullable DocumentNode parent) {
        super(tableNode.name(), tableNode);
        this.name = name;
        this.parent = parent;
    }

    // String methods

    /**
     * Gets a string value at the given path.
     *
     * @param path the dot-separated path to look up
     * @return the string value, or {@code null} if not present
     * @see #getStr(String, String)
     * @see #getStr(String, Supplier)
     * @since 1.0.0
     */
    @Override
    public @Nullable String getStr(@NotNull String path) {
        return this.get(path, String.class);
    }

    /**
     * Gets a string value at the given path,
     * or returns {@code defaultValue} if the path is missing.
     *
     * @param path         the dot-separated path to look up
     * @param defaultValue the default value to return if not found
     * @return the string value, or {@code defaultValue} if not present
     * @since 1.0.0
     */
    @Override
    public @NotNull String getStr(@NotNull String path, @NotNull String defaultValue) {
        final String value = this.getStr(path);
        return value != null ? value : defaultValue;
    }

    /**
     * Gets a string value at the given path,
     * or supplies a default using the given {@link Supplier} if the path is missing.
     *
     * @param path         the dot-separated path to look up
     * @param defaultValue the supplier of the default value
     * @return the string value, or the supplied value if not present
     * @since 1.0.0
     */
    @Override
    public @NotNull String getStr(@NotNull String path, @NotNull Supplier<@NotNull String> defaultValue) {
        final String value = this.getStr(path);
        return value != null ? value : defaultValue.get();
    }

    // List<String> methods

    /**
     * Gets a list of strings at the given path.
     *
     * @param path the dot-separated path to look up
     * @return the list of strings, or {@code null} if not present
     * @see #getStrList(String, List)
     * @see #getStrList(String, Supplier)
     * @since 1.0.0
     */
    @Override
    public @Nullable List<String> getStrList(@NotNull String path) {
        return this.getList(path, ArrayList::new);
    }

    /**
     * Gets a list of strings at the given path,
     * or returns {@code defaultValue} if the path is missing.
     *
     * @param path         the dot-separated path to look up
     * @param defaultValue the default list to return if not found
     * @return the list of strings, or {@code defaultValue} if not present
     * @since 1.0.0
     */
    @Override
    public @NotNull List<String> getStrList(@NotNull String path, @NotNull List<String> defaultValue) {
        final List<String> value = this.getStrList(path);
        return value != null ? value : defaultValue;
    }

    /**
     * Gets a list of strings at the given path,
     * or supplies a default using the given {@link Supplier} if the path is missing.
     *
     * @param path         the dot-separated path to look up
     * @param defaultValue the supplier of the default list
     * @return the list of strings, or the supplied list if not present
     * @since 1.0.0
     */
    @Override
    public @NotNull List<String> getStrList(@NotNull String path, @NotNull Supplier<@NotNull List<String>> defaultValue) {
        final List<String> value = this.getStrList(path);
        return value != null ? value : defaultValue.get();
    }

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
    @Override
    public @Nullable Integer getInt(@NotNull String path) {
        final Node node = super.get(path);
        if (node == null) return null;
        if (!(node instanceof NumberNode numberNode)) {
            throw new TomlTypeMismatchException("Long", node.value().getClass().getSimpleName(), path, node.line(), node.column(), path);
        }
        return numberNode.intValue();
    }

    /**
     * Gets an integer value at the given path,
     * or returns {@code defaultValue} if the path is missing or not a number.
     *
     * @param path         the dot-separated path to look up
     * @param defaultValue the default value to return if not found
     * @return the integer value, or {@code defaultValue} if not present
     * @since 1.0.0
     */
    @Override
    public int getInt(@NotNull String path, int defaultValue) {
        final Integer value = this.getInt(path);
        return value != null ? value : defaultValue;
    }

    /**
     * Gets an integer value at the given path,
     * or supplies a default using the given {@link IntSupplier} if the path is missing or not a number.
     *
     * @param path         the dot-separated path to look up
     * @param defaultValue the supplier of the default value
     * @return the integer value, or the supplied value if not present
     * @since 1.0.0
     */
    @Override
    public int getInt(@NotNull String path, @NotNull IntSupplier defaultValue) {
        final Integer value = this.getInt(path);
        return value != null ? value : defaultValue.getAsInt();
    }

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
    @Override
    public @Nullable IntList getIntList(@NotNull String path) {
        return this.getList(path, IntArrayList::new);
    }

    /**
     * Gets a list of integers at the given path,
     * or returns {@code defaultValue} if the path is missing or not an array.
     *
     * @param path         the dot-separated path to look up
     * @param defaultValue the default list to return if not found
     * @return the list of integers, or {@code defaultValue} if not present
     * @since 1.0.0
     */
    @Override
    public @NotNull IntList getIntList(@NotNull String path, @NotNull IntList defaultValue) {
        final IntList value = this.getIntList(path);
        return value != null ? value : defaultValue;
    }

    /**
     * Gets a list of integers at the given path,
     * or supplies a default using the given {@link Supplier} if the path is missing or not an array.
     *
     * @param path         the dot-separated path to look up
     * @param defaultValue the supplier of the default list
     * @return the list of integers, or the supplied list if not present
     * @since 1.0.0
     */
    @Override
    public @NotNull IntList getIntList(@NotNull String path, @NotNull Supplier<@NotNull IntList> defaultValue) {
        final IntList value = this.getIntList(path);
        return value != null ? value : defaultValue.get();
    }

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
    @Override
    public @Nullable Long getLong(@NotNull String path) {
        final Node node = super.get(path);
        if (node == null) return null;
        if (!(node instanceof NumberNode numberNode)) {
            throw new TomlTypeMismatchException("Long", node.value().getClass().getSimpleName(), path, node.line(), node.column(), path);
        }
        return numberNode.longValue();
    }

    /**
     * Gets a long value at the given path,
     * or returns {@code defaultValue} if the path is missing or not a number.
     *
     * @param path         the dot-separated path to look up
     * @param defaultValue the default value to return if not found
     * @return the long value, or {@code defaultValue} if not present
     * @since 1.0.0
     */
    @Override
    public long getLong(@NotNull String path, long defaultValue) {
        final Long value = this.getLong(path);
        return value != null ? value : defaultValue;
    }

    /**
     * Gets a long value at the given path,
     * or supplies a default using the given {@link LongSupplier} if the path is missing or not a number.
     *
     * @param path         the dot-separated path to look up
     * @param defaultValue the supplier of the default value
     * @return the long value, or the supplied value if not present
     * @since 1.0.0
     */
    @Override
    public long getLong(@NotNull String path, @NotNull LongSupplier defaultValue) {
        final Long value = this.getLong(path);
        return value != null ? value : defaultValue.getAsLong();
    }

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
    @Override
    public @Nullable LongList getLongList(@NotNull String path) {
        return this.getList(path, LongArrayList::new);
    }

    /**
     * Gets a list of longs at the given path,
     * or returns {@code defaultValue} if the path is missing or not an array.
     *
     * @param path         the dot-separated path to look up
     * @param defaultValue the default list to return if not found
     * @return the list of longs, or {@code defaultValue} if not present
     * @since 1.0.0
     */
    @Override
    public @NotNull LongList getLongList(@NotNull String path, @NotNull LongList defaultValue) {
        final LongList value = this.getLongList(path);
        return value != null ? value : defaultValue;
    }

    /**
     * Gets a list of longs at the given path,
     * or supplies a default using the given {@link Supplier} if the path is missing or not an array.
     *
     * @param path         the dot-separated path to look up
     * @param defaultValue the supplier of the default list
     * @return the list of longs, or the supplied list if not present
     * @since 1.0.0
     */
    @Override
    public @NotNull LongList getLongList(@NotNull String path, @NotNull Supplier<@NotNull LongList> defaultValue) {
        final LongList value = this.getLongList(path);
        return value != null ? value : defaultValue.get();
    }

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
    @Override
    public @Nullable Double getDbl(@NotNull String path) {
        final Node node = super.get(path);
        if (node == null) return null;
        if (!(node instanceof NumberNode numberNode)) {
            throw new TomlTypeMismatchException("Float", node.value().getClass().getSimpleName(), path, node.line(), node.column(), path);
        }
        return numberNode.doubleValue();
    }

    /**
     * Gets a double value at the given path,
     * or returns {@code defaultValue} if the path is missing or not a number.
     *
     * @param path         the dot-separated path to look up
     * @param defaultValue the default value to return if not found
     * @return the double value, or {@code defaultValue} if not present
     * @since 1.0.0
     */
    @Override
    public double getDbl(@NotNull String path, double defaultValue) {
        final Double value = this.getDbl(path);
        return value != null ? value : defaultValue;
    }

    /**
     * Gets a double value at the given path,
     * or supplies a default using the given {@link DoubleSupplier} if the path is missing or not a number.
     *
     * @param path         the dot-separated path to look up
     * @param defaultValue the supplier of the default value
     * @return the double value, or the supplied value if not present
     * @since 1.0.0
     */
    @Override
    public double getDbl(@NotNull String path, @NotNull DoubleSupplier defaultValue) {
        final Double value = this.getDbl(path);
        return value != null ? value : defaultValue.getAsDouble();
    }

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
    @Override
    public @Nullable DoubleList getDblList(@NotNull String path) {
        return this.getList(path, DoubleArrayList::new);
    }

    /**
     * Gets a list of doubles at the given path,
     * or returns {@code defaultValue} if the path is missing or not an array.
     *
     * @param path         the dot-separated path to look up
     * @param defaultValue the default list to return if not found
     * @return the list of doubles, or {@code defaultValue} if not present
     * @since 1.0.0
     */
    @Override
    public @NotNull DoubleList getDblList(@NotNull String path, @NotNull DoubleList defaultValue) {
        final DoubleList value = this.getDblList(path);
        return value != null ? value : defaultValue;
    }

    /**
     * Gets a list of doubles at the given path,
     * or supplies a default using the given {@link Supplier} if the path is missing or not an array.
     *
     * @param path         the dot-separated path to look up
     * @param defaultValue the supplier of the default list
     * @return the list of doubles, or the supplied list if not present
     * @since 1.0.0
     */
    @Override
    public @NotNull DoubleList getDblList(@NotNull String path, @NotNull Supplier<@NotNull DoubleList> defaultValue) {
        final DoubleList value = this.getDblList(path);
        return value != null ? value : defaultValue.get();
    }

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
    @Override
    public @Nullable Boolean getBool(@NotNull String path) {
        return this.get(path, Boolean.class);
    }

    /**
     * Gets a boolean value at the given path,
     * or returns {@code defaultValue} if the path is missing or not a boolean.
     *
     * @param path         the dot-separated path to look up
     * @param defaultValue the default value to return if not found
     * @return the boolean value, or {@code defaultValue} if not present
     * @since 1.0.0
     */
    @Override
    public boolean getBool(@NotNull String path, boolean defaultValue) {
        final Boolean value = this.getBool(path);
        return value != null ? value : defaultValue;
    }

    /**
     * Gets a boolean value at the given path,
     * or supplies a default using the given {@link BooleanSupplier} if the path is missing or not a boolean.
     *
     * @param path         the dot-separated path to look up
     * @param defaultValue the supplier of the default value
     * @return the boolean value, or the supplied value if not present
     * @since 1.0.0
     */
    @Override
    public boolean getBool(@NotNull String path, @NotNull BooleanSupplier defaultValue) {
        final Boolean value = this.getBool(path);
        return value != null ? value : defaultValue.getAsBoolean();
    }

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
    @Override
    public @Nullable BooleanList getBoolList(@NotNull String path) {
        return this.getList(path, BooleanArrayList::new);
    }

    /**
     * Gets a list of booleans at the given path,
     * or returns {@code defaultValue} if the path is missing or not an array.
     *
     * @param path         the dot-separated path to look up
     * @param defaultValue the default list to return if not found
     * @return the list of booleans, or {@code defaultValue} if not present
     * @since 1.0.0
     */
    @Override
    public @NotNull BooleanList getBoolList(@NotNull String path, @NotNull BooleanList defaultValue) {
        final BooleanList value = this.getBoolList(path);
        return value != null ? value : defaultValue;
    }

    /**
     * Gets a list of booleans at the given path,
     * or supplies a default using the given {@link Supplier} if the path is missing or not an array.
     *
     * @param path         the dot-separated path to look up
     * @param defaultValue the supplier of the default list
     * @return the list of booleans, or the supplied list if not present
     * @since 1.0.0
     */
    @Override
    public @NotNull BooleanList getBoolList(@NotNull String path, @NotNull Supplier<@NotNull BooleanList> defaultValue) {
        final BooleanList value = this.getBoolList(path);
        return value != null ? value : defaultValue.get();
    }

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
    @Override
    public @Nullable LocalDate getLocalDate(@NotNull String path) {
        return this.get(path, LocalDate.class);
    }

    /**
     * Gets a LocalDate value at the given path,
     * or returns {@code defaultValue} if the path is missing or not a date.
     *
     * @param path         the dot-separated path to look up
     * @param defaultValue the default value to return if not found
     * @return the LocalDate value, or {@code defaultValue} if not present
     * @since 1.0.0
     */
    @Override
    public @NotNull LocalDate getLocalDate(@NotNull String path, @NotNull LocalDate defaultValue) {
        final LocalDate value = this.getLocalDate(path);
        return value != null ? value : defaultValue;
    }

    /**
     * Gets a LocalDate value at the given path,
     * or supplies a default using the given {@link Supplier} if the path is missing or not a date.
     *
     * @param path         the dot-separated path to look up
     * @param defaultValue the supplier of the default value
     * @return the LocalDate value, or the supplied value if not present
     * @since 1.0.0
     */
    @Override
    public @NotNull LocalDate getLocalDate(@NotNull String path, @NotNull Supplier<@NotNull LocalDate> defaultValue) {
        final LocalDate value = this.getLocalDate(path);
        return value != null ? value : defaultValue.get();
    }

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
    @Override
    public @Nullable List<LocalDate> getLocalDateList(@NotNull String path) {
        return this.getList(path, ArrayList::new);
    }

    /**
     * Gets a list of LocalDate values at the given path,
     * or returns {@code defaultValue} if the path is missing or not an array.
     *
     * @param path         the dot-separated path to look up
     * @param defaultValue the default list to return if not found
     * @return the list of LocalDate values, or {@code defaultValue} if not present
     * @since 1.0.0
     */
    @Override
    public @NotNull List<LocalDate> getLocalDateList(@NotNull String path, @NotNull List<LocalDate> defaultValue) {
        final List<LocalDate> value = this.getLocalDateList(path);
        return value != null ? value : defaultValue;
    }

    /**
     * Gets a list of LocalDate values at the given path,
     * or supplies a default using the given {@link Supplier} if the path is missing or not an array.
     *
     * @param path         the dot-separated path to look up
     * @param defaultValue the supplier of the default list
     * @return the list of LocalDate values, or the supplied list if not present
     * @since 1.0.0
     */
    @Override
    public @NotNull List<LocalDate> getLocalDateList(@NotNull String path, @NotNull Supplier<@NotNull List<LocalDate>> defaultValue) {
        final List<LocalDate> value = this.getLocalDateList(path);
        return value != null ? value : defaultValue.get();
    }

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
    @Override
    public @Nullable LocalTime getLocalTime(@NotNull String path) {
        return this.get(path, LocalTime.class);
    }

    /**
     * Gets a LocalTime value at the given path,
     * or returns {@code defaultValue} if the path is missing or not a time.
     *
     * @param path         the dot-separated path to look up
     * @param defaultValue the default value to return if not found
     * @return the LocalTime value, or {@code defaultValue} if not present
     * @since 1.0.0
     */
    @Override
    public @NotNull LocalTime getLocalTime(@NotNull String path, @NotNull LocalTime defaultValue) {
        final LocalTime value = this.getLocalTime(path);
        return value != null ? value : defaultValue;
    }

    /**
     * Gets a LocalTime value at the given path,
     * or supplies a default using the given {@link Supplier} if the path is missing or not a time.
     *
     * @param path         the dot-separated path to look up
     * @param defaultValue the supplier of the default value
     * @return the LocalTime value, or the supplied value if not present
     * @since 1.0.0
     */
    @Override
    public @NotNull LocalTime getLocalTime(@NotNull String path, @NotNull Supplier<@NotNull LocalTime> defaultValue) {
        final LocalTime value = this.getLocalTime(path);
        return value != null ? value : defaultValue.get();
    }

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
    @Override
    public @Nullable List<LocalTime> getLocalTimeList(@NotNull String path) {
        return this.getList(path, ArrayList::new);
    }

    /**
     * Gets a list of LocalTime values at the given path,
     * or returns {@code defaultValue} if the path is missing or not an array.
     *
     * @param path         the dot-separated path to look up
     * @param defaultValue the default list to return if not found
     * @return the list of LocalTime values, or {@code defaultValue} if not present
     * @since 1.0.0
     */
    @Override
    public @NotNull List<LocalTime> getLocalTimeList(@NotNull String path, @NotNull List<LocalTime> defaultValue) {
        final List<LocalTime> value = this.getLocalTimeList(path);
        return value != null ? value : defaultValue;
    }

    /**
     * Gets a list of LocalTime values at the given path,
     * or supplies a default using the given {@link Supplier} if the path is missing or not an array.
     *
     * @param path         the dot-separated path to look up
     * @param defaultValue the supplier of the default list
     * @return the list of LocalTime values, or the supplied list if not present
     * @since 1.0.0
     */
    @Override
    public @NotNull List<LocalTime> getLocalTimeList(@NotNull String path, @NotNull Supplier<@NotNull List<LocalTime>> defaultValue) {
        final List<LocalTime> value = this.getLocalTimeList(path);
        return value != null ? value : defaultValue.get();
    }

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
    @Override
    public @Nullable LocalDateTime getLocalDateTime(@NotNull String path) {
        return this.get(path, LocalDateTime.class);
    }

    /**
     * Gets a LocalDateTime value at the given path,
     * or returns {@code defaultValue} if the path is missing or not a datetime.
     *
     * @param path         the dot-separated path to look up
     * @param defaultValue the default value to return if not found
     * @return the LocalDateTime value, or {@code defaultValue} if not present
     * @since 1.0.0
     */
    @Override
    public @NotNull LocalDateTime getLocalDateTime(@NotNull String path, @NotNull LocalDateTime defaultValue) {
        final LocalDateTime value = this.getLocalDateTime(path);
        return value != null ? value : defaultValue;
    }

    /**
     * Gets a LocalDateTime value at the given path,
     * or supplies a default using the given {@link Supplier} if the path is missing or not a datetime.
     *
     * @param path         the dot-separated path to look up
     * @param defaultValue the supplier of the default value
     * @return the LocalDateTime value, or the supplied value if not present
     * @since 1.0.0
     */
    @Override
    public @NotNull LocalDateTime getLocalDateTime(@NotNull String path, @NotNull Supplier<@NotNull LocalDateTime> defaultValue) {
        final LocalDateTime value = this.getLocalDateTime(path);
        return value != null ? value : defaultValue.get();
    }

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
    @Override
    public @Nullable List<LocalDateTime> getLocalDateTimeList(@NotNull String path) {
        return this.getList(path, ArrayList::new);
    }

    /**
     * Gets a list of LocalDateTime values at the given path,
     * or returns {@code defaultValue} if the path is missing or not an array.
     *
     * @param path         the dot-separated path to look up
     * @param defaultValue the default list to return if not found
     * @return the list of LocalDateTime values, or {@code defaultValue} if not present
     * @since 1.0.0
     */
    @Override
    public @NotNull List<LocalDateTime> getLocalDateTimeList(@NotNull String path, @NotNull List<LocalDateTime> defaultValue) {
        final List<LocalDateTime> value = this.getLocalDateTimeList(path);
        return value != null ? value : defaultValue;
    }

    /**
     * Gets a list of LocalDateTime values at the given path,
     * or supplies a default using the given {@link Supplier} if the path is missing or not an array.
     *
     * @param path         the dot-separated path to look up
     * @param defaultValue the supplier of the default list
     * @return the list of LocalDateTime values, or the supplied list if not present
     * @since 1.0.0
     */
    @Override
    public @NotNull List<LocalDateTime> getLocalDateTimeList(@NotNull String path, @NotNull Supplier<@NotNull List<LocalDateTime>> defaultValue) {
        final List<LocalDateTime> value = this.getLocalDateTimeList(path);
        return value != null ? value : defaultValue.get();
    }

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
    @Override
    public @Nullable OffsetDateTime getOffsetDateTime(@NotNull String path) {
        return this.get(path, OffsetDateTime.class);
    }

    /**
     * Gets an OffsetDateTime value at the given path,
     * or returns {@code defaultValue} if the path is missing or not an offset datetime.
     *
     * @param path         the dot-separated path to look up
     * @param defaultValue the default value to return if not found
     * @return the OffsetDateTime value, or {@code defaultValue} if not present
     * @since 1.0.0
     */
    @Override
    public @NotNull OffsetDateTime getOffsetDateTime(@NotNull String path, @NotNull OffsetDateTime defaultValue) {
        final OffsetDateTime value = this.getOffsetDateTime(path);
        return value != null ? value : defaultValue;
    }

    /**
     * Gets an OffsetDateTime value at the given path,
     * or supplies a default using the given {@link Supplier} if the path is missing or not an offset datetime.
     *
     * @param path         the dot-separated path to look up
     * @param defaultValue the supplier of the default value
     * @return the OffsetDateTime value, or the supplied value if not present
     * @since 1.0.0
     */
    @Override
    public @NotNull OffsetDateTime getOffsetDateTime(@NotNull String path, @NotNull Supplier<@NotNull OffsetDateTime> defaultValue) {
        final OffsetDateTime value = this.getOffsetDateTime(path);
        return value != null ? value : defaultValue.get();
    }

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
    @Override
    public @Nullable List<OffsetDateTime> getOffsetDateTimeList(@NotNull String path) {
        return this.getList(path, ArrayList::new);
    }

    /**
     * Gets a list of OffsetDateTime values at the given path,
     * or returns {@code defaultValue} if the path is missing or not an array.
     *
     * @param path         the dot-separated path to look up
     * @param defaultValue the default list to return if not found
     * @return the list of OffsetDateTime values, or {@code defaultValue} if not present
     * @since 1.0.0
     */
    @Override
    public @NotNull List<OffsetDateTime> getOffsetDateTimeList(@NotNull String path, @NotNull List<OffsetDateTime> defaultValue) {
        final List<OffsetDateTime> value = this.getOffsetDateTimeList(path);
        return value != null ? value : defaultValue;
    }

    /**
     * Gets a list of OffsetDateTime values at the given path,
     * or supplies a default using the given {@link Supplier} if the path is missing or not an array.
     *
     * @param path         the dot-separated path to look up
     * @param defaultValue the supplier of the default list
     * @return the list of OffsetDateTime values, or the supplied list if not present
     * @since 1.0.0
     */
    @Override
    public @NotNull List<OffsetDateTime> getOffsetDateTimeList(@NotNull String path, @NotNull Supplier<@NotNull List<OffsetDateTime>> defaultValue) {
        final List<OffsetDateTime> value = this.getOffsetDateTimeList(path);
        return value != null ? value : defaultValue.get();
    }

    // List<TomlDocument>

    /**
     * Gets a list of sections at the specified path.
     *
     * @param path the dot-separated path to look up
     * @return the List at the path, or {@code null} if not found
     * @since 1.0.0
     */
    @Override
    public @Nullable List<TomlDocument> getSectionList(@NotNull String path) {
        final Node node = super.get(path);
        if (node == null) return null;
        if (!(node instanceof ArrayNode arrayNode)) {
            throw new TomlTypeMismatchException("Table", node.getClass().getSimpleName(), path, node.line(), node.column(), path);
        }
        final List<Node> raw = arrayNode.value();
        final List<TomlDocument> result = new ArrayList<>(raw.size());
        for (int i = 0, len = raw.size(); i < len; i++) {
            result.add((DocumentNode) raw.get(i));
        }
        return result;
    }

    // List<Object> (mixed collection)

    /**
     * Gets a mixed list at the specified path.
     *
     * @param path the dot-separated path to look up
     * @return the List at the path, or {@code null} if not found
     * @since 1.0.0
     */
    @Override
    public @Nullable List<Object> getList(@NotNull String path) {
        return this.getList(path, ArrayList::new);
    }

    // Low-level methods

    /**
     * Gets a node at the specified path.
     *
     * @param path the dot-separated path to the node
     * @return the node at the path, or {@code null} if not found
     * @since 1.0.0
     */
    @Override
    public @Nullable Node get(@NotNull String path) {
        return super.get(path);
    }

    /**
     * Gets a value at the specified path.
     *
     * @param path the dot-separated path to the node
     * @return the value at the path, or {@code null} if not found
     * @since 1.0.0
     */
    @Override
    public @Nullable Object raw(@NotNull String path) {
        final Node node = super.get(path);
        if (node == null) return null;
        if (node instanceof TableNode) {
            return ((TableNode) node).raw();
        } else if (node instanceof ArrayNode) {
            return ((ArrayNode) node).raw();
        } else {
            return node.value();
        }
    }

    /**
     * Checks if a path exists in the document.
     *
     * @param path the dot-separated path to check
     * @return {@code true} if the path exists, {@code false} otherwise
     * @since 1.0.0
     */
    @Override
    public boolean hasPath(@NotNull String path) {
        return super.hasPath(path);
    }

    /**
     * Converts the entire document to a raw Java object map.
     *
     * @return a map with plain Java objects as values
     * @since 1.0.0
     */
    @Override
    public @NotNull Map<String, Object> raw() {
        return super.raw();
    }

    /**
     * Gets a TableNode at the specified path.
     *
     * @param path the dot-separated path
     * @return the TableNode wrapped in a DocumentNode, or {@code null} if not found or not a table
     * @since 1.0.0
     */
    @Override
    public @Nullable DocumentNode getSection(@NotNull String path) {
        final Node node = super.get(path);
        if (node == null) return null;
        if (!(node instanceof DocumentNode documentNode)) {
            throw new TomlTypeMismatchException("Table", node.value().getClass().getSimpleName(), path, node.line(), node.column(), path);
        }
        return documentNode;
    }

    /**
     * Gets the name of the current section.
     *
     * @return the name of the section, or {@code null} if not applicable
     * @since 1.0.0
     */
    @Override
    public @Nullable String getName() {
        return this.name;
    }

    /**
     * Gets the parent section of the current section.
     *
     * @return the parent section, or {@code null} if there is no parent
     * @since 1.0.0
     */
    @Override
    public @Nullable DocumentNode parent() {
        return this.parent;
    }

    /**
     * Sets a value at the specified path.
     *
     * @param path  the dot-separated path to set the value
     * @param value the node value to set
     * @since 1.0.0
     */
    @Override
    public void set(@NotNull String path, @NotNull Node value) {
        super.set(path, value);
    }

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
    public void save(@NotNull Path path, @NotNull WriterOptions options) throws TomlSaveException {
        TomlWriter.write(this.value(), path, options);
    }

    // Helper Methods

    private <T> T get(String path, Class<T> clazz) {
        final Node node = super.get(path);
        if (node == null) return null;
        if (!clazz.isAssignableFrom(node.value().getClass())) {
            throw new TomlTypeMismatchException(clazz.getSimpleName(), node.value().getClass().getSimpleName(), path, node.line(), node.column(), path);
        }
        return clazz.cast(node.value());
    }

    @SuppressWarnings("unchecked")
    private <L extends List<T>, T> L getList(String path, IntFunction<L> listSupplier) {
        final Node node = super.get(path);
        if (node == null) return null;
        if (!(node instanceof ArrayNode arrayNode)) {
            throw new TomlTypeMismatchException("Array", node.value().getClass().getSimpleName(), path, node.line(), node.column(), path);
        }
        final List<Node> raw = arrayNode.value();
        final L result = listSupplier.apply(raw.size());
        for (int i = 0, len = raw.size(); i < len; i++) {
            final Node element = raw.get(i);
            final Object value = element.value();
            result.add((T) value);
        }
        return result;
    }
}
