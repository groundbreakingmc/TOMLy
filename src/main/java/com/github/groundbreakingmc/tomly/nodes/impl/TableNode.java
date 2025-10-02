package com.github.groundbreakingmc.tomly.nodes.impl;

import com.github.groundbreakingmc.tomly.nodes.Node;
import com.github.groundbreakingmc.tomly.options.WriterOptions;
import it.unimi.dsi.fastutil.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public sealed class TableNode extends AbstractNode permits DocumentNode {

    private final @NotNull String name;
    private final @NotNull Map<String, Node> entries;

    protected TableNode(@NotNull String name, @NotNull TableNode tableNode) {
        this(name, tableNode.entries, tableNode.line(), tableNode.column(), tableNode.headerComments(), tableNode.inlineComment());
    }

    public TableNode(@NotNull String name, @NotNull Map<String, Node> entries, @Nullable List<String> headerComments, @Nullable String inlineComment) {
        this(name, entries, -1, -1, headerComments, inlineComment);
    }

    public TableNode(@NotNull String name, @NotNull Map<String, Node> entries,
                     int line, int column,
                     @Nullable List<String> headerComments, @Nullable String inlineComment) {
        super(line, column, headerComments, inlineComment);
        this.name = name;
        this.entries = new LinkedHashMap<>(entries);
    }

    @Override
    public @NotNull Map<String, Node> value() {
        return this.entries;
    }

    public @NotNull Map<String, Object> raw() {
        final Map<String, Object> result = new HashMap<>(this.entries.size());
        for (final Map.Entry<String, Node> entry : this.entries.entrySet()) {
            final Node value = entry.getValue();
            if (value instanceof TableNode tableNode) {
                result.put(entry.getKey(), tableNode.raw());
            } else if (value instanceof ArrayNode arrayNode) {
                result.put(entry.getKey(), arrayNode.raw());
            } else {
                result.put(entry.getKey(), value.value());
            }
        }
        return result;
    }

    @Override
    public @NotNull String saveToString(@NotNull WriterOptions options) {
        if (options.maxLineLength() <= 0) {
            throw new IllegalArgumentException("maxLineLength must be positive");
        }

        if (this.entries.isEmpty()) {
            return "{}";
        }

        final WriterOptions elementOptions = WriterOptions.builder()
                .writeBlankLines(options.writeBlankLines())
                .writeHeaderComments(options.writeHeaderComments())
                .writeInlineComments(options.writeInlineComments())
                .maxLineLength(Integer.MAX_VALUE)
                .build();

        final List<Pair<Node, String>> formattedEntries = new ArrayList<>(this.entries.size());
        int totalLength = 2;
        boolean hasComments = false;
        for (Map.Entry<String, Node> entry : this.entries.entrySet()) {
            final String key = escapeKey(entry.getKey());
            final Node value = entry.getValue();
            final String formatted = value.saveToString(elementOptions);
            final String result;
            if (formatted.charAt(0) != '{' && value instanceof TableNode tableNode) {
                result = "[" + tableNode.name + "]" + '\n' + formatted;
            } else {
                result = key + " = " + formatted;
            }
            formattedEntries.add(Pair.of(value, result));
            totalLength += result.length() + 2;
            if (!hasComments) {
                hasComments = !value.headerComments().isEmpty()
                        || (value.inlineComment() != null && !value.inlineComment().isEmpty());
            }
        }

        // checking if table is not root
        boolean multiLine = hasComments || this.name.isEmpty() || totalLength > options.maxLineLength();

        final StringBuilder result = new StringBuilder();

        if (!multiLine) {
            // Inline format: {key1 = value1, key2 = value2}
            result.append('{');
            for (int i = 0; i < formattedEntries.size(); i++) {
                if (i > 0) result.append(',').append(' ');
                result.append(formattedEntries.get(i).right());
            }
            result.append('}');
        } else {
            // Multiline format:
            // [table]
            // key1 = value1
            // key2 = value2
            for (int i = 0; i < formattedEntries.size(); i++) {
                final Pair<Node, String> pair = formattedEntries.get(i);

                apendComments(result, pair.left().headerComments(), options);

                result.append(pair.right());

                final String inlineComment = pair.left().inlineComment();
                if (inlineComment != null && !inlineComment.isEmpty()) {
                    result.append(" # ").append(inlineComment);
                }
                result.append('\n');
            }
        }

        return result.toString();
    }

    /**
     * Escapes a TOML key if necessary.
     * Simple keys (alphanumeric + underscore + dash) don't need quotes.
     * Complex keys need to be wrapped in quotes and escaped.
     */
    private static String escapeKey(String key) {
        if (key.isEmpty()) {
            return "\"\"";
        }

        boolean needsQuotes = false;
        for (int i = 0; i < key.length(); i++) {
            final char ch = key.charAt(i);
            if (!Character.isLetterOrDigit(ch) && ch != '_' && ch != '-') {
                needsQuotes = true;
                break;
            }
        }

        if (!needsQuotes) {
            return key;
        }

        final StringBuilder escaped = new StringBuilder(key.length() + 10);
        escaped.append('"');
        for (int i = 0; i < key.length(); i++) {
            final char ch = key.charAt(i);
            switch (ch) {
                case '"' -> escaped.append("\\\"");
                case '\\' -> escaped.append("\\\\");
                case '\n' -> escaped.append("\\n");
                case '\r' -> escaped.append("\\r");
                case '\t' -> escaped.append("\\t");
                default -> escaped.append(ch);
            }
        }
        escaped.append('"');
        return escaped.toString();
    }

    private static void apendComments(StringBuilder context, List<String> comments, WriterOptions options) {
        if (!comments.isEmpty() && (options.writeBlankLines() || options.writeHeaderComments())) {
            for (int j = 0, commentsSize = comments.size(); j < commentsSize; j++) {
                final String comment = comments.get(j);
                if ((comment.isEmpty() && options.writeBlankLines())
                        || (!comment.isEmpty() && comment.charAt(0) == '#' && options.writeHeaderComments())) {
                    context.append(comment);
                }
            }
        }
    }

    public @Nullable Node get(@NotNull String path) {
        if (path.indexOf('.') == -1) {
            return this.entries.get(path);
        }

        Object current = this;
        int i1 = 0, i2;

        while ((i2 = path.indexOf('.', i1)) != -1) {
            if (!(current instanceof TableNode tableNode)) return null;
            final String key = path.substring(i1, i2);
            final Node temp = tableNode.get(key);
            if (temp == null) return null;
            current = temp;
            i1 = i2 + 1;
        }

        final String key = path.substring(i1);
        if (!(current instanceof TableNode tableNode)) {
            return null;
        }

        return tableNode.get(key);
    }

    @SuppressWarnings("unchecked")
    public void set(@NotNull String path, @NotNull Node value) {
        if (path.indexOf('.') == -1) {
            this.entries.put(path, value);
            return;
        }

        Object current = this.entries;
        Map<String, Node> parentMap = this.entries;

        int start = 0, end;
        String key = null;

        // Warning: black magic ahead.
        // Don’t ask how it works — just be grateful that it does.
        while ((end = path.indexOf('.', start)) != -1) {
            if (!(current instanceof Map<?, ?> currentMap)) {
                // if value is not a map, then overriding it without any checks
                // because we do not need to save anything anymore :D
                TableNode newTableNode = TableNode.empty(key, -1, -1);
                parentMap.put(key, newTableNode);
                Map<String, Node> currentTable = newTableNode.entries;
                while ((end = path.indexOf('.', start)) != -1) {
                    key = path.substring(start, end);
                    newTableNode = TableNode.empty(key, -1, -1);
                    currentTable.put(key, newTableNode);
                    currentTable = newTableNode.entries;
                    start = end + 1;
                }
                current = currentTable;
                break;
            }
            key = path.substring(start, end);
            parentMap = (Map<String, Node>) currentMap;
            current = currentMap.get(key);
            start = end + 1;
        }

        final String lastKey = path.substring(start);
        if (current instanceof Map<?, ?> currentMap) {
            ((Map<String, Object>) currentMap).put(lastKey, value);
        } else {
            TableNode newTableNode = TableNode.empty(lastKey, -1, -1);
            newTableNode.entries.put(lastKey, value);
            parentMap.put(key, newTableNode);
        }
    }

    public boolean hasPath(@NotNull String path) {
        return this.get(path) != null; // any ideas how to make it others? :D
    }

    public @NotNull String name() {
        return this.name;
    }

    public static TableNode empty(@NotNull String name, int startLine, int startCol) {
        return new TableNode(name, new LinkedHashMap<>(8), startLine, startCol, List.of(), null);
    }
}
