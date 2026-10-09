package com.giftora.chatbot;

import com.giftora.model.Product;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Offline shopping assistant. Uses only live catalogue data and requires no API key.
 * Also acts as the graceful fallback when the Gemini provider is unavailable.
 */
public class MockChatbotProvider implements ChatbotProvider {

    @Override
    public String getProviderName() {
        return "mock";
    }

    @Override
    public String generateReply(String userMessage, List<Product> catalog) {
        String message = userMessage == null ? "" : userMessage.toLowerCase(Locale.ROOT).trim();
        if (message.isEmpty()) {
            return "Hi! I'm the Giftora shopping assistant. Tell me what you're looking for, "
                    + "for example \"gift under 1000\" or \"running shoes\".";
        }
        if (isGreeting(message)) {
            return "Hello! Welcome to Giftora. I can help you find gifts, electronics, fashion and more. "
                    + "What are you shopping for today?";
        }
        if (message.contains("ship") || message.contains("deliver")) {
            return "We offer standard delivery in 3-5 business days. You can track every order from your "
                    + "Order History page.";
        }
        if (message.contains("return") || message.contains("refund")) {
            return "If something isn't right, most items can be returned within 7 days of delivery. "
                    + "Open the product from your Order History to get started.";
        }

        List<Product> matches = searchProducts(message, catalog);
        if (!matches.isEmpty()) {
            StringBuilder sb = new StringBuilder("Here are some options I found:\n");
            int count = 0;
            for (Product product : matches) {
                if (count++ == 3) {
                    break;
                }
                sb.append("- ").append(product.getName())
                        .append(" (").append(product.getCategory()).append(") for Rs.")
                        .append(product.getPrice().setScale(0, java.math.RoundingMode.HALF_UP))
                        .append('\n');
            }
            sb.append("Would you like me to narrow this down further?");
            return sb.toString();
        }
        return "I couldn't find an exact match, but we have great options across Mobiles & Electronics, "
                + "Fashion, Footwear, Home & Kitchen and more. Try something like \"gift under 1000\" "
                + "or \"running shoes\".";
    }

    private boolean isGreeting(String message) {
        return message.startsWith("hi") || message.startsWith("hello") || message.startsWith("hey")
                || message.contains("good morning") || message.contains("good evening");
    }

    private boolean isBudgetSearch(String message) {
        return message.contains("under") || message.contains("below")
                || message.contains("budget") || message.contains("cheap") || message.contains("affordable");
    }

    private List<Product> searchProducts(String message, List<Product> catalog) {
        List<Product> matches = new ArrayList<>();
        if (catalog == null) {
            return matches;
        }
        String[] tokens = message.split("[^a-z0-9]+");
        BigDecimal budget = parseNumber(message);
        boolean budgetSearch = isBudgetSearch(message);
        for (Product product : catalog) {
            if (product == null || !product.isAvailable()) {
                continue;
            }
            boolean matched = false;
            String haystack = (product.getName() + " " + product.getCategory()).toLowerCase(Locale.ROOT);
            for (String token : tokens) {
                if (token.length() >= 3 && haystack.contains(token)) {
                    matched = true;
                    break;
                }
            }
            if (!matched && budgetSearch && budget != null) {
                matched = product.getPrice().compareTo(budget) <= 0;
            }
            if (matched) {
                matches.add(product);
            }
        }
        return matches;
    }

    private BigDecimal parseNumber(String message) {
        StringBuilder digits = new StringBuilder();
        for (char c : message.toCharArray()) {
            if (Character.isDigit(c)) {
                digits.append(c);
            }
        }
        if (digits.length() == 0) {
            return null;
        }
        try {
            return new BigDecimal(digits.toString());
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
