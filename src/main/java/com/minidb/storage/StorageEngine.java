package com.minidb.storage;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * =============================================================================
 * StorageEngine - Saves and loads tables to/from disk
 * =============================================================================
 *
 * WHAT DOES THIS DO?
 * ------------------
 * This class handles PERSISTENCE - making data survive program restarts.
 *
 * Two main operations:
 *   1. SAVE: Table (memory) → File (disk)
 *   2. LOAD: File (disk) → Table (memory)
 *
 * FILE FORMAT:
 * ------------
 * We use a simple text format that's easy to read and debug:
 *
 *   # MiniDB Table File
 *   TABLE:users
 *   COLUMNS:4
 *   COL:id:INTEGER
 *   COL:name:STRING
 *   COL:age:INTEGER
 *   COL:active:BOOLEAN
 *   ROWS:5
 *   ROW:1|Alice|28|true
 *   ROW:2|Bob|35|true
 *   END
 *
 * WHY TEXT FORMAT?
 * ----------------
 * - Easy to read (open in Notepad!)
 * - Easy to debug when something goes wrong
 * - Simple to implement
 * - Perfect for learning
 *
 * Production databases use BINARY format for speed and size.
 *
 * HOW SERIALIZATION WORKS:
 * ------------------------
 *
 * SAVE (Object → File):
 *   Table object          Write line by line
 *        ↓                      ↓
 *   name="users"    →    "TABLE:users"
 *   columns=[...]   →    "COL:id:INTEGER" ...
 *   rows=[...]      →    "ROW:1|Alice|28|true" ...
 *
 * LOAD (File → Object):
 *   Read lines            Parse and create objects
 *        ↓                      ↓
 *   "TABLE:users"   →    name="users"
 *   "COL:id:INT..." →    columns=[...]
 *   "ROW:1|Ali..."  →    rows=[...]
 */
public class StorageEngine {

    /** File extension for our database files */
    public static final String FILE_EXTENSION = ".minidb";

    /** Magic header to identify our files */
    private static final String MAGIC_HEADER = "# MiniDB Table File v1";

    /** Delimiter used to separate values in a row */
    private static final String VALUE_DELIMITER = "|";

    /** The directory where database files are stored */
    private final String dataDirectory;

    /**
     * Creates a StorageEngine with the specified data directory
     *
     * @param dataDirectory Path to the folder where files will be stored
     *
     * EXAMPLE:
     *   StorageEngine engine = new StorageEngine("data");
     *   // Files will be saved to: data/users.minidb, data/orders.minidb, etc.
     */
    public StorageEngine(String dataDirectory) {
        this.dataDirectory = dataDirectory;

        // Create directory if it doesn't exist
        File dir = new File(dataDirectory);
        if (!dir.exists()) {
            dir.mkdirs();
            System.out.println("📁 Created data directory: " + dataDirectory);
        }
    }

    /**
     * Default constructor - uses "data" as the directory
     */
    public StorageEngine() {
        this("data");
    }

    // =========================================================================
    // SAVE OPERATIONS
    // =========================================================================

    /**
     * Saves a table to disk
     *
     * @param table The table to save
     * @throws IOException If file cannot be written
     *
     * EXAMPLE:
     *   engine.saveTable(usersTable);
     *   // Creates file: data/users.minidb
     *
     * WHAT HAPPENS:
     *   1. Create/open file for writing
     *   2. Write header
     *   3. Write column definitions
     *   4. Write each row
     *   5. Close file
     */
    public void saveTable(Table table) throws IOException {
        String filePath = getFilePath(table.getName());

        // Use try-with-resources to ensure file is closed properly
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {

            // Write magic header
            writer.println(MAGIC_HEADER);

            // Write table name
            writer.println("TABLE:" + table.getName());

            // Write column count
            List<Column> columns = table.getColumns();
            writer.println("COLUMNS:" + columns.size());

            // Write each column definition
            for (Column col : columns) {
                writer.println("COL:" + col.getName() + ":" + col.getType().name());
            }

            // Write row count
            List<Row> rows = table.selectAll();
            writer.println("ROWS:" + rows.size());

            // Write each row
            for (Row row : rows) {
                writer.println("ROW:" + serializeRow(row));
            }

            // Write end marker
            writer.println("END");
        }

        System.out.println("💾 Saved table '" + table.getName() + "' to " + filePath);
    }

