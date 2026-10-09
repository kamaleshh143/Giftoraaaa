package com.giftora.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Centralizes configuration read from environment variables, with sane local defaults.
 * Nothing here contains secrets; secrets (Gemini API key) are read directly where needed.
 */
public final class AppConfig {

    private static final Logger log = LoggerFactory.getLogger(AppConfig.class);

    private AppConfig() {
    }

    public static String get(String key, String defaultValue) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            value = System.getProperty(key);
        }
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value.trim();
    }

    public static int getInt(String key, int defaultValue) {
        String value = get(key, null);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            log.warn("Invalid integer for {}: using default {}", key, defaultValue);
            return defaultValue;
        }
    }

    /**
     * Directory where the H2 file database is stored. On Render this should point at the
     * mounted persistent disk (e.g. /var/data). Locally it defaults to ./data.
     */
    public static Path getDataDirectory() {
        String configured = get("GIFTORA_DB_DIR", null);
        if (configured != null) {
            return Paths.get(configured);
        }
        return Paths.get(System.getProperty("user.dir"), "data");
    }

    public static String getJdbcUrl() {
        String explicit = get("GIFTORA_JDBC_URL", null);
        if (explicit != null) {
            return explicit;
        }
        Path dir = getDataDirectory();
        String dbFile = dir.resolve("giftora").toAbsolutePath().toString().replace('\\', '/');
        return "jdbc:h2:file:" + dbFile + ";DB_CLOSE_ON_EXIT=FALSE";
    }

    public static String getDbUser() {
        return get("GIFTORA_DB_USER", "sa");
    }

    public static String getDbPassword() {
        return get("GIFTORA_DB_PASSWORD", "");
    }

    public static boolean seedEnabled() {
        return Boolean.parseBoolean(get("GIFTORA_SEED", "true"));
    }

    public static String getChatbotProvider() {
        return get("AI_CHATBOT_PROVIDER", "mock").toLowerCase();
    }

    public static String getGeminiApiKey() {
        return get("GEMINI_API_KEY", null);
    }

    public static String getGeminiModel() {
        return get("GEMINI_MODEL", "gemini-1.5-flash");
    }
}
