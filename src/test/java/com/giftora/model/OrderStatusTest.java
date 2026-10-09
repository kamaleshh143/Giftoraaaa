package com.giftora.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderStatusTest {

    @Test
    void pendingCanMoveToConfirmedOrCancelled() {
        assertTrue(OrderStatus.PENDING.canTransitionTo(OrderStatus.CONFIRMED));
        assertTrue(OrderStatus.PENDING.canTransitionTo(OrderStatus.CANCELLED));
        assertFalse(OrderStatus.PENDING.canTransitionTo(OrderStatus.SHIPPED));
        assertFalse(OrderStatus.PENDING.canTransitionTo(OrderStatus.DELIVERED));
    }

    @Test
    void shippedCanOnlyMoveToDelivered() {
        assertTrue(OrderStatus.SHIPPED.canTransitionTo(OrderStatus.DELIVERED));
        assertFalse(OrderStatus.SHIPPED.canTransitionTo(OrderStatus.CANCELLED));
        assertFalse(OrderStatus.SHIPPED.canTransitionTo(OrderStatus.CONFIRMED));
    }

    @Test
    void terminalStatesHaveNoNextStates() {
        assertTrue(OrderStatus.DELIVERED.allowedNext().isEmpty());
        assertTrue(OrderStatus.CANCELLED.allowedNext().isEmpty());
        assertFalse(OrderStatus.DELIVERED.canTransitionTo(OrderStatus.CANCELLED));
    }

    @Test
    void fromStringIsCaseInsensitiveAndTrimmed() {
        assertEquals(OrderStatus.SHIPPED, OrderStatus.fromString("  shipped "));
        assertEquals(OrderStatus.PENDING, OrderStatus.fromString("PENDING"));
        assertNull(OrderStatus.fromString("nonsense"));
        assertNull(OrderStatus.fromString(null));
    }
}
