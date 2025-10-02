package com.github.groundbreakingmc.tomly.dispatchers;

import com.github.groundbreakingmc.tomly.contexts.ParsingContext;
import com.github.groundbreakingmc.tomly.exceptions.TomlParsingException;
import com.github.groundbreakingmc.tomly.nodes.Node;
import com.github.groundbreakingmc.tomly.parsers.NodeParser;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Coordinates value parsing by delegating to registered {@link NodeParser} implementations.
 * <p>
 * The {@code ValueDispatcher} acts as a central routing mechanism that attempts to parse
 * TOML values by testing registered parsers in sequence until one successfully recognizes
 * and parses the value. This design allows for extensible parsing where different value
 * types can be handled by specialized parser implementations.
 * <p>
 * <h3>Parser Registration and Ordering:</h3>
 * Parsers are tested in the order they are registered. The order matters for ambiguous
 * syntax where multiple parsers might match the same input. For optimal performance and
 * correctness, parsers should be registered in this order:
 * <ol>
 *   <li><strong>StringParser:</strong> handles quoted strings (basic and literal)</li>
 *   <li><strong>BooleanParser:</strong> recognizes true/false keywords</li>
 *   <li><strong>ArrayParser:</strong> handles array syntax starting with '['</li>
 *   <li><strong>DatetimeParser:</strong> parses ISO 8601 date/time formats</li>
 *   <li><strong>NumberParser:</strong> handles integers, floats, and special numeric values</li>
 *   <li><strong>InlineTableParser:</strong> handles inline table syntax starting with '{'</li>
 * </ol>
 *
 * <h3>Parsing Strategy:</h3>
 * <ul>
 *   <li>Whitespace is automatically skipped before attempting any parser</li>
 *   <li>Each parser's {@link NodeParser#canRead(ParsingContext)} is called without consuming input</li>
 *   <li>First parser that returns {@code true} has its {@link NodeParser#read(ParsingContext)} method called</li>
 *   <li>If no parser can handle the value, a parsing exception is thrown</li>
 * </ul>
 *
 * <h3>Usage Example:</h3>
 * <pre>{@code
 * ValueDispatcher dispatcher = new ValueDispatcher();
 * dispatcher.register(new StringParser());
 * dispatcher.register(new BooleanParser());
 * dispatcher.register(new ArrayParser(dispatcher));
 * dispatcher.register(new DatetimeParser());
 * dispatcher.register(new NumberParser());
 * dispatcher.register(new InlineTableParser(dispatcher));
 *
 * // Later in parsing
 * Node value = dispatcher.readValue(context);
 * }</pre>
 *
 * @author GroundbreakingMC
 * @version 1.0.0
 * @see NodeParser
 * @see ParsingContext
 * @since 1.0.0
 */
public final class ValueDispatcher {

    private final @NotNull List<NodeParser> parsers = new ArrayList<>(8);

    /**
     * Registers a parser to handle specific TOML value types.
     * <p>
     * Parsers are tested in registration order, so register more specific
     * parsers before more general ones to avoid incorrect matches.
     *
     * @param strategy the parser implementation to register
     * @since 1.0.0
     */
    public void register(@NotNull NodeParser strategy) {
        this.parsers.add(strategy);
    }

    /**
     * Attempts to parse a TOML value from the current position.
     * <p>
     * This method automatically skips leading whitespace, then tests each
     * registered parser until one successfully recognizes the value type.
     *
     * @param context the parsing context containing the input stream
     * @return the parsed node
     * @throws TomlParsingException if no registered parser can handle the value
     * @since 1.0.0
     */
    public @NotNull Node readValue(@NotNull ParsingContext context) {
        context.skipWhitespaces();

        // try string first to disambiguate leading quotes
        for (int i = 0; i < this.parsers.size(); i++) {
            final NodeParser parser = this.parsers.get(i);
            if (parser.canRead(context)) {
                return parser.read(context);
            }
        }

        throw context.error("Unexpected value start");
    }
}
