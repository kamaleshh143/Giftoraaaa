package com.giftora.service;

import com.giftora.dao.Database;
import com.giftora.dao.OrderDao;
import com.giftora.dao.ProductDao;
import com.giftora.exception.AuthorizationException;
import com.giftora.exception.NotFoundException;
import com.giftora.exception.ValidationException;
import com.giftora.model.Order;
import com.giftora.model.OrderItem;
import com.giftora.model.OrderStatus;
import com.giftora.model.Product;
import com.giftora.model.User;
import com.giftora.util.ValidationUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

public class OrderService {

    private final Database database;
    private final ProductDao productDao;
    private final OrderDao orderDao;
    private final Random random = new Random();

    public OrderService() {
        this(Database.getInstance(), new ProductDao(), new OrderDao());
    }

    public OrderService(Database database, ProductDao productDao, OrderDao orderDao) {
        this.database = database;
        this.productDao = productDao;
        this.orderDao = orderDao;
    }

    /**
     * Creates an order from the session cart inside a single transaction, verifying prices and
     * stock on the server. Rolls back completely if anything fails.
     */
    public Order checkout(User buyer, Map<Long, Integer> cart, String shippingName,
                          String shippingAddress, String shippingPhone)
            throws ValidationException, SQLException {
        if (buyer == null) {
            throw new ValidationException("Please log in to place an order.");
        }
        if (cart == null || cart.isEmpty()) {
            throw new ValidationException("Your cart is empty.");
        }
        if (ValidationUtil.isBlank(shippingName)) {
            throw new ValidationException("Please enter the recipient name.");
        }
        if (ValidationUtil.isBlank(shippingAddress) || shippingAddress.trim().length() < 8) {
            throw new ValidationException("Please enter a complete shipping address.");
        }
        if (ValidationUtil.isBlank(shippingPhone) || shippingPhone.replaceAll("[^0-9]", "").length() < 7) {
            throw new ValidationException("Please enter a valid phone number.");
        }

        Connection connection = null;
        try {
            connection = database.getConnection();
            connection.setAutoCommit(false);

            List<OrderItem> items = new ArrayList<>();
            BigDecimal total = BigDecimal.ZERO;

            for (Map.Entry<Long, Integer> entry : cart.entrySet()) {
                long productId = entry.getKey();
                int quantity = entry.getValue() == null ? 0 : entry.getValue();
                if (quantity <= 0) {
                    continue;
                }
                Product product = productDao.findByIdForUpdate(connection, productId).orElse(null);
                if (product == null || !product.isActive()) {
                    throw new ValidationException("A product in your cart is no longer available.");
                }
                if (product.getStock() < quantity) {
                    throw new ValidationException("Insufficient stock for " + product.getName() + ".");
                }
                if (!productDao.decrementStock(connection, productId, quantity)) {
                    throw new ValidationException("Insufficient stock for " + product.getName() + ".");
                }
                OrderItem item = new OrderItem();
                item.setProductId(productId);
                item.setProductName(product.getName());
                item.setUnitPrice(product.getPrice());
                item.setQuantity(quantity);
                items.add(item);
                total = total.add(product.getPrice().multiply(BigDecimal.valueOf(quantity)));
            }

            if (items.isEmpty()) {
                throw new ValidationException("Your cart is empty.");
            }

            Order order = new Order();
            order.setOrderNumber(generateOrderNumber());
            order.setUserId(buyer.getId());
            order.setStatus(OrderStatus.PENDING);
            order.setTotal(total);
            order.setShippingName(shippingName.trim());
            order.setShippingAddress(shippingAddress.trim());
            order.setShippingPhone(shippingPhone.trim());
            order.setCreatedAt(Instant.now());

            long orderId = orderDao.insert(connection, order);
            orderDao.insertItems(connection, orderId, items);
            order.setItems(items);

            connection.commit();
            return order;
        } catch (ValidationException ex) {
            rollbackQuietly(connection);
            throw ex;
        } catch (SQLException ex) {
            rollbackQuietly(connection);
            throw ex;
        } finally {
            closeQuietly(connection);
        }
    }

    public List<Order> getHistory(User buyer) throws SQLException {
        return orderDao.findByUser(buyer.getId());
    }

    public Order getForBuyer(User buyer, long orderId) throws SQLException, NotFoundException, AuthorizationException {
        Order order = orderDao.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found."));
        if (order.getUserId() != buyer.getId()) {
            throw new AuthorizationException("You can only view your own orders.");
        }
        return order;
    }

    public Optional<Order> findByNumber(String orderNumber) throws SQLException {
        return orderDao.findByNumber(orderNumber);
    }

    /**
     * Buyer-initiated cancellation. Only PENDING or CONFIRMED orders may be cancelled by the buyer.
     */
    public void cancelByBuyer(User buyer, long orderId)
            throws NotFoundException, AuthorizationException, ValidationException, SQLException {
        Order order = orderDao.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found."));
        if (order.getUserId() != buyer.getId()) {
            throw new AuthorizationException("You can only cancel your own orders.");
        }
        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new ValidationException("This order can no longer be cancelled.");
        }
        orderDao.updateStatus(orderId, OrderStatus.CANCELLED);
    }

    /**
     * Admin/seller status transition with transition validation.
     */
    public void updateStatus(User actor, long orderId, OrderStatus target)
            throws NotFoundException, AuthorizationException, ValidationException, SQLException {
        if (target == null) {
            throw new ValidationException("Invalid order status.");
        }
        Optional<Order> found = orderDao.findById(orderId);
        if (found.isEmpty()) {
            throw new NotFoundException("Order not found.");
        }
        Order order = found.get();
        if (actor == null) {
            throw new AuthorizationException("Not authorized.");
        }
        boolean admin = "ADMIN".equals(actor.getRole().name());
        boolean seller = "SELLER".equals(actor.getRole().name());
        if (!admin && !seller) {
            throw new AuthorizationException("Only sellers and administrators can update order status.");
        }
        if (seller && !orderDao.orderBelongsToSeller(order.getId(), actor.getId())) {
            throw new AuthorizationException("You can only manage orders for your own products.");
        }
        if (!order.getStatus().canTransitionTo(target)) {
            throw new ValidationException("Invalid status transition from " + order.getStatus() + " to " + target + ".");
        }
        orderDao.updateStatus(order.getId(), target);
    }

    public Order getById(long id) throws SQLException, NotFoundException {
        return orderDao.findById(id).orElseThrow(() -> new NotFoundException("Order not found."));
    }

    public List<Order> allOrders() throws SQLException {
        return orderDao.findAll();
    }

    public List<Order> sellerOrders(User seller) throws AuthorizationException, SQLException {
        if (seller == null || !seller.isSeller()) {
            throw new AuthorizationException("Seller access required.");
        }
        return orderDao.findBySeller(seller.getId());
    }

    public List<Order> buyerOrders(User buyer) throws SQLException {
        return orderDao.findByUser(buyer.getId());
    }

    public Optional<Order> findByOrderNumber(String number) throws SQLException {
        return orderDao.findByNumber(number);
    }

    private String generateOrderNumber() {
        long millis = System.currentTimeMillis();
        int suffix = 1000 + random.nextInt(9000);
        return "GFT-" + millis + "-" + suffix;
    }

    private void rollbackQuietly(Connection connection) {
        if (connection != null) {
            try {
                connection.rollback();
            } catch (SQLException ignored) {
                // ignore
            }
        }
    }

    private void closeQuietly(Connection connection) {
        if (connection != null) {
            try {
                connection.setAutoCommit(true);
                connection.close();
            } catch (SQLException ignored) {
                // ignore
            }
        }
    }
}
