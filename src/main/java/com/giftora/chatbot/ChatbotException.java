package com.giftora.chatbot;

/**
 * Raised when a chatbot provider cannot produce a reply (timeout, HTTP error, bad config).
 */
public class ChatbotException extends Exception {
    private static final long serialVersionUID = 1L;

    public ChatbotException(String message) {
        super(message);
    }

    public ChatbotException(String message, Throwable cause) {
        super(message, cause);
    }
}
