package com.minidb.index;

import java.util.ArrayList;
import java.util.List;

/**
 * =============================================================================
 * BTreeIndex - A B-Tree index for fast lookups
 * =============================================================================
 *
 * WHAT IS THIS?
 * -------------
 * A B-Tree index that maps keys (like user IDs) to row positions.
 * Instead of scanning all rows, we traverse the tree in O(log n) time.
 *
 * HOW IT WORKS:
 * -------------
 *
 * INSERT: Add a key and its row position
 *   index.insert(userId=50, rowIndex=3)
 *   → Key 50 points to row 3
 *
 * SEARCH: Find which row has a key
 *   int row = index.search(50)
 *   → Returns 3 (or -1 if not found)
 *
 * RANGE SEARCH: Find all rows in a range
 *   List<Integer> rows = index.searchRange(20, 80)
 *   → Returns all row indices where key is between 20 and 80
 *
 * STRUCTURE:
 * ----------
 *                      [50]                    ← Root
 *                     /    \
 *               [20,30]    [70,90]             ← Internal nodes
 *               /  |  \    /  |  \
 *            [10][25][40][60][80][100]         ← Leaf nodes (have row pointers)
 *
 * ORDER (Branching Factor):
 * -------------------------
 * - Order determines max keys per node
 * - Order 4 means: max 3 keys, max 4 children per node
 * - Higher order = shorter tree = faster searches
 * - Typical databases use order 100-1000
 */
public class BTreeIndex {

    /**
     * The root node of the tree.
     * All searches start here.
     */
    private BTreeNode root;

    /**
     * The order (branching factor) of the tree.
     * - Max keys per node = order - 1
     * - Max children per node = order
     *
     * Example: order=4
     *   Max keys = 3
     *   Max children = 4
     */
    private final int order;

    /**
     * Name of the column this index is built on.
     * Example: "id", "email", "age"
     */
    private final String columnName;

    /**
     * Total number of keys in the index
     */
    private int size;

    /**
     * Creates a new B-Tree index
     *
     * @param columnName The column this index is for
     * @param order The branching factor (default: 4)
     *
     * EXAMPLE:
     *   BTreeIndex idIndex = new BTreeIndex("id", 4);
     */
    public BTreeIndex(String columnName, int order) {
        if (order < 3) {
            throw new IllegalArgumentException("Order must be at least 3");
        }
        this.columnName = columnName;
        this.order = order;
        this.root = new BTreeNode(true);  // Start with empty leaf
        this.size = 0;
    }

    /**
     * Creates a B-Tree index with default order of 4
     */
    public BTreeIndex(String columnName) {
        this(columnName, 4);
    }

    // =========================================================================
    // SEARCH OPERATIONS
    // =========================================================================

    /**
     * Searches for a key and returns its row index
     *
     * @param key The key to search for
     * @return The row index, or -1 if not found
     *
     * EXAMPLE:
     *   int row = index.search(50);
     *   // Returns the row index where id=50, or -1 if not found
     *
     * TIME COMPLEXITY: O(log n)
     *
     * HOW IT WORKS:
     *   1. Start at root
     *   2. Find which child to go to (based on key comparison)
     *   3. Repeat until we reach a leaf
     *   4. Search the leaf for the key
     */
    public int search(int key) {
        return search(root, key);
    }

    /**
     * Recursive search helper
     */
    private int search(BTreeNode node, int key) {
        // Find the index where key would be
        int i = node.findKeyIndex(key);

        // Check if we found the key in this node
        if (i < node.getKeyCount() && node.getKey(i) == key) {
            if (node.isLeaf()) {
                // Found in leaf - return row pointer
                return node.getRowPointer(i);
            }
            // For internal nodes, we could have the key but need to go to leaf
            // In our implementation, exact matches go to the right child
        }

        // If leaf and not found, key doesn't exist
        if (node.isLeaf()) {
            return -1;
        }

        // Recurse to appropriate child
        return search(node.getChild(i), key);
    }

