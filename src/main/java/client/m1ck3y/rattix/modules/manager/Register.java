package client.m1ck3y.rattix.modules.manager;

import client.m1ck3y.rattix.modules.*;
import client.m1ck3y.rattix.modules.clickgui.CheckBoxSetting;
import client.m1ck3y.rattix.modules.clickgui.Setting;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public abstract class Register {
    protected final Minecraft mc = Minecraft.getMinecraft();
    private final String name;
    private String description;
    private Category category;
    private int keyCode;
    private boolean enabled;

    public final CheckBoxSetting loadFromConfig = new CheckBoxSetting("LoadFromConfig", "load_from_config", true);
    public final CheckBoxSetting showInArrayList = new CheckBoxSetting("ShowInArrayList", "show_in_array_list", true);
    public final CheckBoxSetting toggleSound = new CheckBoxSetting("ToggleSound", "toggle_sound", true);
    public final CheckBoxSetting keyHoldMode = new CheckBoxSetting("KeyHoldMode", "key_hold_mode", false);

    private final List<Setting<?>> settings = new ArrayList<>();
    private List<CheckBoxSetting> cachedMetaOptions = null;
    private Map<String, List<Setting<?>>> groupedSettings = null;
    private List<String> groupNames = null;
    private Map<String, List<Setting<?>>> topSettingsByGroup = null;
    private Map<Setting<?>, List<Setting<?>>> childrenSettings = null;

    // Constructors
    public Register() {
        this("", null, 0);
    }

    public Register(Category category) {
        this("", category, 0);
    }

    public Register(Category category, int keyCode) {
        this("", category, keyCode);
    }

    public Register(String description, Category category) {
        this(description, category, 0);
    }

    public Register(String description, Category category, int keyCode) {
        this("", description, category, keyCode);
    }

    public Register(String name, String description, Category category) {
        this(name, description, category, 0);
    }

    public Register(String name, String description, Category category, int keyCode) {
        this.name = (name != null && !name.trim().isEmpty()) ? name : getClass().getSimpleName();
        this.description = (description != null) ? description : "";
        this.category = category;
        this.keyCode = keyCode;
        this.enabled = false;
        this.showInArrayList.setValue(true);
        this.toggleSound.setValue(true);
    }

    public void addSettings(Setting<?>... settings) {
        this.settings.addAll(Arrays.asList(settings));
        this.groupedSettings = null;
    }

    private void ensureSettingsGrouped() {
        if (groupedSettings == null) {
            groupedSettings = new java.util.LinkedHashMap<>();
            topSettingsByGroup = new java.util.LinkedHashMap<>();
            childrenSettings = new java.util.LinkedHashMap<>();
            groupNames = new ArrayList<>();

            for (Setting<?> s : settings) {
                String group = s.getGroup() != null ? s.getGroup() : "General";
                groupedSettings.computeIfAbsent(group, k -> new ArrayList<>()).add(s);
                if (!groupNames.contains(group)) {
                    groupNames.add(group);
                }
                if (s.getParentSetting() == null) {
                    topSettingsByGroup.computeIfAbsent(group, k -> new ArrayList<>()).add(s);
                } else {
                    childrenSettings.computeIfAbsent(s.getParentSetting(), k -> new ArrayList<>()).add(s);
                }
            }
        }
    }

    public List<String> getGroupNames() {
        ensureSettingsGrouped();
        return groupNames;
    }

    public List<Setting<?>> getSettingsByGroup(String group) {
        ensureSettingsGrouped();
        List<Setting<?>> list = groupedSettings.get(group);
        return list != null ? list : Collections.emptyList();
    }

    public List<Setting<?>> getTopSettingsByGroup(String group) {
        ensureSettingsGrouped();
        List<Setting<?>> list = topSettingsByGroup.get(group);
        return list != null ? list : Collections.emptyList();
    }

    public List<Setting<?>> getChildrenSettings(Setting<?> parent) {
        ensureSettingsGrouped();
        List<Setting<?>> list = childrenSettings.get(parent);
        return list != null ? list : Collections.emptyList();
    }

    public List<CheckBoxSetting> getMetaOptions() {
        if (cachedMetaOptions == null) {
            cachedMetaOptions = Collections.unmodifiableList(Arrays.asList(
                    loadFromConfig,
                    showInArrayList,
                    toggleSound
            ));
        }
        return cachedMetaOptions;
    }

    public void toggle() {
        setEnabled(!this.enabled);
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled != enabled) {
            this.enabled = enabled;
            if (enabled) {
                onEnable();
            } else {
                onDisable();
            }
        }
    }

    public void onEnable() {}
    public void onDisable() {}
    public void onTick() {}
    public void onRender2D() {}

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public int getKeyCode() {
        return keyCode;
    }

    public void setKeyCode(int keyCode) {
        this.keyCode = keyCode;
    }

    public void setKey(int key) {
        this.keyCode = key;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isLoadFromConfig() {
        return loadFromConfig.getValue();
    }

    public void setLoadFromConfig(boolean loadFromConfig) {
        this.loadFromConfig.setValue(loadFromConfig);
    }

    public boolean isShowInArrayList() {
        return showInArrayList.getValue();
    }

    public void setShowInArrayList(boolean showInArrayList) {
        this.showInArrayList.setValue(showInArrayList);
    }

    public boolean isToggleSound() {
        return toggleSound.getValue();
    }

    public void setToggleSound(boolean toggleSound) {
        this.toggleSound.setValue(toggleSound);
    }

    public boolean isKeyHoldMode() {
        return keyHoldMode.getValue();
    }

    public void setKeyHoldMode(boolean keyHoldMode) {
        this.keyHoldMode.setValue(keyHoldMode);
    }

    public List<Setting<?>> getSettings() {
        return settings;
    }

    // ==========================================
    // Registry & Auto-Registration
    // ==========================================
    private static final List<Register> registeredModules = new ArrayList<>();
    private static final Map<String, Register> modulesByName = new HashMap<>();
    private static final Map<Class<? extends Register>, Register> modulesByClass = new HashMap<>();
    private static final List<Register> alphabeticalModules = new ArrayList<>();
    private static boolean registered = false;

    public static synchronized void registerAll() {
        if (registered) return;
        registered = true;

        for (Category cat : Category.values()) {
            cat.clearModules();
        }
        registeredModules.clear();
        modulesByName.clear();
        modulesByClass.clear();
        alphabeticalModules.clear();

        Set<Class<? extends Register>> moduleClasses = scanModuleClasses();

        if (moduleClasses.isEmpty()) {
            moduleClasses.addAll(getFallbackClasses());
        }

        for (Class<? extends Register> clazz : moduleClasses) {
            try {
                Register instance = clazz.getDeclaredConstructor().newInstance();
                register(instance);
            } catch (Throwable t) {
                System.err.println("[Rattix] Failed to instantiate module: " + clazz.getName());
                t.printStackTrace();
            }
        }
    }

    public static void register(Register module) {
        if (module == null) return;
        registeredModules.add(module);
        modulesByClass.put(module.getClass(), module);
        if (module.getName() != null) {
            modulesByName.put(module.getName().toLowerCase(), module);
        }

        // 根据 Category 中定义的集合动态同步分类
        Category mappedCategory = Category.getCategoryForModule(module.getName());
        if (mappedCategory != null) {
            module.setCategory(mappedCategory);
        } else if (module.getCategory() == null) {
            module.setCategory(Category.VISUAL);
        }

        if (module.getCategory() != null) {
            module.getCategory().addModule(module);
        }
    }

    private static Set<Class<? extends Register>> scanModuleClasses() {
        Set<Class<? extends Register>> classes = new LinkedHashSet<>();
        String packageName = "client.m1ck3y.rattix.modules";
        String packagePath = packageName.replace('.', '/');

        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            if (classLoader == null) {
                classLoader = Register.class.getClassLoader();
            }
            Enumeration<URL> resources = classLoader.getResources(packagePath);
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                String protocol = resource.getProtocol();

                if ("file".equals(protocol)) {
                    File dir = null;
                    try {
                        dir = new File(resource.toURI());
                    } catch (Exception ignored) {
                    }
                    if (dir == null || !dir.exists()) {
                        String filePath = URLDecoder.decode(resource.getFile(), "UTF-8");
                        if (filePath.startsWith("/") && filePath.contains(":")) {
                            filePath = filePath.substring(1);
                        }
                        dir = new File(filePath);
                    }
                    scanDirectory(dir, packageName, classes);
                } else if ("jar".equals(protocol)) {
                    JarURLConnection jarConn = (JarURLConnection) resource.openConnection();
                    try (JarFile jarFile = jarConn.getJarFile()) {
                        Enumeration<JarEntry> entries = jarFile.entries();
                        while (entries.hasMoreElements()) {
                            JarEntry entry = entries.nextElement();
                            String name = entry.getName();
                            if (name.startsWith(packagePath + "/") && name.endsWith(".class")) {
                                String sub = name.substring(packagePath.length() + 1);
                                if (!sub.contains("/")) { // 排除 manager 与 clickgui 子包
                                    String simpleName = sub.substring(0, sub.length() - 6);
                                    checkAndAddClass(packageName + "." + simpleName, classes);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Throwable t) {
            System.err.println("[Rattix] Note: Auto scan encountered issue, falling back to built-in list: " + t.getMessage());
        }
        return classes;
    }

    private static void scanDirectory(File directory, String packageName, Set<Class<? extends Register>> classes) {
        if (!directory.exists() || !directory.isDirectory()) return;

        File[] files = directory.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isFile() && file.getName().endsWith(".class")) {
                String simpleName = file.getName().substring(0, file.getName().length() - 6);
                checkAndAddClass(packageName + "." + simpleName, classes);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void checkAndAddClass(String fullClassName, Set<Class<? extends Register>> classes) {
        String simpleName = fullClassName.substring(fullClassName.lastIndexOf('.') + 1);
        if ("Category".equals(simpleName) || "Register".equals(simpleName) || simpleName.contains("$")) {
            return;
        }

        try {
            Class<?> clazz = Class.forName(fullClassName);
            if (Register.class.isAssignableFrom(clazz) && !java.lang.reflect.Modifier.isAbstract(clazz.getModifiers())) {
                classes.add((Class<? extends Register>) clazz);
            }
        } catch (Throwable ignored) {
        }
    }

    @SuppressWarnings("unchecked")
    private static List<Class<? extends Register>> getFallbackClasses() {
        List<Class<? extends Register>> list = new ArrayList<>();
        Class<?>[] fallbacks = new Class<?>[]{
                ClickGUI.class,
                ArmorStatus.class,
                CPSDisplay.class,
                Crosshair.class,
                FPSDisplay.class,
                Fullbright.class,
                Keystrokes.class,
                PingDisplay.class,
                TimeChanger.class,
                ToggleSprint.class
        };
        for (Class<?> c : fallbacks) {
            if (Register.class.isAssignableFrom(c)) {
                list.add((Class<? extends Register>) c);
            }
        }
        return list;
    }

    public static List<Register> getRegisteredModules() {
        return registeredModules;
    }

    public static List<Register> getAlphabeticalModules() {
        if (alphabeticalModules.size() != registeredModules.size()) {
            alphabeticalModules.clear();
            alphabeticalModules.addAll(registeredModules);
            alphabeticalModules.sort((m1, m2) -> String.CASE_INSENSITIVE_ORDER.compare(m1.getName(), m2.getName()));
        }
        return alphabeticalModules;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Register> T getModule(Class<T> clazz) {
        return (T) modulesByClass.get(clazz);
    }

    public static Register getModuleByName(String name) {
        if (name == null) return null;
        return modulesByName.get(name.toLowerCase());
    }

    public static Register getModule(String name) {
        return getModuleByName(name);
    }

    // ==========================================
    // Lifecycle Dispatchers
    // ==========================================
    public static void onClientTick() {
        for (Register m : registeredModules) {
            if (m.isEnabled()) {
                m.onTick();
            }
        }
    }

    public static void onRenderOverlay() {
        for (Register m : registeredModules) {
            if (m.isEnabled()) {
                m.onRender2D();
            }
        }
    }

    public static void onKey(int key) {
        if (key == 0) return;
        for (Register m : registeredModules) {
            if (m.getKeyCode() == key) {
                m.toggle();
            }
        }
    }

    // 兼容别名
    public static abstract class Module extends Register {}
}
