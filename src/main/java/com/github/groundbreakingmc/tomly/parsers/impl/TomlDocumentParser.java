package com.github.groundbreakingmc.tomly.parsers.impl;

import com.github.groundbreakingmc.tomly.contexts.ParsingContext;
import com.github.groundbreakingmc.tomly.dispatchers.ValueDispatcher;
import com.github.groundbreakingmc.tomly.nodes.Node;
import com.github.groundbreakingmc.tomly.nodes.impl.ArrayNode;
import com.github.groundbreakingmc.tomly.nodes.impl.DocumentNode;
import com.github.groundbreakingmc.tomly.nodes.impl.TableNode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class TomlDocumentParser {

    private final @NotNull ValueDispatcher dispatcher;

    public TomlDocumentParser(@NotNull ValueDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @NotNull
    public DocumentNode parse(@NotNull ParsingContext context) {
        final int docLine = context.getLine();
        final int docCol = context.getColumn();
        final DocumentNode root = new DocumentNode(new TableNode("", new LinkedHashMap<>(32), docLine, docCol, List.of(), null));

        String currentTablePath = null; // e.g. "server.database"
        DocumentNode parent;
        Map<String, Node> currentTable = root.value();

        while (!context.eof()) {
            context.skipWhitespaceAndCollectHeaderComments();
            if (context.eof()) break;

            if (context.current() == '[') {
                // table header [a.b] or [[a.b]]
                boolean isArrayOfTables = false;
                if (context.advance() == '[') {
                    isArrayOfTables = true;
                    context.advance();
                }

                context.skipWhitespaces();
                final String path = readDottedPath(context);
                if (context.current() != ']') {
                    throw context.error("Expected ']' in table header");
                }
                if (isArrayOfTables && context.advance() != ']') {
                    throw context.error("Expected one more ']' in table header");
                }
                context.advance();

                currentTablePath = path;
                parent = ensurePath(root, context, path, isArrayOfTables);
                currentTable = parent.value();
                // consume rest of line comments
                context.readInlineCommentIfAny();
                continue;
            }

            // key = value line
            final KeyValue kv = this.readKeyValue(context);
            if (currentTable.containsKey(kv.key)) {
                throw context.error("Duplicate key: " + kv.key + (currentTablePath != null ? " in [" + currentTablePath + "]" : ""));
            }
            currentTable.put(kv.key, kv.value);

            // consume line end
            while (context.current() == ' ' || context.current() == '\t') {
                context.advance();
            }
            if (context.current() == '#') {
                context.readInlineCommentIfAny();
            }
            if (context.current() == '\n' || context.current() == '\r') {
                context.advance();
            }
        }

        return root;
    }

    private static @NotNull String readDottedPath(@NotNull ParsingContext context) {
        final StringBuilder result = new StringBuilder(64);
        while (true) {
            final String key = InlineTableParser.readKey(context);
            result.append(key);
            context.skipWhitespaces();
            if (context.current() == '.') {
                result.append('.');
                context.advance();
                context.skipWhitespaces();
                continue;
            }
            break;
        }
        return result.toString();
    }

    private static @NotNull DocumentNode ensurePath(DocumentNode root, ParsingContext context, String path, boolean isArrayOfTables) {
        if (path.isEmpty()) {
            return root;
        }

        DocumentNode parent = root;
        Map<String, Node> currentTable;
        int lastDot = -1;

        for (int i = 0; i < path.length(); i++) {
            if (path.charAt(i) == '.') {
                currentTable = parent.value();

                final String key = path.substring(lastDot + 1, i);
                final String currentPath = path.substring(0, i);

                Node existing = currentTable.get(key);
                if (existing == null) {
                    final TableNode newTable = TableNode.empty(currentPath, context.getLine(), context.getColumn());
                    currentTable.put(key, (parent = new DocumentNode(newTable, key, parent)));
                } else if (existing instanceof DocumentNode documentNode) {
                    parent = documentNode;
                } else {
                    throw context.error("Path clashes with non-table: " + currentPath + " (intermediate path)");
                }

                lastDot = i;
            }
        }

        return handleFinalPathPart(parent, context, path, isArrayOfTables);
    }

    private static @NotNull DocumentNode handleFinalPathPart(DocumentNode parent, ParsingContext context, String path, boolean isArrayOfTables) {
        final int lastDot = path.lastIndexOf('.');
        final String key = path.substring(lastDot + 1);
        final String currentPath = path.substring(0, lastDot != -1 ? lastDot : path.length());

        Map<String, Node> parentTable = parent.value();
        Node existing = parentTable.get(key);

        if (isArrayOfTables) {
            if (existing == null) {
                final DocumentNode newTable = new DocumentNode(TableNode.empty(currentPath, context.getLine(), context.getColumn()), key, parent);

                final List<Node> items = new ArrayList<>(4);
                items.add(newTable);

                final ArrayNode array = new ArrayNode(items, context.getLine(), context.getColumn(), List.of(), null);
                parentTable.put(key, array);

                return newTable;
            } else if (existing instanceof ArrayNode arrayNode) {
                final DocumentNode newTable = new DocumentNode(TableNode.empty(currentPath, context.getLine(), context.getColumn()), key, parent);
                arrayNode.value().add(newTable);
                return newTable;
            } else {
                throw context.error("Cannot redefine key '" + key + "' as array of tables - already exists as " + existing.getClass().getSimpleName());
            }
        } else {
            if (existing == null) {
                final DocumentNode newTable = new DocumentNode(TableNode.empty(currentPath, context.getLine(), context.getColumn()), key, parent);
                parentTable.put(key, newTable);
                return newTable;
            } else if (existing instanceof DocumentNode tableNode) {
                return tableNode;
            } else {
                throw context.error("Cannot redefine key '" + key + "' as array of tables - already exists as " + existing.getClass().getSimpleName());
            }
        }
    }

    private @NotNull KeyValue readKeyValue(@NotNull ParsingContext context) {
        final String key = InlineTableParser.readKey(context);
        context.skipWhitespaces();
        if (context.current() != '=') {
            throw context.error("Expected '=' after key");
        }
        context.advance();
        final Node value = this.dispatcher.readValue(context);
        return new KeyValue(key, value);
    }

    private record KeyValue(String key, Node value) {
    }
}
