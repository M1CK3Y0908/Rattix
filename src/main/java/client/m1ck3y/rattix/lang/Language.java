package client.m1ck3y.rattix.lang;

/**
 * Client language representation.
 * Aligned with LiquidBounce lang/Language.
 */
public class Language {
    private final String key;
    private final String name;

    public Language(String key, String name) {
        this.key = key;
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }
}
