package com.minidb.executor;

import com.minidb.parser.*;
import com.minidb.storage.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * =============================================================================
 * Executor - Runs parsed SQL statements against actual data
 * =============================================================================
 *
 * WHAT DOES THIS DO?
 * ------------------
 * The executor is the "worker" that actually performs the operations.
 * It takes a parsed Statement (AST) and executes it against our tables.
 *
 * THE COMPLETE PIPELINE:
 * ----------------------
 * User types: "SELECT * FROM users WHERE age > 25"
 *      ↓
 * Tokenizer: [SELECT, *, FROM, users, WHERE, age, >, 25, EOF]
 *      ↓
 * Parser: SelectStatement(columns=[*], table="users", where=(age > 25))
 *      ↓
 * Executor: Actually finds the users table, filters rows, returns results
 *      ↓
 * Results: List of matching rows
 *
 * WHAT THE EXECUTOR DOES:
 * -----------------------
 * For SELECT:
 *   1. Find the table by name
 *   2. Apply WHERE filter if present
 *   3. Extract only requested columns
 *   4. Return results
 *
 * For INSERT:
 *   1. Find the table by name
 *   2. Validate value count and types
 *   3. Insert the row
 *   4. Return success/failure
 */
public class Executor {

    /**
     * Map of table name → Table object
     * This acts as our simple "database catalog"
     */
    private final Map<String, Table> tables;

    /**
     * Creates a new Executor with an empty table catalog
     */
    public Executor() {
        this.tables = new HashMap<>();
    }

    // =========================================================================
    // TABLE MANAGEMENT
    // =========================================================================

    /**
     * Adds a table to the executor's catalog
     *
     * @param table The table to add
     *
     * EXAMPLE:
     *   executor.addTable(usersTable);
     */
    public void addTable(Table table) {
        tables.put(table.getName().toLowerCase(), table);
    }

    /**
     * Gets a table by name
     *
     * @param name The table name
     * @return The Table, or null if not found
     */
    public Table getTable(String name) {
        return tables.get(name.toLowerCase());
    }

    /**
     * Lists all table names
     * @return List of table names
     */
    public List<String> getTableNames() {
        return new ArrayList<>(tables.keySet());
    }

    // =========================================================================
    // MAIN EXECUTION METHOD
    // =========================================================================

    /**
     * Executes a SQL string
     *
     * @param sql The SQL to execute
     * @return ExecutionResult containing the results or error
     *
     * EXAMPLE:
     *   ExecutionResult result = executor.execute("SELECT * FROM users");
     *   if (result.isSuccess()) {
     *       for (Row row : result.getRows()) {
     *           // Process row
     *       }
     *   }
     */
    public ExecutionResult execute(String sql) {
        try {
            // Step 1: Parse the SQL
            Statement statement = Parser.parse(sql);

            // Step 2: Execute based on statement type
            return executeStatement(statement);

        } catch (Parser.ParseException e) {
            return ExecutionResult.error(e.getMessage());
        } catch (Exception e) {
            return ExecutionResult.error("Execution error: " + e.getMessage());
        }
    }

    /**
     * Executes a parsed Statement
     *
     * @param statement The statement to execute
     * @return ExecutionResult
     */
    public ExecutionResult executeStatement(Statement statement) {
        switch (statement.getType()) {
            case SELECT:
                return executeSelect((SelectStatement) statement);

            case INSERT:
                return executeInsert((InsertStatement) statement);

            default:
                return ExecutionResult.error(
                        "Unsupported statement type: " + statement.getType()
                );
        }
    }

    // =========================================================================
    // SELECT EXECUTION
    // =========================================================================

    /**
     * Executes a SELECT statement
     *
     * @param stmt The SelectStatement to execute
     * @return ExecutionResult with the rows
     */
    private ExecutionResult executeSelect(SelectStatement stmt) {
        // Step 1: Find the table
        Table table = tables.get(stmt.getTableName());
        if (table == null) {
            return ExecutionResult.error("Table not found: " + stmt.getTableName());
        }

        // Step 2: Get rows (with or without WHERE filter)
        List<Row> rows;
        if (stmt.hasWhereClause()) {
            rows = executeWhere(table, stmt.getWhereClause());
        } else {
            rows = table.selectAll();
        }

        // Step 3: Project columns (if not SELECT *)
        List<Row> projectedRows;
        List<String> resultColumns;

        if (stmt.isSelectAll()) {
            // SELECT * - return all columns
            projectedRows = rows;
            resultColumns = new ArrayList<>();
            for (Column col : table.getColumns()) {
                resultColumns.add(col.getName());
            }
        } else {
            // Specific columns - project them
            projectedRows = projectColumns(table, rows, stmt.getColumns());
            resultColumns = stmt.getColumns();
        }

        return ExecutionResult.success(projectedRows, resultColumns);
    }

