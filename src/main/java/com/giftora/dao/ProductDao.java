package com.giftora.dao;

import com.giftora.model.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductDao {

    private static final String BASE_SELECT =
            "SELECT p.*, COALESCE(r.avg_rating, 0) AS avg_rating, COALESCE(r.review_count, 0) AS review_count "
                    + "FROM products p LEFT JOIN ("
                    + "SELECT product_id, AVG(rating) AS avg_rating, COUNT(*) AS review_count "
                    + "FROM reviews WHERE approved = TRUE GROUP BY product_id) r ON r.product_id = p.id ";

    private final Database database;

    public ProductDao() {
        this(Database.getInstance());
    }

    public ProductDao(Database database) {
        this.database = database;
    }

    public Optional<Product> findById(long id) throws SQLException {
        String sql = BASE_SELECT + "WHERE p.id = ?";
        try (Connection connection = database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Locks and reads a product row inside an existing transaction (SELECT ... FOR UPDATE).
     */
    public Optional<Product> findByIdForUpdate(Connection connection, long id) throws SQLException {
        String sql = "SELECT * FROM products WHERE id = ? FOR UPDATE";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapLocked(rs));
                }
            }
        }
        return Optional.empty();
    }

    private Product mapLocked(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setId(rs.getLong("id"));
        p.setName(rs.getString("name"));
        p.setDescription(rs.getString("description"));
        p.setPrice(rs.getBigDecimal("price"));
        p.setCategory(rs.getString("category"));
        p.setImageUrl(rs.getString("image_url"));
        p.setStock(rs.getInt("stock"));
        p.setActive(rs.getBoolean("active"));
        long sellerId = rs.getLong("seller_id");
        if (!rs.wasNull()) {
            p.setSellerId(sellerId);
        }
        return p;
    }


    public List<Product> search(String query, String category, String sort, boolean includeInactive,
                                int offset, int limit) throws SQLException {
        StringBuilder sql = new StringBuilder(BASE_SELECT);
        List<Object> params = new ArrayList<>();
        sql.append("WHERE 1=1 ");
        if (!includeInactive) {
            sql.append("AND p.active = TRUE ");
        }
        if (category != null && !category.isBlank()) {
            sql.append("AND p.category = ? ");
            params.add(category);
        }
        if (query != null && !query.isBlank()) {
            sql.append("AND (LOWER(p.name) LIKE ? OR LOWER(p.description) LIKE ?) ");
            String like = "%" + query.trim().toLowerCase() + "%";
            params.add(like);
            params.add(like);
        }
        sql.append(orderClause(sort));
        sql.append("LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(Math.max(offset, 0));

        List<Product> products = new ArrayList<>();
        try (Connection connection = database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    products.add(map(rs));
                }
            }
        }
        return products;
    }

    public int count(String query, String category, boolean includeInactive) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM products p WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (!includeInactive) {
            sql.append("AND p.active = TRUE ");
        }
        if (category != null && !category.isBlank()) {
            sql.append("AND p.category = ? ");
            params.add(category);
        }
        if (query != null && !query.isBlank()) {
            sql.append("AND (LOWER(p.name) LIKE ? OR LOWER(p.description) LIKE ?) ");
            String like = "%" + query.trim().toLowerCase() + "%";
            params.add(like);
            params.add(like);
        }
        try (Connection connection = database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    public List<String> listCategories() throws SQLException {
        String sql = "SELECT DISTINCT category FROM products WHERE active = TRUE ORDER BY category";
        List<String> categories = new ArrayList<>();
        try (Connection connection = database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                categories.add(rs.getString(1));
            }
        }
        return categories;
    }

    public List<Product> findBySeller(long sellerId) throws SQLException {
        String sql = BASE_SELECT + "WHERE p.seller_id = ? ORDER BY p.id DESC";
        List<Product> products = new ArrayList<>();
        try (Connection connection = database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, sellerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    products.add(map(rs));
                }
            }
        }
        return products;
    }

    public long insert(Product product) throws SQLException {
        String sql = "INSERT INTO products (name, description, price, category, image_url, stock, active, seller_id, created_at) "
                + "VALUES (?,?,?,?,?,?,?,?,?)";
        try (Connection connection = database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindProduct(ps, product);
            ps.setTimestamp(9, Timestamp.from(Instant.now()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    long id = keys.getLong(1);
                    product.setId(id);
                    return id;
                }
            }
        }
        return -1;
    }

    public boolean update(Product product) throws SQLException {
        String sql = "UPDATE products SET name=?, description=?, price=?, category=?, image_url=?, stock=?, active=? WHERE id=?";
        try (Connection connection = database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, product.getName());
            ps.setString(2, product.getDescription());
            ps.setBigDecimal(3, product.getPrice());
            ps.setString(4, product.getCategory());
            ps.setString(5, product.getImageUrl());
            ps.setInt(6, product.getStock());
            ps.setBoolean(7, product.isActive());
            ps.setLong(8, product.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(long id) throws SQLException {
        try (Connection connection = database.getConnection();
             PreparedStatement ps = connection.prepareStatement("DELETE FROM products WHERE id = ?")) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Decrements stock atomically within a transaction. Returns false if insufficient stock.
     */
    public boolean decrementStock(Connection connection, long productId, int quantity) throws SQLException {
        String lockSql = "SELECT stock FROM products WHERE id = ? FOR UPDATE";
        try (PreparedStatement ps = connection.prepareStatement(lockSql)) {
            ps.setLong(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return false;
                }
                int stock = rs.getInt("stock");
                if (stock < quantity) {
                    return false;
                }
                try (PreparedStatement update = connection.prepareStatement(
                        "UPDATE products SET stock = stock - ? WHERE id = ?")) {
                    update.setInt(1, quantity);
                    update.setLong(2, productId);
                    return update.executeUpdate() > 0;
                }
            }
        }
    }

    private void bindProduct(PreparedStatement ps, Product p) throws SQLException {
        ps.setString(1, p.getName());
        ps.setString(2, p.getDescription());
        ps.setBigDecimal(3, p.getPrice());
        ps.setString(4, p.getCategory());
        ps.setString(5, p.getImageUrl());
        ps.setInt(6, p.getStock());
        ps.setBoolean(7, p.isActive());
        if (p.getSellerId() == null) {
            ps.setNull(8, java.sql.Types.BIGINT);
        } else {
            ps.setLong(8, p.getSellerId());
        }
    }

    private String orderClause(String sort) {
        if (sort == null) {
            sort = "newest";
        }
        switch (sort) {
            case "price_asc":
                return "ORDER BY p.price ASC ";
            case "price_desc":
                return "ORDER BY p.price DESC ";
            case "name_asc":
                return "ORDER BY p.name ASC ";
            case "rating_desc":
                return "ORDER BY avg_rating DESC, review_count DESC ";
            case "newest":
            default:
                return "ORDER BY p.id DESC ";
        }
    }

    private void bind(PreparedStatement ps, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            Object param = params.get(i);
            if (param instanceof Integer) {
                ps.setInt(i + 1, (Integer) param);
            } else {
                ps.setString(i + 1, (String) param);
            }
        }
    }

    private Product map(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setId(rs.getLong("id"));
        p.setName(rs.getString("name"));
        p.setDescription(rs.getString("description"));
        p.setPrice(rs.getBigDecimal("price"));
        p.setCategory(rs.getString("category"));
        p.setImageUrl(rs.getString("image_url"));
        p.setStock(rs.getInt("stock"));
        p.setActive(rs.getBoolean("active"));
        long sellerId = rs.getLong("seller_id");
        if (!rs.wasNull()) {
            p.setSellerId(sellerId);
        }
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) {
            p.setCreatedAt(created.toInstant());
        }
        p.setAverageRating(rs.getDouble("avg_rating"));
        p.setReviewCount(rs.getInt("review_count"));
        return p;
    }
}
