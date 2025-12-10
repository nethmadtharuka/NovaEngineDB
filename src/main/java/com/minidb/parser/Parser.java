package com.minidb.parser;

import java.util.ArrayList;
import java.util.List;

/**
 * =============================================================================
 * Parser - Converts tokens into an Abstract Syntax Tree (AST)
 * =============================================================================
 *
 * WHAT DOES THIS DO?
 * ------------------
 * Takes a list of tokens and builds a structured representation (AST).
 *
 * Input:  [SELECT, *, FROM, users, WHERE, age, >, 25, EOF]
 * Output: SelectStatement(columns=[*], table="users", where=WhereClause(age > 25))
 *
 * HOW DOES PARSING WORK?
 * ----------------------
 * The parser looks at tokens and "recognizes patterns":
 *
 * 1. See SELECT token → "I'm parsing a SELECT statement"
 * 2. Next tokens should be column list → Read columns
 * 3. Expect FROM keyword → Verify it's there
 * 4. Next should be table name → Read it
 * 5. Optional: WHERE keyword → If present, parse WHERE clause
 *
 * This is called "recursive descent parsing" - we have a method
 * for each grammar rule, and methods call each other.
 *
 * THE GRAMMAR (What SQL we understand):
 * -------------------------------------
 * statement      → selectStmt | insertStmt
 *
 * selectStmt     → SELECT columns FROM identifier [whereClause]
 * columns        → * | identifier (, identifier)*
 * whereClause    → WHERE identifier operator value
 * operator       → = | != | > | < | >= | <=
 * value          → NUMBER | STRING | TRUE | FALSE | NULL
 *
 * insertStmt     → INSERT INTO identifier VALUES ( valueList )
 * valueList      → value (, value)*
 *
 * ERROR HANDLING:
 * ---------------
 * If the SQL doesn't match the expected grammar, we throw ParseException
 * with a helpful message about what went wrong and where.
 */
public class Parser {

    /** The list of tokens to parse */
    private final List<Token> tokens;

    /** Current position in the token list */
    private int position;

    /** Current token we're looking at */
    private Token currentToken;

