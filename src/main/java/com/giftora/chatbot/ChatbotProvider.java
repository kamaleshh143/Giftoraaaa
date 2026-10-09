package com.giftora.chatbot;

import com.giftora.model.Product;

import java.util.List;

/**
 * Strategy interface for shopping assistant providers.
 */
public interface ChatbotProvider {

    String getProviderName();

    /**
     * Produces a shopping-related reply for the given user message.
     *
     * @param userMessage sanitized user message (already validated for length)
     * @param catalog     a small snapshot of the live catalogue for grounding suggestions
     * @return assistant reply text
     * @throws ChatbotException if the provider is unavailable or fails
     */
    String generateReply(String userMessage, List<Product> catalog) throws ChatbotException;
}
