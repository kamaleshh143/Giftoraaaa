package com.giftora.servlet;

import com.giftora.exception.AuthorizationException;
import com.giftora.exception.NotFoundException;
import com.giftora.exception.ValidationException;
import com.giftora.model.Product;
import com.giftora.model.User;
import com.giftora.service.ProductService;
import com.giftora.util.SessionUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/seller/products")
public class SellerProductsServlet extends BaseServlet {

    private final ProductService productService = new ProductService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User seller = SessionUtil.getCurrentUser(request);
        try {
            List<Product> products = productService.findBySeller(seller.getId());
            request.setAttribute("products", products);
            request.getRequestDispatcher("/WEB-INF/views/seller-products.jsp").forward(request, response);
        } catch (SQLException ex) {
            request.setAttribute("errorMessage", "We could not load your products right now.");
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        User seller = SessionUtil.getCurrentUser(request);
        try {
            if ("delete".equals(action)) {
                productService.delete(seller, parseLong(request.getParameter("id")));
                flashSuccess(request, "Product deleted.");
            } else if ("toggle".equals(action)) {
                long id = Long.parseLong(request.getParameter("id"));
                Product product = productService.getAnyById(id);
                productService.update(user(seller), id, product.getName(), product.getDescription(),
                        product.getPrice().toPlainString(), product.getCategory(), product.getImageUrl(),
                        String.valueOf(product.getStock()), !product.isActive());
                request.getSession().setAttribute("successMessage", "Product availability updated.");
            }
        } catch (AuthorizationException ex) {
            request.getSession().setAttribute("errorMessage", ex.getMessage());
        } catch (NotFoundException ex) {
            request.getSession().setAttribute("errorMessage", ex.getMessage());
        } catch (ValidationException ex) {
            request.getSession().setAttribute("errorMessage", ex.getMessage());
        } catch (SQLException ex) {
            request.getSession().setAttribute("errorMessage", "We could not update the product right now.");
        }
        response.sendRedirect(request.getContextPath() + "/seller/products");
    }

    private User user(User seller) {
        return seller;
    }

    private long parseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (Exception ex) {
            return -1;
        }
    }
}
