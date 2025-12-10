package com.minidb.parser;

/**
 * =============================================================================
 * TokenType - All possible types of tokens in our SQL language
 * =============================================================================
 *
 * WHAT IS THIS?
 * -------------
 * When we break SQL into tokens, each token needs a "type" so we know
 * what it represents. Is it a keyword? A number? A table name?
 *
 * ANALOGY:
 * --------
 * Think of parts of speech in English:
 *   - "The"     → Article
 *   - "cat"     → Noun
 *   - "runs"    → Verb
 *   - "quickly" → Adverb
 *
 * Similarly in SQL:
 *   - "SELECT"  → Keyword
 *   - "users"   → Identifier (table name)
 *   - "25"      → Number
 *   - ">"       → Operator
 *
 * WHY IS THIS IMPORTANT?
 * ----------------------
 * The parser needs to know what each token IS to understand the SQL structure.
 *
 * For example:
 *   "SELECT * FROM users"
 *
 *   The parser thinks:
 *   - I see SELECT (keyword) → This is a SELECT statement
 *   - I see * (asterisk) → Select all columns
 *   - I see FROM (keyword) → Next comes the table name
 *   - I see users (identifier) → That's the table name!
 */
public enum TokenType {

    // =========================================================================
    // SQL KEYWORDS
    // These are reserved words with special meaning in SQL
    // =========================================================================

    /** SELECT keyword - for querying data */
    SELECT,

    /** INSERT keyword - for adding data */
    INSERT,

    /** INTO keyword - used with INSERT */
    INTO,

    /** VALUES keyword - used with INSERT */
    VALUES,

    /** FROM keyword - specifies the table */
    FROM,

    /** WHERE keyword - for filtering */
    WHERE,

    /** AND keyword - combines conditions */
    AND,

    /** OR keyword - alternative conditions */
    OR,

    /** CREATE keyword - for creating tables */
    CREATE,

    /** TABLE keyword - used with CREATE */
    TABLE,

    /** JOIN keyword - for combining tables */
    JOIN,

    /** ON keyword - specifies join condition */
    ON,

    /** NULL keyword - represents no value */
    NULL,

    /** TRUE keyword - boolean true */
    TRUE,

    /** FALSE keyword - boolean false */
    FALSE,

    // =========================================================================
    // DATA TYPES (for CREATE TABLE)
    // =========================================================================

    /** INTEGER type keyword */
    INTEGER,

    /** STRING type keyword */
    STRING,

    /** BOOLEAN type keyword */
    BOOLEAN,

    // =========================================================================
    // IDENTIFIERS AND LITERALS
    // These hold actual values from the SQL
    // =========================================================================

    /**
     * IDENTIFIER - Names of tables, columns, etc.
     *
     * Examples:
     *   - "users" in "SELECT * FROM users"
     *   - "name" in "SELECT name FROM users"
     *   - "age" in "WHERE age > 25"
     */
    IDENTIFIER,

    /**
     * NUMBER - Numeric literals
     *
     * Examples:
     *   - 25 in "WHERE age > 25"
     *   - 100 in "VALUES (100)"
     */
    NUMBER,

    /**
     * STRING_LITERAL - Text in quotes
     *
     * Examples:
     *   - 'Alice' in "WHERE name = 'Alice'"
     *   - 'hello' in "VALUES ('hello')"
     *
     * Note: We use single quotes like standard SQL
     */
    STRING_LITERAL,

    // =========================================================================
    // OPERATORS
    // For comparisons in WHERE clauses
    // =========================================================================

    /** = (equals) */
    EQUALS,

    /** != or <> (not equals) */
    NOT_EQUALS,

    /** > (greater than) */
    GREATER_THAN,

    /** < (less than) */
    LESS_THAN,

    /** >= (greater than or equal) */
    GREATER_EQUALS,

    /** <= (less than or equal) */
    LESS_EQUALS,

    // =========================================================================
    // SYMBOLS
    // Punctuation and special characters
    // =========================================================================

