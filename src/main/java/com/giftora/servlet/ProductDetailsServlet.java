package com.giftora.servlet;

import com.giftora.exception.NotFoundException;
import com.giftora.model.Product;
import com.giftora.model.Review;
import com.giftora.model.User;
import com.giftora.service.ProductService;
import com.giftora.service.ReviewService;
import com.giftora.util.CsrfUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/product")
public class ProductDetailsServlet extends BaseServlet {

    private final ProductService productService = new ProductService();
    private final ReviewService reviewService = new ReviewService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long id = parseId(request.getParameter("id"));
        try {
            Product product = productService.getById(id);
            List<Review> reviews = reviewService.approvedForProduct(id);
            User user = com.giftora.util.SessionUtil.getCurrentUser(request);
            boolean canReview = false;
            if (user != null && user.isBuyer()) {
                canReview = reviewService.hasDeliveredPurchase(user.getId(), id)
                        && !reviewService.alreadyReviewed(user.getId(), id);
            }
            request.setAttribute("product", product);
            request.setAttribute("reviews", reviews);
            request.setAttribute("canReview", canReview);
            CsrfUtil.getToken(request.getSession(true));
            request.getRequestDispatcher("/WEB-INF/views/product-details.jsp").forward(request, response);
        } catch (NotFoundException ex) {
            request.setAttribute("errorMessage", "That product could not be found.");
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        } catch (SQLException ex) {
            request.setAttribute("errorMessage", "We could not load this product right now.");
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }
    }

    private long parseId(String value) {
        try {
            return Long.parseLong(value);
        } catch (Exception ex) {
            return -1;
        }
    }
}
