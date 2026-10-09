package com.giftora.chatbot;

import com.giftora.config.AppConfig;

/**
 * Chooses the active chatbot provider based on AI_CHATBOT_PROVIDER.
 * Falls back safely to the mock provider when Gemini is requested but not configured.
 */
public final class ChatbotFactory {

    private ChatbotFactory() {
    }

    public static ChatbotProvider create() {
        String provider = AppConfig.getChatbotProvider();
        if ("gemini".equals(provider)) {
            String key = AppConfig.getGeminiApiKey();
            if (key != null && !key.isBlank()) {
                return new GeminiChatbotProvider();
            }
            // No key: use mock so the feature keeps working.
            return new MockChatbotProvider();
        }
        return new MockChatbotProvider();
    }
}
