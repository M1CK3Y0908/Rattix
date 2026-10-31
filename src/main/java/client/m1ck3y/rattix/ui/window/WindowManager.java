package client.m1ck3y.rattix.ui.window;

import client.m1ck3y.rattix.ui.client.GuiClientConfiguration;
import client.m1ck3y.rattix.ui.client.GuiModulesOverview;
import client.m1ck3y.rattix.ui.client.RattixMainMenu;
import client.m1ck3y.rattix.utils.ui.CursorUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSelectWorld;
import net.minecraft.client.multiplayer.GuiConnecting;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * WindowManager - Manages floating, multi-window secondary pages for Rattix Client.
 * Coordinates z-order stacking, window focus, Win11 control actions, hardware cursor updates,
 * dock placement for minimized windows, and deep-stack child screen navigation routing.
 */
public class WindowManager {

    private final RattixMainMenu mainMenu;
    private final List<SubpageWindow> windows = new ArrayList<>();

    public WindowManager(RattixMainMenu mainMenu) {
        this.mainMenu = mainMenu;
    }

    public List<SubpageWindow> getWindows() {
        return windows;
    }

    public boolean isAnyWindowOpen() {
        return !windows.isEmpty();
    }

    public SubpageWindow getWindow(WindowType type) {
        for (SubpageWindow window : windows) {
            if (window.getType() == type) {
                return window;
            }
        }
        return null;
    }

    public SubpageWindow getActiveTopWindow() {
        for (int i = windows.size() - 1; i >= 0; i--) {
            SubpageWindow window = windows.get(i);
            if (!window.isMinimized()) {
                return window;
            }
        }
        return null;
    }

    public void bringToFront(SubpageWindow window) {
        if (window == null) return;
        if (windows.remove(window)) {
            windows.add(window);
        }
    }

    public void closeWindow(SubpageWindow window) {
        if (window == null) return;
        window.onWindowClosed();
        windows.remove(window);
        if (windows.isEmpty()) {
            CursorUtil.setCursor(CursorUtil.CURSOR_ARROW);
        }
    }

    public void closeAll() {
        for (SubpageWindow window : new ArrayList<>(windows)) {
            window.onWindowClosed();
        }
        windows.clear();
        CursorUtil.setCursor(CursorUtil.CURSOR_ARROW);
    }

    public void minimizeAll() {
        for (SubpageWindow window : windows) {
            window.setMinimized(true);
        }
    }

    public SubpageWindow getWindow(String id) {
        if (id == null) return null;
        for (SubpageWindow window : windows) {
            if (id.equalsIgnoreCase(window.getId())) {
                return window;
            }
        }
        return null;
    }

    public void openWindow(SubpageWindow window) {
        if (window == null) return;
        SubpageWindow existing = getWindow(window.getId());
        if (existing == null && window.getType() != null && window.getType() != WindowType.CUSTOM) {
            existing = getWindow(window.getType());
        }
        if (existing != null) {
            if (existing.isMinimized()) {
                existing.setMinimized(false);
            }
            bringToFront(existing);
            return;
        }
        if (window.getPosX() < 0 || window.getPosY() < 0) {
            float[] bounds = calcDefaultBounds(window.getWindowWidth(), window.getWindowHeight());
            window.setPosX(bounds[0]);
            window.setPosY(bounds[1]);
            window.setWindowWidth(bounds[2]);
            window.setWindowHeight(bounds[3]);
        }
        windows.add(window);
        bringToFront(window);
    }

    /**
     * Modular window opener requiring only: Registration Name (id), Title, Display Content, and Default Resolution.
     * Packages window handling together so callers don't need boilerplate.
     *
     * @param id           注册名称 (Unique window identifier, e.g. "singleplayer", "alts", etc.)
     * @param title        标题 (Window title shown in titlebar and taskbar chip)
     * @param content      显示内容 (GuiScreen embedded inside the window)
     * @param defaultWidth 默认分辨率宽 (Default window width)
     * @param defaultHeight 默认分辨率高 (Default window height)
     * @return The opened or focused SubpageWindow instance
     */
    public SubpageWindow open(String id, String title, GuiScreen content, float defaultWidth, float defaultHeight) {
        SubpageWindow existing = getWindow(id);
        if (existing != null) {
            if (existing.isMinimized()) existing.setMinimized(false);
            bringToFront(existing);
            return existing;
        }
        float[] bounds = calcDefaultBounds(defaultWidth, defaultHeight);
        SubpageWindow window = new SubpageWindow(
                id,
                WindowType.fromId(id),
                title,
                content,
                bounds[0], bounds[1], bounds[2], bounds[3]
        );
        windows.add(window);
        bringToFront(window);
        return window;
    }