    /**
     * Searches for all keys in a range [minKey, maxKey]
     *
     * @param minKey Minimum key (inclusive)
     * @param maxKey Maximum key (inclusive)
     * @return List of row indices for keys in range
     *
     * EXAMPLE:
     *   List<Integer> rows = index.searchRange(20, 50);
     *   // Returns all rows where 20 <= key <= 50
     *
     * TIME COMPLEXITY: O(log n + k) where k is number of results
     */
    public List<Integer> searchRange(int minKey, int maxKey) {
        List<Integer> results = new ArrayList<>();
        searchRange(root, minKey, maxKey, results);
        return results;
    }

    /**
     * Recursive range search helper
     */
    private void searchRange(BTreeNode node, int minKey, int maxKey, List<Integer> results) {
        int i = 0;

        // Find starting position
        while (i < node.getKeyCount() && node.getKey(i) < minKey) {
            i++;
        }

        // Traverse keys in range
        while (i < node.getKeyCount() && node.getKey(i) <= maxKey) {
            // If internal node, search left child first
            if (!node.isLeaf()) {
                searchRange(node.getChild(i), minKey, maxKey, results);
            }

            // Add current key if in range and is leaf
            if (node.isLeaf() && node.getKey(i) >= minKey && node.getKey(i) <= maxKey) {
                results.add(node.getRowPointer(i));
            }

            i++;
        }

        // Search rightmost child if internal node
        if (!node.isLeaf() && i < node.getChildren().size()) {
            searchRange(node.getChild(i), minKey, maxKey, results);
        }
    }

    /**
     * Checks if a key exists in the index
     */
    public boolean contains(int key) {
        return search(key) != -1;
    }

    // =========================================================================
    // INSERT OPERATIONS
    // =========================================================================

    /**
     * Inserts a key and its row pointer into the index
     *
     * @param key The key value (e.g., user ID)
     * @param rowIndex The row index in the table
     *
     * EXAMPLE:
     *   index.insert(50, 3);  // Key 50 is in row 3
     *
     * TIME COMPLEXITY: O(log n)
     *
     * HOW IT WORKS:
     *   1. Find the correct leaf node
     *   2. Insert key in sorted position
     *   3. If node is too full, split it
     *   4. Propagate split up if necessary
     */
    public void insert(int key, int rowIndex) {
        BTreeNode r = root;

        // If root is full, split it first
        if (r.getKeyCount() == order - 1) {
            // Create new root
            BTreeNode newRoot = new BTreeNode(false);
            newRoot.addChild(root);

            // Split the old root
            splitChild(newRoot, 0);

            // Update root reference
            root = newRoot;

            // Insert into appropriate child
            insertNonFull(root, key, rowIndex);
        } else {
            insertNonFull(r, key, rowIndex);
        }

        size++;
    }

    /**
     * Inserts into a node that is guaranteed to not be full
     */
    private void insertNonFull(BTreeNode node, int key, int rowIndex) {
        int i = node.getKeyCount() - 1;

        if (node.isLeaf()) {
            // Find position and insert
            while (i >= 0 && key < node.getKey(i)) {
                i--;
            }
            node.addKey(i + 1, key);
            node.addRowPointer(i + 1, rowIndex);
        } else {
            // Find child to insert into
            while (i >= 0 && key < node.getKey(i)) {
                i--;
            }
            i++;

            // If child is full, split it
            if (node.getChild(i).getKeyCount() == order - 1) {
                splitChild(node, i);
                if (key > node.getKey(i)) {
                    i++;
                }
            }

            insertNonFull(node.getChild(i), key, rowIndex);
        }
    }

