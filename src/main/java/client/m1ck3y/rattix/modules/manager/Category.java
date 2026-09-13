package client.m1ck3y.rattix.modules.manager;

import java.util.*;

public enum Category {
    MOVEMENT("Movement", Arrays.asList(
            "ToggleSprint"
    )),
    VISUAL("Visual", Arrays.asList(
            "ClickGUI",
            "ArmorStatus",
            "Fullbright",
            "TimeChanger",
            "Crosshair",
            "FPSDisplay",
            "CPSDisplay",
            "Keystrokes",
            "PingDisplay"
    )),
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
        return null;
    }

    /**
     * 根据名称解析分类，支持旧版本分类别名（如 render, hud 映射为 VISUAL）
     */
    public static Category fromName(String name) {
        if (name == null) return null;
        for (Category cat : values()) {
            if (cat.name().equalsIgnoreCase(name) || cat.displayName.equalsIgnoreCase(name)) {
                return cat;
            }
        }
        if ("render".equalsIgnoreCase(name) || "hud".equalsIgnoreCase(name)) {
            return VISUAL;
        }
        return null;
    }
}
