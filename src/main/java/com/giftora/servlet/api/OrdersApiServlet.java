package com.giftora.servlet.api;

import com.giftora.exception.ValidationException;
import com.giftora.model.Order;
import com.giftora.model.User;
import com.giftora.service.OrderService;
import com.giftora.util.CartSession;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * GET  /api/v1/orders  - order history for the logged-in buyer
 * POST /api/v1/orders  - checkout (mock payment)
 */
@WebServlet("/api/v1/orders")
public class OrdersApiServlet extends ApiServlet {

    private final OrderService orderService = new OrderService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = currentUser(request);
        if (user == null) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "Please log in.");
            return;
        }
        try {
            writeOk(response, orderService.buyerOrders(user));
        } catch (SQLException ex) {
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to load orders.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = currentUser(request);
        if (user == null) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "Please log in.");
            return;
        }
        if (!csrfValid(request)) {
            writeError(response, HttpServletResponse.SC_FORBIDDEN, "Invalid CSRF token.");
            return;
        }
        try {
            Order order = orderService.checkout(user, CartSession.getCart(request),
                    request.getParameter("shippingName"), request.getParameter("shippingAddress"),
                    request.getParameter("shippingPhone"));
            CartSession.getCart(request).clear();
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("orderNumber", order.getOrderNumber());
            body.put("total", order.getTotal());
            body.put("status", order.getStatus().name());
            writeOk(response, body);
        } catch (ValidationException ex) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());
        } catch (SQLException ex) {
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to place order.");
        }
    }
}
