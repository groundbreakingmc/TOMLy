package com.github.groundbreakingmc.tomly.contexts;

import com.github.groundbreakingmc.fission.source.CharSource;
import com.github.groundbreakingmc.tomly.exceptions.TomlParsingException;
import com.github.groundbreakingmc.tomly.options.PreserveOptions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public final class ParsingContext {

    private final @NotNull CharSource source;
    private final boolean debug;
    private final @NotNull PreserveOptions options;
    private final @NotNull StringBuilder currentLineBuffer;
    // comment staging (captured by value parsers)
    private final @NotNull List<String> headerComments;
    private int line = 1;
    private int column = 0;
    private int current = -2; // -2 means uninitialized
    private boolean reachedEof;

    // Rollback support
    private final @NotNull Deque<ParserState> stateStack;

    public ParsingContext(@NotNull CharSource source, boolean debug, @NotNull PreserveOptions options) {
        this.source = source;
        this.debug = debug;
        this.options = options;
        this.currentLineBuffer = new StringBuilder(64);
        this.headerComments = new ArrayList<>(4);
        this.current = this.nextChar();
        this.stateStack = new ArrayDeque<>();
    }

    public int current() {
        return this.current;
    }

    public int peek() {
        return this.source.peek();
    }

    /**
     * Peek ahead multiple characters without advancing the current position.
     *
     * @param count number of characters to peek ahead
     * @return array of peeked characters, -1 for EOF
     */
    public int[] peekAhead(int count) {
        return this.source.peekAhead(count);
    }

    public int advance() {
        this.current = this.nextChar();
        if (this.current == -1) {
            this.reachedEof = true;
        }
        return this.current;
    }

    public boolean eof() {
        return this.reachedEof;
    }

    public boolean lineEnd() {
        return this.current == '\n' || this.current == '\r';
    }

    /**
     * Save current parser state to allow rollback later
     */
    public void saveState() {
        // Mark the source at current position
        this.source.mark();

        final ParserState state = new ParserState(
                this.line,
                this.column,
                this.current,
                this.reachedEof,
                new ArrayList<>(this.headerComments),
                this.debug ? this.currentLineBuffer.toString() : null
        );

        this.stateStack.push(state);
    }

    /**
     * Restore parser to the last saved state and remove that save point
     */
    public void rollback() {
        if (this.stateStack.isEmpty()) {
            throw new IllegalStateException("No saved state to rollback to");
        }

        final ParserState state = this.stateStack.pop();

        // Reset source to saved position
        this.source.reset();
        this.source.commit();

        // Restore parser state
        this.line = state.line;
        this.column = state.column;
        this.current = state.current;
        this.reachedEof = state.reachedEof;

        this.headerComments.clear();
        this.headerComments.addAll(state.headerComments);

        if (this.debug) {
            this.currentLineBuffer.setLength(0);
            if (state.debug() != null) {
                this.currentLineBuffer.append(state.debug());
            }
        }
    }

    /**
     * Remove the last saved state without rolling back
     */
    public void commitState() {
        if (this.stateStack.isEmpty()) {
            throw new IllegalStateException("No saved state to commit");
        }
        this.source.commit();
        this.stateStack.pop();
    }

    private int nextChar() {
        if (!this.source.hasNext()) {
            return -1;
        }

        final int ch = this.source.read();

        if (ch == '\n') {
            ++this.line;
            this.column = 0;
            if (this.debug) {
                this.currentLineBuffer.setLength(0);
            }
        } else if (!this.reachedEof) {
            ++this.column;
            if (this.debug) {
                this.currentLineBuffer.append((char) ch);
            }
        }

        return ch;
    }

    public TomlParsingException error(@NotNull String message) {
        final String context = this.currentLineBuffer.toString();
        return new TomlParsingException(message, this.line, this.column, context);
    }

    public void skipWhitespaces() {
        while (this.current == ' ' || this.current == '\t') {
            this.advance();
        }
    }

    public void skipWhitespaceAndCollectHeaderComments() {
        while (true) {
            // skip spaces/tabs
            this.skipWhitespaces();
            // comment line
            if (this.current == '#') {
                if (this.options.preserveHeaderComments()) {
                    final String comment = this.readUntilLineEnd();
                    this.headerComments.add(comment);
                    // consume line end (already at end, nextChar handles line counters)
                    if (!this.reachedEof) {
                        this.advance();
                    }
                } else skipLine();
                continue;
            }
            // blank lines
            if (this.current == '\n' || this.current == '\r') {
                if (this.options.preserveBlankLines()) {
                    this.headerComments.add("");
                } else {
                    this.advance();
                    this.headerComments.clear();
                }
                continue;
            }
            break;
        }
    }

    public @NotNull List<String> takeHeaderComments() {
        final List<String> out = List.copyOf(this.headerComments);
        this.headerComments.clear();
        return out;
    }

    public @Nullable String readInlineCommentIfAny() {
        this.skipWhitespaces();
        if (this.current == '#') {
            this.advance(); // consume '#'
            return this.readUntilLineEnd().trim();
        } else if (this.current != '\n' && this.current != ',' && this.current != ']' && this.current != '}' && !this.reachedEof) {
            throw error("Unexpected character");
        }
        // move to next line or keep as-is; callers decide further skipping
        return null;
    }

    private @NotNull String readUntilLineEnd() {
        final StringBuilder comment = new StringBuilder(32);
        while (!this.reachedEof && this.current != '\n' && this.current != '\r') {
            comment.append((char) this.current);
            this.advance();
        }
        return comment.toString();
    }

    private void skipLine() {
        if (!this.reachedEof) {
            ++this.line;
            this.column = 0;
        }
        while (this.source.hasNext() && this.current != '\n' && this.current != '\r') {
            this.current = this.source.read();
        }
        this.reachedEof = this.current == -1;
    }

    public int getLine() {
        return this.line;
    }

    public int getColumn() {
        return this.column;
    }

    private record ParserState(
            int line,
            int column,
            int current,
            boolean reachedEof,
            ArrayList<String> headerComments,
            String debug
    ) {
    }
}