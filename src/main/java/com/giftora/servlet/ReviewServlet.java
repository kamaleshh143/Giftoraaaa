package com.giftora.servlet;

import com.giftora.exception.AuthorizationException;
import com.giftora.exception.ValidationException;
import com.giftora.model.User;
import com.giftora.service.ReviewService;
import com.giftora.util.SessionUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/review")
public class ReviewServlet extends BaseServlet {

    private final ReviewService reviewService = new ReviewService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long productId = parseLong(request.getParameter("productId"));
        int rating = parseInt(request.getParameter("rating"), 0);
        String comment = request.getParameter("comment");
        User user = SessionUtil.getCurrentUser(request);
        try {
            reviewService.submitReview(user, productId, rating, comment);
            request.getSession().setAttribute("successMessage", "Thank you! Your review has been posted.");
        } catch (ValidationException ex) {
            request.getSession().setAttribute("errorMessage", ex.getMessage());
        } catch (AuthorizationException ex) {
            request.getSession().setAttribute("errorMessage", ex.getMessage());
        } catch (SQLException ex) {
            request.getSession().setAttribute("errorMessage", "We could not save your review right now.");
        }
        response.sendRedirect(request.getContextPath() + "/product?id=" + productId);
    }

    private long parseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (Exception ex) {
            return -1;
        }
    }

    private int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (Exception ex) {
            return fallback;
        }
    }
}
