package com.minidb.parser;

/**
 * =============================================================================
 * WhereClause - Represents a WHERE condition in SQL
 * =============================================================================
 *
 * WHAT IS THIS?
 * -------------
 * A WHERE clause filters rows based on a condition.
 *
 * EXAMPLES:
 * ---------
 * WHERE age > 25
 *   column: "age"
 *   operator: ">"
 *   value: 25 (Integer)
 *
 * WHERE name = 'Alice'
 *   column: "name"
 *   operator: "="
 *   value: "Alice" (String)
 *
 * WHERE is_active = true
 *   column: "is_active"
 *   operator: "="
 *   value: true (Boolean)
 *
 * STRUCTURE:
 * ----------
 *        WhereClause
 *        /    |    \
 *   column  operator  value
 *      |       |        |
 *   "age"     ">"      25
 *
 * NOTE: This is a simple implementation supporting single conditions.
 * A full implementation would support AND/OR with nested conditions.
 */
public class WhereClause {

    /** The column name to filter on */
    private final String column;

    /** The comparison operator (=, !=, >, <, >=, <=) */
    private final String operator;

    /** The value to compare against */
    private final Object value;

    /**
     * Creates a new WHERE clause condition
     *
     * @param column The column name
     * @param operator The operator (=, !=, >, <, >=, <=)
     * @param value The value to compare against
     *
     * EXAMPLES:
     *   new WhereClause("age", ">", 25)
     *   new WhereClause("name", "=", "Alice")
     *   new WhereClause("is_active", "=", true)
     */
    public WhereClause(String column, String operator, Object value) {
        this.column = column.toLowerCase();  // Normalize to lowercase
        this.operator = operator;
        this.value = value;
    }

    /**
     * Gets the column name
     * @return The column name
     */
    public String getColumn() {
        return column;
    }

    /**
     * Gets the operator
     * @return The operator string
     */
    public String getOperator() {
        return operator;
    }

    /**
     * Gets the comparison value
     * @return The value (could be Integer, String, Boolean, or null)
     */
    public Object getValue() {
        return value;
    }

    /**
     * String representation for debugging
     *
     * Example: "WHERE age > 25"
     */
    @Override
    public String toString() {
        String valueStr;
        if (value == null) {
            valueStr = "NULL";
        } else if (value instanceof String) {
            valueStr = "'" + value + "'";  // Add quotes for strings
        } else {
            valueStr = value.toString();
        }

        return "WHERE " + column + " " + operator + " " + valueStr;
    }
}