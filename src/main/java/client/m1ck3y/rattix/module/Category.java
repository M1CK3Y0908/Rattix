package client.m1ck3y.rattix.module;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Rattix 模块分类系统。
 * 支持内置分类（Movement, Visual, Theme）以及外部模组动态注册自定义分类。
 */
public class Category {
    private static final Map<String, Category> REGISTRY = new ConcurrentHashMap<>();
    private static final List<Category> CATEGORIES_LIST = new CopyOnWriteArrayList<>();

    // 内置分类定义（保持与原有 enum 字段 100% 兼容）
    public static final Category MOVEMENT = registerInternal(new Category("MOVEMENT", "Movement", "movement", Arrays.asList(
            "ToggleSprint"
    )));

    public static final Category VISUAL = registerInternal(new Category("VISUAL", "Visual", "visual", Arrays.asList(
            "ClickGUI",
            "ArmorStatus",
            "Fullbright",
            "TimeChanger",
            "Crosshair",
            "FPSDisplay",
            "CPSDisplay",
            "Keystrokes",
            "PingDisplay",
            "MenuShader"
    )));

    public static final Category THEME = registerInternal(new Category("THEME", "Theme", "theme", Collections.emptyList()));

    private final String name;
    private final String displayName;
    private final String iconName;
    private final List<String> moduleNames;
    private final List<Module> modules = new CopyOnWriteArrayList<>();

    public Category(String name, String displayName) {
        this(name, displayName, name != null ? name.toLowerCase(Locale.ROOT) : "other", Collections.emptyList());
    }

    public Category(String name, String displayName, String iconName) {
        this(name, displayName, iconName, Collections.emptyList());
    }

    public Category(String name, String displayName, String iconName, List<String> moduleNames) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be null or empty");
        }
        this.name = name.trim().toUpperCase(Locale.ROOT);
        this.displayName = (displayName != null && !displayName.trim().isEmpty()) ? displayName.trim() : this.name;
        this.iconName = (iconName != null && !iconName.trim().isEmpty()) ? iconName.trim().toLowerCase(Locale.ROOT) : "other";
        this.moduleNames = new CopyOnWriteArrayList<>(moduleNames != null ? moduleNames : Collections.emptyList());
    }

    public String name() {
        return name;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIconName() {
        return iconName;
    }

    public List<String> getModuleNames() {
        return moduleNames;
    }

    public List<Module> getModules() {
        return modules;
    }

    public void addModule(Module module) {
        if (module != null && !modules.contains(module)) {
            modules.add(module);
            if (!moduleNames.contains(module.getName())) {
                moduleNames.add(module.getName());
            }
        }
    }

    public void removeModule(Module module) {
        if (module != null) {
            modules.remove(module);
            moduleNames.remove(module.getName());
        }
    }

    public void clearModules() {
        modules.clear();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return name.equalsIgnoreCase(category.name);
    }

    @Override
    public int hashCode() {
        return name.toLowerCase(Locale.ROOT).hashCode();
    }

    @Override
    public String toString() {
        return name;
    }

    // ==========================================
    // 注册管理与静态工具方法
    // ==========================================

    private static synchronized Category registerInternal(Category category) {
        String key = category.name.toLowerCase(Locale.ROOT);
        if (!REGISTRY.containsKey(key)) {
            REGISTRY.put(key, category);
            CATEGORIES_LIST.add(category);
        }
        return category;
    }

    /**
     * 允许其他模组注册自定义分类
     *
     * @param category 分类实例
     * @return 注册成功的分类实例
     */
    public static synchronized Category register(Category category) {
        if (category == null) return null;
        String key = category.name.toLowerCase(Locale.ROOT);
        if (REGISTRY.containsKey(key)) {
            return REGISTRY.get(key);
        }
        REGISTRY.put(key, category);
        CATEGORIES_LIST.add(category);
        return category;
    }

    /**
     * 创建并注册一个新分类
     */
    public static Category create(String name, String displayName) {
        return register(new Category(name, displayName));
    }

    /**
     * 创建并注册一个带自定义图标标识的新分类
     */
    public static Category create(String name, String displayName, String iconName) {
        return register(new Category(name, displayName, iconName));
    }

    /**
     * 获取所有可用分类列表
     */
    public static List<Category> getCategories() {
        return Collections.unmodifiableList(CATEGORIES_LIST);
    }

    /**
     * 保持与历史 enum 相同的 values() 接口
     */
    public static Category[] values() {
        return CATEGORIES_LIST.toArray(new Category[0]);
    }

    /**
     * 保持与历史 enum 相同的 valueOf(String) 接口
     */
    public static Category valueOf(String name) {
        if (name == null) throw new NullPointerException("Name is null");
        Category cat = fromName(name);
        if (cat == null) {
            throw new IllegalArgumentException("No category registered with name: " + name);
        }
        return cat;
    }

    /**
     * 根据字符串（标识或显示名称）查找分类
     */
    public static Category fromName(String name) {
        if (name == null) return null;
        String clean = name.trim().toLowerCase(Locale.ROOT);
        Category exact = REGISTRY.get(clean);
        if (exact != null) return exact;

        for (Category cat : CATEGORIES_LIST) {
            if (cat.name.equalsIgnoreCase(clean) || cat.displayName.equalsIgnoreCase(clean)) {
                return cat;
            }
        }
        return null;
    }

    /**
     * 根据模块名称反查其预设分类
     */
    public static Category getCategoryForModule(String moduleName) {
        if (moduleName == null) return null;
        for (Category category : CATEGORIES_LIST) {
            for (String mod : category.moduleNames) {
                if (mod.equalsIgnoreCase(moduleName)) {
                    return category;
                }
            }
        }
        return null;
    }
}
