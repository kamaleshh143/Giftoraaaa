package com.giftora.servlet.api;

import com.giftora.model.User;
import com.giftora.service.AuthService;
import com.giftora.util.CsrfUtil;
import com.giftora.util.SessionUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Versioned auth API:
 *   POST /api/v1/auth/login
 *   POST /api/v1/auth/logout
 *   GET  /api/v1/auth/me
 */
@WebServlet("/api/v1/auth/*")
public class AuthApiServlet extends ApiServlet {

    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String path = request.getPathInfo();
        if ("/me".equals(path)) {
            User user = currentUser(request);
            if (user == null) {
                writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "Not logged in.");
                return;
            }
            writeOk(response, safeUser(user));
            return;
        }
        writeError(response, HttpServletResponse.SC_NOT_FOUND, "Unknown endpoint.");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String path = request.getPathInfo();
        if ("/login".equals(path)) {
            doLogin(request, response);
        } else if ("/logout".equals(path)) {
            doLogout(request, response);
        } else {
            writeError(response, HttpServletResponse.SC_NOT_FOUND, "Unknown endpoint.");
        }
    }

    private void doLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            Optional<User> authenticated = authService.authenticate(
                    request.getParameter("email"), request.getParameter("password"));
            if (authenticated.isEmpty()) {
                writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid email or password.");
                return;
            }
            HttpSession old = request.getSession(false);
            if (old != null) {
                old.invalidate();
            }
            HttpSession session = request.getSession(true);
            request.changeSessionId();
            session.setAttribute(SessionUtil.USER_ATTR, authenticated.get());
            writeOk(response, safeUser(authenticated.get()));
        } catch (SQLException ex) {
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Login failed.");
        }
    }

    private void doLogout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        writeOk(response, Map.of("message", "Logged out."));
    }

    private Map<String, Object> safeUser(User user) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", user.getId());
        map.put("name", user.getName());
        map.put("email", user.getEmail());
        map.put("role", user.getRole().name());
        return map;
    }
}
