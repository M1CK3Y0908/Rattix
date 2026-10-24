package client.m1ck3y.rattix.lang;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rattix 多语言支持管理器，对齐 LiquidBounce lang 架构。
 */
public class LanguageManager {
    private static final Map<String, String> TRANSLATIONS = new ConcurrentHashMap<>();
    private static String currentLocale = Locale.getDefault().getLanguage();

    public static String get(String key) {
        return TRANSLATIONS.getOrDefault(key, key);
    }

    public static String get(String key, String defaultValue) {
        return TRANSLATIONS.getOrDefault(key, defaultValue);
    }

    public static void setTranslation(String key, String value) {
        if (key != null && value != null) {
            TRANSLATIONS.put(key, value);
        }
    }

    public static String getCurrentLocale() {
        return currentLocale;
    }

    public static void setCurrentLocale(String locale) {
        if (locale != null) {
            currentLocale = locale;
        }
    }
}
