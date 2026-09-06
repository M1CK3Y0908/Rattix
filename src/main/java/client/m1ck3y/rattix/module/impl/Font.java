package client.m1ck3y.rattix.module.impl;

import client.m1ck3y.rattix.module.clickgui.BooleanSetting;
import client.m1ck3y.rattix.module.clickgui.ColorSetting;
import client.m1ck3y.rattix.module.clickgui.ModeSetting;
import client.m1ck3y.rattix.module.clickgui.NumberSetting;
import client.m1ck3y.rattix.module.clickgui.Setting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Font {
    private static final Font INSTANCE = new Font();

    public static Font getInstance() {
        return INSTANCE;
    }

    private String selectedFont = "Minecraft";

    public final NumberSetting size = new NumberSetting("Size", 100.0, 50.0, 150.0, 1.0, "Global Settings");
    public final NumberSetting offsetX = new NumberSetting("Offset X", 0.0, -10.0, 10.0, 0.5, "Global Settings");
    public final NumberSetting offsetY = new NumberSetting("Offset Y", 0.0, -10.0, 10.0, 0.5, "Global Settings");
    public final BooleanSetting shadow = new BooleanSetting("Shadow", true, "Global Settings");
    public final ModeSetting shadowMode = new ModeSetting("Mode", "Vanilla", "Global Settings", new String[]{"Vanilla", "Custom"}).withParent(shadow);
    public final ColorSetting shadowColor = new ColorSetting("Color", 0x80000000, "Global Settings").withParent(shadow);
    public final NumberSetting shadowOffsetX = new NumberSetting("Position X Offset", 1.0, -10.0, 10.0, 0.5, "Global Settings").withParent(shadow);
    public final NumberSetting shadowOffsetY = new NumberSetting("Position Y Offset", 1.0, -10.0, 10.0, 0.5, "Global Settings").withParent(shadow);
    public final BooleanSetting outline = new BooleanSetting("Outline", false, "Global Settings");
    public final NumberSetting outlineWidth = new NumberSetting("Outline Width", 1.0, 0.5, 2.0, 0.1, "Global Settings");
    public final ModeSetting outlineColor = new ModeSetting("Outline Color", "Black", "Global Settings", new String[]{"Black", "Dark", "50% Alpha"});

    private final List<Setting<?>> settings = new ArrayList<>();
    private Map<String, List<Setting<?>>> groupedSettings = null;
    private List<String> groupNames = null;
    private Map<String, List<Setting<?>>> topSettingsByGroup = null;
    private Map<Setting<?>, List<Setting<?>>> childrenSettings = null;

    public Font() {
        settings.addAll(Arrays.asList(size, offsetX, offsetY, shadow, shadowMode, shadowColor, shadowOffsetX, shadowOffsetY, outline, outlineWidth, outlineColor));
    }

    private void ensureSettingsGrouped() {
        if (groupedSettings == null) {
            groupedSettings = new java.util.LinkedHashMap<>();
            topSettingsByGroup = new java.util.LinkedHashMap<>();
            childrenSettings = new java.util.LinkedHashMap<>();
            groupNames = new ArrayList<>();

            groupNames.add("Font Manager");
            groupNames.add("Global Font");

            for (Setting<?> s : settings) {
                String group = s.getGroup() != null ? s.getGroup() : "Global Settings";
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

    public List<Setting<?>> getSettings() {
        return settings;
    }

    public String getSelectedFont() {
        return selectedFont;
    }

    public void setSelectedFont(String selectedFont) {
        if (selectedFont == null || selectedFont.trim().isEmpty()) {
            this.selectedFont = "Minecraft";
        } else {
            this.selectedFont = selectedFont.trim();
        }
    }
}
