package com.minidb;

import com.minidb.executor.JoinExecutor;
import com.minidb.executor.JoinResult;
import com.minidb.storage.*;

import java.util.Arrays;

/**
 * =============================================================================
 * Step 5 Demo - JOIN Operations in Action!
 * =============================================================================
 *
 * This demo shows:
 *   1. INNER JOIN - Only matching rows
 *   2. LEFT JOIN  - All left + matched right
 *   3. RIGHT JOIN - All right + matched left
 *   4. CROSS JOIN - All combinations
 *   5. Real-world e-commerce example
 */
public class Step5Demo {

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║          MiniDB - Step 5: JOIN Operations Demo               ║");
        System.out.println("║       Combining data from multiple tables!                   ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝\n");

        // Create sample tables
        Table users = createUsersTable();
        Table orders = createOrdersTable();

        // Show original tables
        System.out.println("═══════════════════════════════════════════════════════════════");
        System.out.println("ORIGINAL TABLES");
        System.out.println("═══════════════════════════════════════════════════════════════\n");

        System.out.println("📋 USERS table:");
        users.printTable();

        System.out.println("📋 ORDERS table:");
        orders.printTable();

        // Create join executor
        JoinExecutor joinExecutor = new JoinExecutor();

        // Demonstrate different JOIN types
        demonstrateInnerJoin(joinExecutor, users, orders);
        demonstrateLeftJoin(joinExecutor, users, orders);
        demonstrateRightJoin(joinExecutor, users, orders);
        demonstrateCrossJoin(joinExecutor, users, orders);
        demonstrateRealWorldExample();

        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                   🎉 STEP 5 COMPLETE! 🎉                     ║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.println("║  What we built:                                              ║");
        System.out.println("║    ✅ INNER JOIN - Only matching rows                       ║");
        System.out.println("║    ✅ LEFT JOIN  - All left + matched right                 ║");
        System.out.println("║    ✅ RIGHT JOIN - All right + matched left                 ║");
        System.out.println("║    ✅ CROSS JOIN - Cartesian product                        ║");
        System.out.println("║    ✅ Nested Loop Join algorithm                            ║");
        System.out.println("║                                                              ║");
        System.out.println("║  🎊 DATABASE ENGINE COMPLETE! 🎊                            ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
    }

    /**
     * Creates the users table for demo
     */
    private static Table createUsersTable() {
        Table users = new Table("users", Arrays.asList(
                new Column("id", DataType.INTEGER),
                new Column("name", DataType.STRING),
                new Column("city", DataType.STRING)
        ));

        users.insert(Arrays.asList(1, "Alice", "New York"));
        users.insert(Arrays.asList(2, "Bob", "Los Angeles"));
        users.insert(Arrays.asList(3, "Charlie", "Chicago"));
        users.insert(Arrays.asList(4, "Diana", "Houston"));  // No orders

        return users;
    }

    /**
     * Creates the orders table for demo
     */
    private static Table createOrdersTable() {
        Table orders = new Table("orders", Arrays.asList(
                new Column("order_id", DataType.INTEGER),
                new Column("user_id", DataType.INTEGER),
                new Column("product", DataType.STRING),
                new Column("amount", DataType.INTEGER)
        ));

        orders.insert(Arrays.asList(101, 1, "Laptop", 1200));
        orders.insert(Arrays.asList(102, 1, "Mouse", 25));
        orders.insert(Arrays.asList(103, 2, "Keyboard", 75));
        orders.insert(Arrays.asList(104, 3, "Monitor", 350));
        orders.insert(Arrays.asList(105, 3, "Webcam", 80));
        orders.insert(Arrays.asList(106, 99, "Headphones", 150));  // User doesn't exist!

        return orders;
    }

    /**
     * PART 1: INNER JOIN Demo
     */
    private static void demonstrateInnerJoin(JoinExecutor executor, Table users, Table orders) {
        System.out.println("═══════════════════════════════════════════════════════════════");
        System.out.println("PART 1: INNER JOIN");
        System.out.println("═══════════════════════════════════════════════════════════════\n");

        System.out.println("🔍 SQL: SELECT * FROM users INNER JOIN orders ON users.id = orders.user_id\n");

        System.out.println("📖 INNER JOIN returns ONLY rows that have matches in BOTH tables.");
        System.out.println("   - Diana (id=4) has no orders → NOT included");
        System.out.println("   - Order 106 (user_id=99) has no user → NOT included\n");

        JoinResult result = executor.innerJoinWithStats(users, orders, "id", "user_id");
        System.out.println("\n📋 Result:");
        result.print();
    }

    /**
     * PART 2: LEFT JOIN Demo
     */
    private static void demonstrateLeftJoin(JoinExecutor executor, Table users, Table orders) {
        System.out.println("\n═══════════════════════════════════════════════════════════════");
        System.out.println("PART 2: LEFT JOIN");
        System.out.println("═══════════════════════════════════════════════════════════════\n");

        System.out.println("🔍 SQL: SELECT * FROM users LEFT JOIN orders ON users.id = orders.user_id\n");

        System.out.println("📖 LEFT JOIN returns ALL rows from the LEFT table (users).");
        System.out.println("   - Diana (id=4) has no orders → included with NULL values");
        System.out.println("   - Order 106 (user_id=99) has no user → NOT included\n");

        JoinResult result = executor.leftJoin(users, orders, "id", "user_id");
        System.out.println("📋 Result:");
        result.print();

        System.out.println("\n👀 Notice: Diana appears with NULL for order columns!");
    }

