package com.minidb;

import com.minidb.storage.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Step 3 Demo - File Storage in Action!
 */
public class Step3Demo {

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║          MiniDB - Step 3: File Storage Demo                  ║");
        System.out.println("║       Data now persists to disk!                             ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝\n");

        try {
            // ═══════════════════════════════════════════════════════════════
            // PART 1: Create database and add data
            // ═══════════════════════════════════════════════════════════════
            System.out.println("═══════════════════════════════════════════════════════════════");
            System.out.println("PART 1: Creating database and adding data");
            System.out.println("═══════════════════════════════════════════════════════════════\n");

            // Create a new database
            Database db = new Database("demo");
            System.out.println("📁 Created database: " + db.getName());

            // Create users table
            Table users = db.createTable("users", Arrays.asList(
                    new Column("id", DataType.INTEGER),
                    new Column("name", DataType.STRING),
                    new Column("age", DataType.INTEGER),
                    new Column("active", DataType.BOOLEAN)
            ));

            // Create orders table
            Table orders = db.createTable("orders", Arrays.asList(
                    new Column("order_id", DataType.INTEGER),
                    new Column("user_id", DataType.INTEGER),
                    new Column("product", DataType.STRING),
                    new Column("amount", DataType.INTEGER)
            ));

            // Insert data into users
            System.out.println("\n📝 Inserting data into 'users' table...");
            users.insert(Arrays.asList(1, "Alice", 28, true));
            users.insert(Arrays.asList(2, "Bob", 35, true));
            users.insert(Arrays.asList(3, "Charlie", 22, false));
            users.insert(Arrays.asList(4, "Diana", 45, true));
            users.insert(Arrays.asList(5, "Eve", 31, false));
            System.out.println("   Inserted 5 rows");

            // Insert data into orders
            System.out.println("\n📝 Inserting data into 'orders' table...");
            orders.insert(Arrays.asList(101, 1, "Laptop", 1200));
            orders.insert(Arrays.asList(102, 1, "Mouse", 25));
            orders.insert(Arrays.asList(103, 2, "Keyboard", 75));
            orders.insert(Arrays.asList(104, 3, "Monitor", 350));
            orders.insert(Arrays.asList(105, 4, "Headphones", 150));
            System.out.println("   Inserted 5 rows");

            // Show database info
            System.out.println("\n📊 Current database state:");
            db.printInfo();

            // ═══════════════════════════════════════════════════════════════
            // PART 2: Save to disk
            // ═══════════════════════════════════════════════════════════════
            System.out.println("\n═══════════════════════════════════════════════════════════════");
            System.out.println("PART 2: Saving to disk");
            System.out.println("═══════════════════════════════════════════════════════════════");

            db.save();

            System.out.println("📄 Check the files created:");
            System.out.println("   " + db.getStorageEngine().getFilePath("users"));
            System.out.println("   " + db.getStorageEngine().getFilePath("orders"));
            System.out.println("\n   You can open these files in Notepad to see the data!");

            // ═══════════════════════════════════════════════════════════════
            // PART 3: Simulate program restart
            // ═══════════════════════════════════════════════════════════════
            System.out.println("\n═══════════════════════════════════════════════════════════════");
            System.out.println("PART 3: Simulating program restart");
            System.out.println("═══════════════════════════════════════════════════════════════\n");

            System.out.println("🔄 Clearing all data from memory...");
            db.clear();

            System.out.println("📊 Database after clearing (empty):");
            db.printInfo();

            System.out.println("\n📂 Loading data back from files...");
            db.load();

            System.out.println("📊 Database after loading (data is back!):");
            db.printInfo();

            // ═══════════════════════════════════════════════════════════════
            // PART 4: Verify data integrity
            // ═══════════════════════════════════════════════════════════════
            System.out.println("\n═══════════════════════════════════════════════════════════════");
            System.out.println("PART 4: Verifying data integrity");
            System.out.println("═══════════════════════════════════════════════════════════════\n");

            // Get tables again
            Table loadedUsers = db.getTable("users");
            Table loadedOrders = db.getTable("orders");

            System.out.println("📋 Users table after reload:");
            loadedUsers.printTable();

            System.out.println("📋 Orders table after reload:");
            loadedOrders.printTable();

            // Test queries still work
            System.out.println("📝 Testing query: SELECT * FROM users WHERE age > 30");
            List<Row> results = loadedUsers.selectWhere("age", ">", 30);
            System.out.println("   Results: " + results.size() + " rows");
            for (Row row : results) {
                System.out.println("   → " + row.toFormattedString(" | "));
            }

            // ═══════════════════════════════════════════════════════════════
            // PART 5: Show file format
            // ═══════════════════════════════════════════════════════════════
            System.out.println("\n═══════════════════════════════════════════════════════════════");
            System.out.println("PART 5: What the file looks like");
            System.out.println("═══════════════════════════════════════════════════════════════\n");

            System.out.println("📄 Contents of users.minidb:");
            System.out.println("┌─────────────────────────────────────────────────────────────┐");
            System.out.println("│ # MiniDB Table File v1                                      │");
            System.out.println("│ TABLE:users                                                 │");
            System.out.println("│ COLUMNS:4                                                   │");
            System.out.println("│ COL:id:INTEGER                                              │");
            System.out.println("│ COL:name:STRING                                             │");
            System.out.println("│ COL:age:INTEGER                                             │");
            System.out.println("│ COL:active:BOOLEAN                                          │");
            System.out.println("│ ROWS:5                                                      │");
            System.out.println("│ ROW:1|Alice|28|true                                         │");
            System.out.println("│ ROW:2|Bob|35|true                                           │");
            System.out.println("│ ...                                                         │");
            System.out.println("│ END                                                         │");
            System.out.println("└─────────────────────────────────────────────────────────────┘");

            System.out.println("\n✅ SUCCESS! Data persists to disk and survives restarts!");

        } catch (IOException e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                   🎉 STEP 3 COMPLETE! 🎉                     ║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.println("║  What we built:                                              ║");
        System.out.println("║    ✅ StorageEngine - Saves/loads tables to files           ║");
        System.out.println("║    ✅ Database - Manages multiple tables                    ║");
        System.out.println("║    ✅ Text-based file format (.minidb files)                ║");
        System.out.println("║                                                              ║");
        System.out.println("║  Next step: B-Tree Index (fast searches!)                   ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
    }
}

//now the step 3 is done
//almost done all the things,left small amount