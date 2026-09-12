package client.m1ck3y.rattix.module;

import client.m1ck3y.rattix.module.clickgui.CheckBoxSetting;
import client.m1ck3y.rattix.module.clickgui.Setting;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public abstract class Module {
    protected final Minecraft mc = Minecraft.getMinecraft();
    private final String name;
    private final String description;
    private final Category category;
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

    public Module(String name, String description, Category category) {
        this(name, description, category, 0);
    }

    public Module(String name, String description, Category category, int keyCode) {
        this.name = name;
        this.description = description;
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
        return list != null ? list : java.util.Collections.emptyList();
    }

    public List<Setting<?>> getTopSettingsByGroup(String group) {
        ensureSettingsGrouped();
        List<Setting<?>> list = topSettingsByGroup.get(group);
        return list != null ? list : java.util.Collections.emptyList();
    }

    public List<Setting<?>> getChildrenSettings(Setting<?> parent) {
        ensureSettingsGrouped();
        List<Setting<?>> list = childrenSettings.get(parent);
        return list != null ? list : java.util.Collections.emptyList();
    }

    public List<CheckBoxSetting> getMetaOptions() {
        if (cachedMetaOptions == null) {
            cachedMetaOptions = java.util.Collections.unmodifiableList(Arrays.asList(
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

    public Category getCategory() {
        return category;
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
}
