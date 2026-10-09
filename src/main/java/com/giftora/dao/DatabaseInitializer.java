package com.giftora.dao;

import com.giftora.config.AppConfig;
import com.giftora.util.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Creates the database schema and seeds exactly 50 demo products plus demo accounts.
 * All seed operations are idempotent: running them again never duplicates rows and never
 * overwrites data that already exists.
 */
public final class DatabaseInitializer {

    private static final Logger log = LoggerFactory.getLogger(DatabaseInitializer.class);

    private DatabaseInitializer() {
    }

    public static void initialize() {
        try (Connection connection = Database.getInstance().getConnection()) {
            createSchema(connection);
            if (AppConfig.seedEnabled()) {
                seedUsers(connection);
                seedProducts(connection);
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Database initialization failed", ex);
        }
    }

    private static void createSchema(Connection connection) throws SQLException {
        String[] ddl = {
                "CREATE TABLE IF NOT EXISTS users (" +
                        "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                        "name VARCHAR(120) NOT NULL," +
                        "email VARCHAR(190) NOT NULL UNIQUE," +
                        "password_hash VARCHAR(120) NOT NULL," +
                        "role VARCHAR(20) NOT NULL," +
                        "active BOOLEAN NOT NULL DEFAULT TRUE," +
                        "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP)",
                "CREATE TABLE IF NOT EXISTS products (" +
                        "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                        "name VARCHAR(200) NOT NULL," +
                        "description VARCHAR(2000)," +
                        "price DECIMAL(12,2) NOT NULL," +
                        "category VARCHAR(80) NOT NULL," +
                        "image_url VARCHAR(2000)," +
                        "stock INT NOT NULL DEFAULT 0," +
                        "active BOOLEAN NOT NULL DEFAULT TRUE," +
                        "seller_id BIGINT," +
                        "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP)",
                "CREATE TABLE IF NOT EXISTS orders (" +
                        "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                        "order_number VARCHAR(40) NOT NULL UNIQUE," +
                        "user_id BIGINT NOT NULL," +
                        "status VARCHAR(20) NOT NULL," +
                        "total DECIMAL(12,2) NOT NULL," +
                        "shipping_name VARCHAR(150)," +
                        "shipping_address VARCHAR(400)," +
                        "shipping_phone VARCHAR(40)," +
                        "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                        "updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP)",
                "CREATE TABLE IF NOT EXISTS order_items (" +
                        "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                        "order_id BIGINT NOT NULL," +
                        "product_id BIGINT NOT NULL," +
                        "product_name VARCHAR(200) NOT NULL," +
                        "unit_price DECIMAL(12,2) NOT NULL," +
                        "quantity INT NOT NULL)",
                "CREATE TABLE IF NOT EXISTS reviews (" +
                        "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                        "product_id BIGINT NOT NULL," +
                        "user_id BIGINT NOT NULL," +
                        "rating INT NOT NULL," +
                        "comment VARCHAR(1500)," +
                        "approved BOOLEAN NOT NULL DEFAULT TRUE," +
                        "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                        "CONSTRAINT uq_review UNIQUE (product_id, user_id))",
                "CREATE INDEX IF NOT EXISTS idx_products_category ON products(category)",
                "CREATE INDEX IF NOT EXISTS idx_products_seller ON products(seller_id)",
                "CREATE INDEX IF NOT EXISTS idx_orders_user ON orders(user_id)",
                "CREATE INDEX IF NOT EXISTS idx_order_items_order ON order_items(order_id)",
                "CREATE INDEX IF NOT EXISTS idx_reviews_product ON reviews(product_id)"
        };
        try (Statement statement = connection.createStatement()) {
            for (String sql : ddl) {
                statement.execute(sql);
            }
        }
        log.info("Schema ready");
    }

    private static void seedUsers(Connection connection) throws SQLException {
        String adminPassword = AppConfig.get("GIFTORA_ADMIN_PASSWORD", "Admin@12345");
        String sellerPassword = AppConfig.get("GIFTORA_SELLER_PASSWORD", "Seller@12345");
        String buyerPassword = AppConfig.get("GIFTORA_BUYER_PASSWORD", "Buyer@12345");
        insertUserIfMissing(connection, "Giftora Admin", "admin@giftora.example", adminPassword, "ADMIN");
        insertUserIfMissing(connection, "Giftora Seller", "seller@giftora.example", sellerPassword, "SELLER");
        insertUserIfMissing(connection, "Giftora Buyer", "buyer@giftora.example", buyerPassword, "BUYER");
    }

    private static void insertUserIfMissing(Connection connection, String name, String email,
                                            String password, String role) throws SQLException {
        String check = "SELECT id FROM users WHERE email = ?";
        try (PreparedStatement ps = connection.prepareStatement(check)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return;
                }
            }
        }
        String insert = "INSERT INTO users (name, email, password_hash, role, active, created_at) VALUES (?,?,?,?,TRUE,?)";
        try (PreparedStatement ps = connection.prepareStatement(insert)) {
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, PasswordUtil.hash(password));
            ps.setString(4, role);
            ps.setTimestamp(5, Timestamp.from(Instant.now()));
            ps.executeUpdate();
        }
        log.info("Seeded demo {} account: {}", role, email);
    }

    private static void seedProducts(Connection connection) throws SQLException {
        List<ProductSeed> seeds = productSeeds();
        int inserted = 0;
        for (ProductSeed seed : seeds) {
            if (!productExists(connection, seed.id)) {
                insertProduct(connection, seed);
                inserted++;
            }
        }
        if (inserted > 0) {
            log.info("Seeded {} demo products (of {} total)", inserted, seeds.size());
        }
    }

    private static boolean productExists(Connection connection, long id) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("SELECT id FROM products WHERE id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static void insertProduct(Connection connection, ProductSeed seed) throws SQLException {
        String insert = "INSERT INTO products (id, name, description, price, category, image_url, stock, active, seller_id, created_at) "
                + "VALUES (?,?,?,?,?,?,?,TRUE,NULL,?)";
        try (PreparedStatement ps = connection.prepareStatement(insert)) {
            ps.setLong(1, seed.id);
            ps.setString(2, seed.name);
            ps.setString(3, seed.description);
            ps.setBigDecimal(4, seed.price);
            ps.setString(5, seed.category);
            ps.setString(6, seed.imageUrl);
            ps.setInt(7, seed.stock);
            ps.setTimestamp(8, Timestamp.from(Instant.now()));
            ps.executeUpdate();
        }
    }

    private static final class ProductSeed {
        final long id;
        final String name;
        final String description;
        final BigDecimal price;
        final String category;
        final String imageUrl;
        final int stock;

        ProductSeed(long id, String name, String description, String price, String category, int stock) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.price = new BigDecimal(price);
            this.category = category;
            this.stock = stock;
            this.imageUrl = "https://picsum.photos/seed/giftora" + id + "/400/400";
        }
    }

    /**
     * Exactly 50 products in the required distribution.
     * Mobiles & Electronics 8, Fashion 8, Footwear 6, Home & Kitchen 7,
     * Beauty & Personal Care 5, Bags Watches & Accessories 5, Books & Stationery 4,
     * Toys & Kids 3, Sports & Fitness 4.
     */
    private static List<ProductSeed> productSeeds() {
        List<ProductSeed> list = new ArrayList<>();
        // Mobiles & Electronics (8) ids 1-8
        list.add(new ProductSeed(1, "Aurora X5 Smartphone",
                "6.5-inch AMOLED display, 128GB storage, 50MP triple camera, 5000mAh battery.",
                "24999.00", "Mobiles & Electronics", 25));
        list.add(new ProductSeed(2, "Pulse Wireless Earbuds",
                "True wireless earbuds with active noise cancellation and 30-hour battery case.",
                "2499.00", "Mobiles & Electronics", 60));
        list.add(new ProductSeed(3, "Nimbus 14 Laptop",
                "14-inch ultrabook, 16GB RAM, 512GB SSD, lightweight aluminium body.",
                "58999.00", "Mobiles & Electronics", 12));
        list.add(new ProductSeed(4, "TurboCharge 65W Adapter",
                "Fast GaN charger with dual USB-C ports and foldable plug.",
                "1499.00", "Mobiles & Electronics", 80));
        list.add(new ProductSeed(5, "ClearView 27 Monitor",
                "27-inch QHD IPS monitor with 75Hz refresh rate and slim bezels.",
                "15999.00", "Mobiles & Electronics", 18));
        list.add(new ProductSeed(6, "EchoSmart Smartwatch",
                "Fitness smartwatch with heart-rate, SpO2 and 10-day battery life.",
                "3999.00", "Mobiles & Electronics", 40));
        list.add(new ProductSeed(7, "PowerCore 20000mAh Power Bank",
                "High-capacity power bank with 22.5W fast charging and dual output.",
                "1999.00", "Mobiles & Electronics", 55));
        list.add(new ProductSeed(8, "SnapCam Action Camera",
                "4K action camera with waterproof housing and image stabilisation.",
                "8999.00", "Mobiles & Electronics", 22));
        // Fashion (8) ids 9-16
        list.add(new ProductSeed(9, "Urban Cotton T-Shirt",
                "Soft 100% cotton crew-neck t-shirt in a regular fit.",
                "799.00", "Fashion", 100));
        list.add(new ProductSeed(10, "Denim Slim Jeans",
                "Stretch denim slim-fit jeans with five-pocket styling.",
                "1799.00", "Fashion", 70));
        list.add(new ProductSeed(11, "Classic Formal Shirt",
                "Wrinkle-resistant cotton blend formal shirt with spread collar.",
                "1299.00", "Fashion", 65));
        list.add(new ProductSeed(12, "Winter Fleece Hoodie",
                "Warm fleece-lined hoodie with kangaroo pocket and drawstring hood.",
                "1599.00", "Fashion", 48));
        list.add(new ProductSeed(13, "Floral Summer Dress",
                "Flowy A-line summer dress with floral print and adjustable straps.",
                "1899.00", "Fashion", 35));
        list.add(new ProductSeed(14, "Leather Belt",
                "Genuine leather reversible belt with brushed metal buckle.",
                "999.00", "Fashion", 90));
        list.add(new ProductSeed(15, "Wool Blend Scarf",
                "Soft wool blend scarf in a timeless check pattern.",
                "699.00", "Fashion", 75));
        list.add(new ProductSeed(16, "Polarized Sunglasses",
                "UV400 polarized sunglasses with lightweight acetate frame.",
                "1199.00", "Fashion", 58));
        // Footwear (6) ids 17-22
        list.add(new ProductSeed(17, "Running Sports Shoes",
                "Breathable mesh running shoes with cushioned EVA midsole.",
                "2199.00", "Footwear", 50));
        list.add(new ProductSeed(18, "Canvas Casual Sneakers",
                "Everyday canvas sneakers with vulcanised rubber sole.",
                "1499.00", "Footwear", 60));
        list.add(new ProductSeed(19, "Formal Oxford Shoes",
                "Polished leather Oxford shoes for professional occasions.",
                "2999.00", "Footwear", 28));
        list.add(new ProductSeed(20, "Comfort Flip Flops",
                "Lightweight cushioned flip flops for daily comfort.",
                "399.00", "Footwear", 120));
        list.add(new ProductSeed(21, "Hiking Trail Boots",
                "Water-resistant hiking boots with high-grip rubber outsole.",
                "3499.00", "Footwear", 30));
        list.add(new ProductSeed(22, "Ballet Flat Shoes",
                "Elegant ballet flats with soft padded insole.",
                "1699.00", "Footwear", 44));
        // Home & Kitchen (7) ids 23-29
        list.add(new ProductSeed(23, "Non-Stick Cookware Set",
                "5-piece non-stick cookware set with heat-resistant handles.",
                "3299.00", "Home & Kitchen", 26));
        list.add(new ProductSeed(24, "Stainless Steel Water Bottle",
                "1-litre insulated stainless steel bottle keeps drinks cold for 24 hours.",
                "899.00", "Home & Kitchen", 95));
        list.add(new ProductSeed(25, "Ceramic Dinner Set",
                "16-piece microwave-safe ceramic dinner set for four people.",
                "2799.00", "Home & Kitchen", 20));
        list.add(new ProductSeed(26, "Memory Foam Pillow",
                "Ergonomic memory foam pillow with breathable washable cover.",
                "1299.00", "Home & Kitchen", 40));
        list.add(new ProductSeed(27, "Aroma Diffuser Lamp",
                "Ultrasonic essential-oil diffuser with 7-colour ambient light.",
                "1499.00", "Home & Kitchen", 38));
        list.add(new ProductSeed(28, "Electric Kettle 1.7L",
                "Stainless steel rapid-boil electric kettle with auto shut-off.",
                "1199.00", "Home & Kitchen", 52));
        list.add(new ProductSeed(29, "Cotton Bath Towel Set",
                "Set of four highly absorbent 500-GSM cotton bath towels.",
                "1599.00", "Home & Kitchen", 68));
        // Beauty & Personal Care (5) ids 30-34
        list.add(new ProductSeed(30, "Vitamin C Face Serum",
                "Brightening vitamin C serum with hyaluronic acid, 30ml.",
                "799.00", "Beauty & Personal Care", 85));
        list.add(new ProductSeed(31, "Herbal Shampoo",
                "Sulphate-free herbal shampoo for smooth, nourished hair, 400ml.",
                "499.00", "Beauty & Personal Care", 110));
        list.add(new ProductSeed(32, "Matte Lipstick Trio",
                "Long-lasting matte lipstick set of three versatile shades.",
                "899.00", "Beauty & Personal Care", 70));
        list.add(new ProductSeed(33, "Beard Grooming Kit",
                "Complete beard kit with oil, balm, comb and trimming scissors.",
                "1299.00", "Beauty & Personal Care", 42));
        list.add(new ProductSeed(34, "Sunscreen SPF 50",
                "Broad-spectrum SPF 50 sunscreen, lightweight and non-greasy, 100g.",
                "599.00", "Beauty & Personal Care", 100));
        // Bags, Watches & Accessories (5) ids 35-39
        list.add(new ProductSeed(35, "Leather Laptop Backpack",
                "Water-resistant backpack with padded 15.6-inch laptop compartment.",
                "2299.00", "Bags, Watches & Accessories", 45));
        list.add(new ProductSeed(36, "Minimalist Analog Watch",
                "Slim analog watch with stainless steel case and leather strap.",
                "2999.00", "Bags, Watches & Accessories", 33));
        list.add(new ProductSeed(37, "Travel Duffel Bag",
                "Spacious water-resistant duffel bag with shoe compartment.",
                "1899.00", "Bags, Watches & Accessories", 40));
        list.add(new ProductSeed(38, "Elegant Wrist Watch",
                "Chronograph wrist watch with date display and mesh band.",
                "3499.00", "Bags, Watches & Accessories", 24));
        list.add(new ProductSeed(39, "Canvas Tote Bag",
                "Reusable canvas tote bag with inner zip pocket.",
                "699.00", "Bags, Watches & Accessories", 88));
        // Books & Stationery (4) ids 40-43
        list.add(new ProductSeed(40, "Bestseller Novel Collection",
                "A curated set of three award-winning contemporary novels.",
                "1199.00", "Books & Stationery", 50));
        list.add(new ProductSeed(41, "Hardcover Notebook",
                "A5 dotted hardcover notebook with 200 acid-free pages.",
                "399.00", "Books & Stationery", 130));
        list.add(new ProductSeed(42, "Premium Gel Pen Set",
                "Pack of ten smooth-writing quick-dry gel pens.",
                "249.00", "Books & Stationery", 150));
        list.add(new ProductSeed(43, "Art Supplies Kit",
                "Beginner art kit with sketch pencils, colours and drawing pad.",
                "999.00", "Books & Stationery", 60));
        // Toys & Kids (3) ids 44-46
        list.add(new ProductSeed(44, "Wooden Building Blocks",
                "Safe non-toxic wooden building blocks set for creative play.",
                "1299.00", "Toys & Kids", 55));
        list.add(new ProductSeed(45, "Remote Control Car",
                "Rechargeable RC car with 2.4GHz remote and off-road tyres.",
                "1899.00", "Toys & Kids", 36));
        list.add(new ProductSeed(46, "Plush Teddy Bear",
                "Soft cuddly teddy bear, 45cm, made from hypoallergenic fabric.",
                "799.00", "Toys & Kids", 70));
        // Sports & Fitness (4) ids 47-50
        list.add(new ProductSeed(47, "Yoga Mat with Carry Strap",
                "Anti-slip 6mm yoga mat with alignment lines and carry strap.",
                "999.00", "Sports & Fitness", 65));
        list.add(new ProductSeed(48, "Adjustable Dumbbell Set",
                "Pair of adjustable dumbbells with secure spinlock collars.",
                "2499.00", "Sports & Fitness", 30));
        list.add(new ProductSeed(49, "Sports Water Bottle",
                "BPA-free 750ml sports bottle with flip-top leak-proof cap.",
                "449.00", "Sports & Fitness", 120));
        list.add(new ProductSeed(50, "Resistance Band Set",
                "Set of five resistance bands with door anchor and handles.",
                "799.00", "Sports & Fitness", 75));
        return list;
    }
}
