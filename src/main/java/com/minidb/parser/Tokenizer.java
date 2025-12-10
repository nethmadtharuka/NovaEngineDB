package com.minidb.parser;

import java.util.ArrayList;
import java.util.List;

/**
 * =============================================================================
 * Tokenizer (also called Lexer) - Breaks SQL into tokens
 * =============================================================================
 *
 * WHAT DOES THIS DO?
 * ------------------
 * Takes a SQL string and breaks it into a list of tokens.
 *
 * Input:  "SELECT * FROM users WHERE age > 25"
 * Output: [SELECT, *, FROM, users, WHERE, age, >, 25, EOF]
 *
 * HOW DOES IT WORK?
 * -----------------
 * The tokenizer reads the SQL string character by character:
 *
 *   "SELECT * FROM users"
 *    ^
 *    Start here
 *
 *   1. See 'S' → It's a letter → Read whole word → "SELECT"
 *   2. Check if "SELECT" is a keyword → YES! → Create KEYWORD token
 *   3. See ' ' → Skip whitespace
 *   4. See '*' → It's a symbol → Create ASTERISK token
 *   5. See ' ' → Skip whitespace
 *   6. See 'F' → Read whole word → "FROM" → KEYWORD token
 *   ... and so on
 *
 * THE ALGORITHM (Simplified):
 * ---------------------------
 * while not at end of string:
 *     skip whitespace
 *     if current char is letter:
 *         read whole word
 *         if word is keyword: create keyword token
 *         else: create identifier token
 *     else if current char is digit:
 *         read whole number
 *         create number token
 *     else if current char is quote:
 *         read until closing quote
 *         create string literal token
 *     else if current char is operator:
 *         create operator token
 *     else if current char is symbol:
 *         create symbol token
 *     else:
 *         create illegal token (error!)
 *
 * add EOF token at end
 */
public class Tokenizer {

    /** The SQL string we're tokenizing */
    private final String input;

    /** Current position in the input string */
    private int position;

    /** Current character at position */
    private char currentChar;

    /**
     * Creates a new Tokenizer for the given SQL string
     *
     * @param input The SQL string to tokenize
     *
     * EXAMPLE:
     *   Tokenizer tokenizer = new Tokenizer("SELECT * FROM users");
     *   List<Token> tokens = tokenizer.tokenize();
     */
    public Tokenizer(String input) {
        this.input = input;
        this.position = 0;
        // Initialize currentChar (handle empty input)
        this.currentChar = input.isEmpty() ? '\0' : input.charAt(0);
    }

    /**
     * Main method: Tokenize the entire input string
     *
     * @return List of tokens
     *
     * EXAMPLE:
     *   Input: "SELECT * FROM users"
     *   Output: [Token(SELECT), Token(*), Token(FROM), Token(users), Token(EOF)]
     */
    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();

        // Keep reading tokens until we hit EOF
        while (currentChar != '\0') {

            // Skip whitespace (spaces, tabs, newlines)
            if (Character.isWhitespace(currentChar)) {
                skipWhitespace();
                continue;
            }

            // Numbers: 0-9
            // Example: "25" in "WHERE age > 25"
            if (Character.isDigit(currentChar)) {
                tokens.add(readNumber());
                continue;
            }

            // Words: letters and identifiers
            // Could be keyword (SELECT) or identifier (users)
            if (Character.isLetter(currentChar) || currentChar == '_') {
                tokens.add(readWord());
                continue;
            }

            // String literals: 'Alice' or "Alice"
            if (currentChar == '\'' || currentChar == '"') {
                tokens.add(readString());
                continue;
            }

            // Operators and symbols
            Token symbolToken = readSymbolOrOperator();
            if (symbolToken != null) {
                tokens.add(symbolToken);
                continue;
            }

            // If we get here, we don't recognize this character
            tokens.add(new Token(TokenType.ILLEGAL,
                    String.valueOf(currentChar), position));
            advance();
        }

        // Always add EOF at the end
        tokens.add(new Token(TokenType.EOF, "", position));

