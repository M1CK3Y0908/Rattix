package client.m1ck3y.rattix.utils.ui;

import client.m1ck3y.rattix.file.FileManager;
import client.m1ck3y.rattix.ui.client.RattixMainMenu;
import client.m1ck3y.rattix.ui.window.SubpageWindow;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

/**
 * Rattix 专属界面 UI 尺寸与 DPI 缩放管理器 (UiScaleManager)。
 * 仅控制 Rattix 专属界面（主界面 RattixMainMenu、窗口边框 SubpageWindow 与非原版窗口内容），
 * 不影响原版 Minecraft 游戏视口与原版界面。
 *
 * 基准对齐：以现有的 2x 界面大小作为 settings 中的 100%（默认），
 * 扩展支持 125%、150%、175%、200%、225%、250% 及根据游戏窗口分辨率自动匹配的 Auto 模式（参考 Windows DPI 规范）。
 */
public class UiScaleManager {

    public enum ScaleMode {
        AUTO("Auto", 0.0f),
        SCALE_100("100%", 1.00f),
        SCALE_125("125%", 1.25f),
        SCALE_150("150%", 1.50f),
        SCALE_175("175%", 1.75f),
        SCALE_200("200%", 2.00f),
        SCALE_225("225%", 2.25f),
        SCALE_250("250%", 2.50f);

        private final String label;
        private final float multiplier;

        ScaleMode(String label, float multiplier) {
            this.label = label;
            this.multiplier = multiplier;
        }

        public String getLabel() {
            return label;
        }

        public float getMultiplier() {
            return multiplier;
        }

        public ScaleMode next() {
            ScaleMode[] values = values();
            return values[(this.ordinal() + 1) % values.length];
        }

        public static ScaleMode fromString(String str) {
            if (str == null) return SCALE_100;
            for (ScaleMode mode : values()) {
                if (mode.name().equalsIgnoreCase(str) || mode.label.equalsIgnoreCase(str)) {
                    return mode;
                }
            }
            return SCALE_100;
        }
    }

    private static final UiScaleManager INSTANCE = new UiScaleManager();
    private static final File CONFIG_FILE = new File(FileManager.RATTIX_DIR, "uiscale.json");

    private ScaleMode currentMode = ScaleMode.SCALE_100;

    private UiScaleManager() {
        loadConfig();
    }

    public static UiScaleManager getInstance() {
        return INSTANCE;
    }

    public ScaleMode getScaleMode() {
        return currentMode;
    }

    public void setScaleMode(ScaleMode mode) {
        if (mode == null) mode = ScaleMode.SCALE_100;
        this.currentMode = mode;
        saveConfig();
        notifyResolutionChanged();
    }

    public void cycleNext() {
        setScaleMode(currentMode.next());
    }

    /**
     * 获取设置界面按钮显示的文本
     */
    public String getDisplayString() {
        if (currentMode == ScaleMode.AUTO) {
            int recommended = getRecommendedPercent();
            return "UI Scale: Auto (" + recommended + "%)";
        } else if (currentMode == ScaleMode.SCALE_100) {
            return "UI Scale: 100% (Default)";
        } else {
            return "UI Scale: " + currentMode.getLabel();
        }
    }

    /**
     * 参考 Windows 分辨率推荐缩放标准计算当前分辨率的最佳缩放百分比
     */
    public int getRecommendedPercent() {
        Minecraft mc = Minecraft.getMinecraft();
        int dw = (mc != null) ? Math.max(1, mc.displayWidth) : 1920;
        int dh = (mc != null) ? Math.max(1, mc.displayHeight) : 1080;

        if (dw >= 3840 || dh >= 2160) {
            return 200; // 4K 推荐 200%
        } else if (dw >= 2880 || dh >= 1600) {
            return 175; // 3K 推荐 175%
        } else if (dw >= 2560 || dh >= 1440) {
            return 150; // 2K 推荐 150%
        } else if (dw >= 1920 && dh >= 1080) {
            return 125; // 1080p 推荐 125%
        } else {
            return 100; // 720p 或更小窗口推荐 100%
        }
    }

