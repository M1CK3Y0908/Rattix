package client.m1ck3y.rattix.ui.client;

import client.m1ck3y.rattix.utils.render.IconUtils;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Properties;

/**
 * StartupScreen - 纯黑背景 + 屏幕中上方 20% 窗口宽度的 Rattix 图标启动屏（仅在游戏启动阶段显示）
 */
public class StartupScreen {
    private static final int BG_COLOR = 0xFF000000; // 纯黑背景

    private static float currentProgress = 0.0f;
    private static String currentStatus = "Initializing Rattix...";
    private static volatile boolean forgeSplashStopped = false;
    private static volatile boolean gameStarted = false;

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

    private static long lastPumpTime = 0;
    private static volatile boolean rendering = false;

    /**
     * 泵送单帧（供 Mixin / Forge 加载管线调用，保持窗口响应与画面刷新）
     */
    public static void pumpFrame() {
        if (gameStarted) return;
        if (!Display.isCreated()) return;

        long now = System.currentTimeMillis();
        if (now - lastPumpTime < 100) {
            return;
        }
        if (rendering) return;
        rendering = true;
        try {
            lastPumpTime = now;
            renderFrameDirect();
            Display.processMessages();
        } catch (Throwable ignored) {
        } finally {
            rendering = false;
        }
    }

    /**
     * 客户端启动完成时调用，停止启动帧泵送，后续加载世界和进入服务器均使用原版界面
     */
    public static void onStartupComplete() {
        gameStarted = true;
    }

    /**
     * 渲染启动屏幕（仅在启动阶段由 MixinSplashScreen / MixinMinecraft 调用）
     */
    public static void renderSplashScreen() {
        if (gameStarted) return;
        stopForgeSplash();
        renderFrameDirect();
        Display.processMessages();
    }

    /**
     * 绘制启动屏界面（仅在启动阶段生效）
     *
     * @param status   当前执行的状态文本
     * @param progress 当前加载进度 (0.0F - 1.0F)
     */
    public static void draw(String status, float progress) {
        if (gameStarted) return;
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

        // 渲染当前帧并刷新系统事件
        renderFrameDirect();
        Display.processMessages();
    }

    /**
     * 直接渲染一帧纯黑背景 + 屏幕中央固定 175x175 的 Rattix 图标
     */
    public static void renderFrameDirect() {
        if (gameStarted || !Display.isCreated()) {
            return;
        }

        int width = Display.getWidth();
        int height = Display.getHeight();
        if (width <= 0 || height <= 0) {
            return;
        }

        // 保存完整的 OpenGL 属性与矩阵堆栈，确保不影响后续 Minecraft 原生渲染状态
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glPushMatrix();

        try {
            // 设置标准 2D 正交投影
            GL11.glViewport(0, 0, width, height);
            GL11.glMatrixMode(GL11.GL_PROJECTION);
            GL11.glLoadIdentity();
            GL11.glOrtho(0.0D, width, height, 0.0D, -1000.0D, 3000.0D);
            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            GL11.glLoadIdentity();
            GL11.glTranslatef(0.0F, 0.0F, -2000.0F);

            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_FOG);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

            // 1. 绘制纯黑背景 (0xFF000000)
            drawSolidRect(0, 0, width, height, BG_COLOR);

            float h = (float) height;
            float w = (float) width;

            // 2. Rattix 图标：固定 175x175 大小，固定在屏幕正中央
            float logoSize = 175.0f;
            float logoX = (w - logoSize) / 2.0f;
            float logoY = (h - logoSize) / 2.0f;
            IconUtils.drawIconDirect(logoX, logoY, logoSize);

            Display.update();
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            GL11.glPopMatrix();
            GL11.glPopAttrib();
        }
    }

    /**
     * 纯 OpenGL 快速实心矩形绘制（无任何 Minecraft / Tessellator 依赖）
     */
    private static void drawSolidRect(float left, float top, float right, float bottom, int color) {
        float f3 = (float) (color >> 24 & 255) / 255.0F;
        float f = (float) (color >> 16 & 255) / 255.0F;
        float f1 = (float) (color >> 8 & 255) / 255.0F;
        float f2 = (float) (color & 255) / 255.0F;
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(f, f1, f2, f3);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(left, bottom);
        GL11.glVertex2f(right, bottom);
        GL11.glVertex2f(right, top);
        GL11.glVertex2f(left, top);
        GL11.glEnd();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    /**
     * 禁用 Forge 原生双缓冲争夺线程，设置 splash.properties enabled=false
     */
    public static void setupSplashProgress() {
        try {
            File configDir = null;
            try {
                configDir = net.minecraftforge.fml.common.Loader.instance().getConfigDir();
            } catch (Throwable ignored) {}
            if (configDir == null) {
                try {
                    Minecraft mc = Minecraft.getMinecraft();
                    if (mc != null && mc.mcDataDir != null) {
                        configDir = new File(mc.mcDataDir, "config");
                    }
                } catch (Throwable ignored) {}
            }
            if (configDir == null) {
                configDir = new File("config");
            }
            if (!configDir.exists()) configDir.mkdirs();

            File splashFile = new File(configDir, "splash.properties");
            Properties props = new Properties();
            props.setProperty("enabled", "false");
            props.setProperty("rotate", "false");
            props.setProperty("background", "0x000000");
            props.setProperty("font", "0x000000");
            props.setProperty("barBorder", "0x000000");
            props.setProperty("bar", "0x000000");
            props.setProperty("barBackground", "0x000000");
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
}
