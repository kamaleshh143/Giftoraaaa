package com.giftora.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidationUtilTest {

    @Test
    void emailValidation() {
        assertTrue(ValidationUtil.isValidEmail("buyer@giftora.example"));
        assertTrue(ValidationUtil.isValidEmail("a.b+c@sub.domain.co"));
        assertFalse(ValidationUtil.isValidEmail("not-an-email"));
        assertFalse(ValidationUtil.isValidEmail("missing@tld"));
        assertFalse(ValidationUtil.isValidEmail(null));
    }

    @Test
    void passwordMustBeAtLeastEightCharacters() {
        assertTrue(ValidationUtil.isValidPassword("12345678"));
        assertFalse(ValidationUtil.isValidPassword("short"));
        assertFalse(ValidationUtil.isValidPassword(null));
    }

    @Test
    void ratingBounds() {
        assertTrue(ValidationUtil.isValidRating(1));
        assertTrue(ValidationUtil.isValidRating(5));
        assertFalse(ValidationUtil.isValidRating(0));
        assertFalse(ValidationUtil.isValidRating(6));
    }

    @Test
    void imageUrlAllowsBlankAbsoluteAndRelativeOnly() {
        assertTrue(ValidationUtil.isValidImageUrl(""));
        assertTrue(ValidationUtil.isValidImageUrl(null));
        assertTrue(ValidationUtil.isValidImageUrl("https://cdn.example.com/x.png"));
        assertTrue(ValidationUtil.isValidImageUrl("http://cdn.example.com/x.png"));
        assertTrue(ValidationUtil.isValidImageUrl("/img/x.png"));
        assertFalse(ValidationUtil.isValidImageUrl("javascript:alert(1)"));
        assertFalse(ValidationUtil.isValidImageUrl("ftp://example.com/x.png"));
    }

    @Test
    void trimToNullNormalisesWhitespace() {
        assertNull(ValidationUtil.trimToNull("   "));
        assertNull(ValidationUtil.trimToNull(null));
        assertEquals("hello", ValidationUtil.trimToNull("  hello  "));
    }

    @Test
    void escapeHtmlNeutralisesMarkup() {
        assertEquals("&lt;script&gt;", ValidationUtil.escapeHtml("<script>"));
        assertEquals("a &amp; b", ValidationUtil.escapeHtml("a & b"));
    }
}
