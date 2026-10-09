package com.giftora.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * CSRF token helpers. A per-session token is generated on demand and validated for
 * all state-changing, session-authenticated operations.
 */
public final class CsrfUtil {

    public static final String SESSION_ATTR = "csrfToken";
    public static final String FORM_FIELD = "csrfToken";
    private static final SecureRandom RANDOM = new SecureRandom();

    private CsrfUtil() {
    }

    public static String getToken(HttpSession session) {
        if (session == null) {
            return null;
        }
        Object existing = session.getAttribute(SESSION_ATTR);
        if (existing instanceof String && !((String) existing).isBlank()) {
            return (String) existing;
        }
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        session.setAttribute(SESSION_ATTR, token);
        return token;
    }

    public static boolean isValid(HttpServletRequest request) {
        if (request == null) {
            return false;
        }
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }
        Object sessionToken = session.getAttribute(SESSION_ATTR);
        String requestToken = request.getParameter(FORM_FIELD);
        if (requestToken == null) {
            requestToken = request.getHeader("X-CSRF-Token");
        }
        if (!(sessionToken instanceof String) || requestToken == null) {
            return false;
        }
        return constantTimeEquals((String) sessionToken, requestToken);
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}
