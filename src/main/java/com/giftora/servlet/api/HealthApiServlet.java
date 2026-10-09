package com.giftora.servlet.api;

import com.giftora.dao.Database;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * GET /api/v1/health - reports application and database status.
 * Always returns JSON; database failure yields HTTP 503.
 */
@WebServlet("/api/v1/health")
public class HealthApiServlet extends ApiServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        boolean dbHealthy = Database.getInstance().isHealthy();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", dbHealthy ? "UP" : "DOWN");
        body.put("application", "Giftora");
        body.put("database", dbHealthy ? "UP" : "DOWN");
        body.put("timestamp", Instant.now().toString());
        if (!dbHealthy) {
            writeJson(response, HttpServletResponse.SC_SERVICE_UNAVAILABLE, body);
        } else {
            writeJson(response, HttpServletResponse.SC_OK, body);
        }
    }
}
