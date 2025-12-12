package com.minidb.executor;

import com.minidb.storage.Column;
import com.minidb.storage.Row;
import com.minidb.storage.Table;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * =============================================================================
 * JoinExecutor - Executes JOIN operations between tables
 * =============================================================================
 *
 * WHAT DOES THIS DO?
 * ------------------
 * This class combines data from two tables based on a join condition.
 * It implements the Nested Loop Join algorithm.
 *
 * SUPPORTED JOIN TYPES:
 * ---------------------
 * 1. INNER JOIN - Returns only matching rows
 * 2. LEFT JOIN  - Returns all left rows + matching right (NULL if no match)
 * 3. RIGHT JOIN - Returns all right rows + matching left (NULL if no match)
 * 4. CROSS JOIN - Returns all combinations (Cartesian product)
 *
 * NESTED LOOP ALGORITHM:
 * ----------------------
 * for each row in leftTable:
 *     for each row in rightTable:
 *         if joinCondition(leftRow, rightRow) is true:
 *             add combined row to result
 *
 * Time Complexity: O(n × m) where n and m are table sizes
 *
 * EXAMPLE:
 * --------
 * SELECT * FROM users JOIN orders ON users.id = orders.user_id
 *
 * users:  [(1, Alice), (2, Bob)]
 * orders: [(101, 1, Laptop), (102, 2, Mouse)]
 *
 * Result: [(1, Alice, 101, 1, Laptop), (2, Bob, 102, 2, Mouse)]
 */
public class JoinExecutor {

    /**
     * Performs an INNER JOIN between two tables
     *
     * @param leftTable The left table
     * @param rightTable The right table
     * @param leftColumn The join column from left table
     * @param rightColumn The join column from right table
     * @return JoinResult containing matched rows
     *
     * EXAMPLE:
     *   JoinResult result = executor.innerJoin(users, orders, "id", "user_id");
     *
     * INNER JOIN only includes rows where the join condition matches.
     * Rows without matches are excluded from the result.
     */
    public JoinResult innerJoin(Table leftTable, Table rightTable,
                                String leftColumn, String rightColumn) {

        // Validate columns exist
        int leftColIndex = leftTable.getColumnIndex(leftColumn);
        int rightColIndex = rightTable.getColumnIndex(rightColumn);

        if (leftColIndex < 0) {
            throw new IllegalArgumentException(
                    "Column '" + leftColumn + "' not found in table '" + leftTable.getName() + "'"
            );
        }
        if (rightColIndex < 0) {
            throw new IllegalArgumentException(
                    "Column '" + rightColumn + "' not found in table '" + rightTable.getName() + "'"
            );
        }

        // Create result structure
        JoinResult result = new JoinResult(leftTable.getName(), rightTable.getName());
        result.addColumns(leftTable.getName(), leftTable.getColumns());
        result.addColumns(rightTable.getName(), rightTable.getColumns());

        // Get all rows
        List<Row> leftRows = leftTable.selectAll();
        List<Row> rightRows = rightTable.selectAll();

        // Nested Loop Join
        for (Row leftRow : leftRows) {
            Object leftValue = leftRow.getValue(leftColIndex);

            for (Row rightRow : rightRows) {
                Object rightValue = rightRow.getValue(rightColIndex);

                // Check join condition
                if (valuesEqual(leftValue, rightValue)) {
                    result.addJoinedRow(leftRow, rightRow);
                }
            }
        }

        return result;
    }

    /**
     * Performs a LEFT JOIN between two tables
     *
     * @param leftTable The left table (all rows included)
     * @param rightTable The right table
     * @param leftColumn The join column from left table
     * @param rightColumn The join column from right table
     * @return JoinResult containing all left rows with matched right rows
     *
     * LEFT JOIN includes ALL rows from the left table.
     * If no match exists in right table, right columns are NULL.
     *
     * EXAMPLE:
     *   users:  [(1, Alice), (2, Bob), (3, Charlie)]
     *   orders: [(101, 1, Laptop)]
     *
     *   LEFT JOIN result:
     *   [(1, Alice, 101, 1, Laptop),
     *    (2, Bob, NULL, NULL, NULL),    ← No matching order
     *    (3, Charlie, NULL, NULL, NULL)] ← No matching order
     */
    public JoinResult leftJoin(Table leftTable, Table rightTable,
                               String leftColumn, String rightColumn) {

        // Validate columns
        int leftColIndex = leftTable.getColumnIndex(leftColumn);
        int rightColIndex = rightTable.getColumnIndex(rightColumn);

        if (leftColIndex < 0) {
            throw new IllegalArgumentException(
                    "Column '" + leftColumn + "' not found in table '" + leftTable.getName() + "'"
            );
        }
        if (rightColIndex < 0) {
            throw new IllegalArgumentException(
                    "Column '" + rightColumn + "' not found in table '" + rightTable.getName() + "'"
            );
        }

        // Create result structure
        JoinResult result = new JoinResult(leftTable.getName(), rightTable.getName());
        result.addColumns(leftTable.getName(), leftTable.getColumns());
        result.addColumns(rightTable.getName(), rightTable.getColumns());

        // Get all rows
        List<Row> leftRows = leftTable.selectAll();
        List<Row> rightRows = rightTable.selectAll();
        int rightColCount = rightTable.getColumnCount();

        // Nested Loop Join with LEFT semantics
        for (Row leftRow : leftRows) {
            Object leftValue = leftRow.getValue(leftColIndex);
            boolean foundMatch = false;

            for (Row rightRow : rightRows) {
                Object rightValue = rightRow.getValue(rightColIndex);

                if (valuesEqual(leftValue, rightValue)) {
                    result.addJoinedRow(leftRow, rightRow);
                    foundMatch = true;
                }
            }

            // If no match found, add left row with NULLs for right side
            if (!foundMatch) {
                result.addLeftJoinRow(leftRow, rightColCount);
            }
        }

        return result;
    }

