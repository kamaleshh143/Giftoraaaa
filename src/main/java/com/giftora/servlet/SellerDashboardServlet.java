package com.giftora.servlet;

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
import java.util.Map;

@WebServlet("/seller/dashboard")
public class SellerDashboardServlet extends BaseServlet {

    private final ProductService productService = new ProductService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User seller = SessionUtil.getCurrentUser(request);
        try {
            List<Product> products = productService.findBySeller(seller.getId());
            int lowStock = 0;
            for (Product p : products) {
                if (p.getStock() <= 5) {
                    lowStock++;
                }
            }
            request.setAttribute("products", products);
            request.setAttribute("productCount", products.size());
            request.setAttribute("lowStockCount", lowStock);
            request.getRequestDispatcher("/WEB-INF/views/seller-dashboard.jsp").forward(request, response);
        } catch (SQLException ex) {
            request.setAttribute("errorMessage", "We could not load your dashboard right now.");
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }
    }
}
