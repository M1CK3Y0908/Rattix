package client.m1ck3y.rattix.ui.client;

import client.m1ck3y.rattix.ui.window.WindowManager;
import client.m1ck3y.rattix.utils.render.AugustusMainMenuRenderer;
import client.m1ck3y.rattix.utils.render.shader.ShaderBackgroundRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
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

    @Override
    public void initGui() {
        buttonList.clear();
        int x = Math.max(250, width / 2 - 115), y = height / 2 - 18;
        buttonList.add(new GuiButton(SINGLEPLAYER, x, y, 220, 24, "Singleplayer"));
        buttonList.add(new GuiButton(MULTIPLAYER, x, y + 31, 220, 24, "Multiplayer"));
        buttonList.add(new GuiButton(OPTIONS, x, y + 62, 106, 24, "Options"));
        buttonList.add(new GuiButton(QUIT, x + 114, y + 62, 106, 24, "Quit"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
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
