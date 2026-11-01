package client.m1ck3y.rattix.utils.render.shader;

import client.m1ck3y.rattix.ui.window.SubpageWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import org.lwjgl.input.Mouse;

/**
 * ShaderBackgroundRenderer - 统一全局 GUI 背景着色器渲染器
 * 连接现有 ShaderManager，负责所有非局内界面的全屏动态背景调度、生命周期管理与帧去重保护。
 * 同时建立背景与 UI 之间的严格隔离屏障，确保着色器渲染绝不污染或影响上层 UI 组件。
 */
public final class ShaderBackgroundRenderer {

    private static final Minecraft MC = Minecraft.getMinecraft();
    private static long lastRenderTime = -1;

    private ShaderBackgroundRenderer() {
    }

    /**
     * 判断当前 GUI 界面是否应当使用着色器背景：
     * 1. 不处于内嵌子页面窗口中（SubpageWindow.isRenderingEmbedded）
     * 2. 着色器已激活 (ShaderManager.isShaderActive())
     * 3. 属于非局内界面（theWorld == null）或者是连接/下载地形等全屏过渡界面
     */
    public static boolean shouldRenderShader() {
        if (SubpageWindow.isRenderingEmbedded) {
            return false;
        }
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
     * 统一渲染当前界面的 GLSL 动态背景着色器，并在渲染后立即恢复标准的 Minecraft 2D UI 渲染管线。
     * @param force 是否强制重绘（忽略帧去重限制）
     */
    public static boolean renderCurrentScreen(boolean force) {
        if (!shouldRenderShader()) {
            return false;
        }

        // 帧防重保护：同一帧内（2 毫秒极短时间内）不重复绘制全屏四边形，避免性能损耗与覆盖已绘制的兄弟列表组件
        long now = System.nanoTime();
        if (!force && lastRenderTime > 0 && (now - lastRenderTime) < 2_000_000L) {
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
            boolean success = manager.canRenderBackground();
            // 严格隔离背景与 UI：背景渲染完毕后立即重置并校准标准 2D GUI 渲染管线
            isolateBackgroundAndPrepareUI();
            return success;
        } catch (Throwable error) {
            System.err.println("[Rattix] Shader background failed; falling back to vanilla GUI background: " + error);
            isolateBackgroundAndPrepareUI();
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
        if (lastRenderTime > 0 && (now - lastRenderTime) < 2_000_000L) {
            return;
        }
        lastRenderTime = now;
        try {
            ShaderManager.getInstance().render(width, height, width / 2, height / 2);
        } finally {
            isolateBackgroundAndPrepareUI();
        }
    }

    /**
     * 严格隔离背景与 UI 渲染：
     * 1. 解绑任何残留的 GLSL 着色器程序，同步 OpenGlHelper 状态
     * 2. 重新绑定主 Framebuffer 并恢复视口大小
     * 3. 通过 setupOverlayRendering 重新校准 2D GUI 正交投影和模型视图矩阵
     * 4. 彻底重置深度、混合、透明度测试、纹理映射及顶点色彩等标准 Minecraft GUI 状态
     */
    public static void isolateBackgroundAndPrepareUI() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null) return;

        if (OpenGlHelper.shadersSupported) {
            OpenGlHelper.glUseProgram(0);
        }

        if (mc.getFramebuffer() != null) {
            mc.getFramebuffer().bindFramebuffer(true);
        }
        GlStateManager.viewport(0, 0, mc.displayWidth, mc.displayHeight);

        if (mc.entityRenderer != null) {
            mc.entityRenderer.setupOverlayRendering();
        }

        GlStateManager.enableTexture2D();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.disableDepth();
        GlStateManager.depthMask(true);
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.shadeModel(7424);
    }
}
