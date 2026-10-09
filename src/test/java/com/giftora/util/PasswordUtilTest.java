package com.giftora.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordUtilTest {

    @Test
    void hashIsSaltedAndVerifiable() {
        String hash1 = PasswordUtil.hash("Secret@123");
        String hash2 = PasswordUtil.hash("Secret@123");
        assertNotEquals(hash1, hash2, "bcrypt must use a random salt per hash");
        assertTrue(PasswordUtil.verify("Secret@123", hash1));
        assertTrue(PasswordUtil.verify("Secret@123", hash2));
    }

    @Test
    void wrongPasswordDoesNotVerify() {
        String hash = PasswordUtil.hash("Secret@123");
        assertFalse(PasswordUtil.verify("wrong-password", hash));
    }
}
