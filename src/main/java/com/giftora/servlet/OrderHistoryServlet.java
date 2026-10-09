package com.giftora.servlet;

import com.giftora.exception.AuthorizationException;
import com.giftora.model.Order;
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

@WebServlet("/orders")
public class OrderHistoryServlet extends BaseServlet {

    private final OrderService orderService = new OrderService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = SessionUtil.getCurrentUser(request);
        try {
            List<Order> orders = orderService.buyerOrders(user);
            request.setAttribute("orders", orders);
            request.getRequestDispatcher("/WEB-INF/views/order-history.jsp").forward(request, response);
        } catch (SQLException ex) {
            request.setAttribute("errorMessage", "We could not load your orders right now.");
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }
    }
}
