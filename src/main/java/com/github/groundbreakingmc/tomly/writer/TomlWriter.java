package com.github.groundbreakingmc.tomly.writer;

import com.github.groundbreakingmc.tomly.exceptions.TomlSaveException;
import com.github.groundbreakingmc.tomly.nodes.Node;
import com.github.groundbreakingmc.tomly.nodes.impl.ArrayNode;
import com.github.groundbreakingmc.tomly.nodes.impl.TableNode;
import com.github.groundbreakingmc.tomly.options.WriterOptions;
import org.jetbrains.annotations.NotNull;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Utility class for serializing TOML document structures to files.
 * <p>
 * The {@code TomlWriter} class provides functionality to convert parsed TOML node structures
 * back to proper TOML format and write them to files. It handles all TOML data types, preserves
 * comments when available, and ensures proper formatting according to TOML specification.
 * <p>
 * This class is designed to work with the node structures created by the TOML parser and
 * supports round-trip serialization, meaning a document can be parsed and then written back
 * while preserving its structure and semantics (root keys will always be before tables, as per TOML 1.0.0).
 * <p>
 * <h3>Features:</h3>
 * <ul>
 *   <li>Complete TOML 1.0.0 specification compliance</li>
 *   <li>Proper handling of all data types (strings, numbers, booleans, dates, arrays, tables)</li>
 *   <li>Comment preservation (header comments and inline comments)</li>
 *   <li>Automatic string escaping and formatting</li>
 *   <li>Nested table structure handling</li>
 *   <li>Array of tables support</li>
 *   <li>Inline table formatting for simple structures</li>
 * </ul>
 *
 * <h3>Usage Examples:</h3>
 * <pre>{@code
 * // Basic usage
 * Map<String, Node> nodeMap = documentNode.value();
 * TomlWriter.write(nodeMap, Path.of("output.toml"));
 *
 * // Using Writer for custom output
 * try (FileWriter writer = new FileWriter("config.toml", StandardCharsets.UTF_8)) {
 *     TomlWriter.write(nodeMap, writer);
 * }
 * }</pre>
 *
 * @author GroundbreakingMC
 * @version 1.0.0
 * @since 1.0.0
 */
public final class TomlWriter {

    /**
     * Private constructor to prevent instantiation.
     */
    private TomlWriter() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Writes a TOML document structure to the specified file.
     * <p>
     * This method serializes the provided node map to TOML format using UTF-8 encoding
     * and writes it to the given file. If the file doesn't exist, it will be created.
     * If it exists, it will be overwritten.
     *
     * @param nodeMap the map of nodes representing the TOML document structure
     * @param path    the path to write to
     * @throws TomlSaveException if an I/O error occurs during writing
     * @since 1.0.0
     */
    public static void write(@NotNull Map<String, Node> nodeMap, @NotNull Path path, @NotNull WriterOptions options) throws TomlSaveException {
        // Create parent directories if they don't exist
        try {
            Files.createDirectories(path.getParent());
        } catch (final IOException ex) {
            throw new TomlSaveException("Unexpected IOException during creating parent directory file for '" + path + "'", ex);
        }

        try (final FileWriter writer = new FileWriter(path.toFile(), StandardCharsets.UTF_8)) {
            write(nodeMap, writer, options);
        } catch (final IOException ex) {
            throw new TomlSaveException("Unexpected IOException during writing values", ex);
        }
    }

    /**
     * Writes a TOML document structure to the specified Writer.
     * <p>
     * This method provides more control over the output destination, allowing
     * custom Writers to be used for different output targets or encoding options.
     *
     * @param nodeMap the map of nodes representing the TOML document structure
     * @param writer  the Writer to write to
     * @throws TomlSaveException if an I/O error occurs during writing
     * @since 1.0.0
     */
    public static void write(@NotNull Map<String, Node> nodeMap, @NotNull Writer writer, @NotNull WriterOptions options) throws
            TomlSaveException {
        try (final TomlWriterContext context = new TomlWriterContext(writer)) {
            writeDocument(nodeMap, context, options);
        }
    }

    /**
     * Converts a TOML document structure to a TOML-formatted string.
     * <p>
     * This method is useful when you need the TOML content as a string
     * rather than writing it directly to a file.
     *
     * @param nodeMap the map of nodes representing the TOML document structure
     * @return the TOML-formatted string
     * @throws NullPointerException if nodeMap is null
     * @since 1.0.0
     */
    @NotNull
    public static String toString(@NotNull Map<String, Node> nodeMap, @NotNull WriterOptions options) {
        final StringWriter stringWriter = new StringWriter();
        write(nodeMap, stringWriter, options);
        return stringWriter.toString();
    }

