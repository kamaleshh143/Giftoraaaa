package com.giftora.servlet;

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
import java.util.Optional;

@WebServlet("/order-success")
public class OrderSuccessServlet extends BaseServlet {

    private final OrderService orderService = new OrderService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String orderNumber = request.getParameter("order");
        User user = SessionUtil.getCurrentUser(request);
        try {
            Optional<Order> order = orderService.findByOrderNumber(orderNumber);
            if (order.isEmpty() || user == null || order.get().getUserId() != user.getId()) {
                request.setAttribute("errorMessage", "We could not find that order.");
                request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
                return;
            }
            request.setAttribute("order", order.get());
            request.getRequestDispatcher("/WEB-INF/views/order-success.jsp").forward(request, response);
        } catch (SQLException ex) {
            request.setAttribute("errorMessage", "We could not load your order.");
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }
    }
}
