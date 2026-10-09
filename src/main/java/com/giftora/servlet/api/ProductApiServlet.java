package com.giftora.servlet.api;

import com.giftora.exception.NotFoundException;
import com.giftora.model.Product;
import com.giftora.service.ProductService;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

/**
 * GET /api/v1/products        - list products (q, category, sort, page)
 * GET /api/v1/products?id=123 - single product
 */
@WebServlet("/api/v1/products")
public class ProductApiServlet extends ApiServlet {

    private final com.giftora.service.ProductService productService = new com.giftora.service.ProductService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String idParam = request.getParameter("id");
            if (idParam != null && !idParam.isBlank()) {
                try {
                    Product product = new com.giftora.service.ProductService().getById(parseLong(idParam));
                    writeOk(response, product);
                } catch (NotFoundException ex) {
                    writeError(response, HttpServletResponse.SC_NOT_FOUND, ex.getMessage());
                }
                return;
            }
            String query = request.getParameter("q");
            String category = request.getParameter("category");
            String sort = request.getParameter("sort");
            java.util.List<Product> products = new com.giftora.service.ProductService().browseAll(query, category, sort);
            writeOk(response, products);
        } catch (java.sql.SQLException ex) {
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to load products.");
        }
    }
}
