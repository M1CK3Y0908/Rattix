package client.m1ck3y.rattix.modules.manager;

import client.m1ck3y.rattix.modules.*;
import client.m1ck3y.rattix.modules.ClickGUI.BooleanSetting;
import client.m1ck3y.rattix.modules.ClickGUI.CheckBoxSetting;
import client.m1ck3y.rattix.modules.ClickGUI.ColorSetting;
import client.m1ck3y.rattix.modules.ClickGUI.ModeSetting;
import client.m1ck3y.rattix.modules.ClickGUI.NumberSetting;
import client.m1ck3y.rattix.modules.ClickGUI.Setting;
import client.m1ck3y.rattix.utils.FontUtil;
import client.m1ck3y.rattix.utils.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

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
        visualOffsetX = new NumberSetting("Offset X", 0.0, -10.0, 10.0, 0.5, "Visual");
        visualOffsetY = new NumberSetting("Offset Y", 0.0, -10.0, 10.0, 0.5, "Visual");

        visualText = new BooleanSetting("Text", true, "Visual");
        visualTextSize = new NumberSetting("Text Size", 100.0, -200.0, 200.0, 1.0, "Visual").withParent(visualText);
        visualTextColor = new ColorSetting("Color", 0xFF38B6FF, "Visual").withParent(visualText);
        visualShadow = new BooleanSetting("Shadow", true, "Visual").withParent(visualText);
        visualShadowColor = new ColorSetting("Shadow Color", 0x80000000, "Visual").withParent(visualShadow);
        visualShadowOffsetX = new NumberSetting("Shadow Offset X", 1.0, -10.0, 10.0, 0.5, "Visual").withParent(visualShadow);
        visualShadowOffsetY = new NumberSetting("Shadow Offset Y", 1.0, -10.0, 10.0, 0.5, "Visual").withParent(visualShadow);

        visualBackground = new BooleanSetting("Background", true, "Visual");
        visualBgSize = new NumberSetting("Background Size", 100.0, -200.0, 200.0, 1.0, "Visual").withParent(visualBackground);
        visualBgOffsetX = new NumberSetting("Background Offset X", 0.0, -10.0, 10.0, 0.5, "Visual").withParent(visualBackground);
        visualBgOffsetY = new NumberSetting("Background Offset Y", 0.0, -10.0, 10.0, 0.5, "Visual").withParent(visualBackground);
        visualBgColor = new ColorSetting("Background Color", 0x90000000, "Visual").withParent(visualBackground);
        visualBgRadius = new NumberSetting("Background Radius", 4.0, 0.0, 20.0, 0.5, "Visual").withParent(visualBackground);
        visualBgOutline = new BooleanSetting("Background Outline", true, "Visual").withParent(visualBackground);
        visualOutlineColor = new ColorSetting("Outline Color", 0x25FFFFFF, "Visual").withParent(visualBgOutline);
        visualOutlineWidth = new NumberSetting("Outline Width", 1.0, 0.5, 5.0, 0.5, "Visual").withParent(visualBgOutline);
        visualOutlinePosition = new ModeSetting("Outline Position", "Center", "Visual", "Inside", "Center", "Outside").withParent(visualBgOutline);

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

    public static int getLoadedModuleCount() {
        return registeredModules.size();
    }

    public static int getLoadedOptionCount() {
        int count = 0;
        for (Register m : registeredModules) {
            // Key 绑定 (1) + 顶部通用选项 (LoadFromConfig, ShowInArrayList, ToggleSound) + addSettings 注册的 Setting
            count += 1 + m.getMetaOptions().size() + m.getSettings().size();
        }
        if (Font.getInstance() != null && Font.getInstance().getSettings() != null) {
            count += Font.getInstance().getSettings().size();
        }
        return count;
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
