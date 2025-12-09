package com.minidb.storage;

/**
 * =============================================================================
 * Column - Defines the structure of a single column in a table
 * =============================================================================
 *
 * CONCEPT EXPLANATION:
 * --------------------
 * A Column is the DEFINITION/SCHEMA of a column, not the actual data.
 *
 * Think of it like this:
 *   - Column = The label on top of a spreadsheet column + its rules
 *   - It tells us: What's the name? What type of data? Can it be empty?
 *
 * EXAMPLE:
 * --------
 * When you write this SQL:
 *   CREATE TABLE users (
 *       id INTEGER,
 *       name STRING,
 *       age INTEGER
 *   );
 *
 * You're creating 3 Column objects:
 *   Column("id", INTEGER)
 *   Column("name", STRING)
 *   Column("age", INTEGER)
 *
 * The Column doesn't hold "25" or "Alice" - it just says:
 *   "This column is called 'age' and it stores integers"
 *
 * REAL-WORLD ANALOGY:
 * -------------------
 * Imagine a form template (like a job application):
 *   - "Name: ________" ← This is a Column (name=Name, type=STRING)
 *   - "Age: ________"  ← This is a Column (name=Age, type=INTEGER)
 *
 * The actual filled-in form with "John" and "25" would be a Row.
 */
public class Column {

    /**
     * The name of this column
     * Example: "id", "name", "email", "created_at"
     */
    private final String name;

    /**
     * The data type of this column
     * Example: INTEGER, STRING, BOOLEAN
     */
    private final DataType type;

    /**
     * Creates a new Column definition
     *
     * @param name The column name (like "id", "name", "email")
     * @param type The data type (INTEGER, STRING, or BOOLEAN)
     *
     * EXAMPLE USAGE:
     *   Column idColumn = new Column("id", DataType.INTEGER);
     *   Column nameColumn = new Column("name", DataType.STRING);
     */
    public Column(String name, DataType type) {
        // Validate inputs
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Column name cannot be null or empty");
        }
        if (type == null) {
            throw new IllegalArgumentException("Column type cannot be null");
        }

        // Store the column name in lowercase for consistency
        // This makes our SQL case-insensitive (like real databases)
        this.name = name.toLowerCase().trim();
        this.type = type;
    }

    /**
     * Gets the column name
     * @return The name of this column
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the column type
     * @return The DataType of this column
     */
    public DataType getType() {
        return type;
    }

    /**
     * Validates if a value can be stored in this column
     *
     * @param value The value to validate
     * @return true if the value is valid for this column's type
     *
     * EXAMPLE:
     *   Column ageColumn = new Column("age", DataType.INTEGER);
     *   ageColumn.isValidValue(25)      → true
     *   ageColumn.isValidValue("hello") → false
     *   ageColumn.isValidValue(null)    → true (NULL is always valid)
     */
    public boolean isValidValue(Object value) {
        return type.isValidValue(value);
    }

    /**
     * String representation for debugging
     * Example output: "Column{name='age', type=INTEGER}"
     */
    @Override
    public String toString() {
        return "Column{name='" + name + "', type=" + type + "}";
    }

    /**
     * Two columns are equal if they have the same name and type
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Column other = (Column) obj;
        return name.equals(other.name) && type == other.type;
    }

    @Override
    public int hashCode() {
        return name.hashCode() * 31 + type.hashCode();
    }
}