    /** * (asterisk) - used in SELECT * */
    ASTERISK,

    /** , (comma) - separates items */
    COMMA,

    /** ( (left parenthesis) */
    LEFT_PAREN,

    /** ) (right parenthesis) */
    RIGHT_PAREN,

    /** ; (semicolon) - end of statement (optional in our DB) */
    SEMICOLON,

    // =========================================================================
    // SPECIAL TOKENS
    // =========================================================================

    /**
     * EOF - End Of File/Input
     *
     * This special token marks the end of the SQL string.
     * It helps the parser know when to stop.
     *
     * Example:
     *   "SELECT * FROM users"
     *   Tokens: [SELECT] [*] [FROM] [users] [EOF]
     *                                        ^^^
     *                              Parser knows: "I'm done!"
     */
    EOF,

    /**
     * ILLEGAL - Invalid/unrecognized token
     *
     * If the tokenizer encounters something it doesn't understand,
     * it creates an ILLEGAL token so we can report a helpful error.
     */
    ILLEGAL;

    // =========================================================================
    // HELPER METHODS
    // =========================================================================

    /**
     * Checks if this token type is a keyword
     *
     * @return true if this is a SQL keyword
     */
    public boolean isKeyword() {
        switch (this) {
            case SELECT:
            case INSERT:
            case INTO:
            case VALUES:
            case FROM:
            case WHERE:
            case AND:
            case OR:
            case CREATE:
            case TABLE:
            case JOIN:
            case ON:
            case NULL:
            case TRUE:
            case FALSE:
            case INTEGER:
            case STRING:
            case BOOLEAN:
                return true;
            default:
                return false;
        }
    }

    /**
     * Checks if this token type is a comparison operator
     *
     * @return true if this is a comparison operator
     */
    public boolean isComparisonOperator() {
        switch (this) {
            case EQUALS:
            case NOT_EQUALS:
            case GREATER_THAN:
            case LESS_THAN:
            case GREATER_EQUALS:
            case LESS_EQUALS:
                return true;
            default:
                return false;
        }
    }

    /**
     * Checks if this token type represents a value
     *
     * @return true if this is a value (number, string, boolean)
     */
    public boolean isValue() {
        switch (this) {
            case NUMBER:
            case STRING_LITERAL:
            case TRUE:
            case FALSE:
            case NULL:
                return true;
            default:
                return false;
        }
    }

    /**
     * Converts a string to its corresponding keyword TokenType
     *
     * @param word The word to check
     * @return The TokenType if it's a keyword, or null if not
     *
     * EXAMPLE:
     *   lookupKeyword("SELECT") → TokenType.SELECT
     *   lookupKeyword("users")  → null (not a keyword)
     */
    public static TokenType lookupKeyword(String word) {
        // Convert to uppercase for case-insensitive matching
        // SQL keywords are case-insensitive (SELECT = select = SeLeCt)
        switch (word.toUpperCase()) {
            case "SELECT":  return SELECT;
            case "INSERT":  return INSERT;
            case "INTO":    return INTO;
            case "VALUES":  return VALUES;
            case "FROM":    return FROM;
            case "WHERE":   return WHERE;
            case "AND":     return AND;
            case "OR":      return OR;
            case "CREATE":  return CREATE;
            case "TABLE":   return TABLE;
            case "JOIN":    return JOIN;
            case "ON":      return ON;
            case "NULL":    return NULL;
            case "TRUE":    return TRUE;
            case "FALSE":   return FALSE;
            case "INTEGER": return INTEGER;
            case "INT":     return INTEGER;  // Allow INT as shorthand
            case "STRING":  return STRING;
            case "VARCHAR": return STRING;   // Allow VARCHAR as alias
            case "BOOLEAN": return BOOLEAN;
            case "BOOL":    return BOOLEAN;  // Allow BOOL as shorthand
            default:        return null;     // Not a keyword
        }
    }
}