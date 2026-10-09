package com.giftora.exception;

/**
 * Base class for all application-level (checked) exceptions that carry a user-facing message.
 */
public class AppException extends Exception {
    private static final long serialVersionUID = 1L;

    public AppException(String message) {
        super(message);
    }

    public AppException(String message, Throwable cause) {
        super(message, cause);
    }
}
