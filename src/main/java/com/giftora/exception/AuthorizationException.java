package com.giftora.exception;

/**
 * Thrown when a user is not allowed to perform an action (authorization failure).
 */
public class AuthorizationException extends AppException {
    private static final long serialVersionUID = 1L;

    public AuthorizationException(String message) {
        super(message);
    }
}
