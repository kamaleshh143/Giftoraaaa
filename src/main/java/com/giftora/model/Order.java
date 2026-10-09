package com.giftora.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Order implements Serializable {
    private static final long serialVersionUID = 1L;

    private long id;
    private String orderNumber;
    private long userId;
    private OrderStatus status;
    private BigDecimal total;
    private String shippingName;
    private String shippingAddress;
    private String shippingPhone;
    private Instant createdAt;
    private Instant updatedAt;
    private List<OrderItem> items = new ArrayList<>();

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getShippingName() {
        return shippingName;
    }

    public void setShippingName(String shippingName) {
        this.shippingName = shippingName;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public String getShippingPhone() {
        return shippingPhone;
    }

    public void setShippingPhone(String shippingPhone) {
        this.shippingPhone = shippingPhone;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * Date view for JSTL {@code <fmt:formatDate>}, which cannot format {@link Instant} directly.
     */
    public java.util.Date getCreatedAtDate() {
        return createdAt == null ? null : java.util.Date.from(createdAt);
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public int getTimelineIndex() {
        if (status == null) {
            return 0;
        }
        switch (status) {
            case PENDING:
                return 0;
            case CONFIRMED:
                return 1;
            case SHIPPED:
                return 2;
            case DELIVERED:
                return 3;
            case CANCELLED:
            default:
                return -1;
        }
    }
}
