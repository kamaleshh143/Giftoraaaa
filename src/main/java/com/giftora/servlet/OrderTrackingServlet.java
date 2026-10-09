package com.giftora.servlet;

import com.giftora.exception.AuthorizationException;
import com.giftora.exception.NotFoundException;
import com.giftora.model.Order;
import com.giftora.model.User;
import com.giftora.service.OrderService;
import com.giftora.util.CsrfUtil;
import com.giftora.util.SessionUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/order/track")
public class OrderTrackingServlet extends BaseServlet {

    private final OrderService orderService = new OrderService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long id = parseLong(request.getParameter("id"));
        User user = SessionUtil.getCurrentUser(request);
        try {
            Order order = orderService.getById(id);
            if (user == null || (!user.isAdmin() && order.getUserId() != user.getId())) {
                throw new AuthorizationException("You are not allowed to view this order.");
            }
            request.setAttribute("order", order);
            CsrfUtil.getToken(request.getSession(true));
            request.getRequestDispatcher("/WEB-INF/views/order-tracking.jsp").forward(request, response);
        } catch (NotFoundException ex) {
            request.setAttribute("errorMessage", "That order could not be found.");
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        } catch (AuthorizationException ex) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            request.setAttribute("errorMessage", ex.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        } catch (SQLException ex) {
            request.setAttribute("errorMessage", "We could not load this order right now.");
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
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
