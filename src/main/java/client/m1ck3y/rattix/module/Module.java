package client.m1ck3y.rattix.module;

import client.m1ck3y.rattix.module.modules.ClickGUI.BooleanSetting;
import client.m1ck3y.rattix.module.modules.ClickGUI.CheckBoxSetting;
import client.m1ck3y.rattix.module.modules.ClickGUI.ColorSetting;
import client.m1ck3y.rattix.module.modules.ClickGUI.ModeSetting;
import client.m1ck3y.rattix.module.modules.ClickGUI.NumberSetting;
import client.m1ck3y.rattix.module.modules.ClickGUI.Setting;
import client.m1ck3y.rattix.utils.FontUtil;
import client.m1ck3y.rattix.utils.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

import java.util.*;

/**
 * Rattix 核心模块基类。
 * 所有客户端功能模块均继承此类。
 */
public abstract class Module {
    protected final Minecraft mc = Minecraft.getMinecraft();
    private final String name;
    private String description;
    private Category category;
    private final int defaultKeyCode;
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
    public Module() {
        this("", null, 0);
    }

    public Module(Category category) {
        this("", category, 0);
    }

    public Module(Category category, int keyCode) {
        this("", category, keyCode);
    }

    public Module(String description, Category category) {
        this(description, category, 0);
    }

    public Module(String description, Category category, int keyCode) {
        this("", description, category, keyCode);
    }

    public Module(String name, String description, Category category) {
        this(name, description, category, 0);
    }

