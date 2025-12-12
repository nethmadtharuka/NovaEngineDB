package com.minidb;

import com.minidb.index.BTreeIndex;
import com.minidb.storage.*;

import java.util.Arrays;
import java.util.List;

/**
 * =============================================================================
 * Step 4 Demo - B-Tree Index in Action!
 * =============================================================================
 *
 * This demo shows:
 *   1. Creating a B-Tree index
 *   2. Inserting keys
 *   3. Fast O(log n) searches
 *   4. Range queries
 *   5. Performance comparison: with vs without index
 */
public class Step4Demo {

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║          MiniDB - Step 4: B-Tree Index Demo                  ║");
        System.out.println("║       Lightning-fast searches with O(log n)!                 ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝\n");

        // Run demos
        demonstrateBTreeBasics();
        demonstrateBTreeStructure();
        demonstrateRangeSearch();
        demonstratePerformance();
        demonstrateWithTable();

        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                   🎉 STEP 4 COMPLETE! 🎉                     ║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.println("║  What we built:                                              ║");
        System.out.println("║    ✅ BTreeNode - Tree node structure                       ║");
        System.out.println("║    ✅ BTreeIndex - Full B-Tree implementation               ║");
        System.out.println("║    ✅ O(log n) search - 20 steps for 1 million rows!        ║");
        System.out.println("║    ✅ Range queries - Find all in range efficiently         ║");
        System.out.println("║                                                              ║");
        System.out.println("║  Next step: JOIN operations!                                ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
    }

    /**
     * PART 1: Basic B-Tree operations
     */
    private static void demonstrateBTreeBasics() {
        System.out.println("═══════════════════════════════════════════════════════════════");
        System.out.println("PART 1: B-Tree Basics - Insert and Search");
        System.out.println("═══════════════════════════════════════════════════════════════\n");

        // Create index on 'id' column
        BTreeIndex index = new BTreeIndex("id", 4);
        System.out.println("📁 Created B-Tree index on 'id' column (order=4)\n");

        // Insert some keys (key = id, value = row index)
        System.out.println("📝 Inserting keys:");
        int[] ids = {50, 25, 75, 10, 30, 60, 90, 5, 15, 35};

        for (int i = 0; i < ids.length; i++) {
            index.insert(ids[i], i);  // id -> row index
            System.out.println("   insert(" + ids[i] + ", row=" + i + ")");
        }

        System.out.println("\n" + index);

        // Search for keys
        System.out.println("\n🔍 Searching for keys:");
        int[] searchKeys = {25, 60, 100, 5};

        for (int key : searchKeys) {
            int rowIndex = index.search(key);
            if (rowIndex >= 0) {
                System.out.println("   search(" + key + ") → Found at row " + rowIndex + " ✅");
            } else {
                System.out.println("   search(" + key + ") → Not found ❌");
            }
        }
    }

    /**
     * PART 2: Visualize the tree structure
     */
    private static void demonstrateBTreeStructure() {
        System.out.println("\n═══════════════════════════════════════════════════════════════");
        System.out.println("PART 2: B-Tree Structure Visualization");
        System.out.println("═══════════════════════════════════════════════════════════════\n");

        BTreeIndex index = new BTreeIndex("id", 4);

        // Insert in specific order to show tree growth
        int[] insertOrder = {10, 20, 30, 40, 50, 60, 70};

        System.out.println("📝 Inserting keys in order: " + Arrays.toString(insertOrder));
        System.out.println("   Watch how the tree grows and splits!\n");

        for (int i = 0; i < insertOrder.length; i++) {
            index.insert(insertOrder[i], i);
            System.out.println("After inserting " + insertOrder[i] + ":");
            index.printTree();
            System.out.println();
        }
    }

    /**
     * PART 3: Range search demonstration
     */
    private static void demonstrateRangeSearch() {
        System.out.println("═══════════════════════════════════════════════════════════════");
        System.out.println("PART 3: Range Search");
        System.out.println("═══════════════════════════════════════════════════════════════\n");

        BTreeIndex index = new BTreeIndex("age", 4);

        // Insert ages
        int[] ages = {22, 35, 28, 45, 31, 19, 52, 38, 25, 41};
        System.out.println("📝 Inserting ages: " + Arrays.toString(ages) + "\n");

        for (int i = 0; i < ages.length; i++) {
            index.insert(ages[i], i);
        }

        // Range searches
        System.out.println("🔍 Range searches:");

        List<Integer> result1 = index.searchRange(25, 40);
        System.out.println("   searchRange(25, 40) → Rows: " + result1);
        System.out.println("   (Ages between 25 and 40)\n");

        List<Integer> result2 = index.searchRange(30, 50);
        System.out.println("   searchRange(30, 50) → Rows: " + result2);
        System.out.println("   (Ages between 30 and 50)\n");

        List<Integer> result3 = index.searchRange(18, 25);
        System.out.println("   searchRange(18, 25) → Rows: " + result3);
        System.out.println("   (Ages between 18 and 25)");
    }

