package com.minidb;

import com.minidb.storage.Column;
import com.minidb.storage.DataType;
import com.minidb.storage.Row;
import com.minidb.storage.Table;

import java.util.Arrays;
import java.util.List;

/**
 * MiniDB - Main entry point for Step 1 Demo
 */
public class MiniDB {

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║              Welcome to MiniDB - Step 1 Demo                 ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝\n");

        // Create users table
        List<Column> userColumns = Arrays.asList(
                new Column("id", DataType.INTEGER),
                new Column("name", DataType.STRING),
                new Column("age", DataType.INTEGER),
                new Column("is_active", DataType.BOOLEAN)
        );

        Table usersTable = new Table("users", userColumns);
        System.out.println("✅ Table created: " + usersTable);

        // Insert data
        usersTable.insert(Arrays.asList(1, "Alice", 25, true));
        usersTable.insert(Arrays.asList(2, "Bob", 30, true));
        usersTable.insert(Arrays.asList(3, "Charlie", 35, false));
        System.out.println("✅ Inserted 3 rows\n");

        // Select all
        System.out.println("📋 SELECT * FROM users:");
        usersTable.printTable();

        // Select with WHERE
        System.out.println("📋 SELECT * FROM users WHERE age > 25:");
        List<Row> results = usersTable.selectWhere("age", ">", 25);
        for (Row row : results) {
            System.out.println("   → " + row.toFormattedString(" | "));
        }

        System.out.println("\n✅ Step 1 Complete! Run Step2Demo for SQL parsing.");
    }
}