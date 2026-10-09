package com.giftora.servlet;

import com.giftora.model.Order;
import com.giftora.model.OrderStatus;
import com.giftora.model.User;
import com.giftora.service.OrderService;
import com.giftora.util.SessionUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/order/cancel")
public class OrderCancelServlet extends BaseServlet {

    private final OrderService orderService = new OrderService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long orderId = parseLong(request.getParameter("orderId"));
        User user = SessionUtil.getCurrentUser(request);
        try {
            orderService.cancelByBuyer(user, orderId);
            request.getSession().setAttribute("successMessage", "Your order has been cancelled.");
        } catch (Exception ex) {
            request.getSession().setAttribute("errorMessage", ex.getMessage() == null
                    ? "We could not cancel this order." : ex.getMessage());
        }
        response.sendRedirect(request.getContextPath() + "/order/track?id=" + orderId);
    }

    private long parseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (Exception ex) {
            return -1;
        }
    }
}
