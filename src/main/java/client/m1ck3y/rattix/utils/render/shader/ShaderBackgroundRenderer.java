package client.m1ck3y.rattix.utils.render.shader;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.multiplayer.GuiConnecting;
import org.lwjgl.input.Mouse;

/**
 * ShaderBackgroundRenderer - 统一全局 GUI 背景着色器渲染器
 * 连接现有 ShaderManager，负责所有非局内界面的全屏动态背景调度、生命周期管理与帧去重保护。
 */
public final class ShaderBackgroundRenderer {

    private static final Minecraft MC = Minecraft.getMinecraft();
    private static long lastRenderTime = -1;

    private ShaderBackgroundRenderer() {
    }

    /**
     * 判断当前 GUI 界面是否应当使用着色器背景：
     * 1. 着色器已激活 (ShaderManager.isShaderActive())
     * 2. 属于非局内界面（theWorld == null）或者是连接/下载地形等全屏过渡界面
     */
    public static boolean shouldRenderShader() {
        if (!ShaderManager.getInstance().isShaderActive()) {
            return false;
        }
        if (MC == null) {
            return true;
        }
        if (MC.theWorld == null) {
            return true;
        }
        GuiScreen current = MC.currentScreen;
        return current instanceof GuiDownloadTerrain || current instanceof GuiConnecting;
    }

    /**
     * 统一渲染当前界面的 GLSL 动态背景着色器，
     * 自动提取屏幕分辨率与鼠标位置，并提供同一帧内的去重保护。
     */
    public static boolean renderCurrentScreen() {
        return renderCurrentScreen(false);
    }

    /**
     * 统一渲染当前界面的 GLSL 动态背景着色器，
     * @param force 是否强制重绘（忽略帧去重限制）
     */
    public static boolean renderCurrentScreen(boolean force) {
        if (!shouldRenderShader()) {
            return false;
        }

        // 帧防重保护：同一帧内（150 微秒极短时间内）不重复绘制全屏四边形，避免性能损耗与覆盖已绘制的兄弟列表组件
        long now = System.nanoTime();
        if (!force && lastRenderTime > 0 && (now - lastRenderTime) < 150_000L) {
            return ShaderManager.getInstance().canRenderBackground();
        }
        lastRenderTime = now;

        GuiScreen screen = MC != null ? MC.currentScreen : null;
        int width;
        int height;

        if (screen != null && screen.width > 0 && screen.height > 0) {
            width = screen.width;
            height = screen.height;
        } else if (MC != null) {
            ScaledResolution res = new ScaledResolution(MC);
            width = res.getScaledWidth();
            height = res.getScaledHeight();
        } else {
            width = 854;
            height = 480;
        }

        int dispW = (MC != null && MC.displayWidth > 0) ? MC.displayWidth : 1;
        int dispH = (MC != null && MC.displayHeight > 0) ? MC.displayHeight : 1;
        int mouseX = Mouse.getX() * width / dispW;
        int mouseY = height - Mouse.getY() * height / dispH - 1;

        try {
            ShaderManager manager = ShaderManager.getInstance();
            manager.render(width, height, mouseX, mouseY);
            // OptiFine may replace the framebuffer/GL capability path. Never cancel
            // vanilla background rendering unless a usable shader program exists.
            return manager.canRenderBackground();
        } catch (Throwable error) {
            System.err.println("[Rattix] Shader background failed; falling back to vanilla GUI background: " + error);
            return false;
        }
    }

    /**
     * 为加载界面（LoadingScreenRenderer 等）提供指定尺寸和中心鼠标位置的着色器渲染
     */
    public static void renderLoadingScreen(int width, int height) {
        if (!ShaderManager.getInstance().isShaderActive()) {
            return;
        }
        long now = System.nanoTime();
        if (lastRenderTime > 0 && (now - lastRenderTime) < 150_000L) {
            return;
        }
        lastRenderTime = now;
        ShaderManager.getInstance().render(width, height, width / 2, height / 2);
    }
}
