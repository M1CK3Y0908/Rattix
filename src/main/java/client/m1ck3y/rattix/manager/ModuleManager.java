package client.m1ck3y.rattix.manager;

import client.m1ck3y.rattix.modules.Category;
import client.m1ck3y.rattix.modules.Register;

import java.util.Collections;
import java.util.List;

public class ModuleManager {
    private static ModuleManager instance;

    public static ModuleManager getInstance() {
        if (instance == null) {
            instance = new ModuleManager();
        }
        return instance;
    }

    public ModuleManager() {
        instance = this;
        // 自动注册 modules 文件夹内的所有模块
        Register.registerAll();
    }

    public void registerModule(Register module) {
        Register.register(module);
    }

    public List<Register> getModules() {
        return Register.getRegisteredModules();
    }

    public List<Register> getAlphabeticalModules() {
        return Register.getAlphabeticalModules();
    }

    public List<Register> getModulesByCategory(Category category) {
        if (category == null) return Collections.emptyList();
        return category.getModules();
    }

    public <T extends Register> T getModule(Class<T> clazz) {
        return Register.getModule(clazz);
    }

    public Register getModuleByName(String name) {
        return Register.getModuleByName(name);
    }

    public Register getModule(String name) {
        return getModuleByName(name);
    }

    public void onTick() {
        for (Register m : Register.getRegisteredModules()) {
            if (m.isEnabled()) {
                m.onTick();
            }
        }
    }

    public void onRender2D() {
        for (Register m : Register.getRegisteredModules()) {
            if (m.isEnabled()) {
                m.onRender2D();
            }
        }
    }

    public void onKey(int key) {
        if (key == 0) return;
        for (Register m : Register.getRegisteredModules()) {
            if (m.getKeyCode() == key) {
                m.toggle();
            }
        }
    }
}
