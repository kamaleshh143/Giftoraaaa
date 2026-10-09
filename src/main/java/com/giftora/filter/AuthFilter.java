package com.giftora.filter;

import com.giftora.model.Role;
import com.giftora.model.User;
import com.giftora.util.SessionUtil;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Enforces login and role requirements on protected areas of the application.
 * Rule-based, so the same rules cannot be bypassed from the browser.
 */
@WebFilter(urlPatterns = {"/*"})
public class AuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
        // no-op
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse)) {
            chain.doFilter(request, response);
            return;
        }
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        String path = req.getRequestURI().substring(req.getContextPath().length());

        if (path.startsWith("/api/")) {
            chain.doFilter(request, response);
            return;
        }

        User user = SessionUtil.getCurrentUser(req);

        if (requiresLogin(path) && user == null) {
            resp.sendRedirect(req.getContextPath() + "/login?message=Please+log+in+to+continue");
            return;
        }

        if (path.startsWith("/seller/") && (user == null
                || (user.getRole() != Role.SELLER && user.getRole() != Role.ADMIN))) {
            deny(req, resp, "Seller access required.");
            return;
        }
        if (path.startsWith("/admin/") && (user == null || user.getRole() != Role.ADMIN)) {
            deny(req, resp, "Administrator access required.");
            return;
        }
        chain.doFilter(request, response);
    }

    private boolean requiresLogin(String path) {
        return path.startsWith("/seller/") || path.startsWith("/admin/")
                || path.startsWith("/account/") || path.startsWith("/checkout")
                || path.startsWith("/orders") || path.startsWith("/order-success")
                || path.startsWith("/review");
    }

    private void deny(HttpServletRequest req, HttpServletResponse resp, String message)
            throws ServletException, IOException {
        resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
        req.setAttribute("errorMessage", message);
        req.getRequestDispatcher("/error.jsp").forward(req, resp);
    }

    @Override
    public void destroy() {
        // no-op
    }
}
