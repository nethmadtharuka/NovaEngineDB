package com.minidb;

import com.minidb.executor.Executor;
import com.minidb.parser.*;
import com.minidb.storage.*;

import java.util.Arrays;

/**
 * Step 2 Demo - SQL Parser in Action!
 */
public class Step2Demo {

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║           MiniDB - Step 2: SQL Parser Demo                   ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝\n");

        // PART 1: Test Tokenizer
        System.out.println("═══════════════════════════════════════════════════════════════");
        System.out.println("PART 1: TOKENIZER - Breaking SQL into tokens");
        System.out.println("═══════════════════════════════════════════════════════════════\n");

        Tokenizer.debugTokenize("SELECT * FROM users");
        Tokenizer.debugTokenize("SELECT name, age FROM users WHERE age > 25");
        Tokenizer.debugTokenize("INSERT INTO users VALUES (1, 'Alice', 25, true)");

        // PART 2: Test Parser
        System.out.println("═══════════════════════════════════════════════════════════════");
        System.out.println("PART 2: PARSER - Building Abstract Syntax Trees");
        System.out.println("═══════════════════════════════════════════════════════════════\n");

        String[] sqlStatements = {
                "SELECT * FROM users",
                "SELECT name, age FROM employees",
                "SELECT * FROM orders WHERE amount > 100",
                "INSERT INTO users VALUES (1, 'Alice', 25, true)"
        };

        for (String sql : sqlStatements) {
            System.out.println("📝 Parsing: \"" + sql + "\"");
            try {
                Statement stmt = Parser.parse(sql);
                System.out.println("   Result: " + stmt);
                System.out.println("   Type: " + stmt.getType() + "\n");
            } catch (Parser.ParseException e) {
                System.out.println("   Error: " + e.getMessage() + "\n");
            }
        }

        // PART 3: Complete Pipeline
        System.out.println("═══════════════════════════════════════════════════════════════");
        System.out.println("PART 3: COMPLETE PIPELINE - Execute real SQL!");
        System.out.println("═══════════════════════════════════════════════════════════════\n");

        // Create executor and tables
        Executor executor = new Executor();

        Table usersTable = new Table("users", Arrays.asList(
                new Column("id", DataType.INTEGER),
                new Column("name", DataType.STRING),
                new Column("age", DataType.INTEGER),
                new Column("active", DataType.BOOLEAN)
        ));
        executor.addTable(usersTable);

        System.out.println("✅ Created table: users\n");

        // Execute INSERT via SQL
        System.out.println("📝 Executing INSERT statements:");
        String[] inserts = {
                "INSERT INTO users VALUES (1, 'Alice', 28, true)",
                "INSERT INTO users VALUES (2, 'Bob', 35, true)",
                "INSERT INTO users VALUES (3, 'Charlie', 22, false)",
                "INSERT INTO users VALUES (4, 'Diana', 45, true)",
                "INSERT INTO users VALUES (5, 'Eve', 31, false)"
        };

        for (String sql : inserts) {
            System.out.println("SQL: " + sql);
            Executor.ExecutionResult result = executor.execute(sql);
            System.out.print("   → ");
            result.print();
        }

        // Execute SELECT via SQL
        System.out.println("\n📝 Executing SELECT statements:\n");

        System.out.println("SQL: SELECT * FROM users");
        executor.execute("SELECT * FROM users").print();

        System.out.println("\nSQL: SELECT name, age FROM users");
        executor.execute("SELECT name, age FROM users").print();

        System.out.println("\nSQL: SELECT * FROM users WHERE age > 30");
        executor.execute("SELECT * FROM users WHERE age > 30").print();

        System.out.println("\nSQL: SELECT * FROM users WHERE name = 'Alice'");
        executor.execute("SELECT * FROM users WHERE name = 'Alice'").print();

        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                   🎉 STEP 2 COMPLETE! 🎉                     ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
    }
}