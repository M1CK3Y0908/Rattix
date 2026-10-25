package client.m1ck3y.rattix.ui.window;

/**
 * WindowType - Types of secondary page windows available in Rattix Client.
 */
public enum WindowType {
    SINGLEPLAYER("Singleplayer"),
    MULTIPLAYER("Multiplayer"),
    OPTIONS("Options"),
    SETTINGS("Client Configuration"),
    MODULES("Modules");

    private final String defaultTitle;

    WindowType(String defaultTitle) {
        this.defaultTitle = defaultTitle;
    }

    public String getDefaultTitle() {
        return defaultTitle;
    }
}
