package com.github.groundbreakingmc.tomly;

import com.github.groundbreakingmc.fission.Fission;
import com.github.groundbreakingmc.fission.exceptions.FileReadException;
import com.github.groundbreakingmc.fission.source.CharSource;
import com.github.groundbreakingmc.tomly.contexts.ParsingContext;
import com.github.groundbreakingmc.tomly.dispatchers.ValueDispatcher;
import com.github.groundbreakingmc.tomly.exceptions.TomlParsingException;
import com.github.groundbreakingmc.tomly.nodes.TomlDocument;
import com.github.groundbreakingmc.tomly.nodes.impl.DocumentNode;
import com.github.groundbreakingmc.tomly.options.PreserveOptions;
import com.github.groundbreakingmc.tomly.parsers.impl.*;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

/**
 * Main entry point for parsing TOML (Tom's Obvious, Minimal Language) content.
 * <p>
 * This utility class provides convenient methods for parsing TOML from various sources
 * including strings and files. The parser supports the full TOML v1.0.0 specification
 * including all data types, nested tables, arrays of tables, and comment preservation.
 * <p>
 * <h3>Supported TOML Features:</h3>
 * <ul>
 *   <li><strong>Basic data types:</strong> strings, integers, floats, booleans</li>
 *   <li><strong>String variants:</strong> basic strings, literal strings, multi-line strings</li>
 *   <li><strong>Date/time types:</strong> local dates, local times, local date-times, offset date-times</li>
 *   <li><strong>Collections:</strong> arrays (including nested) and inline tables</li>
 *   <li><strong>Tables:</strong> standard tables ([table]) and arrays of tables ([[array]])</li>
 *   <li><strong>Comments:</strong> header comments and inline comments (when debug mode enabled)</li>
 * </ul>
 * <p>
 * <h3>Debug Mode:</h3>
 * <p>When debug mode is enabled ({@code debug = true}), the parser:
 * <ul>
 *   <li>Preserves all comments (header and inline) in the parsed nodes</li>
 *   <li>Maintains line and column information for error reporting</li>
 *   <li>Provides detailed parsing context in exception messages</li>
 *   <li>Uses more memory but offers better debugging capabilities</li>
 * </ul>
 * <p>
 * <h3>Usage Examples:</h3>
 * <pre>{@code
 * // Basic parsing from string
 * String toml = """
 *     # Application configuration
 *     name = "MyApp"
 *     version = "1.0.0"
 *     debug = true
 *
 *     [database]
 *     host = "localhost"
 *     port = 5432
 *     """;
 * DocumentNode doc = Tomly.parse(toml, false);
 *
 * // Access values
 * String name = (String) doc.get("name").value();           // "MyApp"
 * Boolean debug = (Boolean) doc.get("debug").value();       // true
 * TableNode db = (TableNode) doc.get("database");
 * String host = (String) db.get("host").value();            // "localhost"
 *
 * // Parse from file with comment preservation
 * DocumentNode config = Tomly.parse(Path.of("config.toml"), true);
 * Node nameNode = config.get("name");
 * List<String> comments = nameNode.headerComments();        // Get header comments
 * String inlineComment = nameNode.inlineComment();          // Get inline comment
 * }</pre>
 *
 * <h3>Error Handling:</h3>
 * <p>All parsing methods throw {@link TomlParsingException} for syntax errors,
 * which includes detailed information about the error location and context.
 *
 * @author GroundbreakingMC
 * @version 1.0.0
 * @see DocumentNode
 * @see TomlParsingException
 * @since 1.0.0
 */
public final class Tomly {

    private Tomly() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Parses a TOML string into a structured document representation.
     * <p>
     * This method converts a TOML-formatted string into a {@link DocumentNode} that
     * provides structured access to the parsed data. The resulting document maintains
     * the hierarchical structure of tables and arrays as defined in the TOML specification.
     *
     * <p><strong>Performance considerations:</strong>
     * <ul>
     *   <li>Debug mode ({@code debug = true}) uses more memory and is slower</li>
     *   <li>Production code should typically use {@code debug = false}</li>
     *   <li>Large TOML documents benefit from streaming parsing via file methods</li>
     * </ul>
     *
     * @param toml    the TOML content as a string; must be valid TOML syntax according to v1.0.0 specification
     * @param debug   {@code true} to preserve comments and enable detailed error reporting,
     *                {@code false} for faster parsing without comment preservation
     * @param options configuration for preserving formatting details such as
     *                blank lines and comments; controls whether these elements
     *                are retained in the parsed document for round-trip
     *                serialization without losing original layout
     * @return a {@link DocumentNode} representing the parsed TOML structure, never {@code null}
     * @throws TomlParsingException if the TOML string contains syntax errors, invalid escape sequences,
     *                              duplicate keys, type conflicts, or other TOML specification violations
     * @throws NullPointerException if {@code toml} is {@code null}
     * @see #parse(Path, boolean, PreserveOptions)
     * @see TomlDocument#getStr(String)
     * @see TomlDocument#getInt(String)
     * @see TomlDocument#getLong(String)
     * @see TomlDocument#getDbl(String)
     * @see TomlDocument#getBool(String)
     * @since 1.0.0
     */
    public static @NotNull TomlDocument parse(@NotNull String toml, boolean debug, @NotNull PreserveOptions options) {
        final CharSource source = Fission.chars(toml);
        return parse(source, debug, options);
    }

