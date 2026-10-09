package com.giftora.filter;

import com.giftora.util.CsrfUtil;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Set;

/**
 * CSRF protection for state-changing, session-authenticated form requests.
 * JSON API requests are validated separately (see ApiSecurity) and login/register are
 * token-exempt because the session token is created when the page is rendered.
 */
@WebFilter("/*")
public class CsrfFilter implements Filter {

    private static final Set<String> EXEMPT_PATHS = Set.of(
            "/login", "/register", "/logout"
    );

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
        String method = req.getMethod();
        String path = req.getRequestURI().substring(req.getContextPath().length());

        boolean stateChanging = "POST".equalsIgnoreCase(method)
                || "PUT".equalsIgnoreCase(method)
                || "DELETE".equalsIgnoreCase(method);
        boolean isApi = path.startsWith("/api/");
        boolean exempt = EXEMPT_PATHS.contains(path);

        HttpSession session = req.getSession(false);
        boolean loggedIn = session != null && session.getAttribute("currentUser") != null;

        if (stateChanging && loggedIn && !isApi && !exempt) {
            if (!CsrfUtil.isValid(req)) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                req.setAttribute("errorMessage", "Your session has expired or the request was invalid. Please try again.");
                req.getRequestDispatcher("/error.jsp").forward(req, resp);
                return;
            }
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // no-op
    }
}
