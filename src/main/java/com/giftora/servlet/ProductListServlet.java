package com.giftora.servlet;

import com.giftora.model.Product;
import com.giftora.service.ProductService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/products")
public class ProductListServlet extends BaseServlet {

    private final ProductService productService = new ProductService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String query = request.getParameter("q");
        String category = request.getParameter("category");
        String sort = request.getParameter("sort");
        int page = parsePage(request.getParameter("page"));
        try {
            List<Product> products = productService.browse(query, category, sort, page);
            int total = productService.count(query, category);
            int totalPages = Math.max(1, (int) Math.ceil(total / (double) ProductService.PAGE_SIZE));
            request.setAttribute("products", products);
            request.setAttribute("categories", productService.categories());
            request.setAttribute("query", query == null ? "" : query);
            request.setAttribute("selectedCategory", category == null ? "" : category);
            request.setAttribute("sort", sort == null ? "newest" : sort);
            request.setAttribute("page", page);
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("totalResults", total);
            request.getRequestDispatcher("/WEB-INF/views/products.jsp").forward(request, response);
        } catch (SQLException ex) {
            request.setAttribute("errorMessage", "We could not load products right now. Please try again.");
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }
    }

    private int parsePage(String value) {
        try {
            int page = Integer.parseInt(value);
            return Math.max(page, 1);
        } catch (Exception ex) {
            return 1;
        }
    }
}
