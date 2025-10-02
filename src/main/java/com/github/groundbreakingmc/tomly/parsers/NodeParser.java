package com.github.groundbreakingmc.tomly.parsers;

import com.github.groundbreakingmc.tomly.contexts.ParsingContext;
import com.github.groundbreakingmc.tomly.nodes.Node;
import org.jetbrains.annotations.NotNull;

/**
 * Interface for parsers that can read and convert TOML syntax into {@link Node} objects.
 * <p>
 * Implementations of this interface are responsible for recognizing and parsing specific
 * TOML value types (strings, numbers, booleans, arrays, tables, etc.) from the character
 * stream. Each parser specializes in one or more value types and uses a two-phase approach:
 * lookahead checking and actual parsing.
 * <p>
 * <h3>Parser Contract:</h3>
 * <ul>
 *   <li><strong>Lookahead:</strong> {@link #canRead(ParsingContext)} must not consume characters</li>
 *   <li><strong>Parsing:</strong> {@link #read(ParsingContext)} is called only after {@code canRead()} returns true</li>
 *   <li><strong>State:</strong> Parsers should be stateless and reusable across multiple parsing operations</li>
 * </ul>
 *
 * @author GroundbreakingMC
 * @version 1.0.0
 * @see Node
 * @see ParsingContext
 * @since 1.0.0
 */
public interface NodeParser {

    /**
     * Check if this parser can read the value starting from current position.
     * Parser may read ahead to make the determination, but must not consume any characters.
     *
     * @param raw parser context
     * @return true if this parser can handle the value
     */
    boolean canRead(@NotNull ParsingContext raw);

    /**
     * Read and parse the value from current position.
     * This method is(should be) called only after canRead() returned true.
     *
     * @param raw parser context
     * @return parsed node
     */
    @NotNull Node read(@NotNull ParsingContext raw);
}