        return tokens;
    }

    // =========================================================================
    // HELPER METHODS: Character Movement
    // =========================================================================

    /**
     * Move to the next character in the input
     *
     * Before: position=5, currentChar='C'
     * After:  position=6, currentChar='T'
     */
    private void advance() {
        position++;
        if (position >= input.length()) {
            currentChar = '\0';  // End of input
        } else {
            currentChar = input.charAt(position);
        }
    }

    /**
     * Look at the next character WITHOUT moving
     * Useful for checking two-character operators like >= or !=
     *
     * @return The next character, or '\0' if at end
     */
    private char peek() {
        int peekPos = position + 1;
        if (peekPos >= input.length()) {
            return '\0';
        }
        return input.charAt(peekPos);
    }

    /**
     * Skip all whitespace characters
     *
     * Before: "   SELECT"  (position at first space)
     * After:  "   SELECT"  (position at 'S')
     */
    private void skipWhitespace() {
        while (currentChar != '\0' && Character.isWhitespace(currentChar)) {
            advance();
        }
    }

    // =========================================================================
    // HELPER METHODS: Reading Different Token Types
    // =========================================================================

    /**
     * Read a number (integer for now)
     *
     * Example:
     *   Input at "25"
     *   Returns: Token(NUMBER, "25", position)
     *
     * ALGORITHM:
     *   1. Remember start position
     *   2. Keep reading while we see digits
     *   3. Create NUMBER token with collected digits
     */
    private Token readNumber() {
        int startPos = position;
        StringBuilder sb = new StringBuilder();

        // Read all consecutive digits
        while (currentChar != '\0' && Character.isDigit(currentChar)) {
            sb.append(currentChar);
            advance();
        }

        return new Token(TokenType.NUMBER, sb.toString(), startPos);
    }

    /**
     * Read a word (keyword or identifier)
     *
     * Keywords: SELECT, FROM, WHERE, etc.
     * Identifiers: table names, column names
     *
     * ALGORITHM:
     *   1. Read all letters, digits, and underscores
     *   2. Check if it's a keyword
     *   3. If keyword: return keyword token
     *   4. If not: return identifier token
     *
     * Example:
     *   "SELECT" → Token(SELECT, "SELECT")
     *   "users"  → Token(IDENTIFIER, "users")
     *   "user_name" → Token(IDENTIFIER, "user_name")
     */
    private Token readWord() {
        int startPos = position;
        StringBuilder sb = new StringBuilder();

        // Read all letters, digits, and underscores
        // First char must be letter or underscore (already checked)
        // Following chars can include digits
        while (currentChar != '\0' &&
                (Character.isLetterOrDigit(currentChar) || currentChar == '_')) {
            sb.append(currentChar);
            advance();
        }

        String word = sb.toString();

        // Check if this word is a keyword
        TokenType keywordType = TokenType.lookupKeyword(word);

        if (keywordType != null) {
            // It's a keyword!
            return new Token(keywordType, word.toUpperCase(), startPos);
        } else {
            // It's an identifier (table name, column name, etc.)
            return new Token(TokenType.IDENTIFIER, word, startPos);
        }
    }

    /**
     * Read a string literal (text in quotes)
     *
     * Supports both single quotes 'text' and double quotes "text"
     *
     * Example:
     *   Input at "'Alice'"
     *   Returns: Token(STRING_LITERAL, "Alice", position)
     *
     *   Note: The quotes are NOT included in the value!
     *
     * ALGORITHM:
     *   1. Remember the quote character (single or double)
     *   2. Skip the opening quote
     *   3. Read until closing quote
     *   4. Skip the closing quote
     *   5. Return string token
     */
    private Token readString() {
        int startPos = position;
        char quoteChar = currentChar;  // Remember if it's ' or "
        advance();  // Skip opening quote

        StringBuilder sb = new StringBuilder();

        // Read until we find the closing quote
        while (currentChar != '\0' && currentChar != quoteChar) {
            // Handle escape sequences like \'
            if (currentChar == '\\' && peek() == quoteChar) {
                advance();  // Skip backslash
                sb.append(currentChar);  // Add the quote
                advance();
            } else {
                sb.append(currentChar);
                advance();
            }
        }

        // Skip closing quote (if present)
        if (currentChar == quoteChar) {
            advance();
        }
        // If we hit end of input without closing quote, that's an error
        // For now, we'll just return what we have

        return new Token(TokenType.STRING_LITERAL, sb.toString(), startPos);
    }

    /**
     * Read operators and symbols
     *
     * Handles:
     *   - Single char: * , ( ) ; = > <
     *   - Double char: >= <= != <>
     *
     * @return Token for the operator/symbol, or null if not recognized
     */
    private Token readSymbolOrOperator() {
        int startPos = position;

        switch (currentChar) {
            // Simple single-character symbols
            case '*':
                advance();
                return new Token(TokenType.ASTERISK, "*", startPos);

            case ',':
                advance();
                return new Token(TokenType.COMMA, ",", startPos);

            case '(':
                advance();
                return new Token(TokenType.LEFT_PAREN, "(", startPos);

            case ')':
                advance();
                return new Token(TokenType.RIGHT_PAREN, ")", startPos);

            case ';':
                advance();
                return new Token(TokenType.SEMICOLON, ";", startPos);

            // Operators that might be one or two characters
            case '=':
                advance();
                return new Token(TokenType.EQUALS, "=", startPos);

            case '!':
                advance();
                if (currentChar == '=') {
                    advance();
                    return new Token(TokenType.NOT_EQUALS, "!=", startPos);
                }
                // Just '!' alone is not valid SQL
                return new Token(TokenType.ILLEGAL, "!", startPos);

            case '>':
                advance();
                if (currentChar == '=') {
                    advance();
                    return new Token(TokenType.GREATER_EQUALS, ">=", startPos);
                }
                return new Token(TokenType.GREATER_THAN, ">", startPos);

            case '<':
                advance();
                if (currentChar == '=') {
                    advance();
                    return new Token(TokenType.LESS_EQUALS, "<=", startPos);
                }
                if (currentChar == '>') {
                    advance();
                    return new Token(TokenType.NOT_EQUALS, "<>", startPos);
                }
                return new Token(TokenType.LESS_THAN, "<", startPos);

            default:
                return null;  // Not a symbol we recognize
        }
    }

    // =========================================================================
    // UTILITY METHOD FOR TESTING
    // =========================================================================

    /**
     * Tokenizes and prints all tokens (useful for debugging)
     *
     * @param sql The SQL string to tokenize
     */
    public static void debugTokenize(String sql) {
        System.out.println("SQL: \"" + sql + "\"");
        System.out.println("Tokens:");

        Tokenizer tokenizer = new Tokenizer(sql);
        List<Token> tokens = tokenizer.tokenize();

        for (Token token : tokens) {
            System.out.println("  " + token);
        }
        System.out.println();
    }
}