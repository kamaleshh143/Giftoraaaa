package com.giftora.servlet;

import com.giftora.exception.NotFoundException;
import com.giftora.exception.ValidationException;
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
import java.sql.SQLException;
import java.util.List;

@WebServlet("/admin/orders")
public class AdminOrdersServlet extends BaseServlet {

    private final OrderService orderService = new OrderService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            List<Order> orders = orderService.allOrders();
            request.setAttribute("orders", orders);
            request.setAttribute("statuses", OrderStatus.values());
            request.getRequestDispatcher("/WEB-INF/views/admin-orders.jsp").forward(request, response);
        } catch (SQLException ex) {
            request.setAttribute("errorMessage", "We could not load orders right now.");
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long orderId = parseLong(request.getParameter("orderId"));
        OrderStatus status = OrderStatus.fromString(request.getParameter("status"));
        User admin = SessionUtil.getCurrentUser(request);
        try {
            orderService.updateStatus(admin, orderId, status);
            request.getSession().setAttribute("successMessage", "Order status updated.");
        } catch (ValidationException ex) {
            request.getSession().setAttribute("errorMessage", ex.getMessage());
        } catch (NotFoundException ex) {
            request.getSession().setAttribute("errorMessage", ex.getMessage());
        } catch (Exception ex) {
            request.getSession().setAttribute("errorMessage", "We could not update the order.");
        }
        response.sendRedirect(request.getContextPath() + "/admin/orders");
    }

    private long parseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (Exception ex) {
            return -1;
        }
    }
}
