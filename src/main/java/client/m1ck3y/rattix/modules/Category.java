package client.m1ck3y.rattix.modules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
    private final List<Register> modules = new ArrayList<>();

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<Register> getModules() {
        return modules;
    }

    public void addModule(Register module) {
        if (module != null && !modules.contains(module)) {
            modules.add(module);
        }
    }

    public void removeModule(Register module) {
        modules.remove(module);
    }

    public void clearModules() {
        modules.clear();
    }

    public static List<Category> getCategories() {
        return Collections.unmodifiableList(Arrays.asList(values()));
    }
}
