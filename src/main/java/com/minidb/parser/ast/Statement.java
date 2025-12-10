package com.minidb.parser;

/**
 * =============================================================================
 * Statement - Abstract base class for all SQL statements
 * =============================================================================
 *
 * WHAT IS THIS?
 * -------------
 * This is the base class for our Abstract Syntax Tree (AST) nodes.
 * Each type of SQL statement (SELECT, INSERT, etc.) extends this class.
 *
 * WHY ABSTRACT?
 * -------------
 * We never create a "Statement" directly - we always create specific
 * types like SelectStatement or InsertStatement. The abstract class
 * defines what ALL statements have in common.
 *
 * AST VISUALIZATION:
 * ------------------
 * SQL: "SELECT * FROM users WHERE age > 25"
 *
 * Becomes:
 *              Statement (abstract)
 *                   |
 *            SelectStatement
 *            /      |      \
 *       columns   table   whereClause
 *          |        |          |
 *         [*]    "users"    WhereClause
 *                           /    |    \
 *                       "age"   ">"   25
 */
public abstract class Statement {

    /**
     * Type of statement (for easy type checking)
     */
    public enum Type {
        SELECT,
        INSERT,
        CREATE_TABLE
    }

    /**
     * Gets the type of this statement
     * @return The statement type
     */
    public abstract Type getType();

    /**
     * Returns a string representation for debugging
     */
    @Override
    public abstract String toString();
}