package com.giftora.servlet;

import com.giftora.exception.AuthorizationException;
import com.giftora.exception.NotFoundException;
import com.giftora.exception.ValidationException;
import com.giftora.model.Order;
import com.giftora.model.OrderStatus;
import com.giftora.model.User;
import com.giftora.service.OrderService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/seller/orders")
public class SellerOrdersServlet extends BaseServlet {

    private final OrderService orderService = new OrderService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User seller = com.giftora.util.SessionUtil.getCurrentUser(request);
        try {
            List<Order> orders = orderService.sellerOrders(seller);
            request.setAttribute("orders", orders);
            request.setAttribute("statuses", OrderStatus.values());
            request.getRequestDispatcher("/WEB-INF/views/seller-orders.jsp").forward(request, response);
        } catch (AuthorizationException | SQLException ex) {
            request.setAttribute("errorMessage", "We could not load orders right now.");
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User seller = com.giftora.util.SessionUtil.getCurrentUser(request);
        long orderId = parseLong(request.getParameter("orderId"));
        OrderStatus status = OrderStatus.fromString(request.getParameter("status"));
        try {
            orderService.updateStatus(seller, orderId, status);
            request.getSession().setAttribute("successMessage", "Order status updated.");
        } catch (ValidationException | AuthorizationException ex) {
            request.getSession().setAttribute("errorMessage", ex.getMessage());
        } catch (NotFoundException ex) {
            request.getSession().setAttribute("errorMessage", ex.getMessage());
        } catch (SQLException ex) {
            request.getSession().setAttribute("errorMessage", "We could not update the order right now.");
        }
        response.sendRedirect(request.getContextPath() + "/seller/orders");
    }

    private long parseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (Exception ex) {
            return -1;
        }
    }
}
