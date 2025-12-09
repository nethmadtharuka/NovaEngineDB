package com.minidb;

import com.minidb.storage.Column;
import com.minidb.storage.DataType;
import com.minidb.storage.Row;
import com.minidb.storage.Table;

import java.util.Arrays;
import java.util.List;

/**
 * =============================================================================
 * MiniDB - Demo showing how our in-memory table works
 * =============================================================================
 *
 * This is STEP 1 of our database engine!
 * We've built the foundation: storing data in memory.
 *
 * WHAT WE'VE ACHIEVED:
 * --------------------
 * ✅ DataType - Defines what types of data we support
 * ✅ Column   - Defines the structure of each column
 * ✅ Row      - Holds actual data values
 * ✅ Table    - Combines everything together
 *
 * WHAT THIS DEMO SHOWS:
 * ---------------------
 * 1. Creating a table with columns
 * 2. Inserting data
 * 3. Selecting all data
 * 4. Selecting with WHERE conditions
 *
 * RUN THIS to see your first database in action!
 */
public class MiniDB {

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║              Welcome to MiniDB - Step 1 Demo                 ║");
        System.out.println("║           Building a Database From Scratch!                  ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝\n");

        // =====================================================================
        // DEMO 1: Creating a Table
        // This is like: CREATE TABLE users (id INTEGER, name STRING, age INTEGER)
        // =====================================================================

        System.out.println("📋 STEP 1: Creating a 'users' table");
        System.out.println("   SQL equivalent: CREATE TABLE users (id INTEGER, name STRING, age INTEGER)");
        System.out.println("   ────────────────────────────────────────────────────────────");

        // Define the columns (schema)
        List<Column> userColumns = Arrays.asList(
                new Column("id", DataType.INTEGER),
                new Column("name", DataType.STRING),
                new Column("age", DataType.INTEGER),
                new Column("is_active", DataType.BOOLEAN)
        );

        // Create the table
        Table usersTable = new Table("users", userColumns);

        System.out.println("   ✅ Table created: " + usersTable);
        System.out.println();

        // =====================================================================
        // DEMO 2: Inserting Data
        // This is like: INSERT INTO users VALUES (1, 'Alice', 25, true)
        // =====================================================================

        System.out.println("📋 STEP 2: Inserting data into the table");
        System.out.println("   ────────────────────────────────────────────────────────────");

        // Insert some rows
        System.out.println("   Executing: INSERT INTO users VALUES (1, 'Alice', 25, true)");
        usersTable.insert(Arrays.asList(1, "Alice", 25, true));

        System.out.println("   Executing: INSERT INTO users VALUES (2, 'Bob', 30, true)");
        usersTable.insert(Arrays.asList(2, "Bob", 30, true));

        System.out.println("   Executing: INSERT INTO users VALUES (3, 'Charlie', 35, false)");
        usersTable.insert(Arrays.asList(3, "Charlie", 35, false));

        System.out.println("   Executing: INSERT INTO users VALUES (4, 'Diana', 28, true)");
        usersTable.insert(Arrays.asList(4, "Diana", 28, true));

        System.out.println("   Executing: INSERT INTO users VALUES (5, 'Eve', 22, false)");
        usersTable.insert(Arrays.asList(5, "Eve", 22, false));

        System.out.println("   ✅ Inserted 5 rows");
        System.out.println();

        // =====================================================================
        // DEMO 3: Select All
        // This is like: SELECT * FROM users
        // =====================================================================

        System.out.println("📋 STEP 3: SELECT * FROM users");
        System.out.println("   ────────────────────────────────────────────────────────────");

        usersTable.printTable();

        // =====================================================================
        // DEMO 4: Select with WHERE
        // This is like: SELECT * FROM users WHERE age > 25
        // =====================================================================

        System.out.println("📋 STEP 4: SELECT * FROM users WHERE age > 25");
        System.out.println("   ────────────────────────────────────────────────────────────");

        List<Row> olderUsers = usersTable.selectWhere("age", ">", 25);

        System.out.println("   Results:");
        for (Row row : olderUsers) {
            System.out.println("   → " + row.toFormattedString(" | "));
        }
        System.out.println("   (" + olderUsers.size() + " rows matched)\n");

