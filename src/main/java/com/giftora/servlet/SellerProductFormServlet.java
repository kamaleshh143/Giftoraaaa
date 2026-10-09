package com.giftora.servlet;

import com.giftora.exception.AuthorizationException;
import com.giftora.exception.NotFoundException;
import com.giftora.exception.ValidationException;
import com.giftora.model.Product;
import com.giftora.model.User;
import com.giftora.service.ProductService;
import com.giftora.util.CsrfUtil;
import com.giftora.util.SessionUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/seller/product")
public class SellerProductFormServlet extends BaseServlet {

    private final ProductService productService = new ProductService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idParam = request.getParameter("id");
        User seller = SessionUtil.getCurrentUser(request);
        try {
            if (idParam != null && !idParam.isBlank()) {
                Product product = productService.getAnyById(parseLong(idParam));
                if (seller.isSeller() && (product.getSellerId() == null || product.getSellerId() != seller.getId())) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    request.setAttribute("errorMessage", "You can only edit your own products.");
                    request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
                    return;
                }
                request.setAttribute("product", product);
            }
            request.setAttribute("categories", productService.categories());
            com.giftora.util.CsrfUtil.getToken(request.getSession(true));
            request.getRequestDispatcher("/WEB-INF/views/product-form.jsp").forward(request, response);
        } catch (NotFoundException ex) {
            request.setAttribute("errorMessage", "That product could not be found.");
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        } catch (SQLException ex) {
            request.setAttribute("errorMessage", "We could not load the product form.");
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User seller = SessionUtil.getCurrentUser(request);
        String idParam = request.getParameter("id");
        boolean isEdit = idParam != null && !idParam.isBlank();
        try {
            if (isEdit) {
                productService.update(seller, parseLong(idParam), request.getParameter("name"),
                        request.getParameter("description"), request.getParameter("price"),
                        request.getParameter("category"), request.getParameter("imageUrl"),
                        request.getParameter("stock"),
                        !"false".equalsIgnoreCase(request.getParameter("active")));
            } else {
                productService.create(seller, request.getParameter("name"), request.getParameter("description"),
                        request.getParameter("price"), request.getParameter("category"),
                        request.getParameter("imageUrl"), request.getParameter("stock"));
            }
            request.getSession().setAttribute("successMessage", isEdit ? "Product updated." : "Product added.");
            response.sendRedirect(request.getContextPath() + "/seller/products");
        } catch (ValidationException ex) {
            request.setAttribute("errorMessage", ex.getMessage());
            try {
                request.setAttribute("categories", new ProductService().categories());
            } catch (SQLException ignored) {
                // keep form usable even if categories fail
            }
            request.getRequestDispatcher("/WEB-INF/views/product-form.jsp").forward(request, response);
        } catch (AuthorizationException ex) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            request.setAttribute("errorMessage", ex.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        } catch (NotFoundException | SQLException ex) {
            request.setAttribute("errorMessage", "We could not save the product right now.");
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
