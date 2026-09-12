package client.m1ck3y.rattix.modules.manager;

import java.util.*;

public enum Category {
    COMBAT("Combat", Arrays.asList(
            "KillAura",
            "BowAimbot",
            "Criticals",
            "MoreKB",
            "Velocity"
    )),
    MOVEMENT("Movement", Arrays.asList(
            "ToggleSprint",
            "Speed"
    )),
    RENDER("Render", Arrays.asList(
            "ArmorStatus",
            "Fullbright",
            "TimeChanger",
            "Crosshair"
    )),
    PLAYER("Player", Arrays.asList(
            "AutoGapple",
            "AutoPot",
            "AutoSoup"
    )),
    HUD("HUD", Arrays.asList(
            "FPSDisplay",
            "CPSDisplay",
            "Keystrokes",
            "PingDisplay"
    )),
    WORLD("World", Collections.emptyList()),
    MISC("Misc", Collections.emptyList()),
    FUN("Fun", Collections.emptyList()),
    LEGIT("Legit", Collections.emptyList()),
    LATENCY("Latency", Collections.emptyList()),
    THEME("Theme", Collections.emptyList());

    private final String displayName;
    private final List<String> moduleNames; // 功能名字集合，直接写在代码内方便修改
    private final List<Register> modules = new ArrayList<>(); // 实际注册的模块实例集合

    Category(String displayName) {
        this(displayName, Collections.emptyList());
    }

    Category(String displayName, List<String> moduleNames) {
        this.displayName = displayName;
        this.moduleNames = new ArrayList<>(moduleNames);
    }

    Category(String displayName, String... moduleNames) {
        this(displayName, Arrays.asList(moduleNames));
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<String> getModuleNames() {
        return moduleNames;
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

    /**
     * 根据模块名称查找对应分类（代码中定义的分类映射）
     */
    public static Category getCategoryForModule(String moduleName) {
        if (moduleName == null) return null;
        for (Category cat : values()) {
            for (String name : cat.moduleNames) {
                if (name.equalsIgnoreCase(moduleName)) {
                    return cat;
                }
            }
        }
        return MISC;
    }
}
