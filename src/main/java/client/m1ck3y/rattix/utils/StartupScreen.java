package client.m1ck3y.rattix.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.Display;

import java.awt.Color;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Properties;

/**
 * StartupScreen - 参考 FPSMaster-Edge 实现的黑底 + ricon 启动屏与加载屏
 */
public class StartupScreen {
    private static final int BG_COLOR = new Color(20, 20, 20).getRGB(); // #141414 (FPSMaster-Edge 启动背景)
    private static final int BAR_BG_COLOR = new Color(30, 35, 46).getRGB(); // #1E232E
    private static final int BAR_FG_COLOR = new Color(56, 182, 255).getRGB(); // #38B6FF (Rattix Cyan)
    private static final int BAR_BORDER_COLOR = new Color(48, 54, 61, 180).getRGB();
    private static final int TEXT_COLOR = 0xFFCCCCCC;
    private static final int PERCENT_COLOR = 0xFFFFFFFF;

    private static float currentProgress = 0.0f;
    private static String currentStatus = "Initializing Rattix...";
    private static volatile boolean forgeSplashStopped = false;

    /**
     * 终止 Forge 原生 SplashProgress 后台渲染线程，杜绝多线程双缓冲画面争夺与闪烁
     */
    public static synchronized void stopForgeSplash() {
        if (forgeSplashStopped) return;
        forgeSplashStopped = true;
        try {
            Class<?> clazz = Class.forName("net.minecraftforge.fml.client.SplashProgress");
            Field enabledField = clazz.getDeclaredField("enabled");
            enabledField.setAccessible(true);
            boolean isEnabled = enabledField.getBoolean(null);

            if (isEnabled) {
                // 如果 Forge 的 SplashProgress 后台线程正在跑，立即调用 finish() 停止并回收资源
                Method finishMethod = clazz.getDeclaredMethod("finish");
                finishMethod.setAccessible(true);
                finishMethod.invoke(null);

                // 将 enabled 设为 false，确保后续 Forge 自身再次调用 finish() 时直接返回，不报错
                enabledField.setBoolean(null, false);
            }
        } catch (Throwable ignored) {
        }
    }

    /**
     * 渲染启动屏幕（由 MixinSplashScreen / MixinMinecraft 或外部调用）
     */
    public static void renderSplashScreen() {
        draw(null, currentProgress);
    }

    /**
     * 绘制启动屏界面（参考 FPSMaster-Edge drawSplashScreen）
     *
     * @param status   当前执行的状态文本
     * @param progress 当前加载进度 (0.0F - 1.0F)
     */
    public static void draw(String status, float progress) {
        // 先确保 Forge 后台线程已停，避免多线程闪烁
        stopForgeSplash();

        if (status != null && !status.isEmpty()) {
            currentStatus = status;
        }
        if (progress >= 0.0f) {
            currentProgress = Math.max(0.0f, Math.min(1.0f, progress));
        }

        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || !Display.isCreated()) {
            return;
        }

        try {
            if (!"Rattix".equals(Display.getTitle())) {
                Display.setTitle("Rattix");
                IconUtils.initLwjglIcon();
            }
        } catch (Throwable ignored) {}

        ScaledResolution sr = new ScaledResolution(mc);
        int scale = sr.getScaleFactor();
        int width = sr.getScaledWidth();
        int height = sr.getScaledHeight();

        if (width <= 0 || height <= 0 || scale <= 0) {
            return;
        }

        int dispWidth = width * scale;
        int dispHeight = height * scale;