    /**
     * Creates a new Parser for the given tokens
     *
     * @param tokens The tokens to parse (from Tokenizer)
     */
    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.position = 0;
        this.currentToken = tokens.isEmpty() ?
                new Token(TokenType.EOF, "") : tokens.get(0);
    }

    /**
     * Convenience method: Parse SQL string directly
     *
     * @param sql The SQL string to parse
     * @return The parsed Statement
     *
     * EXAMPLE:
     *   Statement stmt = Parser.parse("SELECT * FROM users");
     */
    public static Statement parse(String sql) {
        Tokenizer tokenizer = new Tokenizer(sql);
        List<Token> tokens = tokenizer.tokenize();
        Parser parser = new Parser(tokens);
        return parser.parse();
    }

    /**
     * Main parsing method - parses one SQL statement
     *
     * @return The parsed Statement (SelectStatement, InsertStatement, etc.)
     * @throws ParseException if the SQL is invalid
     */
    public Statement parse() {
        // Look at the first token to determine statement type
        switch (currentToken.getType()) {
            case SELECT:
                return parseSelect();

            case INSERT:
                return parseInsert();

            case EOF:
                throw new ParseException("Empty SQL statement");

            default:
                throw new ParseException(
                        "Expected SELECT or INSERT, got " + currentToken.getType()
                );
        }
    }

    // =========================================================================
    // PARSING METHODS FOR EACH STATEMENT TYPE
    // =========================================================================

    /**
     * Parses a SELECT statement
     *
     * Grammar: SELECT columns FROM identifier [WHERE condition]
     *
     * @return SelectStatement object
     */
    private SelectStatement parseSelect() {
        // Consume SELECT keyword
        expect(TokenType.SELECT);

        // Parse column list
        List<String> columns = parseColumnList();

        // Expect and consume FROM
        expect(TokenType.FROM);

        // Parse table name
        String tableName = expectIdentifier();

        // Optional WHERE clause
        WhereClause whereClause = null;
        if (currentToken.is(TokenType.WHERE)) {
            whereClause = parseWhereClause();
        }

        // Should be at EOF or semicolon
        if (!currentToken.is(TokenType.EOF) && !currentToken.is(TokenType.SEMICOLON)) {
            throw new ParseException(
                    "Unexpected token after SELECT: " + currentToken.getValue()
            );
        }

        return new SelectStatement(columns, tableName, whereClause);
    }

    /**
     * Parses an INSERT statement
     *
     * Grammar: INSERT INTO identifier VALUES ( valueList )
     *
     * @return InsertStatement object
     */
    private InsertStatement parseInsert() {
        // Consume INSERT keyword
        expect(TokenType.INSERT);

        // Expect and consume INTO
        expect(TokenType.INTO);

        // Parse table name
        String tableName = expectIdentifier();

        // Expect and consume VALUES
        expect(TokenType.VALUES);

        // Expect opening parenthesis
        expect(TokenType.LEFT_PAREN);

        // Parse value list
        List<Object> values = parseValueList();

        // Expect closing parenthesis
        expect(TokenType.RIGHT_PAREN);

        // Should be at EOF or semicolon
        if (!currentToken.is(TokenType.EOF) && !currentToken.is(TokenType.SEMICOLON)) {
            throw new ParseException(
                    "Unexpected token after INSERT: " + currentToken.getValue()
            );
        }

        return new InsertStatement(tableName, values);
    }

    // =========================================================================
    // HELPER PARSING METHODS
    // =========================================================================

    /**
     * Parses a column list for SELECT
     *
     * Grammar: * | identifier (, identifier)*
     *
     * Examples:
     *   "*"              → ["*"]
     *   "name"           → ["name"]
     *   "name, age"      → ["name", "age"]
     */
    private List<String> parseColumnList() {
        List<String> columns = new ArrayList<>();

        // Check for SELECT *
        if (currentToken.is(TokenType.ASTERISK)) {
            advance();
            columns.add("*");
            return columns;
        }

        // Otherwise, parse comma-separated column names
        columns.add(expectIdentifier());

        while (currentToken.is(TokenType.COMMA)) {
            advance();  // Consume comma
            columns.add(expectIdentifier());
        }

        return columns;
    }

    /**
     * Parses a WHERE clause
     *
     * Grammar: WHERE identifier operator value
     *
     * @return WhereClause object
     */
    private WhereClause parseWhereClause() {
        // Consume WHERE
        expect(TokenType.WHERE);

        // Column name
        String column = expectIdentifier();

        // Operator
        String operator = parseOperator();

        // Value
        Object value = parseValue();

        return new WhereClause(column, operator, value);
    }

    /**
     * Parses a comparison operator
     *
     * Grammar: = | != | <> | > | < | >= | <=
     *
     * @return The operator as a string
     */
    private String parseOperator() {
        if (!currentToken.isComparisonOperator()) {
            throw new ParseException(
                    "Expected operator (=, !=, >, <, >=, <=), got " + currentToken.getValue()
            );
        }

        String op = currentToken.getOperatorString();
        advance();
        return op;
    }

    /**
     * Parses a single value
     *
     * Grammar: NUMBER | STRING | TRUE | FALSE | NULL
     *
     * @return The value as appropriate Java type
     */
    private Object parseValue() {
        switch (currentToken.getType()) {
            case NUMBER:
                int numValue = currentToken.getNumericValue();
                advance();
                return numValue;

            case STRING_LITERAL:
                String strValue = currentToken.getValue();
                advance();
                return strValue;

            case TRUE:
                advance();
                return true;

            case FALSE:
                advance();
                return false;

            case NULL:
                advance();
                return null;

            default:
                throw new ParseException(
                        "Expected value (number, string, true, false, null), got " +
                                currentToken.getValue()
                );
        }
    }

    /**
     * Parses a comma-separated list of values for INSERT
     *
     * Grammar: value (, value)*
     *
     * @return List of values
     */
    private List<Object> parseValueList() {
        List<Object> values = new ArrayList<>();

        // First value
        values.add(parseValue());

        // Additional values after commas
        while (currentToken.is(TokenType.COMMA)) {
            advance();  // Consume comma
            values.add(parseValue());
        }

        return values;
    }

    // =========================================================================
    // TOKEN NAVIGATION METHODS
    // =========================================================================

    /**
     * Move to the next token
     */
    private void advance() {
        position++;
        if (position >= tokens.size()) {
            currentToken = new Token(TokenType.EOF, "");
        } else {
            currentToken = tokens.get(position);
        }
    }

    /**
     * Expect a specific token type, consume it, and move forward
     *
     * @param type The expected token type
     * @throws ParseException if the current token doesn't match
     */
    private void expect(TokenType type) {
        if (!currentToken.is(type)) {
            throw new ParseException(
                    "Expected " + type + ", got " + currentToken.getType() +
                            " ('" + currentToken.getValue() + "')"
            );
        }
        advance();
    }

    /**
     * Expect an identifier token, consume it, and return its value
     *
     * @return The identifier value
     * @throws ParseException if current token isn't an identifier
     */
    private String expectIdentifier() {
        if (!currentToken.is(TokenType.IDENTIFIER)) {
            throw new ParseException(
                    "Expected identifier (table or column name), got " +
                            currentToken.getType() + " ('" + currentToken.getValue() + "')"
            );
        }
        String value = currentToken.getValue();
        advance();
        return value;
    }

    // =========================================================================
    // EXCEPTION CLASS
    // =========================================================================

    /**
     * Exception thrown when parsing fails
     */
    public static class ParseException extends RuntimeException {
        public ParseException(String message) {
            super("Parse error: " + message);
        }
    }
}