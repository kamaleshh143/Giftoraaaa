package com.giftora.servlet;

import com.giftora.dao.UserDao;
import com.giftora.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/admin/users")
public class AdminUsersServlet extends BaseServlet {

    private final UserDao userDao = new UserDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            List<User> users = userDao.findAll();
            request.setAttribute("users", users);
            request.getRequestDispatcher("/WEB-INF/views/admin-users.jsp").forward(request, response);
        } catch (SQLException ex) {
            request.setAttribute("errorMessage", "We could not load users right now.");
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long userId = parseLong(request.getParameter("userId"));
        boolean active = "true".equalsIgnoreCase(request.getParameter("active"));
        User admin = com.giftora.util.SessionUtil.getCurrentUser(request);
        if (admin != null && admin.getId() == userId) {
            request.getSession().setAttribute("errorMessage", "You cannot deactivate your own account.");
            response.sendRedirect(request.getContextPath() + "/admin/users");
            return;
        }
        try {
            userDao.updateActive(userId, active);
            request.getSession().setAttribute("successMessage", "User access updated.");
        } catch (SQLException ex) {
            request.getSession().setAttribute("errorMessage", "We could not update this user.");
        }
        response.sendRedirect(request.getContextPath() + "/admin/users");
    }

    private long parseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (Exception ex) {
            return -1;
        }
    }
}
