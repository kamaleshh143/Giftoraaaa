package com.giftora.service;

import com.giftora.chatbot.ChatbotException;
import com.giftora.chatbot.ChatbotFactory;
import com.giftora.chatbot.ChatbotProvider;
import com.giftora.chatbot.MockChatbotProvider;
import com.giftora.exception.RateLimitException;
import com.giftora.exception.ValidationException;
import com.giftora.model.Product;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Applies validation, per-session rate limiting, caching and provider fallback around the
 * selected chatbot provider.
 */
public class ChatbotService {

    public static final int MAX_MESSAGES_PER_MINUTE = 10;
    public static final int MAX_MESSAGE_LENGTH = 500;

    private final ChatbotProvider primary;
    private final ChatbotProvider fallback;
    private final long windowMillis;

    private final Map<String, Deque<Long>> accessLog = new ConcurrentHashMap<>();
    private final Map<String, String> replyCache = new ConcurrentHashMap<>();

    public ChatbotService() {
        this(ChatbotFactory.create(), new MockChatbotProvider(), 60_000L);
    }

    public ChatbotService(ChatbotProvider primary, ChatbotProvider fallback, long windowMillis) {
        this.primary = primary;
        this.fallback = fallback;
        this.windowMillis = windowMillis;
    }

    public String providerName() {
        return primary.getProviderName();
    }

    public String reply(String sessionId, String message, List<Product> catalog)
            throws ValidationException, RateLimitException {
        if (message == null || message.trim().isEmpty()) {
            throw new ValidationException("Please type a message.");
        }
        String trimmed = message.trim();
        if (trimmed.length() > MAX_MESSAGE_LENGTH) {
            throw new ValidationException("Message is too long. Please keep it under "
                    + MAX_MESSAGE_LENGTH + " characters.");
        }
        String key = sessionId + "|" + trimmed.toLowerCase(Locale.ROOT);
        String cached = replyCache.get(key);
        if (cached != null) {
            return cached;
        }
        enforceRateLimit(sessionId);
        String reply;
        try {
            reply = primary.generateReply(trimmed, catalog);
        } catch (ChatbotException ex) {
            reply = fallbackReply(trimmed, catalog);
        }
        replyCache.put(key, reply);
        return reply;
    }

    private void enforceRateLimit(String sessionId) throws RateLimitException {
        long now = System.currentTimeMillis();
        Deque<Long> log = accessLog.computeIfAbsent(sessionId, k -> new ArrayDeque<>());
        synchronized (log) {
            while (!log.isEmpty() && now - log.peekFirst() > windowMillis) {
                log.pollFirst();
            }
            if (log.size() >= MAX_MESSAGES_PER_MINUTE) {
                throw new RateLimitException("You're sending messages too quickly. "
                        + "Please wait a minute and try again.");
            }
            log.addLast(now);
        }
    }

    private String fallbackReply(String message, List<Product> catalog) {
        try {
            return fallback.generateReply(message, catalog);
        } catch (ChatbotException ex) {
            return "I'm having trouble responding right now. Please try again shortly or browse our categories.";
        }
    }
}
