package com.giftora.servlet;

import com.giftora.model.Review;
import com.giftora.service.ReviewService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/admin/reviews")
public class AdminReviewsServlet extends BaseServlet {

    private final ReviewService reviewService = new ReviewService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            List<Review> reviews = reviewService.allReviews();
            request.setAttribute("reviews", reviews);
            request.getRequestDispatcher("/WEB-INF/views/admin-reviews.jsp").forward(request, response);
        } catch (SQLException ex) {
            request.setAttribute("errorMessage", "We could not load reviews right now.");
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long reviewId = parseLong(request.getParameter("reviewId"));
        String action = request.getParameter("action");
        try {
            if ("approve".equals(action)) {
                reviewService.moderate(reviewId, true);
                request.getSession().setAttribute("successMessage", "Review approved.");
            } else if ("reject".equals(action)) {
                reviewService.moderate(reviewId, false);
                request.getSession().setAttribute("successMessage", "Review hidden.");
            } else if ("delete".equals(action)) {
                reviewService.delete(reviewId);
                request.getSession().setAttribute("successMessage", "Review deleted.");
            }
        } catch (SQLException ex) {
            request.getSession().setAttribute("errorMessage", "We could not update this review.");
        }
        response.sendRedirect(request.getContextPath() + "/admin/reviews");
    }

    private long parseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (Exception ex) {
            return -1;
        }
    }
}
