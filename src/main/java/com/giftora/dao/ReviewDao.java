package com.giftora.dao;

import com.giftora.model.Review;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class ReviewDao {

    private final Database database;

    public ReviewDao() {
        this(Database.getInstance());
    }

    public ReviewDao(Database database) {
        this.database = database;
    }

    public List<Review> findByProduct(long productId, boolean approvedOnly) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT r.*, u.name AS user_name FROM reviews r JOIN users u ON r.user_id = u.id "
                        + "WHERE r.product_id = ? ");
        if (approvedOnly) {
            sql.append("AND r.approved = TRUE ");
        }
        sql.append("ORDER BY r.created_at DESC");
        List<Review> reviews = new ArrayList<>();
        try (Connection connection = database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            ps.setLong(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    reviews.add(map(rs));
                }
            }
        }
        return reviews;
    }

    public List<Review> findAll() throws SQLException {
        String sql = "SELECT r.*, u.name AS user_name FROM reviews r JOIN users u ON r.user_id = u.id "
                + "ORDER BY r.created_at DESC";
        List<Review> reviews = new ArrayList<>();
        try (Connection connection = database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                reviews.add(map(rs));
            }
        }
        return reviews;
    }

    public boolean existsForUser(long productId, long userId) throws SQLException {
        String sql = "SELECT 1 FROM reviews WHERE product_id = ? AND user_id = ?";
        try (Connection connection = database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, productId);
            ps.setLong(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public long insert(Review review) throws SQLException {
        String sql = "INSERT INTO reviews (product_id, user_id, rating, comment, approved, created_at) VALUES (?,?,?,?,?,?)";
        try (Connection connection = database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, review.getProductId());
            ps.setLong(2, review.getUserId());
            ps.setInt(3, review.getRating());
            ps.setString(4, review.getComment());
            ps.setBoolean(5, review.isApproved());
            ps.setTimestamp(6, Timestamp.from(Instant.now()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }
        return -1;
    }

    public void setApproved(long reviewId, boolean approved) throws SQLException {
        try (Connection connection = database.getConnection();
             PreparedStatement ps = connection.prepareStatement("UPDATE reviews SET approved = ? WHERE id = ?")) {
            ps.setBoolean(1, approved);
            ps.setLong(2, reviewId);
            ps.executeUpdate();
        }
    }

    public boolean delete(long reviewId) throws SQLException {
        try (Connection connection = database.getConnection();
             PreparedStatement ps = connection.prepareStatement("DELETE FROM reviews WHERE id = ?")) {
            ps.setLong(1, reviewId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * A buyer may review only if they have a DELIVERED order containing the product.
     */
    public boolean hasDeliveredPurchase(long userId, long productId) throws SQLException {
        String sql = "SELECT 1 FROM orders o JOIN order_items oi ON oi.order_id = o.id "
                + "WHERE o.user_id = ? AND oi.product_id = ? AND o.status = 'DELIVERED' LIMIT 1";
        try (Connection connection = database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, productId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private Review map(ResultSet rs) throws SQLException {
        Review review = new Review();
        review.setId(rs.getLong("id"));
        review.setProductId(rs.getLong("product_id"));
        review.setUserId(rs.getLong("user_id"));
        try {
            review.setUserName(rs.getString("user_name"));
        } catch (SQLException ignored) {
            review.setUserName("Customer");
        }
        review.setRating(rs.getInt("rating"));
        review.setComment(rs.getString("comment"));
        review.setApproved(rs.getBoolean("approved"));
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) {
            review.setCreatedAt(created.toInstant());
        }
        return review;
    }
}
