package com.giftora.model;

import java.util.EnumSet;
import java.util.Set;

public enum OrderStatus {
    PENDING,
    CONFIRMED,
    SHIPPED,
    DELIVERED,
    CANCELLED;

    public static OrderStatus fromString(String value) {
        if (value == null) {
            return null;
        }
        for (OrderStatus status : values()) {
            if (status.name().equalsIgnoreCase(value.trim())) {
                return status;
            }
        }
        return null;
    }

    /**
     * Defines the only permitted forward transitions for an order.
     */
    public Set<OrderStatus> allowedNext() {
        switch (this) {
            case PENDING:
                return EnumSet.of(CONFIRMED, CANCELLED);
            case CONFIRMED:
                return EnumSet.of(SHIPPED, CANCELLED);
            case SHIPPED:
                return EnumSet.of(DELIVERED);
            case DELIVERED:
                return EnumSet.noneOf(OrderStatus.class);
            case CANCELLED:
            default:
                return EnumSet.noneOf(OrderStatus.class);
        }
    }

    public boolean canTransitionTo(OrderStatus target) {
        return target != null && allowedNext().contains(target);
    }
}
