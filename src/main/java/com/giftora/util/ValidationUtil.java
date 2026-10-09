package com.giftora.util;

import java.util.regex.Pattern;

/**
 * Small, dependency-free input validation helpers used by services and servlets.
 */
public final class ValidationUtil {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern SAFE_IMAGE_URL =
            Pattern.compile("^(https?://|/|data:image/).*$", Pattern.CASE_INSENSITIVE);

    private ValidationUtil() {
    }

    public static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static boolean isNotBlank(String value) {
        return !isBlank(value);
    }

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 8;
    }

    public static boolean isValidRating(int rating) {
        return rating >= 1 && rating <= 5;
    }

    public static boolean isValidImageUrl(String url) {
        if (isBlank(url)) {
            return true;
        }
        String trimmed = url.trim();
        if (url.length() > 2000) {
            return false;
        }
        return SAFE_IMAGE_URL.matcher(trimmed).matches();
    }

    public static String trimToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }

    /**
     * Escapes text used inside HTML attribute contexts if it must be rendered outside JSTL.
     */
    public static String escapeHtml(String input) {
        if (input == null) {
            return "";
        }
        return input
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
