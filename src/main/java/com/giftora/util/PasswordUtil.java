package com.giftora.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Password hashing helper built on jBCrypt. Never store or log plaintext passwords.
 */
public final class PasswordUtil {

    private static final int LOG_ROUNDS = 12;

    private PasswordUtil() {
    }

    public static String hash(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password must not be empty");
        }
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(LOG_ROUNDS));
    }

    public static boolean verify(String plainPassword, String hash) {
        if (plainPassword == null || hash == null || hash.isEmpty()) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainPassword, hash);
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}
