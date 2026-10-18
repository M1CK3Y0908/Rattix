package client.m1ck3y.rattix.module.modules;

import client.m1ck3y.rattix.module.modules.ClickGUI.BooleanSetting;
import client.m1ck3y.rattix.module.modules.ClickGUI.ColorSetting;
import client.m1ck3y.rattix.module.modules.ClickGUI.ModeSetting;
import client.m1ck3y.rattix.module.modules.ClickGUI.NumberSetting;
import client.m1ck3y.rattix.module.modules.ClickGUI.Setting;
import client.m1ck3y.rattix.utils.FontUtil;
import client.m1ck3y.rattix.utils.font.CustomFontRenderer;

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
    public final NumberSetting offsetX = new NumberSetting("OffsetX", 0.0, -10.0, 10.0, 0.5, "Global Settings");
    public final NumberSetting offsetY = new NumberSetting("OffsetY", 0.0, -10.0, 10.0, 0.5, "Global Settings");
    public final BooleanSetting shadow = new BooleanSetting("Shadow", true, "Global Settings");
    public final ModeSetting shadowMode = new ModeSetting("Mode", "Vanilla", "Global Settings", new String[]{"Vanilla", "Custom"}).withParent(shadow);
    public final ColorSetting shadowColor = new ColorSetting("Color", 0x80000000, "Global Settings").withParent(shadow);
    public final NumberSetting shadowOffsetX = new NumberSetting("PositionXOffset", 1.0, -10.0, 10.0, 0.5, "Global Settings").withParent(shadow);
    public final NumberSetting shadowOffsetY = new NumberSetting("PositionYOffset", 1.0, -10.0, 10.0, 0.5, "Global Settings").withParent(shadow);
    public final BooleanSetting outline = new BooleanSetting("Outline", false, "Global Settings");
    public final NumberSetting outlineWidth = new NumberSetting("OutlineWidth", 1.0, 0.5, 2.0, 0.1, "Global Settings");
    public final ModeSetting outlineColor = new ModeSetting("OutlineColor", "Black", "Global Settings", new String[]{"Black", "Dark", "50% Alpha"});

    // Pre-cached fast access primitive fields for zero-allocation rendering loop
    public volatile float cachedEffectiveScale = 1.0f;
    public volatile boolean cachedIsCustom = false;
    public volatile CustomFontRenderer cachedCustomFont = null;
    public volatile float cachedOffsetX = 0.0f;
    public volatile float cachedOffsetY = 0.0f;
    public volatile boolean cachedShadow = true;
    public volatile int cachedShadowColor = 0x80000000;
    public volatile float cachedShadowOffsetX = 1.0f;
    public volatile float cachedShadowOffsetY = 1.0f;
    public volatile String cachedShadowMode = "Vanilla";
    public volatile boolean cachedOutline = false;
    public volatile float cachedOutlineWidth = 1.0f;
    public volatile String cachedOutlineColor = "Black";
    public volatile boolean cachedIsConsolas = false;
    public volatile int cachedFontHeight = 9;

    private final List<Setting<?>> settings = new ArrayList<>();
    private Map<String, List<Setting<?>>> groupedSettings = null;
    private List<String> groupNames = null;
    private Map<String, List<Setting<?>>> topSettingsByGroup = null;
    private Map<Setting<?>, List<Setting<?>>> childrenSettings = null;

    public Font() {
        settings.addAll(Arrays.asList(size, offsetX, offsetY, shadow, shadowMode, shadowColor, shadowOffsetX, shadowOffsetY, outline, outlineWidth, outlineColor));
        Runnable cacheUpdater = this::updateCache;
        for (Setting<?> s : settings) {
            s.setChangeListener(cacheUpdater);
        }
        updateCache();
    }

    public synchronized void updateCache() {
        this.cachedEffectiveScale = size != null ? size.getValue().floatValue() / 100.0f : 1.0f;
        this.cachedIsCustom = selectedFont != null && !"Minecraft".equalsIgnoreCase(selectedFont);
        this.cachedCustomFont = cachedIsCustom ? FontUtil.getFontRenderer(selectedFont) : null;
        this.cachedOffsetX = offsetX != null ? offsetX.getValue().floatValue() : 0.0f;
        this.cachedOffsetY = offsetY != null ? offsetY.getValue().floatValue() : 0.0f;
        this.cachedShadow = shadow == null || shadow.getValue();
        this.cachedShadowColor = shadowColor != null ? shadowColor.getRGB() : 0x80000000;
        this.cachedShadowOffsetX = shadowOffsetX != null ? shadowOffsetX.getValue().floatValue() : 1.0f;
        this.cachedShadowOffsetY = shadowOffsetY != null ? shadowOffsetY.getValue().floatValue() : 1.0f;
        this.cachedShadowMode = shadowMode != null ? shadowMode.getValue() : "Vanilla";
        this.cachedOutline = outline != null && outline.getValue();
        this.cachedOutlineWidth = outlineWidth != null ? outlineWidth.getValue().floatValue() : 1.0f;
        this.cachedOutlineColor = outlineColor != null ? outlineColor.getValue() : "Black";
        this.cachedIsConsolas = "consolas.ttf".equalsIgnoreCase(selectedFont);
        this.cachedFontHeight = (int) Math.max(1, Math.round(9 * this.cachedEffectiveScale));
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
        updateCache();
    }

    public void resetSettings() {
        for (Setting<?> s : settings) {
            s.reset();
        }
        this.selectedFont = "Minecraft";
        updateCache();
    }
}
