package com.minidb.parser;

import java.util.ArrayList;
import java.util.List;

/**
 * =============================================================================
 * SelectStatement - Represents a parsed SELECT query
 * =============================================================================
 *
 * WHAT IS THIS?
 * -------------
 * After parsing "SELECT name, age FROM users WHERE age > 25",
 * we create a SelectStatement object that holds all the information:
 *   - What columns to select (name, age)
 *   - What table to select from (users)
 *   - What filter to apply (age > 25)
 *
 * SQL SYNTAX SUPPORTED:
 * ---------------------
 * SELECT * FROM table_name
 * SELECT column1, column2 FROM table_name
 * SELECT * FROM table_name WHERE column operator value
 * SELECT column1 FROM table_name WHERE column operator value
 *
 * AST STRUCTURE:
 * --------------
 *              SelectStatement
 *              /      |      \
 *         columns   table   whereClause
 *            |        |          |
 *    ["name","age"] "users"   WhereClause
 *                             /    |    \
 *                         "age"   ">"   25
 *
 * WHY STORE IT THIS WAY?
 * ----------------------
 * The executor can easily:
 *   1. Find the table by name
 *   2. Apply the WHERE filter
 *   3. Return only the requested columns
 */
public class SelectStatement extends Statement {

    /**
     * The columns to select
     *
     * If selecting all columns (SELECT *), this list contains just "*"
     * Otherwise, it contains the column names: ["name", "age"]
     */
    private final List<String> columns;

    /**
     * The table name to select from
     */
    private final String tableName;

    /**
     * The WHERE clause (optional - can be null)
     *
     * NULL means: SELECT without filtering (return all rows)
     */
    private final WhereClause whereClause;

    /**
     * Creates a new SelectStatement
     *
     * @param columns List of column names (or ["*"] for all)
     * @param tableName The table to select from
     * @param whereClause The WHERE condition (or null if none)
     *
     * EXAMPLES:
     *   // SELECT * FROM users
     *   new SelectStatement(Arrays.asList("*"), "users", null)
     *
     *   // SELECT name, age FROM users WHERE age > 25
     *   new SelectStatement(
     *       Arrays.asList("name", "age"),
     *       "users",
     *       new WhereClause("age", ">", 25)
     *   )
     */
    public SelectStatement(List<String> columns, String tableName, WhereClause whereClause) {
        // Defensive copy and normalize
        this.columns = new ArrayList<>();
        for (String col : columns) {
            this.columns.add(col.toLowerCase());
        }
        this.tableName = tableName.toLowerCase();
        this.whereClause = whereClause;
    }

    /**
     * Creates a SelectStatement without WHERE clause
     */
    public SelectStatement(List<String> columns, String tableName) {
        this(columns, tableName, null);
    }

    @Override
    public Type getType() {
        return Type.SELECT;
    }

    /**
     * Gets the list of columns to select
     * @return Column names, or ["*"] if selecting all
     */
    public List<String> getColumns() {
        return new ArrayList<>(columns);  // Return copy
    }

    /**
     * Gets the table name
     * @return The table name
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * Gets the WHERE clause
     * @return The WhereClause, or null if none
     */
    public WhereClause getWhereClause() {
        return whereClause;
    }

    /**
     * Checks if this is a SELECT * (all columns)
     * @return true if selecting all columns
     */
    public boolean isSelectAll() {
        return columns.size() == 1 && columns.get(0).equals("*");
    }

    /**
     * Checks if this SELECT has a WHERE clause
     * @return true if there's a WHERE condition
     */
    public boolean hasWhereClause() {
        return whereClause != null;
    }

    /**
     * String representation for debugging
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("SELECT ");

        // Columns
        sb.append(String.join(", ", columns));

        // Table
        sb.append(" FROM ").append(tableName);

        // WHERE (if present)
        if (whereClause != null) {
            sb.append(" ").append(whereClause.toString());
        }

        return sb.toString();
    }
}