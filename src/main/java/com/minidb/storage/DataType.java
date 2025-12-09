package com.minidb.storage;

/**
 * =============================================================================
 * DataType - The types of data our database can store
 * =============================================================================
 *
 * CONCEPT EXPLANATION:
 * --------------------
 * In a database, each column has a "type" that defines what kind of data
 * it can hold. This is similar to Java's primitive types.
 *
 * Real databases like MySQL have many types:
 *   - INT, BIGINT, SMALLINT (numbers)
 *   - VARCHAR, TEXT, CHAR (strings)
 *   - DATE, DATETIME, TIMESTAMP (dates)
 *   - DECIMAL, FLOAT, DOUBLE (decimals)
 *   - BOOLEAN
 *   - BLOB (binary data)
 *
 * For our simple database, we'll start with just 3 types:
 *   1. INTEGER - whole numbers (like Java's int)
 *   2. STRING  - text (like Java's String)
 *   3. BOOLEAN - true/false
 *
 * WHY IS THIS IMPORTANT?
 * ----------------------
 * 1. Storage: Different types need different amounts of space
 *    - INTEGER: 4 bytes
 *    - STRING: variable (depends on length)
 *    - BOOLEAN: 1 byte
 *
 * 2. Comparison: You can only compare compatible types
 *    - "age > 25" works (comparing integers)
 *    - "name > 25" doesn't make sense!
 *
 * 3. Validation: Prevents bad data from entering the database
 *    - INSERT INTO users (age) VALUES ("hello") should fail!
 */
public enum DataType {

    /**
     * INTEGER - Stores whole numbers
     * Range: -2,147,483,648 to 2,147,483,647 (same as Java int)
     * Size: 4 bytes when stored to disk
     *
     * Examples:
     *   - age: 25
     *   - quantity: 100
     *   - user_id: 12345
     */
    INTEGER,

    /**
     * STRING - Stores text of any length
     * Size: variable (length of string + overhead)
     *
     * Examples:
     *   - name: "Alice"
     *   - email: "alice@example.com"
     *   - address: "123 Main Street"
     */
    STRING,

    /**
     * BOOLEAN - Stores true or false
     * Size: 1 byte when stored to disk
     *
     * Examples:
     *   - is_active: true
     *   - is_admin: false
     *   - email_verified: true
     */
    BOOLEAN;

    /**
     * Validates if a Java object matches this DataType
     *
     * @param value The value to check
     * @return true if the value is valid for this type
     *
     * EXAMPLE:
     *   DataType.INTEGER.isValidValue(42)      → true
     *   DataType.INTEGER.isValidValue("hello") → false
     *   DataType.STRING.isValidValue("hello")  → true
     */
    public boolean isValidValue(Object value) {
        if (value == null) {
            return true; // NULL is valid for all types (like SQL NULL)
        }

        switch (this) {
            case INTEGER:
                return value instanceof Integer;
            case STRING:
                return value instanceof String;
            case BOOLEAN:
                return value instanceof Boolean;
            default:
                return false;
        }
    }

    /**
     * Returns the size in bytes for fixed-size types
     * Used when storing data to disk
     *
     * @return size in bytes, or -1 for variable-length types
     */
    public int getByteSize() {
        switch (this) {
            case INTEGER:
                return 4;  // 4 bytes for int
            case BOOLEAN:
                return 1;  // 1 byte for boolean
            case STRING:
                return -1; // Variable length
            default:
                return -1;
        }
    }
}