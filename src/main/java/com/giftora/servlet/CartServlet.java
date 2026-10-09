package com.giftora.servlet;

import com.giftora.model.Product;
import com.giftora.service.CartService;
import com.giftora.service.ProductService;
import com.giftora.util.CartSession;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

@WebServlet("/cart")
public class CartServlet extends BaseServlet {

    private final CartService cartService = new CartService();
    private final ProductService productService = new ProductService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Map<Long, Integer> cart = CartSession.getCart(request);
        Map<Long, Product> lookup = new LinkedHashMap<>();
        for (Long id : cart.keySet()) {
            try {
                lookup.put(id, productService.getAnyById(id));
            } catch (Exception ignored) {
                // product no longer exists; skip
            }
        }
        request.setAttribute("cartItems", cartService.viewItems(cart, lookup));
        request.setAttribute("cartTotal", cartService.subtotal(cartService.viewItems(cart, lookup)));
        request.setAttribute("cartCount", cartService.totalQuantity(cart));
        request.getRequestDispatcher("/WEB-INF/views/cart.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        Map<Long, Integer> cart = CartSession.getCart(request);
        try {
            long productId = parseLong(request.getParameter("productId"));
            if ("add".equals(action)) {
                int qty = parseInt(request.getParameter("quantity"), 1);
                Product product = productService.getById(productId);
                cartService.addItem(cart, product, qty);
            } else if ("update".equals(action)) {
                int qty = parseInt(request.getParameter("quantity"), 1);
                Product product = productService.getAnyById(productId);
                cartService.setQuantity(cart, product, qty);
            } else if ("remove".equals(action)) {
                cartService.removeItem(cart, productId);
            } else if ("clear".equals(action)) {
                cartService.clear(cart);
            }
        } catch (Exception ex) {
            request.getSession().setAttribute("errorMessage", "We could not update your cart. Please try again.");
        }
        response.sendRedirect(request.getContextPath() + "/cart");
    }

    private long parseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (Exception ex) {
            return -1;
        }
    }

    private int parseInt(String value, int def) {
        try {
            return Integer.parseInt(value);
        } catch (Exception ex) {
            return def;
        }
    }
}