    /**
     * PART 3: RIGHT JOIN Demo
     */
    private static void demonstrateRightJoin(JoinExecutor executor, Table users, Table orders) {
        System.out.println("\n═══════════════════════════════════════════════════════════════");
        System.out.println("PART 3: RIGHT JOIN");
        System.out.println("═══════════════════════════════════════════════════════════════\n");

        System.out.println("🔍 SQL: SELECT * FROM users RIGHT JOIN orders ON users.id = orders.user_id\n");

        System.out.println("📖 RIGHT JOIN returns ALL rows from the RIGHT table (orders).");
        System.out.println("   - Diana (id=4) has no orders → NOT included");
        System.out.println("   - Order 106 (user_id=99) has no user → included with NULL values\n");

        JoinResult result = executor.rightJoin(users, orders, "id", "user_id");
        System.out.println("📋 Result:");
        result.print();

        System.out.println("\n👀 Notice: Order 106 appears with NULL for user columns!");
    }

    /**
     * PART 4: CROSS JOIN Demo
     */
    private static void demonstrateCrossJoin(JoinExecutor executor, Table users, Table orders) {
        System.out.println("\n═══════════════════════════════════════════════════════════════");
        System.out.println("PART 4: CROSS JOIN (Cartesian Product)");
        System.out.println("═══════════════════════════════════════════════════════════════\n");

        // Use smaller tables for demo
        Table colors = new Table("colors", Arrays.asList(
                new Column("color", DataType.STRING)
        ));
        colors.insert(Arrays.asList("Red"));
        colors.insert(Arrays.asList("Blue"));

        Table sizes = new Table("sizes", Arrays.asList(
                new Column("size", DataType.STRING)
        ));
        sizes.insert(Arrays.asList("Small"));
        sizes.insert(Arrays.asList("Medium"));
        sizes.insert(Arrays.asList("Large"));

        System.out.println("📋 COLORS table:");
        colors.printTable();

        System.out.println("📋 SIZES table:");
        sizes.printTable();

        System.out.println("🔍 SQL: SELECT * FROM colors CROSS JOIN sizes\n");

        System.out.println("📖 CROSS JOIN returns EVERY combination of rows.");
        System.out.println("   2 colors × 3 sizes = 6 result rows\n");

        JoinResult result = executor.crossJoin(colors, sizes);
        System.out.println("📋 Result:");
        result.print();
    }

    /**
     * PART 5: Real-World E-Commerce Example
     */
    private static void demonstrateRealWorldExample() {
        System.out.println("\n═══════════════════════════════════════════════════════════════");
        System.out.println("PART 5: Real-World E-Commerce Example");
        System.out.println("═══════════════════════════════════════════════════════════════\n");

        // Create customers table
        Table customers = new Table("customers", Arrays.asList(
                new Column("customer_id", DataType.INTEGER),
                new Column("name", DataType.STRING),
                new Column("email", DataType.STRING)
        ));
        customers.insert(Arrays.asList(1, "John Smith", "john@email.com"));
        customers.insert(Arrays.asList(2, "Jane Doe", "jane@email.com"));
        customers.insert(Arrays.asList(3, "Bob Wilson", "bob@email.com"));

        // Create products table
        Table products = new Table("products", Arrays.asList(
                new Column("product_id", DataType.INTEGER),
                new Column("name", DataType.STRING),
                new Column("price", DataType.INTEGER)
        ));
        products.insert(Arrays.asList(101, "iPhone 15", 999));
        products.insert(Arrays.asList(102, "MacBook Pro", 2499));
        products.insert(Arrays.asList(103, "AirPods", 249));

        // Create purchases table (links customers to products)
        Table purchases = new Table("purchases", Arrays.asList(
                new Column("purchase_id", DataType.INTEGER),
                new Column("customer_id", DataType.INTEGER),
                new Column("product_id", DataType.INTEGER),
                new Column("quantity", DataType.INTEGER)
        ));
        purchases.insert(Arrays.asList(1001, 1, 101, 1));  // John bought iPhone
        purchases.insert(Arrays.asList(1002, 1, 103, 2));  // John bought 2 AirPods
        purchases.insert(Arrays.asList(1003, 2, 102, 1));  // Jane bought MacBook
        purchases.insert(Arrays.asList(1004, 2, 103, 1));  // Jane bought AirPods

        System.out.println("📋 CUSTOMERS table:");
        customers.printTable();

        System.out.println("📋 PRODUCTS table:");
        products.printTable();

        System.out.println("📋 PURCHASES table:");
        purchases.printTable();

        JoinExecutor executor = new JoinExecutor();

        // First JOIN: customers with purchases
        System.out.println("🔍 Query: Who bought what? (customers JOIN purchases)\n");
        JoinResult customerPurchases = executor.innerJoin(
                customers, purchases, "customer_id", "customer_id"
        );
        customerPurchases.print();

        // Now we would need a second JOIN to get product names
        // For now, let's show the concept
        System.out.println("\n💡 In a full implementation, we would then JOIN with products");
        System.out.println("   to get: Customer Name | Product Name | Quantity | Price");
        System.out.println("\n   This is called a MULTI-TABLE JOIN:\n");
        System.out.println("   SELECT c.name, p.name, pu.quantity, p.price");
        System.out.println("   FROM customers c");
        System.out.println("   JOIN purchases pu ON c.customer_id = pu.customer_id");
        System.out.println("   JOIN products p ON pu.product_id = p.product_id");
    }
}