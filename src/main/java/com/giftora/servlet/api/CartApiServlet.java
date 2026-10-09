package com.giftora.servlet.api;

import com.giftora.model.CartItem;
import com.giftora.model.Product;
import com.giftora.service.CartService;
import com.giftora.service.ProductService;
import com.giftora.util.CartSession;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * GET  /api/v1/cart  - current cart contents
 * POST /api/v1/cart  - add / update / remove / clear
 */
@WebServlet("/api/v1/cart")
public class CartApiServlet extends ApiServlet {

    private final CartService cartService = new CartService();
    private final ProductService productService = new ProductService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<Long, Integer> cart = CartSession.getCart(request);
        Map<Long, Product> lookup = new LinkedHashMap<>();
        for (Long id : cart.keySet()) {
            try {
                lookup.put(id, productService.getAnyById(id));
            } catch (Exception ignored) {
                // skip
            }
        }
        List<CartItem> items = cartService.viewItems(cart, lookup);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("items", items);
        body.put("total", cartService.subtotal(items));
        body.put("count", cartService.totalQuantity(cart));
        writeOk(response, body);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!csrfValid(request)) {
            writeError(response, HttpServletResponse.SC_FORBIDDEN, "Invalid CSRF token.");
            return;
        }
        String action = request.getParameter("action");
        Map<Long, Integer> cart = CartSession.getCart(request);
        try {
            long productId = parseLong(request.getParameter("productId"));
            if ("add".equals(action)) {
                cartService.addItem(cart, productService.getById(productId), parseInt(request.getParameter("quantity")));
            } else if ("update".equals(action)) {
                cartService.setQuantity(cart, productService.getAnyById(productId), parseInt(request.getParameter("quantity")));
            } else if ("remove".equals(action)) {
                cartService.removeItem(cart, productId);
            } else if ("clear".equals(action)) {
                cartService.clear(cart);
            } else {
                writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Unknown action.");
                return;
            }
            writeOk(response, Map.of("count", cartService.totalQuantity(cart)));
        } catch (Exception ex) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Unable to update cart.");
        }
    }

    private int parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception ex) {
            return 1;
        }
    }
}
