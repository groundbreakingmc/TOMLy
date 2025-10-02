package com.github.groundbreakingmc.tomly.nodes.impl;

import com.github.groundbreakingmc.tomly.nodes.Node;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Comprehensive test suite for TableNode.
 * Tests path-based operations including get, set, and hasPath
 * for both simple and nested keys.
 */
@DisplayName("TableNode Tests")
class TableNodeTest {

    private TableNode root;

    @BeforeEach
    void setUp() {
        this.root = TableNode.empty("", -1, -1);
    }

    @Nested
    @DisplayName("get() method tests")
    class GetTests {

        @Test
        @DisplayName("Should return value for simple key")
        void testGetSimpleKey() {
            final Node value = new StringNode("hello", List.of(), null);
            TableNodeTest.this.root.set("greeting", value);

            assertEquals(value, TableNodeTest.this.root.get("greeting"));
        }

        @Test
        @DisplayName("Should return value for nested path")
        void testGetNestedKey() {
            final Node value = new StringNode("world", List.of(), null);
            TableNodeTest.this.root.set("a.b.c", value);

            assertEquals(value, TableNodeTest.this.root.get("a.b.c"));
        }

        @Test
        @DisplayName("Should return null for non-existent key")
        void testGetNonExistentKey() {
            assertNull(TableNodeTest.this.root.get("missing"));
            assertNull(TableNodeTest.this.root.get("a.b.c"));
        }
    }

    @Nested
    @DisplayName("set() method tests")
    class SetTests {

        @Test
        @DisplayName("Should set simple key")
        void testSetSimpleKey() {
            final Node value = new NumberNode(42, List.of(), null);
            TableNodeTest.this.root.set("answer", value);

            assertEquals(value, TableNodeTest.this.root.get("answer"));
        }

        @Test
        @DisplayName("Should set nested key and create intermediate tables")
        void testSetNestedKey() {
            final Node value = new NumberNode(123, List.of(), null);
            TableNodeTest.this.root.set("x.y.z", value);

            assertInstanceOf(TableNode.class, TableNodeTest.this.root.get("x"));
            assertInstanceOf(TableNode.class, TableNodeTest.this.root.get("x.y"));
            assertEquals(value, TableNodeTest.this.root.get("x.y.z"));
        }

        @Test
        @DisplayName("Should overwrite existing key")
        void testOverwriteKey() {
            final Node first = new StringNode("old", List.of(), null);
            final Node second = new StringNode("new", List.of(), null);

            TableNodeTest.this.root.set("key", first);
            TableNodeTest.this.root.set("key", second);

            assertEquals(second, TableNodeTest.this.root.get("key"));
        }

        @Test
        @DisplayName("Should replace non-table with table when needed")
        void testReplaceNonTable() {
            final Node primitive = new StringNode("value", List.of(), null);
            final Node nested = new StringNode("deep", List.of(), null);

            TableNodeTest.this.root.set("a", primitive);
            TableNodeTest.this.root.set("a.b", nested);

            assertEquals(nested, TableNodeTest.this.root.get("a.b"));
        }
    }

    @Nested
    @DisplayName("hasPath() method tests")
    class HasPathTests {

        @Test
        @DisplayName("Should return true for existing simple key")
        void testHasPathSimple() {
            TableNodeTest.this.root.set("name", new StringNode("Groundbreaking", List.of(), null));
            assertTrue(TableNodeTest.this.root.hasPath("name"));
        }

        @Test
        @DisplayName("Should return true for existing nested path")
        void testHasPathNested() {
            TableNodeTest.this.root.set("config.option.enabled", new BooleanNode(true, List.of(), null));
            assertTrue(TableNodeTest.this.root.hasPath("config.option.enabled"));
        }

        @Test
        @DisplayName("Should return false for missing path")
        void testHasPathMissing() {
            assertFalse(TableNodeTest.this.root.hasPath("not.there"));
        }
    }
}