    public Module(String name, String description, Category category, int keyCode) {
        this.name = (name != null && !name.trim().isEmpty()) ? name : getClass().getSimpleName();
        this.description = (description != null) ? description : "";
        this.category = category;
        this.defaultKeyCode = keyCode;
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

    public void resetSettings() {
        this.keyCode = this.defaultKeyCode;
        for (Setting<?> s : settings) {
            s.reset();
        }
        loadFromConfig.reset();
        showInArrayList.reset();
        toggleSound.reset();
        keyHoldMode.reset();
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
    // Common Visual HUD Settings ("Visual" group)
    // ==========================================
    public NumberSetting visualSize;
    public NumberSetting visualOffsetX;
    public NumberSetting visualOffsetY;

    public BooleanSetting visualText;
    public NumberSetting visualTextSize;
    public ColorSetting visualTextColor;
    public BooleanSetting visualShadow;
    public ColorSetting visualShadowColor;
    public NumberSetting visualShadowOffsetX;
    public NumberSetting visualShadowOffsetY;

    public BooleanSetting visualBackground;
    public NumberSetting visualBgSize;
    public NumberSetting visualBgOffsetX;
    public NumberSetting visualBgOffsetY;
    public ColorSetting visualBgColor;
    public NumberSetting visualBgRadius;
    public BooleanSetting visualBgOutline;
    public ColorSetting visualOutlineColor;
    public NumberSetting visualOutlineWidth;
    public ModeSetting visualOutlinePosition;

    protected void initVisualSettings() {
        visualSize = new NumberSetting("Size", 100.0, -200.0, 200.0, 1.0, "Visual");
        visualOffsetX = new NumberSetting("OffsetX", 0.0, -10.0, 10.0, 0.5, "Visual");
        visualOffsetY = new NumberSetting("OffsetY", 0.0, -10.0, 10.0, 0.5, "Visual");

        visualText = new BooleanSetting("Text", true, "Visual");
        visualTextSize = new NumberSetting("TextSize", 100.0, -200.0, 200.0, 1.0, "Visual").withParent(visualText);
        visualTextColor = new ColorSetting("Color", 0xFF38B6FF, "Visual").withParent(visualText);
        visualShadow = new BooleanSetting("Shadow", true, "Visual").withParent(visualText);
        visualShadowColor = new ColorSetting("ShadowColor", 0x80000000, "Visual").withParent(visualShadow);
        visualShadowOffsetX = new NumberSetting("ShadowOffsetX", 1.0, -10.0, 10.0, 0.5, "Visual").withParent(visualShadow);
        visualShadowOffsetY = new NumberSetting("ShadowOffsetY", 1.0, -10.0, 10.0, 0.5, "Visual").withParent(visualShadow);

        visualBackground = new BooleanSetting("Background", true, "Visual");
        visualBgSize = new NumberSetting("BackgroundSize", 100.0, -200.0, 200.0, 1.0, "Visual").withParent(visualBackground);
        visualBgOffsetX = new NumberSetting("BackgroundOffsetX", 0.0, -10.0, 10.0, 0.5, "Visual").withParent(visualBackground);
        visualBgOffsetY = new NumberSetting("BackgroundOffsetY", 0.0, -10.0, 10.0, 0.5, "Visual").withParent(visualBackground);
        visualBgColor = new ColorSetting("BackgroundColor", 0x90000000, "Visual").withParent(visualBackground);
        visualBgRadius = new NumberSetting("BackgroundRadius", 4.0, 0.0, 20.0, 0.5, "Visual").withParent(visualBackground);
        visualBgOutline = new BooleanSetting("BackgroundOutline", true, "Visual").withParent(visualBackground);
        visualOutlineColor = new ColorSetting("OutlineColor", 0x25FFFFFF, "Visual").withParent(visualBgOutline);
        visualOutlineWidth = new NumberSetting("OutlineWidth", 1.0, 0.5, 5.0, 0.5, "Visual").withParent(visualBgOutline);
        visualOutlinePosition = new ModeSetting("OutlinePosition", "Center", "Visual", "Inside", "Center", "Outside").withParent(visualBgOutline);

        addSettings(
                visualSize, visualOffsetX, visualOffsetY,
                visualText, visualTextSize, visualTextColor, visualShadow, visualShadowColor, visualShadowOffsetX, visualShadowOffsetY,
                visualBackground, visualBgSize, visualBgOffsetX, visualBgOffsetY, visualBgColor, visualBgRadius,
                visualBgOutline, visualOutlineColor, visualOutlineWidth, visualOutlinePosition
        );
    }

    public static float getScaleFromPercent(double percent) {
        if (percent >= 100.0) {
            return (float) (percent / 100.0);
        }
        return Math.max(0.1f, 0.25f + (float) ((percent + 200.0) / 300.0) * 0.75f);
    }

    public void beginVisualTransform(float pivotX, float pivotY) {
        GlStateManager.pushMatrix();
        float offX = visualOffsetX != null ? visualOffsetX.getValue().floatValue() : 0.0f;
        float offY = visualOffsetY != null ? visualOffsetY.getValue().floatValue() : 0.0f;
        float scale = visualSize != null ? getScaleFromPercent(visualSize.getValue()) : 1.0f;
        GlStateManager.translate(pivotX + offX, pivotY + offY, 0.0f);
        GlStateManager.scale(scale, scale, 1.0f);
        GlStateManager.translate(-pivotX, -pivotY, 0.0f);
    }

    public void endVisualTransform() {
        GlStateManager.popMatrix();
    }

    public void drawVisualBackground(float x, float y, float width, float height) {
        if (visualBackground != null && !visualBackground.getValue()) {
            return;
        }
        float bgScale = visualBgSize != null ? getScaleFromPercent(visualBgSize.getValue()) : 1.0f;
        float bgOffX = visualBgOffsetX != null ? visualBgOffsetX.getValue().floatValue() : 0.0f;
        float bgOffY = visualBgOffsetY != null ? visualBgOffsetY.getValue().floatValue() : 0.0f;
        float radius = visualBgRadius != null ? visualBgRadius.getValue().floatValue() : 4.0f;
        int bgColor = visualBgColor != null ? visualBgColor.getRGB() : 0x90000000;

        float bw = width * bgScale;
        float bh = height * bgScale;
        float bx = x + (width - bw) * 0.5f + bgOffX;
        float by = y + (height - bh) * 0.5f + bgOffY;

        RenderUtil.drawRoundedRect(bx, by, bw, bh, radius, bgColor);

        if (visualBgOutline == null || visualBgOutline.getValue()) {
            float ow = visualOutlineWidth != null ? visualOutlineWidth.getValue().floatValue() : 1.0f;
            int outColor = visualOutlineColor != null ? visualOutlineColor.getRGB() : 0x25FFFFFF;
            String pos = visualOutlinePosition != null ? visualOutlinePosition.getValue() : "Center";

            float ox = bx;
            float oy = by;
            float owBox = bw;
            float ohBox = bh;
            if ("Inside".equalsIgnoreCase(pos)) {
                ox += ow * 0.5f;
                oy += ow * 0.5f;
                owBox = Math.max(0.0f, bw - ow);
                ohBox = Math.max(0.0f, bh - ow);
            } else if ("Outside".equalsIgnoreCase(pos)) {
                ox -= ow * 0.5f;
                oy -= ow * 0.5f;
                owBox = bw + ow;
                ohBox = bh + ow;
            }
            RenderUtil.drawRoundedOutline(ox, oy, owBox, ohBox, radius, ow, outColor);
        }
    }

    public float getVisualTextWidth(String text) {
        if (text == null || text.isEmpty()) return 0.0f;
        if (visualText != null && !visualText.getValue()) return 0.0f;
        float tScale = visualTextSize != null ? getScaleFromPercent(visualTextSize.getValue()) : 1.0f;
        return FontUtil.getHudStringWidth(FontUtil.stripColorCodes(text)) * tScale;
    }

    public float getVisualTextHeight() {
        if (visualText != null && !visualText.getValue()) return 0.0f;
        float tScale = visualTextSize != null ? getScaleFromPercent(visualTextSize.getValue()) : 1.0f;
        return 8.0f * tScale;
    }

    public void drawVisualText(String text, float x, float y) {
        int color = visualTextColor != null ? visualTextColor.getRGB() : 0xFFFFFFFF;
        drawVisualText(text, x, y, color);
    }

    public void drawVisualText(String text, float x, float y, int overrideColor) {
        if (text == null || text.isEmpty()) return;
        if (visualText != null && !visualText.getValue()) return;
        if (mc.fontRendererObj == null) return;

        float tScale = visualTextSize != null ? getScaleFromPercent(visualTextSize.getValue()) : 1.0f;
        boolean drawShadow = visualShadow == null || visualShadow.getValue();
        int shColor = visualShadowColor != null ? visualShadowColor.getRGB() : 0x80000000;
        float shOffX = visualShadowOffsetX != null ? visualShadowOffsetX.getValue().floatValue() : 1.0f;
        float shOffY = visualShadowOffsetY != null ? visualShadowOffsetY.getValue().floatValue() : 1.0f;

        String clean = FontUtil.stripColorCodes(text);

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 0.0f);
        GlStateManager.scale(tScale, tScale, 1.0f);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.enableAlpha();

        if (drawShadow) {
            mc.fontRendererObj.drawString(clean, shOffX, shOffY, shColor, false);
        }
        mc.fontRendererObj.drawString(clean, 0.0f, 0.0f, overrideColor, false);

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
    }
}
