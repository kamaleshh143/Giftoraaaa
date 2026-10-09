package com.giftora.service;

import com.giftora.model.CartItem;
import com.giftora.model.Product;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Session cart operations. The cart is stored as productId -> quantity.
 * Prices are always re-read from the catalogue; the browser never supplies prices.
 */
public class CartService {

    public static final int MAX_QUANTITY_PER_ITEM = 20;

    public void addItem(Map<Long, Integer> cart, Product product, int quantity) {
        if (product == null || !product.isAvailable()) {
            return;
        }
        int current = cart.getOrDefault(product.getId(), 0);
        int desired = current + Math.max(quantity, 1);
        int capped = Math.min(desired, Math.min(MAX_QUANTITY_PER_ITEM, product.getStock()));
        cart.put(product.getId(), Math.max(capped, 1));
    }

    public void setQuantity(Map<Long, Integer> cart, Product product, int quantity) {
        if (product == null) {
            cart.remove(productId(cart, product));
            return;
        }
        if (quantity <= 0) {
            cart.remove(product.getId());
            return;
        }
        int capped = Math.min(quantity, Math.min(MAX_QUANTITY_PER_ITEM, Math.max(product.getStock(), 0)));
        if (capped <= 0) {
            cart.remove(product.getId());
        } else {
            cart.put(product.getId(), capped);
        }
    }

    public void removeItem(Map<Long, Integer> cart, long productId) {
        cart.remove(productId);
    }

    public void clear(Map<Long, Integer> cart) {
        cart.clear();
    }

    public int totalQuantity(Map<Long, Integer> cart) {
        int total = 0;
        for (Integer q : cart.values()) {
            if (q != null) {
                total += q;
            }
        }
        return total;
    }

    /**
     * Builds the view model for the cart page using the provided product lookup.
     * Items whose product no longer exists are skipped.
     */
    public List<CartItem> viewItems(Map<Long, Integer> cart, Map<Long, Product> productLookup) {
        List<CartItem> items = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : cart.entrySet()) {
            Product product = productLookup.get(entry.getKey());
            if (product != null) {
                items.add(new CartItem(product, entry.getValue()));
            }
        }
        return items;
    }

    public BigDecimal subtotal(List<CartItem> items) {
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : items) {
            total = total.add(item.getLineTotal());
        }
        return total;
    }

    private long productId(Map<Long, Integer> cart, Product product) {
        return product == null ? -1 : product.getId();
    }
}
