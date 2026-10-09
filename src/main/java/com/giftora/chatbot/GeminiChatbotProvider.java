package com.giftora.chatbot;

import com.giftora.config.AppConfig;
import com.giftora.model.Product;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Gemini provider that calls the Google Generative Language REST API directly using
 * HttpURLConnection and Gson. No third-party SDK is used. The API key is read from the
 * server-side environment variable GEMINI_API_KEY and is never exposed to the browser.
 */
public class GeminiChatbotProvider implements ChatbotProvider {

    private static final String ENDPOINT =
            "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent";
    private static final int CONNECT_TIMEOUT_MS = 5000;
    private static final int READ_TIMEOUT_MS = 12000;

    private final String apiKey;
    private final String model;

    public GeminiChatbotProvider() {
        this(AppConfig.getGeminiApiKey(), AppConfig.getGeminiModel());
    }

    public GeminiChatbotProvider(String apiKey, String model) {
        this.apiKey = apiKey;
        this.model = model;
    }

    @Override
    public String getProviderName() {
        return "gemini";
    }

    @Override
    public String generateReply(String userMessage, List<Product> catalog) throws ChatbotException {
        if (apiKey == null || apiKey.isBlank()) {
            throw new ChatbotException("Gemini API key is not configured");
        }
        try {
            String prompt = buildPrompt(userMessage, catalog);
            JsonObject body = new JsonObject();
            JsonArray contents = new JsonArray();
            JsonObject content = new JsonObject();
            JsonArray parts = new JsonArray();
            JsonObject part = new JsonObject();
            part.addProperty("text", prompt);
            parts.add(part);
            content.add("parts", parts);
            contents.add(content);
            body.add("contents", contents);

            JsonObject generationConfig = new JsonObject();
            generationConfig.addProperty("maxOutputTokens", 300);
            generationConfig.addProperty("temperature", 0.6);
            body.add("generationConfig", generationConfig);

            String jsonBody = body.toString();
            URL url = new URL(String.format(ENDPOINT, model) + "?key=" + apiKey);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(READ_TIMEOUT_MS);
            connection.setDoOutput(true);

            try (OutputStream os = connection.getOutputStream()) {
                os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
            }

            int status = connection.getResponseCode();
            InputStream stream = status >= 200 && status < 300
                    ? connection.getInputStream() : connection.getErrorStream();
            String responseText = readAll(stream);
            if (status < 200 || status >= 300) {
                throw new ChatbotException("Gemini API returned status " + status);
            }
            String reply = extractText(responseText);
            if (reply == null || reply.isBlank()) {
                throw new ChatbotException("Gemini returned an empty reply");
            }
            return reply.trim();
        } catch (ChatbotException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ChatbotException("Failed to reach Gemini", ex);
        }
    }

    private String buildPrompt(String userMessage, List<Product> catalog) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are the Giftora shopping assistant. Be friendly, concise (max 3 sentences) ")
                .append("and only discuss products available in this store. Do not invent products.\n\n");
        sb.append("Available catalogue sample:\n");
        if (catalog != null) {
            int count = 0;
            for (Product product : catalog) {
                if (count++ == 20) {
                    break;
                }
                sb.append("- ").append(product.getName())
                        .append(" | ").append(product.getCategory())
                        .append(" | Rs.").append(product.getPrice()).append('\n');
            }
        }
        sb.append("\nCustomer: ").append(userMessage);
        return sb.toString();
    }

    private String extractText(String responseText) {
        try {
            JsonObject root = JsonParser.parseString(responseText).getAsJsonObject();
            JsonArray candidates = root.getAsJsonArray("candidates");
            if (candidates == null || candidates.size() == 0) {
                return null;
            }
            JsonObject candidate = candidates.get(0).getAsJsonObject();
            JsonObject content = candidate.getAsJsonObject("content");
            if (content == null) {
                return null;
            }
            JsonArray parts = content.getAsJsonArray("parts");
            if (parts == null || parts.size() == 0) {
                return null;
            }
            JsonObject part = parts.get(0).getAsJsonObject();
            if (!part.has("text")) {
                return null;
            }
            return part.get("text").getAsString();
        } catch (Exception ex) {
            return null;
        }
    }

    private String readAll(InputStream stream) throws Exception {
        if (stream == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }
}
