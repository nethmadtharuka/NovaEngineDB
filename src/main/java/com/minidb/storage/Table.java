package com.minidb.storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * =============================================================================
 * Table - The main data structure that holds everything together
 * =============================================================================
 *
 * CONCEPT EXPLANATION:
 * --------------------
 * A Table is the fundamental structure in a relational database.
 * It combines:
 *   1. A name (like "users", "orders", "products")
 *   2. A schema (list of Column definitions)
 *   3. Data (list of Row objects)
 *
 * VISUAL REPRESENTATION:
 * ----------------------
 *   Table name: "users"
 *
 *   Schema (Columns):
 *   ┌─────────────┬─────────────┬─────────────┐
 *   │ id:INTEGER  │ name:STRING │ age:INTEGER │
 *   └─────────────┴─────────────┴─────────────┘
 *
 *   Data (Rows):
 *   ┌────┬─────────┬─────┐
 *   │ 1  │ Alice   │ 25  │ ← Row 0
 *   │ 2  │ Bob     │ 30  │ ← Row 1
 *   │ 3  │ Charlie │ 35  │ ← Row 2
 *   └────┴─────────┴─────┘
 *
 * KEY OPERATIONS:
 * ---------------
 * 1. INSERT - Add new rows
 * 2. SELECT - Get rows (all or filtered)
 * 3. UPDATE - Modify existing rows (coming later)
 * 4. DELETE - Remove rows (coming later)
 */
public class Table {

    /**
     * The name of this table
     * Example: "users", "orders", "products"
     */
    private final String name;

    /**
     * The columns (schema) of this table
     * This defines the STRUCTURE: what columns exist and their types
     */
    private final List<Column> columns;

    /**
     * Map from column name to column index for fast lookup
     * Example: {"id" → 0, "name" → 1, "age" → 2}
     *
     * WHY? When we run "SELECT name FROM users", we need to quickly
     * find which index "name" corresponds to (index 1 in this case)
     */
    private final Map<String, Integer> columnIndexMap;

    /**
     * The actual data - a list of rows
     * Each Row contains values in the same order as columns
     */
    private final List<Row> rows;

    /**
     * Creates a new empty Table with the given structure
     *
     * @param name The table name
     * @param columns The list of column definitions
     *
     * EXAMPLE USAGE:
     *   List<Column> cols = Arrays.asList(
     *       new Column("id", DataType.INTEGER),
     *       new Column("name", DataType.STRING),
     *       new Column("age", DataType.INTEGER)
     *   );
     *   Table users = new Table("users", cols);
     */
    public Table(String name, List<Column> columns) {
        // Validate inputs
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Table name cannot be null or empty");
        }
        if (columns == null || columns.isEmpty()) {
            throw new IllegalArgumentException("Table must have at least one column");
        }

        this.name = name.toLowerCase().trim();
        this.columns = new ArrayList<>(columns); // Defensive copy
        this.rows = new ArrayList<>();

