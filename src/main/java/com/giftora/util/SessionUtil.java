package com.giftora.util;

import com.giftora.model.Role;
import com.giftora.model.User;

import javax.servlet.http.HttpServletRequest;

/**
 * Helpers for reading the authenticated user out of the HTTP session.
 */
public final class SessionUtil {

    public static final String USER_ATTR = "currentUser";

    private SessionUtil() {
    }

    public static User getCurrentUser(HttpServletRequest request) {
        if (request == null || request.getSession(false) == null) {
            return null;
        }
        Object value = request.getSession(false).getAttribute(USER_ATTR);
        return value instanceof User ? (User) value : null;
    }

    public static boolean isLoggedIn(HttpServletRequest request) {
        return getCurrentUser(request) != null;
    }

    public static boolean hasRole(HttpServletRequest request, Role role) {
        User user = getCurrentUser(request);
        return user != null && user.getRole() == role;
    }

    public static boolean isAdmin(HttpServletRequest request) {
        return hasRole(request, Role.ADMIN);
    }

    public static boolean isSeller(HttpServletRequest request) {
        return hasRole(request, Role.SELLER);
    }

    public static boolean isBuyer(HttpServletRequest request) {
        return hasRole(request, Role.BUYER);
    }
}