    private static void writeDocument(Map<String, Node> nodeMap, TomlWriterContext context, @NotNull WriterOptions options) {
        // It's better to create lists with a reserve than to expand them
        final List<Map.Entry<String, Node>> simpleKeys = new ArrayList<>(nodeMap.size());
        final List<Map.Entry<String, Node>> tables = new ArrayList<>(nodeMap.size());
        final List<Map.Entry<String, Node>> nestedTables = new ArrayList<>(nodeMap.size());

        // First we separate different types.
        // Why? A user could add a new value to the value map,
        // and it turns out that a regular key could be later than the table.
        // This means that - Round-trip is preserved at the content level,
        // but the location of root keys will always be before tables, as per TOML 1.0.0.
        for (final Map.Entry<String, Node> entry : nodeMap.entrySet()) {
            final Node node = entry.getValue();
            if (node instanceof TableNode) {
                tables.add(entry);
            } else if (node instanceof ArrayNode arrayNode
                    && !arrayNode.value().isEmpty()
                    && arrayNode.value().get(0) instanceof TableNode) {
                nestedTables.add(entry);
            } else {
                simpleKeys.add(entry);
            }
        }

        // Write simple key-value pairs first
        for (int i = 0, size = simpleKeys.size(); i < size; i++) {
            final Map.Entry<String, Node> entry = simpleKeys.get(i);
            final String key = entry.getKey();
            final Node node = entry.getValue();
            writeComments(context, node.headerComments(), options);
            context.writeLine(key + " = " + node.saveToString(options)
                    + (options.writeInlineComments() ? node.inlineComment() != null && !node.inlineComment().isEmpty() ? " # " + node.inlineComment() : "" : "")
            );
        }

        // Add separator between simple keys and tables
        context.writeLine("");

        // Write tables
        for (int i = 0, size = tables.size(); i < size; i++) {
            final Map.Entry<String, Node> entry = tables.get(i);
            final String key = entry.getKey();
            final Node node = entry.getValue();
            writeComments(context, node.headerComments(), options);
            context.writeLine("[" + key + "]");
            context.writeLine(node.saveToString(options));
        }

        final WriterOptions nestedTableOptions = WriterOptions.builder()
                .writeBlankLines(options.writeBlankLines())
                .writeHeaderComments(options.writeHeaderComments())
                .writeInlineComments(options.writeInlineComments())
                .maxLineLength(1)
                .build();

        // Write nested tables
        for (int i = 0, size = nestedTables.size(); i < size; i++) {
            final Map.Entry<String, Node> entry = nestedTables.get(i);
            final String key = entry.getKey();
            final ArrayNode arrayNode = (ArrayNode) entry.getValue();
            for (int j = 0, arraySize = arrayNode.value().size(); j < arraySize; j++) {
                final Node nestedTable = arrayNode.value().get(j);
                writeComments(context, nestedTable.headerComments(), options);
                context.writeLine("[[" + key + "]]");
                context.writeLine(nestedTable.saveToString(nestedTableOptions));
            }
        }
    }

    private static void writeComments(TomlWriterContext context, List<String> comments, WriterOptions options) {
        if (!comments.isEmpty() && (options.writeBlankLines() || options.writeHeaderComments())) {
            for (int j = 0, commentsSize = comments.size(); j < commentsSize; j++) {
                final String comment = comments.get(j);
                if ((comment.isEmpty() && options.writeBlankLines())
                        || (!comment.isEmpty() && comment.charAt(0) == '#' && options.writeHeaderComments())) {
                    context.writeLine(comment);
                }
            }
        }
    }

    /**
     * Internal context class for managing writing state.
     */
    private record TomlWriterContext(Writer writer) implements AutoCloseable {

        public void writeLine(String line) throws TomlSaveException {
            try {
                this.writer.write(line);
                this.writer.write('\n');
            } catch (final IOException ex) {
                throw new TomlSaveException("Failed to write values", ex);
            }
        }

        public void flush() {
            try {
                this.writer.flush();
            } catch (final IOException ex) {
                throw new TomlSaveException("Failed to write values", ex);
            }
        }

        @Override
        public void close() {
            this.flush();
        }
    }
}