    /**
     * PART 4: Performance comparison
     */
    private static void demonstratePerformance() {
        System.out.println("\n═══════════════════════════════════════════════════════════════");
        System.out.println("PART 4: Performance Comparison");
        System.out.println("═══════════════════════════════════════════════════════════════\n");

        int numRows = 100000;  // 100K rows
        int searchKey = 75000;

        System.out.println("📊 Testing with " + numRows + " rows...\n");

        // Create and populate index
        BTreeIndex index = new BTreeIndex("id", 100);  // Higher order for better performance

        System.out.println("📝 Building index...");
        long startBuild = System.currentTimeMillis();
        for (int i = 0; i < numRows; i++) {
            index.insert(i, i);
        }
        long buildTime = System.currentTimeMillis() - startBuild;
        System.out.println("   Index built in " + buildTime + " ms");
        System.out.println("   Tree height: " + index.getHeight());
        System.out.println();

        // Simulate linear search (without index)
        System.out.println("🐢 WITHOUT INDEX (Linear Search):");
        long startLinear = System.nanoTime();
        int linearResult = -1;
        int linearComparisons = 0;
        for (int i = 0; i < numRows; i++) {
            linearComparisons++;
            if (i == searchKey) {
                linearResult = i;
                break;
            }
        }
        long linearTime = System.nanoTime() - startLinear;
        System.out.println("   Found key " + searchKey + " at row " + linearResult);
        System.out.println("   Comparisons: " + linearComparisons);
        System.out.println("   Time: " + linearTime + " ns");

        // B-Tree search (with index)
        System.out.println("\n🚀 WITH B-TREE INDEX:");
        long startBTree = System.nanoTime();
        int btreeResult = index.search(searchKey);
        long btreeTime = System.nanoTime() - startBTree;
        System.out.println("   Found key " + searchKey + " at row " + btreeResult);
        System.out.println("   Max comparisons: ~" + (int)(Math.log(numRows) / Math.log(index.getOrder())) + " (tree height)");
        System.out.println("   Time: " + btreeTime + " ns");

        // Speedup calculation
        System.out.println("\n📈 SPEEDUP:");
        double speedup = (double) linearTime / btreeTime;
        System.out.println("   B-Tree is ~" + String.format("%.1f", speedup) + "x faster!");
        System.out.println("   Linear: O(n) = O(" + numRows + ")");
        System.out.println("   B-Tree: O(log n) = O(" + (int)(Math.log(numRows) / Math.log(2)) + ")");
    }

    /**
     * PART 5: Using index with actual table
     */
    private static void demonstrateWithTable() {
        System.out.println("\n═══════════════════════════════════════════════════════════════");
        System.out.println("PART 5: Using Index with Table");
        System.out.println("═══════════════════════════════════════════════════════════════\n");

        // Create a table
        Table users = new Table("users", Arrays.asList(
                new Column("id", DataType.INTEGER),
                new Column("name", DataType.STRING),
                new Column("age", DataType.INTEGER)
        ));

        // Create index on 'id' column
        BTreeIndex idIndex = new BTreeIndex("id", 4);

        // Insert data and build index simultaneously
        System.out.println("📝 Creating table and building index:");
        String[][] userData = {
                {"100", "Alice", "28"},
                {"200", "Bob", "35"},
                {"150", "Charlie", "22"},
                {"300", "Diana", "45"},
                {"250", "Eve", "31"}
        };

        for (int i = 0; i < userData.length; i++) {
            int id = Integer.parseInt(userData[i][0]);
            String name = userData[i][1];
            int age = Integer.parseInt(userData[i][2]);

            // Insert into table
            users.insert(Arrays.asList(id, name, age));

            // Insert into index (key=id, value=row index)
            idIndex.insert(id, i);

            System.out.println("   Row " + i + ": id=" + id + ", name=" + name);
        }

        System.out.println("\n📋 Table contents:");
        users.printTable();

        System.out.println("🌳 Index structure:");
        idIndex.printTree();

        // Use index to find rows
        System.out.println("\n🔍 Using index to find rows:");

        int searchId = 150;
        int rowIndex = idIndex.search(searchId);
        System.out.println("   Query: SELECT * FROM users WHERE id = " + searchId);
        System.out.println("   Index lookup: id " + searchId + " → row " + rowIndex);

        if (rowIndex >= 0) {
            Row foundRow = users.selectAll().get(rowIndex);
            System.out.println("   Result: " + foundRow.toFormattedString(" | "));
        }

        // Range query using index
        System.out.println("\n   Query: SELECT * FROM users WHERE id BETWEEN 150 AND 250");
        List<Integer> rangeRows = idIndex.searchRange(150, 250);
        System.out.println("   Index lookup: rows " + rangeRows);
        System.out.println("   Results:");
        for (int ri : rangeRows) {
            Row r = users.selectAll().get(ri);
            System.out.println("      " + r.toFormattedString(" | "));
        }
    }
}