        // Build the column index map for fast lookups
        // This is like creating an index in your mind:
        // "id is column 0, name is column 1, age is column 2"
        this.columnIndexMap = new HashMap<>();
        for (int i = 0; i < columns.size(); i++) {
            String colName = columns.get(i).getName();
            if (columnIndexMap.containsKey(colName)) {
                throw new IllegalArgumentException(
                        "Duplicate column name: " + colName
                );
            }
            columnIndexMap.put(colName, i);
        }
    }

    // =========================================================================
    // GETTER METHODS
    // =========================================================================

    public String getName() {
        return name;
    }

    public List<Column> getColumns() {
        return new ArrayList<>(columns); // Return copy
    }

    public int getColumnCount() {
        return columns.size();
    }

    public int getRowCount() {
        return rows.size();
    }

    /**
     * Gets the index of a column by name
     *
     * @param columnName The name of the column
     * @return The index, or -1 if not found
     *
     * EXAMPLE:
     *   table.getColumnIndex("name")  → 1
     *   table.getColumnIndex("xyz")   → -1
     */
    public int getColumnIndex(String columnName) {
        Integer index = columnIndexMap.get(columnName.toLowerCase());
        return index != null ? index : -1;
    }

    /**
     * Gets a column by name
     *
     * @param columnName The name of the column
     * @return The Column object, or null if not found
     */
    public Column getColumn(String columnName) {
        int index = getColumnIndex(columnName);
        return index >= 0 ? columns.get(index) : null;
    }

    /**
     * Checks if a column exists
     *
     * @param columnName The name to check
     * @return true if the column exists
     */
    public boolean hasColumn(String columnName) {
        return columnIndexMap.containsKey(columnName.toLowerCase());
    }

    // =========================================================================
    // INSERT OPERATION
    // This is like: INSERT INTO users VALUES (1, 'Alice', 25)
    // =========================================================================

    /**
     * Inserts a new row into the table
     *
     * @param values The values to insert (must match column order)
     * @throws IllegalArgumentException if values don't match schema
     *
     * EXAMPLE:
     *   table.insert(Arrays.asList(1, "Alice", 25));
     *
     * WHAT HAPPENS INSIDE:
     *   1. Check that number of values matches number of columns
     *   2. Validate each value's type matches the column's type
     *   3. Create a new Row and add it to our list
     */
    public void insert(List<Object> values) {
        // Step 1: Check value count
        if (values.size() != columns.size()) {
            throw new IllegalArgumentException(
                    "Expected " + columns.size() + " values, got " + values.size() +
                            ". Columns: " + getColumnNames()
            );
        }

        // Step 2: Validate each value's type
        for (int i = 0; i < values.size(); i++) {
            Object value = values.get(i);
            Column column = columns.get(i);

            if (!column.isValidValue(value)) {
                throw new IllegalArgumentException(
                        "Invalid value for column '" + column.getName() + "': " +
                                "expected " + column.getType() + ", got " +
                                (value == null ? "null" : value.getClass().getSimpleName())
                );
            }
        }

        // Step 3: Create and add the row
        Row newRow = new Row(values);
        rows.add(newRow);
    }

    // =========================================================================
    // SELECT OPERATIONS
    // These are like: SELECT * FROM users
    //            or: SELECT * FROM users WHERE age > 25
    // =========================================================================

    /**
     * Selects ALL rows from the table
     * This is like: SELECT * FROM tablename
     *
     * @return A copy of all rows
     */
    public List<Row> selectAll() {
        return new ArrayList<>(rows); // Return copy
    }

    /**
     * Selects rows that match a condition
     * This is like: SELECT * FROM users WHERE <condition>
     *
     * @param condition A function that returns true for rows to include
     * @return List of matching rows
     *
     * EXAMPLE:
     *   // Select rows where age > 25
     *   int ageIndex = table.getColumnIndex("age");
     *   List<Row> results = table.selectWhere(row -> {
     *       Integer age = (Integer) row.getValue(ageIndex);
     *       return age != null && age > 25;
     *   });
     *
     * HOW IT WORKS:
     *   We use Java's Predicate interface (functional programming!)
     *   - Predicate<Row> is a function that takes a Row and returns boolean
     *   - We check each row against this function
     *   - If it returns true, we include that row in results
     */
    public List<Row> selectWhere(Predicate<Row> condition) {
        List<Row> results = new ArrayList<>();

        for (Row row : rows) {
            if (condition.test(row)) {
                results.add(row);
            }
        }

        return results;
    }

    /**
     * A simpler way to select with a WHERE condition on one column
     *
     * @param columnName The column to check
     * @param operator The comparison operator (=, >, <, >=, <=, !=)
     * @param value The value to compare against
     * @return List of matching rows
     *
     * EXAMPLE:
     *   table.selectWhere("age", ">", 25)
     *   table.selectWhere("name", "=", "Alice")
     */
    public List<Row> selectWhere(String columnName, String operator, Object value) {
        int colIndex = getColumnIndex(columnName);
        if (colIndex < 0) {
            throw new IllegalArgumentException("Column not found: " + columnName);
        }

        return selectWhere(row -> compareValues(row.getValue(colIndex), operator, value));
    }

    /**
     * Compares two values using the given operator
     *
     * This is the heart of WHERE clause evaluation!
     *
     * @param rowValue The value from the row
     * @param operator The comparison operator
     * @param compareValue The value to compare against
     * @return true if the comparison is satisfied
     */
    @SuppressWarnings("unchecked")
    private boolean compareValues(Object rowValue, String operator, Object compareValue) {
        // Handle NULL values
        if (rowValue == null) {
            return false; // NULL comparisons are always false in SQL
        }

        switch (operator) {
            case "=":
                return rowValue.equals(compareValue);

            case "!=":
            case "<>":
                return !rowValue.equals(compareValue);

            case ">":
            case "<":
            case ">=":
            case "<=":
                // For numeric comparisons, both values must be comparable
                if (rowValue instanceof Comparable && compareValue instanceof Comparable) {
                    Comparable<Object> c1 = (Comparable<Object>) rowValue;
                    int comparison = c1.compareTo(compareValue);

                    switch (operator) {
                        case ">":  return comparison > 0;
                        case "<":  return comparison < 0;
                        case ">=": return comparison >= 0;
                        case "<=": return comparison <= 0;
                    }
                }
                throw new IllegalArgumentException(
                        "Cannot compare " + rowValue.getClass().getSimpleName() +
                                " with " + compareValue.getClass().getSimpleName()
                );

            default:
                throw new IllegalArgumentException("Unknown operator: " + operator);
        }
    }

    // =========================================================================
    // HELPER METHODS
    // =========================================================================

    /**
     * Gets all column names as a list
     */
    private List<String> getColumnNames() {
        List<String> names = new ArrayList<>();
        for (Column col : columns) {
            names.add(col.getName());
        }
        return names;
    }

    /**
     * Prints the table in a nice format (for debugging)
     */
    public void printTable() {
        // Print header
        StringBuilder header = new StringBuilder("| ");
        StringBuilder separator = new StringBuilder("+");

        for (Column col : columns) {
            String name = col.getName();
            header.append(String.format("%-15s | ", name));
            separator.append("-----------------+");
        }

        System.out.println("\nTable: " + name);
        System.out.println(separator);
        System.out.println(header);
        System.out.println(separator);

        // Print rows
        for (Row row : rows) {
            StringBuilder rowStr = new StringBuilder("| ");
            for (int i = 0; i < columns.size(); i++) {
                Object value = row.getValue(i);
                String valStr = value == null ? "NULL" : value.toString();
                rowStr.append(String.format("%-15s | ", valStr));
            }
            System.out.println(rowStr);
        }

        System.out.println(separator);
        System.out.println("(" + rows.size() + " rows)\n");
    }

    @Override
    public String toString() {
        return "Table{name='" + name + "', columns=" + columns.size() +
                ", rows=" + rows.size() + "}";
    }
}