    /**
     * Convenience window opener using standard default resolution (500 x 340).
     */
    public SubpageWindow open(String id, String title, GuiScreen content) {
        return open(id, title, content, 500.0f, 340.0f);
    }

    public void openSingleplayer(GuiScreen parent) {
        open("singleplayer", "Singleplayer", new GuiSelectWorld(parent != null ? parent : mainMenu), 500, 340);
    }

    public void openMultiplayer(GuiScreen parent) {
        open("multiplayer", "Multiplayer", new GuiMultiplayer(parent != null ? parent : mainMenu), 500, 340);
    }

    public void openOptions(GuiScreen parent) {
        Minecraft mc = Minecraft.getMinecraft();
        open("options", "Options", new GuiOptions(parent != null ? parent : mainMenu, mc.gameSettings), 500, 340);
    }

    public void openClientSettings(GuiScreen parent) {
        open("settings", "Client Settings", new GuiClientConfiguration(parent != null ? parent : mainMenu), 500, 340);
    }

    public void openModules() {
        open("modules", "Modules", new GuiModulesOverview(mainMenu), 500, 340);
    }

    public float[] calcDefaultBounds(float defaultW, float defaultH) {
        Minecraft mc = Minecraft.getMinecraft();
        float screenW = mc.currentScreen != null ? mc.currentScreen.width : 854;
        float screenH = mc.currentScreen != null ? mc.currentScreen.height : 480;

        float targetW = Math.max(SubpageWindow.MIN_WINDOW_WIDTH, Math.min(screenW - 220.0f, defaultW));
        float targetH = Math.max(SubpageWindow.MIN_WINDOW_HEIGHT, Math.min(screenH - 50.0f, defaultH));

        int offsetIndex = windows.size() % 6;
        float startX = Math.max(200.0f, Math.min(screenW - targetW - 10.0f, 210.0f + offsetIndex * 16.0f));
        float startY = Math.max(24.0f, Math.min(screenH - targetH - 20.0f, 32.0f + offsetIndex * 14.0f));

        return new float[]{startX, startY, targetW, targetH};
    }

    public float[] calcDefaultBounds() {
        return calcDefaultBounds(500.0f, 340.0f);
    }

    public boolean isMouseOverAnyWindow(int mouseX, int mouseY) {
        Minecraft mc = Minecraft.getMinecraft();
        float screenW = mc.currentScreen != null ? mc.currentScreen.width : 854;
        float screenH = mc.currentScreen != null ? mc.currentScreen.height : 480;

        int minIdx = 0;
        for (int i = 0; i < windows.size(); i++) {
            SubpageWindow w = windows.get(i);
            if (w.isMinimized()) {
                float minX = w.getMinimizedX(minIdx, screenW);
                float minY = w.getMinimizedY(minIdx, screenH);
                if (mouseX >= minX && mouseX <= minX + SubpageWindow.MINIMIZED_WIDTH &&
                        mouseY >= minY && mouseY <= minY + SubpageWindow.MINIMIZED_HEIGHT) {
                    return true;
                }
                minIdx++;
            } else if (w.isInsideWindow(mouseX, mouseY)) {
                return true;
            }
        }
        return false;
    }

    public void drawWindows(int mouseX, int mouseY, float partialTicks) {
        SubpageWindow topNonMin = getActiveTopWindow();
        int minIndex = 0;
        for (int i = 0; i < windows.size(); i++) {
            SubpageWindow w = windows.get(i);
            boolean isTop = (w == topNonMin);
            w.drawWindow(mouseX, mouseY, isTop, w.isMinimized() ? minIndex++ : 0, partialTicks);
        }
    }

    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        // 1. Minimized items first (reverse order)
        int minCount = 0;
        for (SubpageWindow w : windows) {
            if (w.isMinimized()) minCount++;
        }
        int currentMinIdx = minCount - 1;
        for (int i = windows.size() - 1; i >= 0; i--) {
            SubpageWindow w = windows.get(i);
            if (w.isMinimized()) {
                if (w.mouseClicked(mouseX, mouseY, mouseButton, currentMinIdx, this)) {
                    return true;
                }
                currentMinIdx--;
            }
        }