    /**
     * Performs a RIGHT JOIN between two tables
     *
     * @param leftTable The left table
     * @param rightTable The right table (all rows included)
     * @param leftColumn The join column from left table
     * @param rightColumn The join column from right table
     * @return JoinResult containing all right rows with matched left rows
     *
     * RIGHT JOIN includes ALL rows from the right table.
     * If no match exists in left table, left columns are NULL.
     */
    public JoinResult rightJoin(Table leftTable, Table rightTable,
                                String leftColumn, String rightColumn) {

        // Validate columns
        int leftColIndex = leftTable.getColumnIndex(leftColumn);
        int rightColIndex = rightTable.getColumnIndex(rightColumn);

        if (leftColIndex < 0) {
            throw new IllegalArgumentException(
                    "Column '" + leftColumn + "' not found in table '" + leftTable.getName() + "'"
            );
        }
        if (rightColIndex < 0) {
            throw new IllegalArgumentException(
                    "Column '" + rightColumn + "' not found in table '" + rightTable.getName() + "'"
            );
        }

        // Create result structure
        JoinResult result = new JoinResult(leftTable.getName(), rightTable.getName());
        result.addColumns(leftTable.getName(), leftTable.getColumns());
        result.addColumns(rightTable.getName(), rightTable.getColumns());

        // Get all rows
        List<Row> leftRows = leftTable.selectAll();
        List<Row> rightRows = rightTable.selectAll();
        int leftColCount = leftTable.getColumnCount();

        // Track which right rows have been matched
        Set<Integer> matchedRightIndices = new HashSet<>();

        // First pass: find all matches
        for (Row leftRow : leftRows) {
            Object leftValue = leftRow.getValue(leftColIndex);

            for (int ri = 0; ri < rightRows.size(); ri++) {
                Row rightRow = rightRows.get(ri);
                Object rightValue = rightRow.getValue(rightColIndex);

                if (valuesEqual(leftValue, rightValue)) {
                    result.addJoinedRow(leftRow, rightRow);
                    matchedRightIndices.add(ri);
                }
            }
        }

        // Second pass: add unmatched right rows with NULLs
        for (int ri = 0; ri < rightRows.size(); ri++) {
            if (!matchedRightIndices.contains(ri)) {
                result.addRightJoinRow(rightRows.get(ri), leftColCount);
            }
        }

        return result;
    }

    /**
     * Performs a CROSS JOIN (Cartesian product) between two tables
     *
     * @param leftTable The left table
     * @param rightTable The right table
     * @return JoinResult containing all combinations
     *
     * CROSS JOIN returns every possible combination of rows.
     * Result size = leftRows × rightRows
     *
     * WARNING: Can produce very large results!
     * 3 left rows × 3 right rows = 9 result rows
     * 1000 × 1000 = 1,000,000 result rows!
     */
    public JoinResult crossJoin(Table leftTable, Table rightTable) {
        // Create result structure
        JoinResult result = new JoinResult(leftTable.getName(), rightTable.getName());
        result.addColumns(leftTable.getName(), leftTable.getColumns());
        result.addColumns(rightTable.getName(), rightTable.getColumns());

        // Get all rows
        List<Row> leftRows = leftTable.selectAll();
        List<Row> rightRows = rightTable.selectAll();

        // Every combination
        for (Row leftRow : leftRows) {
            for (Row rightRow : rightRows) {
                result.addJoinedRow(leftRow, rightRow);
            }
        }

        return result;
    }

    // =========================================================================
    // HELPER METHODS
    // =========================================================================

    /**
     * Compares two values for equality (handles NULLs)
     *
     * @param left Left value
     * @param right Right value
     * @return true if values are equal
     *
     * Note: In SQL, NULL = NULL is false, but for simplicity
     * we treat NULL = NULL as false (no match)
     */
    private boolean valuesEqual(Object left, Object right) {
        // NULL never equals anything (including NULL)
        if (left == null || right == null) {
            return false;
        }

        // Compare values
        return left.equals(right);
    }

    /**
     * Performs a join with statistics output
     */
    public JoinResult innerJoinWithStats(Table leftTable, Table rightTable,
                                         String leftColumn, String rightColumn) {
        long startTime = System.nanoTime();

        JoinResult result = innerJoin(leftTable, rightTable, leftColumn, rightColumn);

        long endTime = System.nanoTime();
        double milliseconds = (endTime - startTime) / 1_000_000.0;

        int comparisons = leftTable.getRowCount() * rightTable.getRowCount();

        System.out.println("📊 Join Statistics:");
        System.out.println("   Left table rows:  " + leftTable.getRowCount());
        System.out.println("   Right table rows: " + rightTable.getRowCount());
        System.out.println("   Comparisons made: " + comparisons);
        System.out.println("   Result rows:      " + result.getRowCount());
        System.out.println("   Time:             " + String.format("%.2f", milliseconds) + " ms");

        return result;
    }
}