        // =====================================================================
        // DEMO 5: Select with String comparison
        // This is like: SELECT * FROM users WHERE name = 'Alice'
        // =====================================================================

        System.out.println("📋 STEP 5: SELECT * FROM users WHERE name = 'Alice'");
        System.out.println("   ────────────────────────────────────────────────────────────");

        List<Row> aliceRows = usersTable.selectWhere("name", "=", "Alice");

        System.out.println("   Results:");
        for (Row row : aliceRows) {
            System.out.println("   → " + row.toFormattedString(" | "));
        }
        System.out.println("   (" + aliceRows.size() + " rows matched)\n");

        // =====================================================================
        // DEMO 6: Select active users
        // This is like: SELECT * FROM users WHERE is_active = true
        // =====================================================================

        System.out.println("📋 STEP 6: SELECT * FROM users WHERE is_active = true");
        System.out.println("   ────────────────────────────────────────────────────────────");

        List<Row> activeUsers = usersTable.selectWhere("is_active", "=", true);

        System.out.println("   Results:");
        for (Row row : activeUsers) {
            System.out.println("   → " + row.toFormattedString(" | "));
        }
        System.out.println("   (" + activeUsers.size() + " rows matched)\n");

        // =====================================================================
        // DEMO 7: Creating another table (for future JOIN demo)
        // =====================================================================

        System.out.println("📋 STEP 7: Creating an 'orders' table (for future JOIN)");
        System.out.println("   ────────────────────────────────────────────────────────────");

        List<Column> orderColumns = Arrays.asList(
                new Column("order_id", DataType.INTEGER),
                new Column("user_id", DataType.INTEGER),
                new Column("product", DataType.STRING),
                new Column("amount", DataType.INTEGER)
        );

        Table ordersTable = new Table("orders", orderColumns);

        ordersTable.insert(Arrays.asList(101, 1, "Laptop", 1200));
        ordersTable.insert(Arrays.asList(102, 1, "Mouse", 25));
        ordersTable.insert(Arrays.asList(103, 2, "Keyboard", 75));
        ordersTable.insert(Arrays.asList(104, 3, "Monitor", 300));
        ordersTable.insert(Arrays.asList(105, 4, "Headphones", 150));

        ordersTable.printTable();

        // =====================================================================
        // DEMO 8: Error Handling
        // =====================================================================

        System.out.println("📋 STEP 8: Error Handling Demo");
        System.out.println("   ────────────────────────────────────────────────────────────");

        // Try inserting wrong type
        System.out.println("   Trying to insert wrong type (string where integer expected)...");
        try {
            usersTable.insert(Arrays.asList("not-a-number", "Test", 25, true));
            System.out.println("   ❌ Should have thrown an error!");
        } catch (IllegalArgumentException e) {
            System.out.println("   ✅ Correctly caught error: " + e.getMessage());
        }

        // Try inserting wrong number of values
        System.out.println("\n   Trying to insert wrong number of values...");
        try {
            usersTable.insert(Arrays.asList(6, "Test")); // Missing age and is_active
            System.out.println("   ❌ Should have thrown an error!");
        } catch (IllegalArgumentException e) {
            System.out.println("   ✅ Correctly caught error: " + e.getMessage());
        }

        System.out.println();

        // =====================================================================
        // SUMMARY
        // =====================================================================

        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                    🎉 STEP 1 COMPLETE! 🎉                    ║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.println("║  What we built:                                              ║");
        System.out.println("║    ✅ DataType enum (INTEGER, STRING, BOOLEAN)              ║");
        System.out.println("║    ✅ Column class (name + type)                            ║");
        System.out.println("║    ✅ Row class (holds actual data)                         ║");
        System.out.println("║    ✅ Table class (combines columns + rows)                 ║");
        System.out.println("║                                                              ║");
        System.out.println("║  Operations working:                                         ║");
        System.out.println("║    ✅ INSERT (add data)                                     ║");
        System.out.println("║    ✅ SELECT * (get all rows)                               ║");
        System.out.println("║    ✅ SELECT WHERE (filter with conditions)                 ║");
        System.out.println("║                                                              ║");
        System.out.println("║  Next step: Build the SQL Parser!                           ║");
        System.out.println("║    - Parse: \"SELECT * FROM users WHERE age > 25\"           ║");
        System.out.println("║    - Convert text to commands our engine understands        ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
    }
}