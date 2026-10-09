package com.giftora.servlet;

import com.giftora.exception.ValidationException;
import com.giftora.model.User;
import com.giftora.service.AuthService;
import com.giftora.util.CsrfUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/register")
public class RegisterServlet extends BaseServlet {

    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        CsrfUtil.getToken(request.getSession(true));
        request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");
        String role = request.getParameter("role");
        try {
            User user = authService.register(name, email, password, confirmPassword, role);
            request.setAttribute("registeredName", user.getName());
            request.setAttribute("registeredRole", user.getRole().name());
            request.getRequestDispatcher("/WEB-INF/views/register-success.jsp").forward(request, response);
        } catch (ValidationException ex) {
            request.setAttribute("errorMessage", ex.getMessage());
            request.setAttribute("name", name);
            request.setAttribute("email", email);
            request.setAttribute("role", role);
            request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
        } catch (SQLException ex) {
            request.setAttribute("errorMessage", "We could not create your account right now. Please try again.");
            request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
        }
    }
}
