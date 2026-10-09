package com.giftora.servlet;

import com.giftora.exception.ValidationException;
import com.giftora.model.Order;
import com.giftora.model.Product;
import com.giftora.model.User;
import com.giftora.service.CartService;
import com.giftora.service.OrderService;
import com.giftora.service.ProductService;
import com.giftora.util.CartSession;
import com.giftora.util.CsrfUtil;
import com.giftora.util.SessionUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@WebServlet("/checkout")
public class CheckoutServlet extends BaseServlet {

    private final CartService cartService = new CartService();
    private final ProductService productService = new ProductService();
    private final OrderService orderService = new OrderService();
    private static final String CHECKOUT_TOKEN = "checkoutToken";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Map<Long, Integer> cart = CartSession.getCart(request);
        if (cart.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/cart");
            return;
        }
        Map<Long, Product> lookup = new LinkedHashMap<>();
        for (Long id : cart.keySet()) {
            try {
                lookup.put(id, productService.getAnyById(id));
            } catch (Exception ignored) {
                // skip missing
            }
        }
        request.setAttribute("cartItems", cartService.viewItems(cart, lookup));
        request.setAttribute("cartTotal", cartService.subtotal(cartService.viewItems(cart, lookup)));
        String token = java.util.UUID.randomUUID().toString();
        request.getSession(true).setAttribute("checkoutToken", token);
        request.setAttribute("checkoutToken", token);
        com.giftora.util.CsrfUtil.getToken(request.getSession(true));
        request.getRequestDispatcher("/WEB-INF/views/checkout.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Map<Long, Integer> cart = CartSession.getCart(request);
        String submittedToken = request.getParameter("checkoutToken");
        Object sessionToken = request.getSession().getAttribute("checkoutToken");
        if (sessionToken == null || !sessionToken.equals(submittedToken)) {
            request.getSession().setAttribute("errorMessage",
                    "This order was already submitted. Please check your order history.");
            response.sendRedirect(request.getContextPath() + "/orders");
            return;
        }
        request.getSession().removeAttribute("checkoutToken");

        String name = request.getParameter("shippingName");
        String address = request.getParameter("shippingAddress");
        String phone = request.getParameter("shippingPhone");
        com.giftora.model.User user = com.giftora.util.SessionUtil.getCurrentUser(request);
        try {
            com.giftora.model.Order order = new com.giftora.service.OrderService()
                    .checkout(user, cart, name, address, phone);
            cartService.clear(cart);
            response.sendRedirect(request.getContextPath() + "/order-success?order=" + order.getOrderNumber());
        } catch (com.giftora.exception.ValidationException ex) {
            request.setAttribute("errorMessage", ex.getMessage());
            request.setAttribute("shippingName", name);
            request.setAttribute("shippingAddress", address);
            request.setAttribute("shippingPhone", phone);
            doGet(request, response);
        } catch (SQLException ex) {
            request.setAttribute("errorMessage", "We could not place your order right now. Please try again.");
            doGet(request, response);
        }
    }

    private long parseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (Exception ex) {
            return -1;
        }
    }
}
