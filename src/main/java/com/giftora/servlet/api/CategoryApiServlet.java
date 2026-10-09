package com.giftora.servlet.api;

import com.giftora.service.ProductService;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/api/v1/categories")
public class CategoryApiServlet extends ApiServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            writeOk(response, new ProductService().categories());
        } catch (SQLException ex) {
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to load categories.");
        }
    }
}
