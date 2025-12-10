package com.minidb.parser;

/**
 * =============================================================================
 * Token - Represents a single token from the SQL string
 * =============================================================================
 *
 * WHAT IS A TOKEN?
 * ----------------
 * A token is one "piece" of the SQL statement with:
 *   1. A TYPE - What kind of thing is it? (keyword, number, etc.)
 *   2. A VALUE - What exactly is it? ("SELECT", "25", "users")
 *   3. A POSITION - Where in the original SQL string? (for error messages)
 *
 * EXAMPLE:
 * --------
 * SQL: "SELECT * FROM users WHERE age > 25"
 *
 * Tokens created:
 *   Token(SELECT, "SELECT", 0)
 *   Token(ASTERISK, "*", 7)
 *   Token(FROM, "FROM", 9)
 *   Token(IDENTIFIER, "users", 14)
 *   Token(WHERE, "WHERE", 20)
 *   Token(IDENTIFIER, "age", 26)
 *   Token(GREATER_THAN, ">", 30)
 *   Token(NUMBER, "25", 32)
 *   Token(EOF, "", 34)
 *
 * WHY STORE POSITION?
 * -------------------
 * When there's an error, we can tell the user WHERE:
 *   "Error at position 14: Unknown table 'users'"
 *
 * This makes debugging much easier!
 */
public class Token {

    /**
     * The type of this token
     * Examples: SELECT, IDENTIFIER, NUMBER, EQUALS
     */
    private final TokenType type;

    /**
     * The actual text value of this token
     *
     * Examples:
     *   - For KEYWORD SELECT: value = "SELECT"
     *   - For IDENTIFIER: value = "users" or "age"
     *   - For NUMBER: value = "25"
     *   - For STRING_LITERAL: value = "Alice" (without the quotes)
     */
    private final String value;

    /**
     * Position in the original SQL string where this token starts
     *
     * This is 0-indexed (first character is position 0)
     *
     * Example:
     *   "SELECT * FROM users"
     *    ^      ^
     *    0      7
     *
     *    SELECT is at position 0
     *    * is at position 7
     */
    private final int position;

    /**
     * Creates a new Token
     *
     * @param type The token type (SELECT, NUMBER, etc.)
     * @param value The text value of the token
     * @param position Starting position in original SQL string
     *
     * EXAMPLE USAGE:
     *   Token selectToken = new Token(TokenType.SELECT, "SELECT", 0);
     *   Token numberToken = new Token(TokenType.NUMBER, "25", 32);
     */
    public Token(TokenType type, String value, int position) {
        this.type = type;
        this.value = value;
        this.position = position;
    }

    /**
     * Creates a Token without tracking position
     * Useful for simple cases where position doesn't matter
     *
     * @param type The token type
     * @param value The text value
     */
    public Token(TokenType type, String value) {
        this(type, value, -1);
    }

    // =========================================================================
    // GETTER METHODS
    // =========================================================================

    /**
     * Gets the type of this token
     * @return The TokenType
     */
    public TokenType getType() {
        return type;
    }

    /**
     * Gets the text value of this token
     * @return The value string
     */
    public String getValue() {
        return value;
    }

    /**
     * Gets the position in the original SQL string
     * @return The position (0-indexed), or -1 if not tracked
     */
    public int getPosition() {
        return position;
    }

    // =========================================================================
    // CONVENIENCE METHODS
    // =========================================================================

    /**
     * Checks if this token is of a specific type
     *
     * @param type The type to check against
     * @return true if this token is of that type
     *
     * EXAMPLE:
     *   if (token.is(TokenType.SELECT)) {
     *       // Handle SELECT
     *   }
     */
    public boolean is(TokenType type) {
        return this.type == type;
    }

    /**
     * Checks if this token is one of several types
     *
     * @param types The types to check against
     * @return true if this token matches any of the types
     *
     * EXAMPLE:
     *   if (token.isOneOf(TokenType.SELECT, TokenType.INSERT)) {
     *       // Handle SELECT or INSERT
     *   }
     */
    public boolean isOneOf(TokenType... types) {
        for (TokenType t : types) {
            if (this.type == t) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if this token is a keyword
     * @return true if this is a SQL keyword
     */
    public boolean isKeyword() {
        return type.isKeyword();
    }

    /**
     * Checks if this token is a comparison operator
     * @return true if this is =, !=, >, <, >=, or <=
     */
    public boolean isComparisonOperator() {
        return type.isComparisonOperator();
    }

    /**
     * Gets the numeric value if this is a NUMBER token
     *
     * @return The integer value
     * @throws NumberFormatException if not a valid number
     *
     * EXAMPLE:
     *   Token numToken = new Token(TokenType.NUMBER, "25", 0);
     *   int age = numToken.getNumericValue(); // Returns 25
     */
    public int getNumericValue() {
        return Integer.parseInt(value);
    }

    /**
     * Converts the operator token type to its string representation
     *
     * @return The operator string (=, !=, >, <, >=, <=)
     */
    public String getOperatorString() {
        switch (type) {
            case EQUALS:         return "=";
            case NOT_EQUALS:     return "!=";
            case GREATER_THAN:   return ">";
            case LESS_THAN:      return "<";
            case GREATER_EQUALS: return ">=";
            case LESS_EQUALS:    return "<=";
            default:             return value;
        }
    }

    // =========================================================================
    // OBJECT METHODS
    // =========================================================================

    /**
     * String representation for debugging
     *
     * Example outputs:
     *   "Token{SELECT, 'SELECT', pos=0}"
     *   "Token{IDENTIFIER, 'users', pos=14}"
     *   "Token{NUMBER, '25', pos=32}"
     */
    @Override
    public String toString() {
        if (position >= 0) {
            return "Token{" + type + ", '" + value + "', pos=" + position + "}";
        } else {
            return "Token{" + type + ", '" + value + "'}";
        }
    }

    /**
     * Two tokens are equal if they have the same type and value
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Token other = (Token) obj;
        return type == other.type && value.equals(other.value);
    }

    @Override
    public int hashCode() {
        return type.hashCode() * 31 + value.hashCode();
    }
}