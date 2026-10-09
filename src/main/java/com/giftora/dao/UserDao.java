package com.giftora.dao;

import com.giftora.model.Role;
import com.giftora.model.User;

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

public class UserDao {

    private final Database database;

    public UserDao() {
        this(Database.getInstance());
    }

    public UserDao(Database database) {
        this.database = database;
    }

    public Optional<User> findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM users WHERE LOWER(email) = LOWER(?)";
        try (Connection connection = database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
            }
        }
        return Optional.empty();
    }

    public Optional<User> findById(long id) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ?";
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

    public long insert(User user) throws SQLException {
        String sql = "INSERT INTO users (name, email, password_hash, role, active, created_at) VALUES (?,?,?,?,?,?)";
        try (Connection connection = database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPasswordHash());
            ps.setString(4, user.getRole().name());
            ps.setBoolean(5, user.isActive());
            ps.setTimestamp(6, Timestamp.from(Instant.now()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    long id = keys.getLong(1);
                    user.setId(id);
                    return id;
                }
            }
        }
        return -1;
    }

    public boolean emailExists(String email) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE LOWER(email) = LOWER(?)";
        try (Connection connection = database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public List<User> findAll() throws SQLException {
        String sql = "SELECT * FROM users ORDER BY id";
        List<User> users = new ArrayList<>();
        try (Connection connection = database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                users.add(map(rs));
            }
        }
        return users;
    }

    public void updateActive(long userId, boolean active) throws SQLException {
        String sql = "UPDATE users SET active = ? WHERE id = ?";
        try (Connection connection = database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setBoolean(1, active);
            ps.setLong(2, userId);
            ps.executeUpdate();
        }
    }

    private User map(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setName(rs.getString("name"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setRole(Role.fromString(rs.getString("role")));
        user.setActive(rs.getBoolean("active"));
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) {
            user.setCreatedAt(created.toInstant());
        }
        return user;
    }
}