    /**
     * Filters rows based on WHERE clause
     *
     * @param table The table to filter
     * @param where The WHERE clause
     * @return Filtered list of rows
     */
    private List<Row> executeWhere(Table table, WhereClause where) {
        String column = where.getColumn();
        String operator = where.getOperator();
        Object value = where.getValue();

        // Find column index
        int colIndex = table.getColumnIndex(column);
        if (colIndex < 0) {
            throw new RuntimeException("Column not found: " + column);
        }

        // Filter rows
        return table.selectWhere(column, operator, value);
    }

    /**
     * Projects (extracts) specific columns from rows
     *
     * @param table The source table (for column info)
     * @param rows The rows to project
     * @param columns The columns to extract
     * @return New rows with only the requested columns
     */
    private List<Row> projectColumns(Table table, List<Row> rows, List<String> columns) {
        // Find column indices
        List<Integer> indices = new ArrayList<>();
        for (String colName : columns) {
            int idx = table.getColumnIndex(colName);
            if (idx < 0) {
                throw new RuntimeException("Column not found: " + colName);
            }
            indices.add(idx);
        }

        // Extract values for each row
        List<Row> projectedRows = new ArrayList<>();
        for (Row row : rows) {
            List<Object> values = new ArrayList<>();
            for (int idx : indices) {
                values.add(row.getValue(idx));
            }
            projectedRows.add(new Row(values));
        }

        return projectedRows;
    }

    // =========================================================================
    // INSERT EXECUTION
    // =========================================================================

    /**
     * Executes an INSERT statement
     *
     * @param stmt The InsertStatement to execute
     * @return ExecutionResult indicating success or failure
     */
    private ExecutionResult executeInsert(InsertStatement stmt) {
        // Step 1: Find the table
        Table table = tables.get(stmt.getTableName());
        if (table == null) {
            return ExecutionResult.error("Table not found: " + stmt.getTableName());
        }

        // Step 2: Validate and insert
        try {
            table.insert(stmt.getValues());
            return ExecutionResult.success(1);  // 1 row inserted
        } catch (IllegalArgumentException e) {
            return ExecutionResult.error("Insert failed: " + e.getMessage());
        }
    }

    // =========================================================================
    // RESULT CLASS
    // =========================================================================

    /**
     * Represents the result of executing a SQL statement
     */
    public static class ExecutionResult {
        private final boolean success;
        private final String errorMessage;
        private final List<Row> rows;
        private final List<String> columns;
        private final int rowsAffected;

        private ExecutionResult(boolean success, String errorMessage,
                                List<Row> rows, List<String> columns, int rowsAffected) {
            this.success = success;
            this.errorMessage = errorMessage;
            this.rows = rows;
            this.columns = columns;
            this.rowsAffected = rowsAffected;
        }

        /** Create a success result for SELECT */
        public static ExecutionResult success(List<Row> rows, List<String> columns) {
            return new ExecutionResult(true, null, rows, columns, rows.size());
        }

        /** Create a success result for INSERT/UPDATE/DELETE */
        public static ExecutionResult success(int rowsAffected) {
            return new ExecutionResult(true, null, null, null, rowsAffected);
        }

        /** Create an error result */
        public static ExecutionResult error(String message) {
            return new ExecutionResult(false, message, null, null, 0);
        }

        public boolean isSuccess() { return success; }
        public String getErrorMessage() { return errorMessage; }
        public List<Row> getRows() { return rows; }
        public List<String> getColumns() { return columns; }
        public int getRowsAffected() { return rowsAffected; }

        /**
         * Prints results in a nice table format
         */
        public void print() {
            if (!success) {
                System.out.println("ERROR: " + errorMessage);
                return;
            }

            if (rows == null) {
                // INSERT/UPDATE/DELETE result
                System.out.println("OK, " + rowsAffected + " row(s) affected");
                return;
            }

            // SELECT result - print as table
            if (rows.isEmpty()) {
                System.out.println("(0 rows)");
                return;
            }

            // Print header
            StringBuilder header = new StringBuilder("| ");
            StringBuilder separator = new StringBuilder("+");

            for (String col : columns) {
                header.append(String.format("%-15s | ", col));
                separator.append("-----------------+");
            }

            System.out.println(separator);
            System.out.println(header);
            System.out.println(separator);

            // Print rows
            for (Row row : rows) {
                StringBuilder rowStr = new StringBuilder("| ");
                for (int i = 0; i < row.size(); i++) {
                    Object value = row.getValue(i);
                    String valStr = value == null ? "NULL" : value.toString();
                    rowStr.append(String.format("%-15s | ", valStr));
                }
                System.out.println(rowStr);
            }

            System.out.println(separator);
            System.out.println("(" + rows.size() + " rows)");
        }
    }
}