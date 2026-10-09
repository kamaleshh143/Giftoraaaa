package com.giftora.servlet;

import com.giftora.dao.ProductDao;
import com.giftora.dao.UserDao;
import com.giftora.service.OrderService;
import com.giftora.service.ReviewService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/admin/dashboard")
public class AdminDashboardServlet extends BaseServlet {

    private final UserDao userDao = new UserDao();
    private final ProductDao productDao = new ProductDao();
    private final OrderService orderService = new OrderService();
    private final ReviewService reviewService = new ReviewService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            request.setAttribute("userCount", userDao.findAll().size());
            request.setAttribute("productCount", productDao.count(null, null, true));
            request.setAttribute("orderCount", orderService.allOrders().size());
            request.setAttribute("reviewCount", reviewService.allReviews().size());
            request.getRequestDispatcher("/WEB-INF/views/admin-dashboard.jsp").forward(request, response);
        } catch (SQLException ex) {
            request.setAttribute("errorMessage", "We could not load the admin dashboard.");
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }
    }
}