        // 2. Active floating windows (top to bottom)
        for (int i = windows.size() - 1; i >= 0; i--) {
            SubpageWindow w = windows.get(i);
            if (!w.isMinimized()) {
                if (w.mouseClicked(mouseX, mouseY, mouseButton, 0, this)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void mouseReleased(int mouseX, int mouseY, int state) {
        for (int i = windows.size() - 1; i >= 0; i--) {
            if (i < windows.size()) {
                windows.get(i).mouseReleased(mouseX, mouseY, state);
            }
        }
    }

    public void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        for (int i = windows.size() - 1; i >= 0; i--) {
            if (i < windows.size()) {
                windows.get(i).mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
            }
        }
    }

    public void handleMouseInput(int mouseX, int mouseY) throws IOException {
        for (int i = windows.size() - 1; i >= 0; i--) {
            if (i < windows.size()) {
                SubpageWindow w = windows.get(i);
                if (!w.isMinimized() && w.isInsideContent(mouseX, mouseY)) {
                    w.handleMouseInput(mouseX, mouseY);
                    return;
                }
            }
        }
    }

    public boolean keyTyped(char typedChar, int keyCode) throws IOException {
        SubpageWindow top = getActiveTopWindow();
        if (top != null) {
            if (!top.keyTyped(typedChar, keyCode)) {
                if (keyCode == 1) { // Escape closes window if stack is exhausted
                    closeWindow(top);
                    return true;
                }
            } else {
                return true;
            }
        }
        return false;
    }

    public void updateScreen() {
        for (int i = 0; i < windows.size(); i++) {
            windows.get(i).update();
        }
    }

    /**
     * Intercepts Minecraft.displayGuiScreen while RattixMainMenu is the active screen.
     * @param screen The incoming GuiScreen requested by vanilla or mod code
     * @return true to allow Minecraft to switch screens (e.g. entering world/server);
     *         false to intercept and handle inside the window stack.
     */
    public boolean onDisplayGuiScreen(GuiScreen screen) {
        if (screen instanceof GuiConnecting || screen instanceof GuiDownloadTerrain || screen instanceof net.minecraft.client.gui.GuiMemoryErrorScreen) {
            // World entry or connection: transition full screen
            return true;
        }

        SubpageWindow top = getActiveTopWindow();

        if (screen == null || screen instanceof GuiMainMenu || screen instanceof RattixMainMenu) {
            if (top != null) {
                if (!top.popScreen()) {
                    closeWindow(top);
                }
            }
            return false;
        }

        if (top != null) {
            // Check if user clicked Cancel / Back to return to parent screen
            if (!top.getHistory().isEmpty() && top.getHistory().peek() == screen) {
                top.popScreen();
                return false;
            }
            top.pushScreen(screen);
            return false;
        } else {
            // No window currently focused: automatically wrap child screen into a new window
            WindowType type = resolveWindowType(screen);
            float[] bounds = calcDefaultBounds();
            openWindow(new SubpageWindow(
                    type.name().toLowerCase(),
                    type,
                    type.getDefaultTitle(),
                    screen,
                    bounds[0], bounds[1], bounds[2], bounds[3]
            ));
            return false;
        }
    }

    private WindowType resolveWindowType(GuiScreen screen) {
        if (screen instanceof GuiSelectWorld) return WindowType.SINGLEPLAYER;
        if (screen instanceof GuiMultiplayer) return WindowType.MULTIPLAYER;
        if (screen instanceof GuiOptions) return WindowType.OPTIONS;
        if (screen instanceof GuiClientConfiguration) return WindowType.SETTINGS;
        if (screen instanceof GuiModulesOverview) return WindowType.MODULES;
        return WindowType.OPTIONS;
    }
}
