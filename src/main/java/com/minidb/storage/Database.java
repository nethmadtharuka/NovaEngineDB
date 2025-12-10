package com.minidb.storage;

import java.io.IOException;
import java.util.*;

/**
 * =============================================================================
 * Database - The central manager for all tables
 * =============================================================================
 *
 * WHAT DOES THIS DO?
 * ------------------
 * This is the "brain" of our database. It:
 *   1. Holds all tables in memory
 *   2. Coordinates saving/loading with StorageEngine
 *   3. Provides a clean interface for database operations
 *
 * USAGE EXAMPLE:
 * --------------
 *   // Create database
 *   Database db = new Database("mydb");
 *
 *   // Create a table
 *   db.createTable("users", Arrays.asList(
 *       new Column("id", DataType.INTEGER),
 *       new Column("name", DataType.STRING)
 *   ));
 *
 *   // Get table and use it
 *   Table users = db.getTable("users");
 *   users.insert(Arrays.asList(1, "Alice"));
 *
 *   // Save to disk
 *   db.save();
 *
 *   // Later... load from disk
 *   Database db2 = new Database("mydb");
 *   db2.load();
 *   // Data is back!
 *
 * LIFECYCLE:
 * ----------
 *   1. Create Database object
 *   2. Either:
 *      a. Load existing data: db.load()
 *      b. Create new tables: db.createTable(...)
 *   3. Work with data (insert, select, etc.)
 *   4. Save changes: db.save()
 *   5. (Program can exit, data is safe)
 */
public class Database {

    /** Name of this database */
    private final String name;

    /** All tables in this database */
    private final Map<String, Table> tables;

    /** Storage engine for persistence */
    private final StorageEngine storageEngine;

    /**
     * Creates a new Database
     *
     * @param name The database name (also used as directory name)
     *
     * EXAMPLE:
     *   Database db = new Database("myapp");
     *   // Data will be stored in: data/myapp/
     */
    public Database(String name) {
        this.name = name.toLowerCase();
        this.tables = new HashMap<>();
        this.storageEngine = new StorageEngine("data" + java.io.File.separator + this.name);
    }

    /**
     * Creates a Database with default name "default"
     */
    public Database() {
        this("default");
    }

    // =========================================================================
    // TABLE MANAGEMENT
    // =========================================================================

    /**
     * Creates a new table
     *
     * @param tableName Name of the table
     * @param columns List of column definitions
     * @return The created Table
     * @throws IllegalArgumentException if table already exists
     *
     * EXAMPLE:
     *   db.createTable("users", Arrays.asList(
     *       new Column("id", DataType.INTEGER),
     *       new Column("name", DataType.STRING),
     *       new Column("age", DataType.INTEGER)
     *   ));
     */
    public Table createTable(String tableName, List<Column> columns) {
        String normalizedName = tableName.toLowerCase();

        if (tables.containsKey(normalizedName)) {
            throw new IllegalArgumentException("Table already exists: " + tableName);
        }

        Table table = new Table(normalizedName, columns);
        tables.put(normalizedName, table);

        System.out.println("✅ Created table: " + normalizedName);
        return table;
    }

    /**
     * Gets a table by name
     *
     * @param tableName The table name
     * @return The Table, or null if not found
     */
    public Table getTable(String tableName) {
        return tables.get(tableName.toLowerCase());
    }

    /**
     * Checks if a table exists
     *
     * @param tableName The table name
     * @return true if table exists
     */
    public boolean hasTable(String tableName) {
        return tables.containsKey(tableName.toLowerCase());
    }

    /**
     * Gets all table names
     *
     * @return Set of table names
     */
    public Set<String> getTableNames() {
        return new HashSet<>(tables.keySet());
    }

    /**
     * Gets the number of tables
     *
     * @return Table count
     */
    public int getTableCount() {
        return tables.size();
    }

    /**
     * Drops (deletes) a table
     *
     * @param tableName The table to drop
     * @return true if table was dropped
     */
    public boolean dropTable(String tableName) {
        String normalizedName = tableName.toLowerCase();

        if (tables.remove(normalizedName) != null) {
            // Also delete the file
            storageEngine.deleteTableFile(normalizedName);
            System.out.println("🗑️ Dropped table: " + normalizedName);
            return true;
        }
        return false;
    }

    // =========================================================================
    // PERSISTENCE - SAVE & LOAD
    // =========================================================================

    /**
     * Saves ALL tables to disk
     *
     * @throws IOException If any table fails to save
     *
     * EXAMPLE:
     *   db.save();
     *   // All tables are now saved to data/dbname/*.minidb
     */
    public void save() throws IOException {
        System.out.println("\n💾 Saving database '" + name + "'...");

        for (Table table : tables.values()) {
            storageEngine.saveTable(table);
        }

        System.out.println("✅ Saved " + tables.size() + " table(s)\n");
    }

    /**
     * Saves a specific table to disk
     *
     * @param tableName The table to save
     * @throws IOException If save fails
     */
    public void saveTable(String tableName) throws IOException {
        Table table = getTable(tableName);
        if (table == null) {
            throw new IllegalArgumentException("Table not found: " + tableName);
        }
        storageEngine.saveTable(table);
    }

    /**
     * Loads ALL tables from disk
     *
     * @throws IOException If loading fails
     *
     * EXAMPLE:
     *   Database db = new Database("myapp");
     *   db.load();  // Loads all tables from data/myapp/
     */
    public void load() throws IOException {
        System.out.println("\n📂 Loading database '" + name + "'...");

        List<String> tableFiles = storageEngine.listTables();

        if (tableFiles.isEmpty()) {
            System.out.println("📭 No existing tables found. Starting fresh.");
            return;
        }

        for (String tableName : tableFiles) {
            try {
                Table table = storageEngine.loadTable(tableName);
                tables.put(tableName.toLowerCase(), table);
            } catch (IOException e) {
                System.err.println("⚠️ Failed to load table '" + tableName + "': " + e.getMessage());
            }
        }

        System.out.println("✅ Loaded " + tables.size() + " table(s)\n");
    }

    /**
     * Loads a specific table from disk
     *
     * @param tableName The table to load
     * @throws IOException If loading fails
     */
    public void loadTable(String tableName) throws IOException {
        Table table = storageEngine.loadTable(tableName);
        tables.put(tableName.toLowerCase(), table);
    }

    // =========================================================================
    // UTILITY METHODS
    // =========================================================================

    /**
     * Gets the database name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the storage engine
     */
    public StorageEngine getStorageEngine() {
        return storageEngine;
    }

    /**
     * Prints information about all tables
     */
    public void printInfo() {
        System.out.println("╔═══════════════════════════════════════════════════════════════╗");
        System.out.println("║  Database: " + String.format("%-50s", name) + "║");
        System.out.println("╠═══════════════════════════════════════════════════════════════╣");

        if (tables.isEmpty()) {
            System.out.println("║  (No tables)                                                  ║");
        } else {
            for (Table table : tables.values()) {
                String info = String.format("  %-20s %d columns, %d rows",
                        table.getName(),
                        table.getColumnCount(),
                        table.getRowCount());
                System.out.println("║" + String.format("%-63s", info) + "║");
            }
        }

        System.out.println("╚═══════════════════════════════════════════════════════════════╝");
    }

    /**
     * Clears all tables from memory (does NOT delete files)
     */
    public void clear() {
        tables.clear();
        System.out.println("🧹 Cleared all tables from memory");
    }

    @Override
    public String toString() {
        return "Database{name='" + name + "', tables=" + tables.size() + "}";
    }
}