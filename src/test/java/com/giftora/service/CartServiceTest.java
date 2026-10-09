package com.giftora.service;

import com.giftora.model.CartItem;
import com.giftora.model.Product;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CartServiceTest {

    private final CartService cartService = new CartService();

    private Product product(long id, String price, int stock) {
        Product p = new Product();
        p.setId(id);
        p.setName("Product " + id);
        p.setPrice(new BigDecimal(price));
        p.setStock(stock);
        p.setActive(true);
        return p;
    }

    @Test
    void addItemAccumulatesAndCapsAtStock() {
        Map<Long, Integer> cart = new LinkedHashMap<>();
        Product p = product(1, "100.00", 3);

        cartService.addItem(cart, p, 2);
        assertEquals(2, cart.get(1L));

        cartService.addItem(cart, p, 5);
        assertEquals(3, cart.get(1L), "quantity must not exceed available stock");
    }

    @Test
    void addIgnoresUnavailableProducts() {
        Map<Long, Integer> cart = new LinkedHashMap<>();
        Product inactive = product(2, "50.00", 10);
        inactive.setActive(false);

        cartService.addItem(cart, inactive, 1);
        assertTrue(cart.isEmpty());
    }

    @Test
    void quantityIsCappedAtPerItemMaximum() {
        Map<Long, Integer> cart = new LinkedHashMap<>();
        Product p = product(3, "10.00", 1000);
        cartService.addItem(cart, p, 999);
        assertEquals(CartService.MAX_QUANTITY_PER_ITEM, cart.get(3L));
    }

    @Test
    void setQuantityToZeroRemovesItem() {
        Map<Long, Integer> cart = new LinkedHashMap<>();
        Product p = product(4, "20.00", 10);
        cartService.addItem(cart, p, 2);

        cartService.setQuantity(cart, p, 0);
        assertFalse(cart.containsKey(4L));
    }

    @Test
    void totalQuantitySumsAllLines() {
        Map<Long, Integer> cart = new LinkedHashMap<>();
        cart.put(1L, 2);
        cart.put(2L, 3);
        assertEquals(5, cartService.totalQuantity(cart));
    }

    @Test
    void viewItemsAndSubtotalComputeLineTotals() {
        Map<Long, Integer> cart = new LinkedHashMap<>();
        cart.put(1L, 2);
        cart.put(2L, 1);

        Map<Long, Product> lookup = new LinkedHashMap<>();
        lookup.put(1L, product(1, "100.00", 10));
        lookup.put(2L, product(2, "50.00", 10));

        List<CartItem> items = cartService.viewItems(cart, lookup);
        assertEquals(2, items.size());
        assertEquals(new BigDecimal("250.00"), cartService.subtotal(items));
    }

    @Test
    void viewItemsSkipsMissingProducts() {
        Map<Long, Integer> cart = new LinkedHashMap<>();
        cart.put(99L, 1);
        List<CartItem> items = cartService.viewItems(cart, new LinkedHashMap<>());
        assertTrue(items.isEmpty());
    }
}