    /**
     * 获取相对 2x 基准的缩放倍率 (1.0f 对应 100%)
     */
    public float getScaleMultiplier() {
        if (currentMode == ScaleMode.AUTO) {
            return getRecommendedPercent() / 100.0f;
        }
        return currentMode.getMultiplier();
    }

    /**
     * 计算实际渲染时的有效 scaleFactor（基准 2.0 对应 100%）
     * 并根据游戏窗口尺寸进行防溢出安全钳位，确保在极端小窗口下 UI 界面依然完整可用。
     */
    public float getEffectiveScale() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null) return 2.0f;

        float baseScale = 2.0f;
        float target = baseScale * getScaleMultiplier();

        int dw = Math.max(1, mc.displayWidth);
        int dh = Math.max(1, mc.displayHeight);

        // 窗口尺寸约束：确保虚拟视口宽度 >= 340, 高度 >= 220，防止窗口超出游戏画布
        float maxAllowed = Math.max(1.0f, Math.min(dw / 340.0f, dh / 220.0f));
        target = Math.min(target, maxAllowed);
        return Math.max(1.0f, target);
    }

    /**
     * 获取 Rattix 视口的虚拟缩放宽度
     */
    public int getScaledWidth() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null) return 960;
        return (int) Math.ceil(mc.displayWidth / getEffectiveScale());
    }

    /**
     * 获取 Rattix 视口的虚拟缩放高度
     */
    public int getScaledHeight() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null) return 540;
        return (int) Math.ceil(mc.displayHeight / getEffectiveScale());
    }

    /**
     * 将当前鼠标硬件 X 坐标准确映射到 Rattix 视口坐标空间
     */
    public int getMouseX() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.displayWidth <= 0) return 0;
        return Mouse.getX() * getScaledWidth() / mc.displayWidth;
    }

    /**
     * 将当前鼠标硬件 Y 坐标准确映射到 Rattix 视口坐标空间
     */
    public int getMouseY() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.displayHeight <= 0) return 0;
        return getScaledHeight() - Mouse.getY() * getScaledHeight() / mc.displayHeight - 1;
    }

    /**
     * 建立匹配 Rattix UI Scale 的标准 OpenGL 正交投影管线
     */
    public void setupOverlayRendering() {
        int w = getScaledWidth();
        int h = getScaledHeight();
        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.loadIdentity();
        GlStateManager.ortho(0.0D, (double) w, (double) h, 0.0D, 1000.0D, 3000.0D);
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.loadIdentity();
        GlStateManager.translate(0.0F, 0.0F, -2000.0F);
    }

    /**
     * 判断当前是否处于 Rattix 专属 UI 渲染上下文中（包括主界面、窗口嵌入渲染、ClickGUI 等）
     */
    public static boolean isRattixUiActive() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null) return false;
        if (SubpageWindow.isRenderingEmbedded) return true;
        if (mc.currentScreen instanceof RattixMainMenu) return true;
        if (mc.currentScreen != null && mc.currentScreen.getClass().getName().contains("ClickGUI")) return true;
        return false;
    }

    /**
     * 通知当前界面刷新布局与窗口尺寸
     */
    public void notifyResolutionChanged() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc != null && mc.currentScreen instanceof RattixMainMenu) {
            ((RattixMainMenu) mc.currentScreen).checkResolutionUpdate();
        }
    }

    public void loadConfig() {
        try {
            if (CONFIG_FILE.exists()) {
                try (FileReader fr = new FileReader(CONFIG_FILE)) {
                    JsonObject obj = new JsonParser().parse(fr).getAsJsonObject();
                    if (obj.has("scaleMode")) {
                        this.currentMode = ScaleMode.fromString(obj.get("scaleMode").getAsString());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[Rattix] Failed to load UI Scale config: " + e.getMessage());
        }
    }

    public void saveConfig() {
        try {
            if (!FileManager.RATTIX_DIR.exists()) {
                FileManager.RATTIX_DIR.mkdirs();
            }
            JsonObject obj = new JsonObject();
            obj.addProperty("scaleMode", this.currentMode.name());
            try (FileWriter fw = new FileWriter(CONFIG_FILE)) {
                fw.write(obj.toString());
            }
        } catch (Exception e) {
            System.err.println("[Rattix] Failed to save UI Scale config: " + e.getMessage());
        }
    }
}
