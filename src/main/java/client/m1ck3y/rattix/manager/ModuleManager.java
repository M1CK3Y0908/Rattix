package client.m1ck3y.rattix.manager;

import client.m1ck3y.rattix.module.Category;
import client.m1ck3y.rattix.module.Module;
import client.m1ck3y.rattix.module.impl.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModuleManager {
    private static ModuleManager instance;

    public static ModuleManager getInstance() {
        if (instance == null) {
            instance = new ModuleManager();
        }
        return instance;
    }

    private final List<Module> modules = new ArrayList<>();
    private final Map<Category, List<Module>> modulesByCategory = new EnumMap<>(Category.class);
    private final Map<Class<? extends Module>, Module> modulesByClass = new HashMap<>();
    private final Map<String, Module> modulesByName = new HashMap<>();

    public ModuleManager() {
        instance = this;
        // Combat
        registerModule(new KillAura());
        registerModule(new BowAimbot());
        registerModule(new Criticals());
        registerModule(new MoreKB());
        registerModule(new Velocity());

        // Player
        registerModule(new AutoGapple());
        registerModule(new AutoPot());
        registerModule(new AutoSoup());

        // Movement
        registerModule(new ToggleSprint());
        registerModule(new Speed());

        // Render & HUD
        registerModule(new FPSDisplay());
        registerModule(new CPSDisplay());
        registerModule(new Keystrokes());
        registerModule(new PingDisplay());
        registerModule(new ArmorStatus());
        registerModule(new Fullbright());
        registerModule(new TimeChanger());
        registerModule(new Crosshair());
    }

    public void registerModule(Module module) {
        modules.add(module);
        modulesByClass.put(module.getClass(), module);
        if (module.getName() != null) {
            modulesByName.put(module.getName().toLowerCase(), module);
        }
        if (module.getCategory() != null) {
            modulesByCategory.computeIfAbsent(module.getCategory(), k -> new ArrayList<>()).add(module);
        }
    }

    private final List<Module> alphabeticalModules = new ArrayList<>();

    public List<Module> getModules() {
        return modules;
    }

    public List<Module> getAlphabeticalModules() {
        if (alphabeticalModules.size() != modules.size()) {
            alphabeticalModules.clear();
            alphabeticalModules.addAll(modules);
            alphabeticalModules.sort((m1, m2) -> String.CASE_INSENSITIVE_ORDER.compare(m1.getName(), m2.getName()));
        }
        return alphabeticalModules;
    }

    public List<Module> getModulesByCategory(Category category) {
        if (category == null) return Collections.emptyList();
        List<Module> list = modulesByCategory.get(category);
        return list != null ? list : Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    public <T extends Module> T getModule(Class<T> clazz) {
        return (T) modulesByClass.get(clazz);
    }

    public Module getModuleByName(String name) {
        if (name == null) return null;
        return modulesByName.get(name.toLowerCase());
    }

    public Module getModule(String name) {
        return getModuleByName(name);
    }

    public void onTick() {
        for (Module m : modules) {
            if (m.isEnabled()) {
                m.onTick();
            }
        }
    }

    public void onRender2D() {
        for (Module m : modules) {
            if (m.isEnabled()) {
                m.onRender2D();
            }
        }
    }

    public void onKey(int key) {
        if (key == 0) return;
        for (Module m : modules) {
            if (m.getKeyCode() == key) {
                m.toggle();
            }
        }
    }
}
