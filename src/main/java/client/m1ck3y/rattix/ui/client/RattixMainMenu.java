package client.m1ck3y.rattix.ui.client;

import client.m1ck3y.rattix.ui.elements.FluentGuiButton;
import client.m1ck3y.rattix.ui.window.WindowManager;
import client.m1ck3y.rattix.utils.render.AugustusMainMenuRenderer;
import client.m1ck3y.rattix.utils.render.shader.ShaderBackgroundRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Mouse;

import java.io.IOException;

/**
 * RattixMainMenu - Desktop canvas for Rattix Client.
 * Features an Augustus-inspired dark launcher layout with an integrated WindowManager
 * for floating, resizable, draggable, maximizable, and minimizable secondary page windows.
 */
public class RattixMainMenu extends GuiScreen {
    private static final int SINGLEPLAYER = 1;
    private static final int MULTIPLAYER = 2;
    private static final int MODULES = 3;
    private static final int SETTINGS = 4;
    private static final int OPTIONS = 5;
    private static final int QUIT = 6;

    private final WindowManager windowManager = new WindowManager(this);

    public WindowManager getWindowManager() {
        return windowManager;
    }

    public int getRailWidth() {
        if (width < 400) {
            return Math.max(140, width / 2);
        }
        return 180;
    }

    public int getButtonsStartY() {
        int btnH = (height < 280) ? 22 : 26;
        int btnGap = (height < 280) ? 5 : 8;
        int totalH = 6 * btnH + 5 * btnGap;
        return Math.max(45, (height - totalH) / 2);
    }

    private int lastScaleFactor = -1;

    public void checkResolutionUpdate() {
        if (mc == null) return;
        ScaledResolution sr = new ScaledResolution(mc);
        int srW = sr.getScaledWidth();
        int srH = sr.getScaledHeight();
        int scaleFactor = sr.getScaleFactor();

        if (this.width != srW || this.height != srH || this.lastScaleFactor != scaleFactor) {
            int oldW = this.width;
            int oldH = this.height;
            this.width = srW;
            this.height = srH;
            this.lastScaleFactor = scaleFactor;

            this.buttonList.clear();
            this.initGui();

            if (windowManager != null) {
                windowManager.onScreenResize(oldW, oldH, srW, srH);
            }
        }
    }

    @Override
    public void setWorldAndResolution(Minecraft mc, int width, int height) {
        int oldW = this.width;
        int oldH = this.height;
        super.setWorldAndResolution(mc, width, height);
        if (mc != null) {
            ScaledResolution sr = new ScaledResolution(mc);
            this.lastScaleFactor = sr.getScaleFactor();
        }
        if (windowManager != null && (oldW != width || oldH != height)) {
            windowManager.onScreenResize(oldW, oldH, width, height);
        }
    }

    @Override
    public void onResize(Minecraft mc, int w, int h) {
        int oldW = this.width;
        int oldH = this.height;
        super.onResize(mc, w, h);
        if (mc != null) {
            ScaledResolution sr = new ScaledResolution(mc);
            this.lastScaleFactor = sr.getScaleFactor();
        }
        if (windowManager != null) {
            windowManager.onScreenResize(oldW, oldH, w, h);
        }
    }

    @Override
    public void initGui() {
        buttonList.clear();

        int railW = getRailWidth();
        int btnW = Math.max(110, railW - 30);
        int btnX = (railW - btnW) / 2;

        int btnH = (height < 280) ? 22 : 26;
        int btnGap = (height < 280) ? 5 : 8;
        int startY = getButtonsStartY();

        buttonList.add(new FluentGuiButton(SINGLEPLAYER, btnX, startY, btnW, btnH, "Singleplayer"));
        buttonList.add(new FluentGuiButton(MULTIPLAYER, btnX, startY + (btnH + btnGap), btnW, btnH, "Multiplayer"));
        buttonList.add(new FluentGuiButton(MODULES, btnX, startY + (btnH + btnGap) * 2, btnW, btnH, "Modules"));
        buttonList.add(new FluentGuiButton(SETTINGS, btnX, startY + (btnH + btnGap) * 3, btnW, btnH, "Settings"));
        buttonList.add(new FluentGuiButton(OPTIONS, btnX, startY + (btnH + btnGap) * 4, btnW, btnH, "Options"));
        buttonList.add(new FluentGuiButton(QUIT, btnX, startY + (btnH + btnGap) * 5, btnW, btnH, "Quit"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        checkResolutionUpdate();

        // 1. 独立背景渲染阶段（Background Pass）：动态 GLSL 着色器或后备默认背景
        renderBackgroundPass();

        // 2. 独立 UI 渲染阶段（UI Pass）：完全隔离在干净的标准 GUI 正交管线中
        renderUIPass(mouseX, mouseY, partialTicks);
    }

    private void renderBackgroundPass() {
        if (ShaderBackgroundRenderer.shouldRenderShader()) {
            ShaderBackgroundRenderer.renderCurrentScreen(true);
        } else {
            drawDefaultBackground();
            ShaderBackgroundRenderer.isolateBackgroundAndPrepareUI();
        }
    }

    private void renderUIPass(int mouseX, int mouseY, float partialTicks) {
        // 保证 UI 渲染阶段的管线纯净，不受背景着色器的任何状态影响
        ShaderBackgroundRenderer.isolateBackgroundAndPrepareUI();

        // 绘制左侧半透明黑色导航栏及 4x Rattix 标题
        AugustusMainMenuRenderer.draw(this, mc, mouseX, mouseY, null, windowManager);

        // 绘制竖向居中排列的导航按钮
        super.drawScreen(mouseX, mouseY, partialTicks);

        // 绘制窗口管理器的浮动桌面窗口
        if (windowManager != null) {
            windowManager.drawWindows(mouseX, mouseY, partialTicks);
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        switch (button.id) {
            case SINGLEPLAYER:
                windowManager.openSingleplayer(this);
                break;
            case MULTIPLAYER:
                windowManager.openMultiplayer(this);
                break;
            case MODULES:
                windowManager.openModules();
                break;
            case SETTINGS:
                windowManager.openClientSettings(this);
                break;
            case OPTIONS:
                windowManager.openOptions(this);
                break;
            case QUIT:
                mc.shutdown();
                break;
            default:
                break;
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (windowManager != null && windowManager.mouseClicked(mouseX, mouseY, mouseButton)) {
            return;
        }
        int railW = getRailWidth();
        int btnStartY = getButtonsStartY();
        if (mouseX < railW && mouseY < btnStartY) {
            // Clicking top brand logo minimizes all windows to show desktop
            if (windowManager != null) {
                windowManager.minimizeAll();
                return;
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        if (windowManager != null) {
            windowManager.mouseReleased(mouseX, mouseY, state);
        }
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (windowManager != null) {
            windowManager.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        }
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    @Override
    public void handleMouseInput() throws IOException {
        checkResolutionUpdate();
        if (windowManager != null) {
            int mouseX = Mouse.getEventX() * width / Math.max(1, mc.displayWidth);
            int mouseY = height - Mouse.getEventY() * height / Math.max(1, mc.displayHeight) - 1;
            windowManager.handleMouseInput(mouseX, mouseY);
        }
        super.handleMouseInput();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (windowManager != null && windowManager.keyTyped(typedChar, keyCode)) {
            return;
        }
        if (keyCode == 1) {
            // Main menu desktop ignores Escape key when no window is active
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void updateScreen() {
        if (windowManager != null) {
            windowManager.updateScreen();
        }
        super.updateScreen();
    }
}
