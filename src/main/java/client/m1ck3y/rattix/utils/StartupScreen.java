package client.m1ck3y.rattix.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Properties;

/**
 * StartupScreen - 纯黑背景 + 居中Logo + Win11同款转圈动画启动屏与加载屏
 */
public class StartupScreen {
    private static final int BG_COLOR = 0xFF000000; // 纯黑背景

    private static float currentProgress = 0.0f;
    private static String currentStatus = "Initializing Rattix...";
    private static volatile boolean forgeSplashStopped = false;
    private static volatile boolean gameStarted = false;

    // Win11 转圈动画关键帧数据：时间进度 (0.0~1.0), 旋转角度 (度), 不透明度 (0.0~1.0)
    private static final float[][] WIN11_KEYFRAMES = new float[][]{
            {0.00f, 225.0f, 0.0f},
            {0.06f, 345.0f, 1.0f},
            {0.28f, 455.0f, 1.0f},
            {0.40f, 690.0f, 1.0f},
            {0.68f, 815.0f, 1.0f},
            {0.80f, 945.0f, 1.0f},
            {0.84f, 945.0f, 0.0f},
            {1.00f, 945.0f, 0.0f}
    };

    private static final int NUM_DOTS = 5;
    private static final float CYCLE_DURATION_MS = 3200.0f;
    private static final float DOT_DELAY_MS = 150.0f;

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
     * 泵送单帧（供 Mixin / Forge 加载管线高频调用，内置 ~60 FPS 节流阀）
     */
    public static void pumpFrame() {
        if (gameStarted) return;
        if (!Display.isCreated()) return;

        long now = System.currentTimeMillis();
        if (now - lastPumpTime < 16) {
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
     * 客户端启动完成时调用，停止启动帧泵送
     */
    public static void onStartupComplete() {
        gameStarted = true;
    }

    /**
     * 渲染启动屏幕（由 MixinSplashScreen / MixinMinecraft 或外部调用）
     */
    public static void renderSplashScreen() {
        stopForgeSplash();
        renderFrameDirect();
        Display.processMessages();
    }

    /**
     * 绘制启动屏界面
     *
     * @param status   当前执行的状态文本
     * @param progress 当前加载进度 (0.0F - 1.0F)
     */
    public static void draw(String status, float progress) {
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
     * 直接渲染一帧纯黑背景 + 居中Logo + Win11转圈动画
     */
    public static void renderFrameDirect() {
        if (!Display.isCreated()) {
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

            // 2. Logo位置为距离顶部30%的垂直，水平居中，大小为15%的垂直高度
            float logoSize = h * 0.15f;
            float logoX = (w - logoSize) / 2.0f;
            float logoY = h * 0.30f;
            IconUtils.drawIconDirect(logoX, logoY, logoSize);

            // 3. 在距离顶部80%的垂直高度显示win11同款转圈动画，大小为5%的垂直高度
            float spinnerSize = h * 0.05f;
            float spinnerX = (w - spinnerSize) / 2.0f;
            float spinnerY = h * 0.80f;
            drawWin11Spinner(spinnerX, spinnerY, spinnerSize, System.currentTimeMillis());

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
     * 计算 Win11 动效小圆点的实时角度和透明度
     */
    private static float[] getDotState(float p) {
        p = Math.max(0.0f, Math.min(1.0f, p));
        for (int k = 0; k < WIN11_KEYFRAMES.length - 1; k++) {
            float t0 = WIN11_KEYFRAMES[k][0];
            float t1 = WIN11_KEYFRAMES[k + 1][0];
            if (t0 <= p && p <= t1) {
                float deg0 = WIN11_KEYFRAMES[k][1];
                float deg1 = WIN11_KEYFRAMES[k + 1][1];
                float op0 = WIN11_KEYFRAMES[k][2];
                float op1 = WIN11_KEYFRAMES[k + 1][2];
                float u = (p - t0) / (t1 - t0);
                float ease = u * u * (3.0f - 2.0f * u); // Smooth cubic ease-in-out
                float deg = deg0 + (deg1 - deg0) * ease;
                float op = op0 + (op1 - op0) * u;
                return new float[]{deg, op};
            }
        }
        return new float[]{945.0f, 0.0f};
    }

    /**
     * 绘制 Win11 5点聚散加速转圈动效
     *
     * @param x    转圈区域左上角 X
     * @param y    转圈区域左上角 Y (距离顶部80%高度)
     * @param size 转圈区域尺寸 (5%高度)
     * @param time 当前系统毫秒时间
     */
    public static void drawWin11Spinner(float x, float y, float size, long time) {
        float centerX = x + size / 2.0f;
        float centerY = y + size / 2.0f;
        float dotRadius = Math.max(1.5f, size * 0.075f);
        float orbitRadius = (size / 2.0f) - dotRadius;

        for (int i = 0; i < NUM_DOTS; i++) {
            float tau = (time - i * DOT_DELAY_MS) % CYCLE_DURATION_MS;
            if (tau < 0) tau += CYCLE_DURATION_MS;
            float p = tau / CYCLE_DURATION_MS;
            float[] state = getDotState(p);
            float deg = state[0];
            float opacity = state[1];

            if (opacity > 0.01f) {
                double rad = Math.toRadians(deg);
                float dotX = centerX + (float) (Math.cos(rad) * orbitRadius);
                float dotY = centerY + (float) (Math.sin(rad) * orbitRadius);
                drawSmoothDot(dotX, dotY, dotRadius, opacity);
            }
        }
    }

    /**
     * 绘制高抗锯齿平滑小圆点
     */
    private static void drawSmoothDot(float cx, float cy, float r, float alpha) {
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.disableCull();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(1.0f, 1.0f, 1.0f, alpha);

        GL11.glEnable(GL11.GL_POINT_SMOOTH);
        GL11.glHint(GL11.GL_POINT_SMOOTH_HINT, GL11.GL_NICEST);
        GL11.glPointSize(r * 2.0f);
        GL11.glBegin(GL11.GL_POINTS);
        GL11.glVertex2f(cx, cy);
        GL11.glEnd();

        GL11.glEnable(GL11.GL_POLYGON_SMOOTH);
        GL11.glHint(GL11.GL_POLYGON_SMOOTH_HINT, GL11.GL_NICEST);
        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GL11.glVertex2f(cx, cy);
        int segments = 24;
        for (int i = 0; i <= segments; i++) {
            double theta = i * (Math.PI * 2.0) / segments;
            GL11.glVertex2f(cx + (float) (Math.cos(theta) * r), cy + (float) (Math.sin(theta) * r));
        }
        GL11.glEnd();

        GlStateManager.enableAlpha();
        GlStateManager.enableCull();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
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

    /**
     * 替换原版 loadingScreen，使后续世界载入和资源刷新均呈现统一的黑底 + ricon 风格
     */
    public static void installLoadingScreen() {
        gameStarted = true;
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
