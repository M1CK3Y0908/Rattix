package client.m1ck3y.rattix.module;

public enum Category {
    COMBAT("COMBAT"),
    LEGIT("LEGIT"),
    LATENCY("LATENCY"),
    MOVEMENT("MOVEMENT"),
    PLAYER("PLAYER"),
    RENDER("RENDER"),
    HUD("HUD"),
    WORLD("WORLD"),
    MISC("MISC"),
    FUN("FUN"),
    THEME("THEME");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
