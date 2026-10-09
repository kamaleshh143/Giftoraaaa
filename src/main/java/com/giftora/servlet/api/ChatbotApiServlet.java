package com.giftora.servlet.api;

import com.giftora.exception.RateLimitException;
import com.giftora.exception.ValidationException;
import com.giftora.listener.AppContextListener;
import com.giftora.model.Product;
import com.giftora.service.ChatbotService;
import com.giftora.service.ProductService;
import com.giftora.util.JsonUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * POST /api/v1/chatbot - shopping assistant endpoint.
 * Accepts a JSON body {"message": "..."} or a form parameter "message".
 */
@WebServlet("/api/v1/chatbot")
public class ChatbotApiServlet extends ApiServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String message = extractMessage(request);
        ChatbotService chatbot = (ChatbotService) getServletContext().getAttribute(AppContextListener.CHATBOT_ATTR);
        if (chatbot == null) {
            writeError(response, HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Chatbot is not available.");
            return;
        }
        List<Product> catalog;
        try {
            catalog = new ProductService().browseAll(null, null, "newest");
        } catch (SQLException ex) {
            catalog = java.util.Collections.emptyList();
        }
        try {
            String reply = chatbot.reply(request.getSession(true).getId(), message, catalog);
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("reply", reply);
            body.put("provider", chatbot.providerName());
            writeOk(response, body);
        } catch (ValidationException ex) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());
        } catch (RateLimitException ex) {
            writeError(response, 429, ex.getMessage());
        }
    }

    private String extractMessage(HttpServletRequest request) {
        String contentType = request.getContentType();
        if (contentType != null && contentType.contains("application/json")) {
            try {
                StringBuilder sb = new StringBuilder();
                String line;
                java.io.BufferedReader reader = request.getReader();
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                if (sb.length() > 0) {
                    Map<?, ?> parsed = JsonUtil.fromJson(sb.toString(), Map.class);
                    Object value = parsed.get("message");
                    return value == null ? null : value.toString();
                }
            } catch (Exception ignored) {
                // fall through to form parameter
            }
        }
        return request.getParameter("message");
    }
}