    /**
     * Parses a TOML file into a structured document representation.
     * <p>
     * This method reads and parses a TOML file from the specified path. The file
     * is read using UTF-8 encoding and parsed according to the TOML v1.0.0 specification.
     * This method is preferred for large TOML files as it provides better memory efficiency
     * through streaming parsing.
     *
     * <p><strong>File handling:</strong>
     * <ul>
     *   <li>Files are read using UTF-8 encoding</li>
     *   <li>File access is read-only and does not lock the file</li>
     *   <li>Symbolic links are followed</li>
     *   <li>Relative paths are resolved against the current working directory</li>
     * </ul>
     *
     * @param path    the path to the TOML file to parse; must point to a readable file with valid TOML content
     * @param debug   {@code true} to preserve comments and enable detailed error reporting,
     *                {@code false} for faster parsing without comment preservation
     * @param options configuration for preserving formatting details such as
     *                blank lines and comments; controls whether these elements
     *                are retained in the parsed document for round-trip
     *                serialization without losing original layout
     * @return a {@link DocumentNode} representing the parsed TOML structure, never {@code null}
     * @throws TomlParsingException if the file contains invalid TOML syntax, invalid escape sequences,
     *                              duplicate keys, type conflicts, or other TOML specification violations
     * @throws FileReadException    if the file cannot be read, does not exist, or access is denied
     * @throws NullPointerException if {@code path} is {@code null}
     * @see #parse(String, boolean, PreserveOptions)
     * @see TomlDocument#getStr(String)
     * @see TomlDocument#getInt(String)
     * @see TomlDocument#getLong(String)
     * @see TomlDocument#getDbl(String)
     * @see TomlDocument#getBool(String)
     * @since 1.0.0
     */
    public static @NotNull TomlDocument parse(@NotNull Path path, boolean debug, @NotNull PreserveOptions options) {
        final CharSource source = Fission.chars(path);
        return parse(source, debug, options);
    }

    /**
     * Internal parsing implementation that handles the actual TOML parsing logic.
     * <p>
     * This method sets up the parsing context with rollback support and configures
     * all necessary parsers for different TOML value types through a {@link ValueDispatcher}.
     * The parsing order is optimized for performance and correctness.
     *
     * <p><strong>Registered Parsers (in order of evaluation):</strong>
     * <ol>
     *   <li><strong>StringParser:</strong> handles basic, literal, and multi-line strings</li>
     *   <li><strong>BooleanParser:</strong> parses {@code true} and {@code false} literals</li>
     *   <li><strong>ArrayParser:</strong> handles arrays including nested structures</li>
     *   <li><strong>DatetimeParser:</strong> parses all TOML date/time formats</li>
     *   <li><strong>NumberParser:</strong> handles integers, floats, and special values</li>
     *   <li><strong>InlineTableParser:</strong> parses inline table syntax {@code { key = value }}</li>
     * </ol>
     *
     * <p><strong>Parser context features:</strong>
     * <ul>
     *   <li>Line and column tracking for precise error reporting</li>
     *   <li>Comment collection and preservation (debug mode)</li>
     *   <li>State rollback support for complex parsing scenarios</li>
     *   <li>Whitespace normalization and handling</li>
     * </ul>
     *
     * @param source  the character source containing TOML content, provided by Fission library
     * @param debug   whether to enable debug mode for comment preservation and detailed error context
     * @param options configuration for preserving formatting details such as
     *                blank lines and comments; controls whether these elements
     *                are retained in the parsed document for round-trip
     *                serialization without losing original layout
     * @return a {@link DocumentNode} representing the parsed TOML structure
     * @throws TomlParsingException if the source contains invalid TOML syntax or specification violations
     * @implNote The parser uses a recursive descent approach with backtracking support for ambiguous constructs
     */
    public static @NotNull TomlDocument parse(CharSource source, boolean debug, @NotNull PreserveOptions options) {
        final ParsingContext context = new ParsingContext(source, debug, options);

        final ValueDispatcher dispatcher = new ValueDispatcher();
        dispatcher.register(new StringParser());
        dispatcher.register(new BooleanParser());
        dispatcher.register(new ArrayParser(dispatcher));
        dispatcher.register(new DatetimeParser());
        dispatcher.register(new NumberParser());
        dispatcher.register(new InlineTableParser(dispatcher));

        return new TomlDocumentParser(dispatcher).parse(context);
    }
}
