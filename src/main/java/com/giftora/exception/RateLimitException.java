package com.giftora.exception;

public class RateLimitException extends AppException {
    private static final long serialVersionUID = 1L;

    public RateLimitException(String message) {
        super(message);
    }
}
