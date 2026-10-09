package com.giftora.util;

import javax.servlet.http.HttpServletRequest;

/**
 * Simple flash-message helpers backed by request attributes (session-independent).
 */
public final class FlashUtil {

    public static final String SUCCESS = "successMessage";
    public static final String ERROR = "errorMessage";

    private FlashUtil() {
    }

    public static void success(HttpServletRequest request, String message) {
        request.getSession(true).setAttribute(SUCCESS, message);
    }

    public static void error(HttpServletRequest request, String message) {
        request.getSession(true).setAttribute(ERROR, message);
    }
}
