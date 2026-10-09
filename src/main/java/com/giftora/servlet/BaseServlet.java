package com.giftora.servlet;

import com.giftora.model.User;
import com.giftora.util.CsrfUtil;
import com.giftora.util.SessionUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;

/**
 * Small helper base class for server-rendered (JSP) servlets.
 */
public abstract class BaseServlet extends HttpServlet {

    protected User currentUser(javax.servlet.http.HttpServletRequest request) {
        return com.giftora.util.SessionUtil.getCurrentUser(request);
    }

    protected void forward(String jsp, javax.servlet.http.HttpServletRequest req,
                           javax.servlet.http.HttpServletResponse resp)
            throws javax.servlet.ServletException, java.io.IOException {
        req.getRequestDispatcher("/WEB-INF/views/" + jsp).forward(req, resp);
    }

    protected void flashSuccess(javax.servlet.http.HttpServletRequest request, String message) {
        com.giftora.util.FlashUtil.success(request, message);
    }

    protected void flashError(javax.servlet.http.HttpServletRequest request, String message) {
        com.giftora.util.FlashUtil.error(request, message);
    }

    protected java.util.Map<Long, Integer> cart(javax.servlet.http.HttpServletRequest request) {
        return com.giftora.util.CartSession.getCart(request);
    }
}
