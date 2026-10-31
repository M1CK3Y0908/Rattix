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
    private static final int SINGLEPLAYER = 1, MULTIPLAYER = 2, OPTIONS = 3, QUIT = 4;

    private final WindowManager windowManager = new WindowManager(this);

    public WindowManager getWindowManager() {
        return windowManager;
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
        int minX = 196;
        int btnW = 220;
        if (width < minX + btnW + 10) {
            btnW = Math.max(120, width - minX - 10);
        }
        int x = Math.max(minX, Math.min(width - btnW - 10, (188 + width) / 2 - btnW / 2));
        int y = Math.max(20, Math.min(height - 100, height / 2 - 18));
        buttonList.add(new FluentGuiButton(SINGLEPLAYER, x, y, btnW, 24, "Singleplayer"));
        buttonList.add(new FluentGuiButton(MULTIPLAYER, x, y + 31, btnW, 24, "Multiplayer"));
        int halfW = (btnW - 8) / 2;
        buttonList.add(new FluentGuiButton(OPTIONS, x, y + 62, halfW, 24, "Options"));
        buttonList.add(new FluentGuiButton(QUIT, x + halfW + 8, y + 62, btnW - halfW - 8, 24, "Quit"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        checkResolutionUpdate();
        if (ShaderBackgroundRenderer.shouldRenderShader()) {
            ShaderBackgroundRenderer.renderCurrentScreen(true);
        } else {
            drawDefaultBackground();
        }
        AugustusMainMenuRenderer.draw(this, mc, mouseX, mouseY, null, windowManager);
        super.drawScreen(mouseX, mouseY, partialTicks);
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
        // Left navigation rail click handling (mouseX < 188)
        if (mouseX < 188) {
            if (mouseY >= 82 - 9 && mouseY <= 82 + 19) {
                windowManager.minimizeAll();
                return;
            } else if (mouseY >= 118 - 9 && mouseY <= 118 + 19) {
                windowManager.openModules();
                return;
            } else if (mouseY >= 154 - 9 && mouseY <= 154 + 19) {
                windowManager.openClientSettings(this);
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
