package client.m1ck3y.rattix.ui.window;

/**
 * WindowType - Types of secondary page windows available in Rattix Client.
 */
public enum WindowType {
    SINGLEPLAYER("Singleplayer"),
    MULTIPLAYER("Multiplayer"),
    OPTIONS("Options"),
    SETTINGS("Client Configuration"),
    MODULES("Modules"),
    CUSTOM("Window");

    private final String defaultTitle;

    WindowType(String defaultTitle) {
        this.defaultTitle = defaultTitle;
    }

    public String getDefaultTitle() {
        return defaultTitle;
    }

    public static WindowType fromId(String id) {
        if (id == null) return CUSTOM;
        String lower = id.toLowerCase();
        if (lower.contains("single")) return SINGLEPLAYER;
        if (lower.contains("multi")) return MULTIPLAYER;
        if (lower.contains("opt")) return OPTIONS;
        if (lower.contains("set") || lower.contains("conf")) return SETTINGS;
        if (lower.contains("mod")) return MODULES;
        return CUSTOM;
    }
}
