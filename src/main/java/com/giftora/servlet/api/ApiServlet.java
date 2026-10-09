package com.giftora.servlet.api;

import com.giftora.model.User;
import com.giftora.util.CsrfUtil;
import com.giftora.util.JsonUtil;
import com.giftora.util.SessionUtil;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Base class for JSON API servlets. Provides consistent JSON responses and error handling.
 */
public abstract class ApiServlet extends HttpServlet {

    protected void writeJson(HttpServletResponse response, int status, Object body) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json; charset=UTF-8");
        response.getWriter().write(JsonUtil.toJson(body));
    }

    protected void writeError(HttpServletResponse response, int status, String message) throws IOException {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", false);
        body.put("error", message);
        writeJson(response, status, body);
    }

    protected void writeOk(HttpServletResponse response, Object data) throws IOException {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", true);
        body.put("data", data);
        writeJson(response, HttpServletResponse.SC_OK, body);
    }

    protected User currentUser(HttpServletRequest request) {
        return SessionUtil.getCurrentUser(request);
    }

    /**
     * API CSRF check for state-changing, cookie-authenticated requests via X-CSRF-Token header.
     */
    protected boolean csrfValid(HttpServletRequest request) {
        return CsrfUtil.isValid(request);
    }

    protected long parseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (Exception ex) {
            return -1;
        }
    }
}