    /**
     * Converts a Row to a string for storage
     *
     * @param row The row to serialize
     * @return String representation like "1|Alice|28|true"
     *
     * EXAMPLE:
     *   Row with values [1, "Alice", 28, true]
     *   Returns: "1|Alice|28|true"
     *
     * SPECIAL HANDLING:
     *   - NULL values become "NULL"
     *   - Strings are stored as-is (no quotes needed in our format)
     */
    private String serializeRow(Row row) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < row.size(); i++) {
            if (i > 0) {
                sb.append(VALUE_DELIMITER);
            }

            Object value = row.getValue(i);

            if (value == null) {
                sb.append("NULL");
            } else {
                sb.append(value.toString());
            }
        }

        return sb.toString();
    }

    // =========================================================================
    // LOAD OPERATIONS
    // =========================================================================

    /**
     * Loads a table from disk
     *
     * @param tableName The name of the table to load
     * @return The loaded Table object
     * @throws IOException If file cannot be read or is invalid
     *
     * EXAMPLE:
     *   Table users = engine.loadTable("users");
     *   // Loads from: data/users.minidb
     *
     * WHAT HAPPENS:
     *   1. Open file for reading
     *   2. Verify magic header
     *   3. Read table name
     *   4. Read column definitions
     *   5. Read rows
     *   6. Create and return Table object
     */
    public Table loadTable(String tableName) throws IOException {
        String filePath = getFilePath(tableName);
        File file = new File(filePath);

        if (!file.exists()) {
            throw new IOException("Table file not found: " + filePath);
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {

            // Read and verify magic header
            String header = reader.readLine();
            if (!MAGIC_HEADER.equals(header)) {
                throw new IOException("Invalid file format. Expected MiniDB file.");
            }

            // Read table name
            String tableNameLine = reader.readLine();
            if (!tableNameLine.startsWith("TABLE:")) {
                throw new IOException("Expected TABLE: line");
            }
            String loadedTableName = tableNameLine.substring(6);

            // Read column count
            String columnsLine = reader.readLine();
            if (!columnsLine.startsWith("COLUMNS:")) {
                throw new IOException("Expected COLUMNS: line");
            }
            int columnCount = Integer.parseInt(columnsLine.substring(8));

            // Read column definitions
            List<Column> columns = new ArrayList<>();
            for (int i = 0; i < columnCount; i++) {
                String colLine = reader.readLine();
                if (!colLine.startsWith("COL:")) {
                    throw new IOException("Expected COL: line");
                }
                Column col = parseColumnDefinition(colLine.substring(4));
                columns.add(col);
            }

            // Create the table with loaded schema
            Table table = new Table(loadedTableName, columns);

            // Read row count
            String rowsLine = reader.readLine();
            if (!rowsLine.startsWith("ROWS:")) {
                throw new IOException("Expected ROWS: line");
            }
            int rowCount = Integer.parseInt(rowsLine.substring(5));

            // Read rows
            for (int i = 0; i < rowCount; i++) {
                String rowLine = reader.readLine();
                if (!rowLine.startsWith("ROW:")) {
                    throw new IOException("Expected ROW: line");
                }
                List<Object> values = deserializeRow(rowLine.substring(4), columns);
                table.insert(values);
            }

            // Verify end marker
            String endLine = reader.readLine();
            if (!"END".equals(endLine)) {
                throw new IOException("Expected END marker");
            }

            System.out.println("📂 Loaded table '" + loadedTableName + "' with " + rowCount + " rows");
            return table;
        }
    }

    /**
     * Parses a column definition string
     *
     * @param colDef String like "id:INTEGER"
     * @return Column object
     */
    private Column parseColumnDefinition(String colDef) {
        String[] parts = colDef.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid column definition: " + colDef);
        }

        String name = parts[0];
        DataType type = DataType.valueOf(parts[1]);

        return new Column(name, type);
    }

    /**
     * Converts a stored string back to a Row's values
     *
     * @param rowData String like "1|Alice|28|true"
     * @param columns Column definitions (to know what type each value is)
     * @return List of values [1, "Alice", 28, true]
     *
     * EXAMPLE:
     *   Input: "1|Alice|28|true"
     *   Columns: [INTEGER, STRING, INTEGER, BOOLEAN]
     *   Output: [1, "Alice", 28, true]
     */
    private List<Object> deserializeRow(String rowData, List<Column> columns) {
        String[] parts = rowData.split("\\|", -1);  // -1 keeps empty strings

        if (parts.length != columns.size()) {
            throw new IllegalArgumentException(
                    "Row has " + parts.length + " values, expected " + columns.size()
            );
        }

        List<Object> values = new ArrayList<>();

        for (int i = 0; i < parts.length; i++) {
            String valueStr = parts[i];
            DataType type = columns.get(i).getType();

            Object value = parseValue(valueStr, type);
            values.add(value);
        }

        return values;
    }

    /**
     * Parses a string value into the appropriate Java type
     *
     * @param valueStr The string representation
     * @param type The expected DataType
     * @return The parsed value (Integer, String, Boolean, or null)
     */
    private Object parseValue(String valueStr, DataType type) {
        // Handle NULL
        if ("NULL".equals(valueStr) || valueStr.isEmpty()) {
            return null;
        }

        switch (type) {
            case INTEGER:
                return Integer.parseInt(valueStr);

            case STRING:
                return valueStr;

            case BOOLEAN:
                return Boolean.parseBoolean(valueStr);

            default:
                throw new IllegalArgumentException("Unknown type: " + type);
        }
    }

    // =========================================================================
    // UTILITY METHODS
    // =========================================================================

    /**
     * Gets the full file path for a table
     *
     * @param tableName The table name
     * @return Full path like "data/users.minidb"
     */
    public String getFilePath(String tableName) {
        return dataDirectory + File.separator + tableName.toLowerCase() + FILE_EXTENSION;
    }

    /**
     * Checks if a table file exists
     *
     * @param tableName The table name
     * @return true if the file exists
     */
    public boolean tableFileExists(String tableName) {
        return new File(getFilePath(tableName)).exists();
    }

    /**
     * Lists all table files in the data directory
     *
     * @return List of table names (without extension)
     */
    public List<String> listTables() {
        List<String> tables = new ArrayList<>();

        File dir = new File(dataDirectory);
        File[] files = dir.listFiles((d, name) -> name.endsWith(FILE_EXTENSION));

        if (files != null) {
            for (File file : files) {
                String name = file.getName();
                // Remove .minidb extension
                tables.add(name.substring(0, name.length() - FILE_EXTENSION.length()));
            }
        }

        return tables;
    }

    /**
     * Deletes a table file
     *
     * @param tableName The table name
     * @return true if deleted successfully
     */
    public boolean deleteTableFile(String tableName) {
        File file = new File(getFilePath(tableName));
        if (file.exists()) {
            boolean deleted = file.delete();
            if (deleted) {
                System.out.println("🗑️ Deleted table file: " + tableName);
            }
            return deleted;
        }
        return false;
    }

    /**
     * Gets the data directory path
     */
    public String getDataDirectory() {
        return dataDirectory;
    }
}