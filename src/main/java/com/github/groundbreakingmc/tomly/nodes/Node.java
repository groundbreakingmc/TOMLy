package com.github.groundbreakingmc.tomly.nodes;

import com.github.groundbreakingmc.tomly.nodes.impl.*;
import com.github.groundbreakingmc.tomly.options.WriterOptions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * Represents a parsed node in a TOML document structure.
 * <p>
 * This interface defines the common contract for all TOML value types including
 * primitives (strings, numbers, booleans), collections (arrays, tables), and
 * complex structures (date-times). Each node maintains its position information
 * and associated comments from the original TOML source.
 * <p>
 * <h3>Node Hierarchy:</h3>
 * <p>The TOML parser creates different node implementations based on the parsed value type:
 * <ul>
 *   <li><strong>{@link StringNode}:</strong> basic, literal, and multi-line strings</li>
 *   <li><strong>{@link NumberNode}:</strong> integers and floating-point numbers</li>
 *   <li><strong>{@link BooleanNode}:</strong> true/false values</li>
 *   <li><strong>{@link DatetimeNode}:</strong> local dates, times, date-times, and offset date-times</li>
 *   <li><strong>{@link ArrayNode}:</strong> arrays including nested structures</li>
 *   <li><strong>{@link TableNode}:</strong> inline tables and regular tables</li>
 *   <li><strong>{@link DocumentNode}:</strong> root document (extends TableNode)</li>
 * </ul>
 *
 * <h3>Comment Preservation:</h3>
 * <p>When parsing in debug mode, nodes preserve comments from the original TOML:
 * <ul>
 *   <li><strong>Header comments:</strong> lines starting with '#' that appear before the key-value pair</li>
 *   <li><strong>Inline comments:</strong> comments on the same line after the value</li>
 * </ul>
 *
 * <h3>Usage Examples:</h3>
 * <pre>{@code
 * // Parse TOML with comments
 * String toml = """
 *     # Database configuration
 *     # This is the primary database
 *     host = "localhost"  # default host
 *     port = 5432
 *     """;
 * DocumentNode doc = Tomly.parse(toml, true);
 *
 * // Access node information
 * Node hostNode = doc.get("host");
 * String value = (String) hostNode.value();           // "localhost"
 * List<String> comments = hostNode.headerComments();  // ["Database configuration", "This is the primary database"]
 * String inline = hostNode.inlineComment();           // "default host"
 * int line = hostNode.line();                         // 3 (line number in source)
 * int col = hostNode.column();                        // 0 (column position)
 *
 * // Type-specific access
 * if (hostNode instanceof StringNode stringNode) {
 *     String stringValue = stringNode.value();
 * }
 *
 * // Convert node values back to TOML format
 * Node arrayNode = doc.get("servers");
 * String tomlArray = arrayNode.saveToString(80);  // "["server1", "server2", "server3"]"
 *
 * Node tableNode = doc.get("database");
 * String tomlTable = tableNode.saveToString(60);  // "{host = "localhost", port = 5432}"
 *
 * // Multi-line formatting for long values
 * Node longStringNode = doc.get("description");
 * String formatted = longStringNode.saveToString(40);  // Breaks into multi-line string if needed
 * }</pre>
 * <p>
 * <h3>Thread Safety:</h3>
 * <p>Node implementations are generally immutable after creation and can be safely
 * accessed from multiple threads. However, the underlying collections returned by
 * {@link #value()} may be mutable depending on the node type.
 *
 * @author GroundbreakingMC
 * @version 1.0.0
 * @see com.github.groundbreakingmc.tomly.nodes.impl.DocumentNode
 * @see com.github.groundbreakingmc.tomly.nodes.impl.TableNode
 * @see com.github.groundbreakingmc.tomly.nodes.impl.ArrayNode
 * @since 1.0.0
 */
public interface Node {

    /**
     * Returns the line number where this node appears in the original TOML source.
     * <p>
     * Line numbers start from 1 and represent the position where the key-value pair
     * or table header begins in the source text. This information is primarily useful
     * for error reporting and debugging.
     *
     * <p><strong>Examples:</strong>
     * <pre>{@code
     * # Line 1: comment
     * name = "value"  # Line 2: this node reports line = 2
     *
     * [table]         # Line 4: table header reports line = 4
     * key = 123       # Line 5: this node reports line = 5
     * }</pre>
     *
     * @return the 1-based line number in the source TOML document
     * @since 1.0.0
     */
    int line();

    /**
     * Returns the column number where this node appears in the original TOML source.
     * <p>
     * Column numbers start from 0 and represent the character position where the
     * key name begins (for key-value pairs) or where the table header starts.
     * Tabs count as single characters for column calculation.
     *
     * <p><strong>Examples:</strong>
     * <pre>{@code
     * name = "value"     # column = 0 (key starts at beginning)
     *   indented = 123   # column = 2 (key starts after 2 spaces)
     * [table]            # column = 0 (bracket starts at beginning)
     * }</pre>
     *
     * @return the 0-based column number in the source TOML document
     * @since 1.0.0
     */
    int column();

    /**
     * Returns the header comments that appear before this node in the source TOML.
     * <p>
     * Header comments are comment lines (starting with '#') that appear immediately
     * before a key-value pair or table header, with no blank lines in between.
     *
     * <p><strong>Comment collection rules:</strong>
     * <ul>
     *   <li>Only consecutive comment lines immediately before the node are included</li>
     *   <li>Blank lines break the comment collection sequence</li>
     *   <li>Leading and trailing whitespace is not trimmed from each comment</li>
     *   <li>First '#' character is not included in the comment text</li>
     * </ul>
     *
     * <p><strong>Example:</strong>
     * <pre>{@code
     * # First comment
     * # Second comment
     * key = "value"  # This node gets both header comments
     *
     * # Isolated comment
     *
     * # New comment block
     * other = 123    # This node gets only "New comment block"
     * }</pre>
     *
     * @return an immutable list of header comment strings, empty if no comments
     * @see #inlineComment()
     * @since 1.0.0
     */
    @NotNull
    List<String> headerComments();

    /**
     * Returns the inline comment that appears on the same line as this node's value.
     * <p>
     * Inline comments are comments that appear after the value on the same line,
     * separated by whitespace and starting with '#'.
     *
     * <p><strong>Inline comment rules:</strong>
     * <ul>
     *   <li>Must be on the same line as the value</li>
     *   <li>Must be preceded by at least one space or tab</li>
     *   <li>Leading and trailing whitespace is trimmed</li>
     *   <li>The '#' character is not included in the comment text</li>
     * </ul>
     *
     * <p><strong>Examples:</strong>
     * <pre>{@code
     * name = "value"  # This is an inline comment
     * port = 8080     # Default port number
     * debug = true    # Enable debugging
     *
     * # This is NOT an inline comment for the next line
     * host = "localhost"
     * }</pre>
     *
     * @return the inline comment string with whitespace trimmed, or {@code null}
     * if no inline comment exists
     * @see #headerComments()
     * @since 1.0.0
     */
    @Nullable
    String inlineComment();

    /**
     * Returns the parsed value as a plain Java object.
     * <p>
     * This method provides access to the actual parsed value, converted to
     * appropriate Java types according to the TOML specification. The return
     * type varies based on the TOML value type parsed.
     * <p>
     * <h3>Type Mappings:</h3>
     * <table border="1" cellpadding="5">
     *   <tr><th>TOML Type</th><th>Java Type</th><th>Example TOML</th><th>Java Value</th></tr>
     *   <tr><td>String</td><td>{@link String}</td><td>{@code "hello"}</td><td>{@code "hello"}</td></tr>
     *   <tr><td>Integer</td><td>{@link Long}</td><td>{@code 42}</td><td>{@code 42L}</td></tr>
     *   <tr><td>Float</td><td>{@link Double}</td><td>{@code 3.14}</td><td>{@code 3.14}</td></tr>
     *   <tr><td>Boolean</td><td>{@link Boolean}</td><td>{@code true}</td><td>{@code Boolean.TRUE}</td></tr>
     *   <tr><td>Local Date</td><td>{@link java.time.LocalDate}</td><td>{@code 2025-08-23}</td><td>{@code LocalDate.of(2025, 8, 23)}</td></tr>
     *   <tr><td>Local Time</td><td>{@link java.time.LocalTime}</td><td>{@code 12:34:56}</td><td>{@code LocalTime.of(12, 34, 56)}</td></tr>
     *   <tr><td>Local DateTime</td><td>{@link java.time.LocalDateTime}</td><td>{@code 2025-08-23T12:34:56}</td><td>{@code LocalDateTime.of(2025, 8, 23, 12, 34, 56)}</td></tr>
     *   <tr><td>Offset DateTime</td><td>{@link java.time.OffsetDateTime}</td><td>{@code 2025-08-23T12:34:56+02:00}</td><td>{@code OffsetDateTime.of(...)}</td></tr>
     *   <tr><td>Array</td><td>{@link List}&lt;Node&gt;</td><td>{@code [1, "2", [3]]}</td><td>{@code List.of(NumberNode, StringNode, ArrayNode)}</td></tr>
     *   <tr><td>Table</td><td>{@link Map}&lt;String, Node&gt;</td><td>{@code {x = 1, y = 2}}</td><td>{@code Map.of("x", NumberNode, "y", NumberNode)}</td></tr>
     * </table>
     *
     * <h3>Collection Handling:</h3>
     * <ul>
     *   <li><strong>Arrays:</strong> returned as {@link List} with elements converted recursively</li>
     *   <li><strong>Tables:</strong> returned as {@link Map} with string keys and converted values</li>
     *   <li><strong>Nested structures:</strong> converted recursively maintaining type safety</li>
     * </ul>
     *
     * <h3>Usage Examples:</h3>
     * <pre>{@code
     * // Simple values
     * Node stringNode = doc.get("name");
     * String name = (String) stringNode.value();
     *
     * Node numberNode = doc.get("port");
     * Long port = (Long) numberNode.value();
     *
     * // Collections - returns List<Node> and Map<String, Node>
     * Node arrayNode = doc.get("servers");
     * List<Node> serverNodes = (List<Node>) arrayNode.value();
     *
     * Node tableNode = doc.get("database");
     * Map<String, Node> dbNodes = (Map<String, Node>) tableNode.value();
     *
     * // Raw values - for plain Java objects
     * ArrayNode arrayNode = (ArrayNode) doc.get("numbers");
     * List<Object> numbers = arrayNode.raw();  // [1L, 2L, 3L]
     *
     * TableNode tableNode = (TableNode) doc.get("config");
     * Map<String, Object> config = tableNode.raw();  // {"key": "value"}
     *
     * // Mixed array handling (TOML supports different types in arrays)
     * // TOML: mixed = [1, "text", true, 3.14]
     * List<Node> mixedNodes = (List<Node>) doc.get("mixed").value();
     * Long first = (Long) mixedNodes.get(0).value();        // 1L
     * String second = (String) mixedNodes.get(1).value();   // "text"
     * Boolean third = (Boolean) mixedNodes.get(2).value();  // true
     * Double fourth = (Double) mixedNodes.get(3).value();   // 3.14
     * }</pre>
     *
     * @return the parsed value converted to appropriate Java types
     * for undefined values
     * @see java.time.LocalDate
     * @see java.time.LocalTime
     * @see java.time.LocalDateTime
     * @see java.time.OffsetDateTime
     * @since 1.0.0
     */
    @NotNull
    Object value();

    /**
     * Converts this node's value back to valid TOML format as a string.
     * <p>
     * This method serializes the node's parsed value back to its TOML representation,
     * using the provided writer options to control formatting. The output contains
     * only the value portion without the key name.
     *
     * <p><strong>Formatting is controlled by {@link WriterOptions}:</strong>
     * <ul>
     *   <li><strong>maxLineLength:</strong> determines when to break long values across lines</li>
     *   <li><strong>Comment options:</strong> do not affect this method (comments are handled at document level)</li>
     * </ul>
     *
     * @param options the writer options controlling formatting behavior
     * @return the node's value formatted as valid TOML syntax string
     * @since 1.0.0
     */
    @NotNull
    String saveToString(@NotNull WriterOptions options);
}
