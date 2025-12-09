package com.minidb.storage;

import java.util.ArrayList;
import java.util.List;

/**
 * =============================================================================
 * Row - A single record of data (one line in a table)
 * =============================================================================
 *
 * CONCEPT EXPLANATION:
 * --------------------
 * A Row holds the ACTUAL DATA values, while Column defines the structure.
 *
 * Think of it like a spreadsheet:
 *   - Columns define the headers: [id] [name] [age]
 *   - Each Row is a horizontal line of data: [1] [Alice] [25]
 *
 * EXAMPLE:
 * --------
 * Table: users
 *   ┌────┬─────────┬─────┐
 *   │ id │  name   │ age │  ← Columns (schema)
 *   ├────┼─────────┼─────┤
 *   │ 1  │ Alice   │ 25  │  ← Row 1 (data)
 *   │ 2  │ Bob     │ 30  │  ← Row 2 (data)
 *   │ 3  │ Charlie │ 35  │  ← Row 3 (data)
 *   └────┴─────────┴─────┘
 *
 * In Java:
 *   Row row1 = new Row(Arrays.asList(1, "Alice", 25));
 *   Row row2 = new Row(Arrays.asList(2, "Bob", 30));
 *
 * IMPORTANT NOTE:
 * ---------------
 * The Row stores values in the SAME ORDER as the table's columns.
 *   - values.get(0) corresponds to the first column (id)
 *   - values.get(1) corresponds to the second column (name)
 *   - values.get(2) corresponds to the third column (age)
 *
 * The Row itself doesn't know column names - that's the Table's job!
 */
public class Row {

    /**
     * The actual data values, stored in column order
     *
     * For a table with columns [id, name, age]:
     *   values.get(0) = id value (e.g., 1)
     *   values.get(1) = name value (e.g., "Alice")
     *   values.get(2) = age value (e.g., 25)
     */
    private final List<Object> values;

    /**
     * Creates a new Row with the given values
     *
     * @param values The data values in column order
     *
     * EXAMPLE:
     *   // For table with columns: id (INTEGER), name (STRING), age (INTEGER)
     *   Row row = new Row(Arrays.asList(1, "Alice", 25));
     */
    public Row(List<Object> values) {
        // Create a copy to prevent external modification
        // This is called "defensive copying" - a good practice!
        this.values = new ArrayList<>(values);
    }

    /**
     * Gets the value at a specific column index
     *
     * @param index The column index (0-based)
     * @return The value at that position
     *
     * EXAMPLE:
     *   Row row = new Row(Arrays.asList(1, "Alice", 25));
     *   row.getValue(0)  → 1
     *   row.getValue(1)  → "Alice"
     *   row.getValue(2)  → 25
     */
    public Object getValue(int index) {
        if (index < 0 || index >= values.size()) {
            throw new IndexOutOfBoundsException(
                    "Column index " + index + " is out of range. " +
                            "Valid range: 0 to " + (values.size() - 1)
            );
        }
        return values.get(index);
    }

    /**
     * Gets all values in this row
     *
     * @return A copy of all values (to prevent modification)
     */
    public List<Object> getValues() {
        return new ArrayList<>(values); // Return a copy
    }

    /**
     * Gets the number of values (columns) in this row
     *
     * @return The number of columns
     */
    public int size() {
        return values.size();
    }

    /**
     * Sets a value at a specific index
     * Used for UPDATE operations
     *
     * @param index The column index
     * @param value The new value
     */
    public void setValue(int index, Object value) {
        if (index < 0 || index >= values.size()) {
            throw new IndexOutOfBoundsException(
                    "Column index " + index + " is out of range"
            );
        }
        values.set(index, value);
    }

    /**
     * String representation for debugging and display
     *
     * EXAMPLE OUTPUT:
     *   Row{values=[1, Alice, 25]}
     */
    @Override
    public String toString() {
        return "Row{values=" + values + "}";
    }

    /**
     * Creates a nicely formatted string for display
     *
     * @param separator The character(s) between values
     * @return Formatted string
     *
     * EXAMPLE:
     *   row.toFormattedString(" | ")  → "1 | Alice | 25"
     */
    public String toFormattedString(String separator) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                sb.append(separator);
            }
            Object value = values.get(i);
            sb.append(value == null ? "NULL" : value.toString());
        }
        return sb.toString();
    }
}