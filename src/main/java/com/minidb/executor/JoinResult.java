package com.minidb.executor;

import com.minidb.storage.Column;
import com.minidb.storage.DataType;
import com.minidb.storage.Row;

import java.util.ArrayList;
import java.util.List;

/**
 * =============================================================================
 * JoinResult - Holds the result of a JOIN operation
 * =============================================================================
 *
 * WHAT IS THIS?
 * -------------
 * When we JOIN two tables, we create a NEW result set that combines
 * columns from both tables. This class holds that combined result.
 *
 * EXAMPLE:
 * --------
 * USERS:                    ORDERS:
 * +----+-------+            +----------+---------+---------+
 * | id | name  |            | order_id | user_id | product |
 * +----+-------+            +----------+---------+---------+
 * | 1  | Alice |            | 101      | 1       | Laptop  |
 * +----+-------+            +----------+---------+---------+
 *
 * JOIN RESULT:
 * +----+-------+----------+---------+---------+
 * | id | name  | order_id | user_id | product |
 * +----+-------+----------+---------+---------+
 * | 1  | Alice | 101      | 1       | Laptop  |
 * +----+-------+----------+---------+---------+
 *
 * The JoinResult contains:
 *   - Combined columns from both tables
 *   - Combined rows (matched data)
 */
public class JoinResult {

    /** Column definitions for the joined result */
    private final List<Column> columns;

    /**
     * Column names with table prefixes for disambiguation
     * e.g., "users.id", "orders.id"
     */
    private final List<String> qualifiedColumnNames;

    /** The result rows */
    private final List<Row> rows;

    /** Name of the left table */
    private final String leftTableName;

    /** Name of the right table */
    private final String rightTableName;

    /**
     * Creates a new JoinResult
     *
     * @param leftTableName Name of the left table
     * @param rightTableName Name of the right table
     */
    public JoinResult(String leftTableName, String rightTableName) {
        this.leftTableName = leftTableName;
        this.rightTableName = rightTableName;
        this.columns = new ArrayList<>();
        this.qualifiedColumnNames = new ArrayList<>();
        this.rows = new ArrayList<>();
    }

    /**
     * Adds columns from a table to the result schema
     *
     * @param tableName The table name (for prefixing)
     * @param tableColumns The columns to add
     */
    public void addColumns(String tableName, List<Column> tableColumns) {
        for (Column col : tableColumns) {
            columns.add(col);
            // Create qualified name: "tablename.columnname"
            qualifiedColumnNames.add(tableName + "." + col.getName());
        }
    }

    /**
     * Adds a joined row to the result
     *
     * @param leftRow Row from left table
     * @param rightRow Row from right table
     */
    public void addJoinedRow(Row leftRow, Row rightRow) {
        List<Object> combinedValues = new ArrayList<>();

        // Add all values from left row
        combinedValues.addAll(leftRow.getValues());

        // Add all values from right row
        combinedValues.addAll(rightRow.getValues());

        rows.add(new Row(combinedValues));
    }

    /**
     * Adds a row with NULLs for the right side (used in LEFT JOIN)
     *
     * @param leftRow Row from left table
     * @param rightColumnCount Number of columns in right table
     */
    public void addLeftJoinRow(Row leftRow, int rightColumnCount) {
        List<Object> combinedValues = new ArrayList<>();

        // Add all values from left row
        combinedValues.addAll(leftRow.getValues());

        // Add NULLs for right side
        for (int i = 0; i < rightColumnCount; i++) {
            combinedValues.add(null);
        }

        rows.add(new Row(combinedValues));
    }

    /**
     * Adds a row with NULLs for the left side (used in RIGHT JOIN)
     *
     * @param rightRow Row from right table
     * @param leftColumnCount Number of columns in left table
     */
    public void addRightJoinRow(Row rightRow, int leftColumnCount) {
        List<Object> combinedValues = new ArrayList<>();

        // Add NULLs for left side
        for (int i = 0; i < leftColumnCount; i++) {
            combinedValues.add(null);
        }

        // Add all values from right row
        combinedValues.addAll(rightRow.getValues());

        rows.add(new Row(combinedValues));
    }

    // =========================================================================
    // GETTERS
    // =========================================================================

    public List<Column> getColumns() {
        return columns;
    }

    public List<String> getQualifiedColumnNames() {
        return qualifiedColumnNames;
    }

    public List<Row> getRows() {
        return rows;
    }

    public int getRowCount() {
        return rows.size();
    }

    public int getColumnCount() {
        return columns.size();
    }

    public String getLeftTableName() {
        return leftTableName;
    }

    public String getRightTableName() {
        return rightTableName;
    }

    /**
     * Gets column index by qualified name (e.g., "users.id")
     */
    public int getColumnIndex(String qualifiedName) {
        return qualifiedColumnNames.indexOf(qualifiedName.toLowerCase());
    }

    /**
     * Gets column index by simple name (returns first match)
     */
    public int getColumnIndexByName(String columnName) {
        String lowerName = columnName.toLowerCase();
        for (int i = 0; i < qualifiedColumnNames.size(); i++) {
            if (qualifiedColumnNames.get(i).endsWith("." + lowerName)) {
                return i;
            }
        }
        return -1;
    }

    // =========================================================================
    // DISPLAY
    // =========================================================================

    /**
     * Prints the join result as a formatted table
     */
    public void print() {
        if (rows.isEmpty()) {
            System.out.println("(0 rows)");
            return;
        }

        // Calculate column widths
        int[] widths = new int[columns.size()];
        for (int i = 0; i < columns.size(); i++) {
            widths[i] = Math.max(qualifiedColumnNames.get(i).length(), 10);
        }

        // Check data widths
        for (Row row : rows) {
            for (int i = 0; i < row.size(); i++) {
                Object val = row.getValue(i);
                String valStr = val == null ? "NULL" : val.toString();
                widths[i] = Math.max(widths[i], valStr.length());
            }
        }

        // Build separator
        StringBuilder separator = new StringBuilder("+");
        for (int width : widths) {
            separator.append("-".repeat(width + 2)).append("+");
        }

        // Print header
        System.out.println(separator);
        StringBuilder header = new StringBuilder("|");
        for (int i = 0; i < columns.size(); i++) {
            header.append(String.format(" %-" + widths[i] + "s |", qualifiedColumnNames.get(i)));
        }
        System.out.println(header);
        System.out.println(separator);

        // Print rows
        for (Row row : rows) {
            StringBuilder rowStr = new StringBuilder("|");
            for (int i = 0; i < row.size(); i++) {
                Object val = row.getValue(i);
                String valStr = val == null ? "NULL" : val.toString();
                rowStr.append(String.format(" %-" + widths[i] + "s |", valStr));
            }
            System.out.println(rowStr);
        }

        System.out.println(separator);
        System.out.println("(" + rows.size() + " rows)");
    }

    @Override
    public String toString() {
        return "JoinResult{" + leftTableName + " ⋈ " + rightTableName +
                ", columns=" + columns.size() + ", rows=" + rows.size() + "}";
    }
}