    /**
     * Splits a full child node
     *
     * BEFORE SPLIT (child has 3 keys, order=4):
     *     Parent: [20, 60]
     *              |
     *     Child:  [30, 40, 50]  ← Too full!
     *
     * AFTER SPLIT:
     *     Parent: [20, 40, 60]  ← Middle key (40) moved up
     *              /     \
     *          [30]     [50]    ← Split into two nodes
     */
    private void splitChild(BTreeNode parent, int childIndex) {
        BTreeNode fullChild = parent.getChild(childIndex);
        BTreeNode newChild = new BTreeNode(fullChild.isLeaf());

        int midIndex = (order - 1) / 2;

        // Move right half of keys to new child
        // For a node with keys [10, 20, 30], mid=1, so we move key 30
        for (int j = midIndex + 1; j < fullChild.getKeyCount(); j++) {
            newChild.addKey(fullChild.getKey(j));
            if (fullChild.isLeaf()) {
                newChild.addRowPointer(fullChild.getRowPointer(j));
            }
        }

        // Move right half of children (for internal nodes)
        if (!fullChild.isLeaf()) {
            for (int j = midIndex + 1; j <= fullChild.getChildren().size() - 1; j++) {
                newChild.addChild(fullChild.getChild(j));
            }
            // Remove moved children
            while (fullChild.getChildren().size() > midIndex + 1) {
                fullChild.getChildren().remove(fullChild.getChildren().size() - 1);
            }
        }

        // Get the middle key to move up
        int middleKey = fullChild.getKey(midIndex);
        int middleRowPtr = fullChild.isLeaf() ? fullChild.getRowPointer(midIndex) : -1;

        // Remove keys/pointers that were moved or promoted
        while (fullChild.getKeyCount() > midIndex) {
            fullChild.getKeys().remove(fullChild.getKeyCount() - 1);
            if (fullChild.isLeaf() && fullChild.getRowPointers().size() > midIndex) {
                fullChild.getRowPointers().remove(fullChild.getRowPointers().size() - 1);
            }
        }

        // Insert middle key into parent
        parent.addKey(childIndex, middleKey);
        if (parent.isLeaf()) {
            parent.addRowPointer(childIndex, middleRowPtr);
        }

        // Add new child to parent
        parent.addChild(childIndex + 1, newChild);
    }

    // =========================================================================
    // UTILITY METHODS
    // =========================================================================

    /**
     * Gets the column name this index is built on
     */
    public String getColumnName() {
        return columnName;
    }

    /**
     * Gets the order (branching factor)
     */
    public int getOrder() {
        return order;
    }

    /**
     * Gets the number of keys in the index
     */
    public int getSize() {
        return size;
    }

    /**
     * Checks if the index is empty
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Gets the height of the tree
     */
    public int getHeight() {
        int height = 0;
        BTreeNode node = root;
        while (!node.isLeaf()) {
            height++;
            node = node.getChild(0);
        }
        return height;
    }

    /**
     * Prints the tree structure (for debugging)
     */
    public void printTree() {
        System.out.println("\n🌳 B-Tree Index on '" + columnName + "'");
        System.out.println("   Order: " + order + ", Size: " + size + ", Height: " + getHeight());
        System.out.println("   Structure:");
        root.print("   ");
    }

    /**
     * Gets all keys in sorted order (for verification)
     */
    public List<Integer> getAllKeys() {
        List<Integer> keys = new ArrayList<>();
        collectKeys(root, keys);
        return keys;
    }

    private void collectKeys(BTreeNode node, List<Integer> keys) {
        for (int i = 0; i < node.getKeyCount(); i++) {
            if (!node.isLeaf()) {
                collectKeys(node.getChild(i), keys);
            }
            if (node.isLeaf()) {
                keys.add(node.getKey(i));
            }
        }
        if (!node.isLeaf() && node.getChildren().size() > node.getKeyCount()) {
            collectKeys(node.getChild(node.getKeyCount()), keys);
        }
    }

    @Override
    public String toString() {
        return "BTreeIndex{column='" + columnName + "', order=" + order +
                ", size=" + size + ", height=" + getHeight() + "}";
    }
}