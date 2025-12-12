package com.minidb.index;

import java.util.ArrayList;
import java.util.List;

/**
 * =============================================================================
 * BTreeNode - A single node in the B-Tree
 * =============================================================================
 *
 * WHAT IS A NODE?
 * ---------------
 * A node is one "box" in our tree that holds:
 *   - Keys: The indexed values (e.g., user IDs: [10, 20, 30])
 *   - Children: Pointers to child nodes (for internal nodes)
 *   - Row Pointers: Which row in the table has this key (for leaf nodes)
 *
 * TWO TYPES OF NODES:
 * -------------------
 *
 * 1. LEAF NODE (isLeaf = true):
 *    - Contains actual data pointers
 *    - No children
 *    - At the bottom of the tree
 *
 *    ┌─────────────────────────────┐
 *    │ Keys:     [10, 20, 30]      │
 *    │ RowPtrs:  [0,  3,  7]       │  ← Row 0 has id=10, Row 3 has id=20, etc.
 *    │ Children: (none)            │
 *    │ isLeaf:   true              │
 *    └─────────────────────────────┘
 *
 * 2. INTERNAL NODE (isLeaf = false):
 *    - Contains keys for navigation
 *    - Has children (other nodes)
 *    - Guides the search
 *
 *    ┌─────────────────────────────┐
 *    │ Keys:     [50, 100]         │
 *    │ Children: [→,  →,  →]       │  ← 3 children (keys < 50, 50-100, > 100)
 *    │ RowPtrs:  (none)            │
 *    │ isLeaf:   false             │
 *    └─────────────────────────────┘
 *           /       |       \
 *        <50     50-100     >100
 *
 * NODE CAPACITY:
 * --------------
 * Each node can hold at most (order - 1) keys.
 * Example: order=4 means max 3 keys per node.
 *
 * When a node gets too full, it SPLITS into two nodes.
 */
public class BTreeNode {

    /**
     * The keys stored in this node.
     * These are the indexed values (e.g., user IDs).
     * Always kept in SORTED order.
     */
    private List<Integer> keys;

    /**
     * Child nodes (only for internal nodes).
     * children.size() = keys.size() + 1
     *
     * Example: If keys = [50, 100]
     *   children[0] = all keys < 50
     *   children[1] = all keys >= 50 and < 100
     *   children[2] = all keys >= 100
     */
    private List<BTreeNode> children;

    /**
     * Row pointers (only for leaf nodes).
     * Maps each key to its row index in the table.
     * rowPointers.size() = keys.size()
     *
     * Example: If keys = [10, 20, 30] and rowPointers = [0, 5, 2]
     *   Key 10 is in row 0
     *   Key 20 is in row 5
     *   Key 30 is in row 2
     */
    private List<Integer> rowPointers;

    /**
     * Is this a leaf node?
     * true = leaf (bottom of tree, has row pointers)
     * false = internal (middle of tree, has children)
     */
    private boolean isLeaf;

    /**
     * Creates a new BTreeNode
     *
     * @param isLeaf true if this is a leaf node
     */
    public BTreeNode(boolean isLeaf) {
        this.isLeaf = isLeaf;
        this.keys = new ArrayList<>();
        this.children = new ArrayList<>();
        this.rowPointers = new ArrayList<>();
    }

    // =========================================================================
    // GETTERS
    // =========================================================================

    public List<Integer> getKeys() {
        return keys;
    }

    public List<BTreeNode> getChildren() {
        return children;
    }

    public List<Integer> getRowPointers() {
        return rowPointers;
    }

    public boolean isLeaf() {
        return isLeaf;
    }

    /**
     * Gets the number of keys in this node
     */
    public int getKeyCount() {
        return keys.size();
    }

    /**
     * Gets a key at specific index
     */
    public int getKey(int index) {
        return keys.get(index);
    }

    /**
     * Gets a child at specific index
     */
    public BTreeNode getChild(int index) {
        return children.get(index);
    }

    /**
     * Gets a row pointer at specific index
     */
    public int getRowPointer(int index) {
        return rowPointers.get(index);
    }

    // =========================================================================
    // SETTERS / MODIFIERS
    // =========================================================================

    public void setLeaf(boolean isLeaf) {
        this.isLeaf = isLeaf;
    }

    /**
     * Adds a key at a specific position
     */
    public void addKey(int index, int key) {
        keys.add(index, key);
    }

    /**
     * Adds a key at the end
     */
    public void addKey(int key) {
        keys.add(key);
    }

    /**
     * Removes and returns the key at index
     */
    public int removeKey(int index) {
        return keys.remove(index);
    }

    /**
     * Adds a child at a specific position
     */
    public void addChild(int index, BTreeNode child) {
        children.add(index, child);
    }

    /**
     * Adds a child at the end
     */
    public void addChild(BTreeNode child) {
        children.add(child);
    }

    /**
     * Removes and returns the child at index
     */
    public BTreeNode removeChild(int index) {
        return children.remove(index);
    }

    /**
     * Adds a row pointer at a specific position
     */
    public void addRowPointer(int index, int rowPointer) {
        rowPointers.add(index, rowPointer);
    }

    /**
     * Adds a row pointer at the end
     */
    public void addRowPointer(int rowPointer) {
        rowPointers.add(rowPointer);
    }

    /**
     * Removes and returns the row pointer at index
     */
    public int removeRowPointer(int index) {
        return rowPointers.remove(index);
    }

    // =========================================================================
    // SEARCH HELPERS
    // =========================================================================

    /**
     * Finds the index where a key should be inserted to maintain sorted order.
     * Also used to find which child to traverse.
     *
     * @param key The key to find position for
     * @return The index where key should be (or is)
     *
     * EXAMPLE:
     *   keys = [10, 30, 50]
     *   findKeyIndex(20) returns 1 (between 10 and 30)
     *   findKeyIndex(30) returns 1 (exact match)
     *   findKeyIndex(5) returns 0 (before all)
     *   findKeyIndex(60) returns 3 (after all)
     */
    public int findKeyIndex(int key) {
        int index = 0;
        while (index < keys.size() && keys.get(index) < key) {
            index++;
        }
        return index;
    }

    /**
     * Checks if this node contains a specific key
     *
     * @param key The key to search for
     * @return true if the key exists in this node
     */
    public boolean containsKey(int key) {
        return keys.contains(key);
    }

    /**
     * Gets the index of a key (or -1 if not found)
     */
    public int getKeyIndex(int key) {
        return keys.indexOf(key);
    }

    // =========================================================================
    // DEBUG / DISPLAY
    // =========================================================================

    /**
     * String representation for debugging
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(isLeaf ? "Leaf" : "Internal");
        sb.append("{keys=").append(keys);
        if (isLeaf) {
            sb.append(", rows=").append(rowPointers);
        } else {
            sb.append(", children=").append(children.size());
        }
        sb.append("}");
        return sb.toString();
    }

    /**
     * Prints this node with indentation (for tree visualization)
     */
    public void print(String indent) {
        System.out.println(indent + (isLeaf ? "🍃 " : "📁 ") + "Keys: " + keys);
        if (isLeaf) {
            System.out.println(indent + "   Rows: " + rowPointers);
        }
        for (BTreeNode child : children) {
            child.print(indent + "    ");
        }
    }
}