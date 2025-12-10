package com.minidb.parser;

import java.util.ArrayList;
import java.util.List;

/**
 * =============================================================================
 * InsertStatement - Represents a parsed INSERT statement
 * =============================================================================
 *
 * WHAT IS THIS?
 * -------------
 * After parsing "INSERT INTO users VALUES (1, 'Alice', 25, true)",
 * we create an InsertStatement object that holds:
 *   - What table to insert into (users)
 *   - What values to insert (1, 'Alice', 25, true)
 *
 * SQL SYNTAX SUPPORTED:
 * ---------------------
 * INSERT INTO table_name VALUES (value1, value2, ...)
 *
 * FUTURE EXTENSION:
 * INSERT INTO table_name (col1, col2) VALUES (val1, val2)
 * (Column specification - not implemented yet)
 *
 * AST STRUCTURE:
 * --------------
 *           InsertStatement
 *           /            \
 *       table           values
 *         |               |
 *      "users"    [1, "Alice", 25, true]
 */
public class InsertStatement extends Statement {

    /**
     * The table name to insert into
     */
    private final String tableName;

    /**
     * The values to insert
     *
     * These are already converted to their proper Java types:
     *   - Numbers become Integer
     *   - Quoted strings become String
     *   - true/false become Boolean
     *   - NULL becomes null
     */
    private final List<Object> values;

    /**
     * Creates a new InsertStatement
     *
     * @param tableName The table to insert into
     * @param values The values to insert (in column order)
     *
     * EXAMPLE:
     *   // INSERT INTO users VALUES (1, 'Alice', 25, true)
     *   new InsertStatement("users", Arrays.asList(1, "Alice", 25, true))
     */
    public InsertStatement(String tableName, List<Object> values) {
        this.tableName = tableName.toLowerCase();
        this.values = new ArrayList<>(values);  // Defensive copy
    }

    @Override
    public Type getType() {
        return Type.INSERT;
    }

    /**
     * Gets the table name
     * @return The table name
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * Gets the values to insert
     * @return List of values
     */
    public List<Object> getValues() {
        return new ArrayList<>(values);  // Return copy
    }

    /**
     * Gets the number of values
     * @return Count of values
     */
    public int getValueCount() {
        return values.size();
    }

    /**
     * String representation for debugging
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("INSERT INTO ");
        sb.append(tableName);
        sb.append(" VALUES (");

        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }

            Object value = values.get(i);
            if (value == null) {
                sb.append("NULL");
            } else if (value instanceof String) {
                sb.append("'").append(value).append("'");
            } else {
                sb.append(value);
            }
        }

        sb.append(")");
        return sb.toString();
    }
}