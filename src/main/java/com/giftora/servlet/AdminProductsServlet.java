package com.giftora.servlet;

import com.giftora.dao.ProductDao;
import com.giftora.model.Product;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/admin/products")
public class AdminProductsServlet extends BaseServlet {

    private final ProductDao productDao = new ProductDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            List<Product> products = productDao.search(null, null, "newest", true, 0, 500);
            request.setAttribute("products", products);
            request.getRequestDispatcher("/WEB-INF/views/admin-products.jsp").forward(request, response);
        } catch (SQLException ex) {
            request.setAttribute("errorMessage", "We could not load products right now.");
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long productId = parseLong(request.getParameter("productId"));
        String action = request.getParameter("action");
        try {
            Product product = productDao.findById(productId).orElse(null);
            if (product == null) {
                request.getSession().setAttribute("errorMessage", "Product not found.");
            } else if ("toggle".equals(action)) {
                product.setActive(!product.isActive());
                productDao.update(product);
                request.getSession().setAttribute("successMessage", "Product availability updated.");
            } else if ("delete".equals(action)) {
                productDao.delete(productId);
                request.getSession().setAttribute("successMessage", "Product deleted.");
            }
        } catch (SQLException ex) {
            request.getSession().setAttribute("errorMessage", "We could not update this product.");
        }
        response.sendRedirect(request.getContextPath() + "/admin/products");
    }

    private long parseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (Exception ex) {
            return -1;
        }
    }
}