        Framebuffer framebuffer = new Framebuffer(dispWidth, dispHeight, true);
        try {
            framebuffer.bindFramebuffer(false);
            GlStateManager.matrixMode(5889); // GL_PROJECTION
            GlStateManager.loadIdentity();
            GlStateManager.ortho(0.0D, width, height, 0.0D, 1000.0D, 3000.0D);
            GlStateManager.matrixMode(5888); // GL_MODELVIEW
            GlStateManager.loadIdentity();
            GlStateManager.translate(0.0F, 0.0F, -2000.0F);
            GlStateManager.disableLighting();
            GlStateManager.disableFog();
            GlStateManager.disableDepth();
            GlStateManager.resetColor();

            // 1. 绘制纯黑背景 (FPSMaster 同款 new Color(20, 20, 20))
            Gui.drawRect(0, 0, width, height, BG_COLOR);

            // 2. 居中绘制高清 ricon 图标
            float iconSize = 96.0f;
            float iconX = (width - iconSize) / 2.0f;
            float iconY = (height - iconSize) / 2.0f - (currentProgress > 0 ? 24.0f : 0.0f);
            IconUtils.drawIconDirect(iconX, iconY, iconSize);

            // 3. 绘制进度条（仅当有进度时显示）
            if (currentProgress > 0.0f) {
                float barWidth = 220.0f;
                float barHeight = 4.0f;
                float barX = (width - barWidth) / 2.0f;
                float barY = iconY + iconSize + 22.0f;

                // 进度条背景及外框
                Gui.drawRect((int) (barX - 1), (int) (barY - 1), (int) (barX + barWidth + 1), (int) (barY + barHeight + 1), BAR_BORDER_COLOR);
                Gui.drawRect((int) barX, (int) barY, (int) (barX + barWidth), (int) (barY + barHeight), BAR_BG_COLOR);

                // 进度条填充
                float fillWidth = barWidth * currentProgress;
                if (fillWidth > 0) {
                    Gui.drawRect((int) barX, (int) barY, (int) (barX + fillWidth), (int) (barY + barHeight), BAR_FG_COLOR);
                }

                // 4. 文本提示与百分比
                FontRenderer font = mc.fontRendererObj;
                if (font != null) {
                    String percentStr = (int) (currentProgress * 100) + "%";
                    int pctW = font.getStringWidth(percentStr);
                    font.drawString(percentStr, (width - pctW) / 2.0f, barY - 12.0f, PERCENT_COLOR, false);

                    if (currentStatus != null && !currentStatus.isEmpty()) {
                        int strW = font.getStringWidth(currentStatus);
                        font.drawString(currentStatus, (width - strW) / 2.0f, barY + barHeight + 10.0f, TEXT_COLOR, false);
                    }
                }
            }

            GlStateManager.disableLighting();
            GlStateManager.disableFog();
            framebuffer.unbindFramebuffer();
            framebuffer.framebufferRender(dispWidth, dispHeight);
            GlStateManager.enableAlpha();
            GlStateManager.alphaFunc(516, 0.1F);
            Display.update();
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            framebuffer.deleteFramebuffer();
        }
    }

    /**
     * 禁用 Forge 原生双缓冲争夺线程，设置 splash.properties enabled=false
     */
    public static void setupSplashProgress() {
        try {
            File mcDir = Minecraft.getMinecraft().mcDataDir;
            File configDir = new File(mcDir, "config");
            if (!configDir.exists()) configDir.mkdirs();

            File splashFile = new File(configDir, "splash.properties");
            Properties props = new Properties();
            props.setProperty("enabled", "false"); // 禁用 Forge 原生后台线程，杜绝多线程冲突与原始 GUI 闪烁
            props.setProperty("rotate", "false");
            props.setProperty("background", "0x141414");
            props.setProperty("font", "0x141414");
            props.setProperty("barBorder", "0x141414");
            props.setProperty("bar", "0x141414");
            props.setProperty("barBackground", "0x141414");
            props.setProperty("logoTexture", "textures/gui/title/mojang.png");
            props.setProperty("logoOffset", "0");
            props.setProperty("showMemory", "false");

            try (FileWriter writer = new FileWriter(splashFile)) {
                props.store(writer, "Rattix Splash Screen Properties");
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /**
     * 替换原版 loadingScreen，使后续世界载入和资源刷新均呈现统一的黑底 + ricon 风格
     */
    public static void installLoadingScreen() {
        try {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc != null && !(mc.loadingScreen instanceof RattixLoadingScreen)) {
                mc.loadingScreen = new RattixLoadingScreen(mc);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
