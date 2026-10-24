package client.m1ck3y.rattix.features.module;

import client.m1ck3y.rattix.features.module.modules.*;

import java.io.File;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Rattix 模块管理器 (ModuleManager)。
 * 负责客户端所有功能模块的生命周期、注册发现、事件分发以及快速检索。
 */
public class ModuleManager {
    private static final List<Module> registeredModules = new ArrayList<>();
    private static final Map<String, Module> modulesByName = new HashMap<>();
    private static final Map<Class<? extends Module>, Module> modulesByClass = new HashMap<>();
    private static final List<Module> alphabeticalModules = new ArrayList<>();
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

        Set<Class<? extends Module>> moduleClasses = scanModuleClasses();

        if (moduleClasses.isEmpty()) {
            moduleClasses.addAll(getFallbackClasses());
        }

        for (Class<? extends Module> clazz : moduleClasses) {
            try {
                Module instance = clazz.getDeclaredConstructor().newInstance();
                register(instance);
            } catch (Throwable t) {
                System.err.println("[Rattix] Failed to instantiate module: " + clazz.getName());
                t.printStackTrace();
            }
        }
    }

    public static synchronized void register(Module module) {
        if (module == null) return;
        if (!registeredModules.contains(module)) {
            registeredModules.add(module);
        }
        modulesByClass.put(module.getClass(), module);
        if (module.getName() != null) {
            modulesByName.put(module.getName().toLowerCase(java.util.Locale.ROOT), module);
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

        alphabeticalModules.clear();
    }

    @SuppressWarnings("unchecked")
    public static synchronized <T extends Module> T register(Class<T> clazz) {
        if (clazz == null) return null;
        try {
            Module instance = clazz.getDeclaredConstructor().newInstance();
            register(instance);
            return (T) instance;
        } catch (Throwable t) {
            System.err.println("[Rattix] Failed to instantiate and register module: " + clazz.getName());
            t.printStackTrace();
            return null;
        }
    }

    public static synchronized void unregister(Module module) {
        if (module == null) return;
        if (module.isEnabled()) {
            module.setEnabled(false);
        }
        registeredModules.remove(module);
        modulesByClass.remove(module.getClass());
        if (module.getName() != null) {
            modulesByName.remove(module.getName().toLowerCase(java.util.Locale.ROOT));
        }
        if (module.getCategory() != null) {
            module.getCategory().removeModule(module);
        }
        alphabeticalModules.clear();
    }

    public static synchronized void unregister(String moduleName) {
        if (moduleName == null) return;
        Module module = modulesByName.get(moduleName.toLowerCase(java.util.Locale.ROOT));
        if (module != null) {
            unregister(module);
        }
    }

    private static Set<Class<? extends Module>> scanModuleClasses() {
        Set<Class<? extends Module>> classes = new LinkedHashSet<>();
        String packageName = "client.m1ck3y.rattix.features.module.modules";
        String packagePath = packageName.replace('.', '/');

        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            if (classLoader == null) {
                classLoader = ModuleManager.class.getClassLoader();
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
                                String className = name.replace('/', '.').substring(0, name.length() - 6);
                                checkAndAddClass(className, classes);
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

    private static void scanDirectory(File directory, String packageName, Set<Class<? extends Module>> classes) {
        if (!directory.exists() || !directory.isDirectory()) return;

        File[] files = directory.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                scanDirectory(file, packageName + "." + file.getName(), classes);
            } else if (file.isFile() && file.getName().endsWith(".class")) {
                String simpleName = file.getName().substring(0, file.getName().length() - 6);
                checkAndAddClass(packageName + "." + simpleName, classes);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void checkAndAddClass(String fullClassName, Set<Class<? extends Module>> classes) {
        String simpleName = fullClassName.substring(fullClassName.lastIndexOf('.') + 1);
        if ("Category".equals(simpleName) || "Module".equals(simpleName) || "ModuleManager".equals(simpleName) || simpleName.contains("$")) {
            return;
        }

        try {
            Class<?> clazz = Class.forName(fullClassName);
            if (Module.class.isAssignableFrom(clazz) && !java.lang.reflect.Modifier.isAbstract(clazz.getModifiers())) {
                classes.add((Class<? extends Module>) clazz);
            }
        } catch (Throwable ignored) {
        }
    }

    @SuppressWarnings("unchecked")
    private static List<Class<? extends Module>> getFallbackClasses() {
        List<Class<? extends Module>> list = new ArrayList<>();
        Class<?>[] fallbacks = new Class<?>[]{
                ClickGUI.class,
                ArmorStatus.class,
                CPSDisplay.class,
                Crosshair.class,
                FPSDisplay.class,
                Fullbright.class,
                Keystrokes.class,
                PingDisplay.class,
                Ambiance.class,
                MenuShader.class,
                ToggleSprint.class
        };
        for (Class<?> c : fallbacks) {
            if (Module.class.isAssignableFrom(c)) {
                list.add((Class<? extends Module>) c);
            }
        }
        return list;
    }

    public static List<Module> getRegisteredModules() {
        return registeredModules;
    }

    public static int getLoadedModuleCount() {
        return registeredModules.size();
    }

    public static int getLoadedOptionCount() {
        int count = 0;
        for (Module m : registeredModules) {
            count += 1 + m.getMetaOptions().size() + m.getSettings().size();
        }
        if (Font.getInstance() != null && Font.getInstance().getSettings() != null) {
            count += Font.getInstance().getSettings().size();
        }
        return count;
    }

    public static List<Module> getAlphabeticalModules() {
        if (alphabeticalModules.size() != registeredModules.size()) {
            alphabeticalModules.clear();
            alphabeticalModules.addAll(registeredModules);
            alphabeticalModules.sort((m1, m2) -> String.CASE_INSENSITIVE_ORDER.compare(m1.getName(), m2.getName()));
        }
        return alphabeticalModules;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Module> T getModule(Class<T> clazz) {
        return (T) modulesByClass.get(clazz);
    }

    public static Module getModuleByName(String name) {
        if (name == null) return null;
        return modulesByName.get(name.toLowerCase(java.util.Locale.ROOT));
    }

    public static Module getModule(String name) {
        return getModuleByName(name);
    }

    // ==========================================
    // Lifecycle Dispatchers
    // ==========================================
    public static void onClientTick() {
        int size = registeredModules.size();
        for (int i = 0; i < size; i++) {
            Module m = registeredModules.get(i);
            if (m.isEnabled()) {
                m.onTick();
            }
        }
    }

    public static void onRenderOverlay() {
        int size = registeredModules.size();
        for (int i = 0; i < size; i++) {
            Module m = registeredModules.get(i);
            if (m.isEnabled()) {
                m.onRender2D();
            }
        }
    }

    public static void onKey(int key) {
        if (key == 0) return;
        int size = registeredModules.size();
        for (int i = 0; i < size; i++) {
            Module m = registeredModules.get(i);
            if (m.getKeyCode() == key) {
                m.toggle();
            }
        }
    }
}
