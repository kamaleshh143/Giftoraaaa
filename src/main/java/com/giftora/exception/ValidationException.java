package com.giftora.exception;

/**
 * Thrown when user input fails validation. The message is safe to show to the user.
 */
public class ValidationException extends AppException {
    private static final long serialVersionUID = 1L;

    public ValidationException(String message) {
        super(message);
    }
}
