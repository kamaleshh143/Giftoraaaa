package com.giftora.util;

import javax.servlet.http.HttpSession;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Stores the shopping cart in the HTTP session as a product id -> quantity map.
 */
public final class CartSession {

    public static final String CART_ATTR = "cart";

    private CartSession() {
    }

    @SuppressWarnings("unchecked")
    public static Map<Long, Integer> getCart(javax.servlet.http.HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        Object existing = session.getAttribute(CART_ATTR);
        if (existing instanceof Map) {
            return (Map<Long, Integer>) existing;
        }
        Map<Long, Integer> cart = new LinkedHashMap<>();
        session.setAttribute(CART_ATTR, cart);
        return cart;
    }
}
