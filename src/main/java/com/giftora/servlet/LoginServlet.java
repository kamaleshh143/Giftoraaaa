package com.giftora.servlet;

import com.giftora.model.Role;
import com.giftora.model.User;
import com.giftora.service.AuthService;
import com.giftora.util.CsrfUtil;
import com.giftora.util.SessionUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

@WebServlet("/login")
public class LoginServlet extends BaseServlet {

    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        CsrfUtil.getToken(request.getSession(true));
        request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        try {
            Optional<User> authenticated = authService.authenticate(email, password);
            if (authenticated.isEmpty()) {
                request.setAttribute("errorMessage", "Invalid email or password.");
                request.setAttribute("email", email);
                request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
                return;
            }
            User user = authenticated.get();

            // Session fixation protection: drop any existing session, then create a fresh one.
            HttpSession oldSession = request.getSession(false);
            if (oldSession != null) {
                oldSession.invalidate();
            }
            HttpSession session = request.getSession(true);
            request.changeSessionId();
            session.setAttribute(SessionUtil.USER_ATTR, user);
            session.setMaxInactiveInterval(30 * 60);
            CsrfUtil.getToken(session);

            request.setAttribute("loginUser", user);
            String target = dashboardFor(user.getRole());
            request.setAttribute("dashboardLink", target);
            request.getRequestDispatcher("/WEB-INF/views/login-success.jsp").forward(request, response);
        } catch (SQLException ex) {
            request.setAttribute("errorMessage", "Something went wrong during login. Please try again.");
            request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
        }
    }

    private String dashboardFor(Role role) {
        if (role == Role.ADMIN) {
            return "admin/dashboard";
        }
        if (role == Role.SELLER) {
            return "seller/dashboard";
        }
        return "products";
    }
}
