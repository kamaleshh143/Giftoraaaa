package com.giftora.dao;

import com.giftora.model.Order;
import com.giftora.model.OrderItem;
import com.giftora.model.OrderStatus;

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

public class OrderDao {

    private final Database database;

    public OrderDao() {
        this(Database.getInstance());
    }

    public OrderDao(Database database) {
        this.database = database;
    }

    public long insert(Connection connection, Order order) throws SQLException {
        String sql = "INSERT INTO orders (order_number, user_id, status, total, shipping_name, shipping_address, shipping_phone, created_at, updated_at) "
                + "VALUES (?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, order.getOrderNumber());
            ps.setLong(2, order.getUserId());
            ps.setString(3, order.getStatus().name());
            ps.setBigDecimal(4, order.getTotal());
            ps.setString(5, order.getShippingName());
            ps.setString(6, order.getShippingAddress());
            ps.setString(7, order.getShippingPhone());
            Timestamp now = Timestamp.from(Instant.now());
            ps.setTimestamp(8, now);
            ps.setTimestamp(9, now);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    long id = keys.getLong(1);
                    order.setId(id);
                    return id;
                }
            }
        }
        return -1;
    }

    public void insertItems(Connection connection, long orderId, List<OrderItem> items) throws SQLException {
        String sql = "INSERT INTO order_items (order_id, product_id, product_name, unit_price, quantity) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (OrderItem item : items) {
                ps.setLong(1, orderId);
                ps.setLong(2, item.getProductId());
                ps.setString(3, item.getProductName());
                ps.setBigDecimal(4, item.getUnitPrice());
                ps.setInt(5, item.getQuantity());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public Optional<Order> findById(long id) throws SQLException {
        String sql = "SELECT * FROM orders WHERE id = ?";
        try (Connection connection = database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Order order = map(rs);
                    order.setItems(findItemsByOrderId(connection, order.getId()));
                    return Optional.of(order);
                }
            }
        }
        return Optional.empty();
    }

    public Optional<Order> findByNumber(String orderNumber) throws SQLException {
        String sql = "SELECT * FROM orders WHERE order_number = ?";
        try (Connection connection = database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, orderNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Order order = map(rs);
                    order.setItems(findItemsByOrderId(connection, order.getId()));
                    return Optional.of(order);
                }
            }
        }
        return Optional.empty();
    }

    public List<Order> findByUser(long userId) throws SQLException {
        String sql = "SELECT * FROM orders WHERE user_id = ? ORDER BY created_at DESC";
        List<Order> orders = new ArrayList<>();
        try (Connection connection = database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Order order = map(rs);
                    order.setItems(findItemsByOrderId(connection, order.getId()));
                    orders.add(order);
                }
            }
        }
        return orders;
    }

    public List<Order> findAll() throws SQLException {
        String sql = "SELECT * FROM orders ORDER BY created_at DESC";
        List<Order> orders = new ArrayList<>();
        try (Connection connection = database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Order order = map(rs);
                order.setItems(findItemsByOrderId(connection, order.getId()));
                orders.add(order);
            }
        }
        return orders;
    }

    public List<Order> findBySeller(long sellerId) throws SQLException {
        String sql = "SELECT DISTINCT o.* FROM orders o "
                + "JOIN order_items oi ON oi.order_id = o.id "
                + "JOIN products p ON p.id = oi.product_id "
                + "WHERE p.seller_id = ? ORDER BY o.created_at DESC";
        List<Order> orders = new ArrayList<>();
        try (Connection connection = database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, sellerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Order order = map(rs);
                    order.setItems(findItemsByOrderId(connection, order.getId()));
                    orders.add(order);
                }
            }
        }
        return orders;
    }

    public boolean updateStatus(long orderId, OrderStatus status) throws SQLException {
        String sql = "UPDATE orders SET status = ?, updated_at = ? WHERE id = ?";
        try (Connection connection = database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setTimestamp(2, Timestamp.from(Instant.now()));
            ps.setLong(3, orderId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Returns true if the given order contains at least one item sold by the given seller.
     */
    public boolean orderBelongsToSeller(long orderId, long sellerId) throws SQLException {
        String sql = "SELECT 1 FROM order_items oi JOIN products p ON oi.product_id = p.id "
                + "WHERE oi.order_id = ? AND p.seller_id = ? LIMIT 1";
        try (Connection connection = database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, orderId);
            ps.setLong(2, sellerId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public List<OrderItem> findItemsByOrderId(Connection connection, long orderId) throws SQLException {
        String sql = "SELECT * FROM order_items WHERE order_id = ? ORDER BY id";
        List<OrderItem> items = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(mapItem(rs));
                }
            }
        }
        return items;
    }

    private OrderItem mapItem(ResultSet rs) throws SQLException {
        OrderItem item = new OrderItem();
        item.setId(rs.getLong("id"));
        item.setOrderId(rs.getLong("order_id"));
        item.setProductId(rs.getLong("product_id"));
        item.setProductName(rs.getString("product_name"));
        item.setUnitPrice(rs.getBigDecimal("unit_price"));
        item.setQuantity(rs.getInt("quantity"));
        return item;
    }

    private Order map(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setId(rs.getLong("id"));
        order.setOrderNumber(rs.getString("order_number"));
        order.setUserId(rs.getLong("user_id"));
        order.setStatus(OrderStatus.fromString(rs.getString("status")));
        order.setTotal(rs.getBigDecimal("total"));
        order.setShippingName(rs.getString("shipping_name"));
        order.setShippingAddress(rs.getString("shipping_address"));
        order.setShippingPhone(rs.getString("shipping_phone"));
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) {
            order.setCreatedAt(created.toInstant());
        }
        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) {
            order.setUpdatedAt(updated.toInstant());
        }
        return order;
    }
}
