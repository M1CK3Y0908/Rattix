package client.m1ck3y.rattix.ui.window;

import client.m1ck3y.rattix.ui.font.FontUtil;
import client.m1ck3y.rattix.utils.render.RenderUtil;
import client.m1ck3y.rattix.utils.render.SvgIconHelper;
import client.m1ck3y.rattix.utils.ui.CursorUtil;
import client.m1ck3y.rattix.utils.ui.GuiScreenBridge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.Stack;

/**
 * SubpageWindow - Floating, resizable, draggable, maximizable, and minimizable secondary page window.
 * Matches ClickGUI's design language, dimensions (640x400 / 300x40 minimized), multi-window focus,
 * and Win11 vector control architecture with 100% parity.
 */
public class SubpageWindow {

    // Global rendering flags used by mixins to avoid fullscreen shader conflicts
    public static boolean isRenderingEmbedded = false;
    public static float currentContentX = 0;
    public static float currentContentY = 0;

    // Resizing constants
    public static final int RESIZE_NONE = 0;
    public static final int RESIZE_N = 1;
    public static final int RESIZE_S = 2;
    public static final int RESIZE_W = 3;
    public static final int RESIZE_E = 4;
    public static final int RESIZE_NW = 5;
    public static final int RESIZE_NE = 6;
    public static final int RESIZE_SW = 7;
    public static final int RESIZE_SE = 8;

    public static final float RESIZE_BORDER = 5.0f;
    public static final float RESIZE_CORNER = 12.0f;

    // Dimensions matching ClickGUI
    public static final float MIN_WINDOW_WIDTH = 380.0f;
    public static final float MIN_WINDOW_HEIGHT = 240.0f;
    public static final float MINIMIZED_WIDTH = 180.0f;
    public static final float MINIMIZED_HEIGHT = 20.0f;

    private final String id;
    private String title;
    private final WindowType type;
    private GuiScreen content;
    private final Stack<GuiScreen> history = new Stack<>();

    private float posX;
    private float posY;
    private float windowWidth;
    private float windowHeight;
    private float minWidth = MIN_WINDOW_WIDTH;
    private float minHeight = MIN_WINDOW_HEIGHT;

    private float restoreX;
    private float restoreY;
    private float restoreW;
    private float restoreH;

    private boolean isMaximized = false;
    private boolean isMinimized = false;

    // Dragging state
    private boolean dragging = false;
    private float dragX = 0;
    private float dragY = 0;
    private long lastTitlebarClickTime = 0;

    // Resizing state
    private boolean resizing = false;
    private int currentResizeDir = RESIZE_NONE;
    private float resizeMouseStartX = 0;
    private float resizeMouseStartY = 0;
    private float resizeStartPosX = 0;
    private float resizeStartPosY = 0;
    private float resizeStartW = 0;
    private float resizeStartH = 0;

    // Layout matching ClickGUI (23px control buttons, 1/2 proportion, unified titlebar)
    private final float titlebarH = 20.0f;
    private final float controlW = 23.0f;

    /**
     * Modular constructor requiring only: Registration Name (id), Title, Display Content, and Default Resolution.
     * Packages window components together for clean, unified invocation across the client.
     *
     * @param id           注册名称 (Unique window identifier, e.g. "singleplayer", "alts", etc.)
     * @param title        标题 (Window title displayed in tab and taskbar chip)
     * @param content      显示内容 (GuiScreen embedded inside the window)
     * @param defaultWidth 默认分辨率宽 (Default window width)
     * @param defaultHeight 默认分辨率高 (Default window height)
     */
    public SubpageWindow(String id, String title, GuiScreen content, float defaultWidth, float defaultHeight) {
        this(id, WindowType.fromId(id), title, content, -1.0f, -1.0f, defaultWidth, defaultHeight);
    }

    /**
     * Convenience constructor requiring only: Registration Name (id), Title, and Display Content.
     * Uses standard default resolution (500 x 340).
     *
     * @param id      注册名称 (Unique window identifier)
     * @param title   标题 (Window title)
     * @param content 显示内容 (GuiScreen embedded inside the window)
     */
    public SubpageWindow(String id, String title, GuiScreen content) {
        this(id, title, content, 500.0f, 340.0f);
    }

    public SubpageWindow(String id, WindowType type, String title, GuiScreen content, float posX, float posY, float width, float height) {
        this.id = (id != null && !id.isEmpty()) ? id : (type != null ? type.name().toLowerCase() : "window");
        this.type = (type != null) ? type : WindowType.fromId(this.id);
        this.title = (title != null && !title.isEmpty()) ? title : (this.type != null ? this.type.getDefaultTitle() : "Window");
        this.content = content;
        this.posX = posX;
        this.posY = posY;
        this.windowWidth = Math.max(minWidth, width);
        this.windowHeight = Math.max(minHeight, height);
        this.restoreX = this.posX;
        this.restoreY = this.posY;
        this.restoreW = this.windowWidth;
        this.restoreH = this.windowHeight;

        Minecraft mc = Minecraft.getMinecraft();
        if (content != null && mc != null) {
            content.setWorldAndResolution(mc, (int) getContentW(), (int) getContentH());
        }
    }

    public String getId() {
        return id;
    }

    public WindowType getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public GuiScreen getContent() {
        return content;
    }

    public Stack<GuiScreen> getHistory() {
        return history;
    }

    public boolean isMaximized() {
        return isMaximized;
    }

    public void setMaximized(boolean maximized) {
        if (this.isMaximized != maximized) {
            toggleMaximize();
        }
    }

    public boolean isMinimized() {
        return isMinimized;
    }

    public void setMinimized(boolean minimized) {
        this.isMinimized = minimized;
        if (!minimized) {
            Minecraft mc = Minecraft.getMinecraft();
            if (content != null && mc != null) {
                content.setWorldAndResolution(mc, (int) getContentW(), (int) getContentH());
            }
        }
    }

    public float getPosX() {
        return posX;
    }

    public void setPosX(float posX) {
        this.posX = posX;
    }

    public float getPosY() {
        return posY;
    }

    public void setPosY(float posY) {
        this.posY = posY;
    }

    public float getWindowWidth() {
        return windowWidth;
    }

    public void setWindowWidth(float windowWidth) {
        this.windowWidth = Math.max(minWidth, windowWidth);
    }

    public float getWindowHeight() {
        return windowHeight;
    }

    public void setWindowHeight(float windowHeight) {
        this.windowHeight = Math.max(minHeight, windowHeight);
    }

    public float getMinWidth() {
        return minWidth;
    }

    public void setMinWidth(float minWidth) {
        this.minWidth = minWidth;
    }

    public float getMinHeight() {
        return minHeight;
    }

    public void setMinHeight(float minHeight) {
        this.minHeight = minHeight;
    }

    public float getContentW() {
        Minecraft mc = Minecraft.getMinecraft();
        float screenW = mc.currentScreen != null ? mc.currentScreen.width : 854;
        return isMaximized ? screenW : windowWidth;
    }

    public float getContentH() {
        Minecraft mc = Minecraft.getMinecraft();
        float screenH = mc.currentScreen != null ? mc.currentScreen.height : 480;
        return (isMaximized ? screenH : windowHeight) - titlebarH;
    }

    public void pushScreen(GuiScreen next) {
        if (next == null) return;
        if (this.content != null) {
            this.history.push(this.content);
        }
        this.content = next;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc != null) {
            this.content.setWorldAndResolution(mc, (int) getContentW(), (int) getContentH());
        }
    }

    public boolean popScreen() {
        if (!history.isEmpty()) {
            if (this.content != null) {
                this.content.onGuiClosed();
            }
            this.content = history.pop();
            Minecraft mc = Minecraft.getMinecraft();
            if (this.content != null && mc != null) {
                this.content.setWorldAndResolution(mc, (int) getContentW(), (int) getContentH());
            }
            return true;
        }
        return false;
    }

    public void toggleMaximize() {
        if (!isMaximized) {
            restoreX = posX;
            restoreY = posY;
            restoreW = windowWidth;
            restoreH = windowHeight;
            isMaximized = true;
        } else {
            isMaximized = false;
            posX = restoreX;
            posY = restoreY;
            windowWidth = restoreW;
            windowHeight = restoreH;
        }
        Minecraft mc = Minecraft.getMinecraft();
        if (content != null && mc != null) {
            content.setWorldAndResolution(mc, (int) getContentW(), (int) getContentH());
        }
    }

    public void update() {
        if (content != null && !isMinimized) {
            content.updateScreen();
        }
    }

    public void onWindowClosed() {
        if (content != null) {
            content.onGuiClosed();
        }
        while (!history.isEmpty()) {
            GuiScreen s = history.pop();
            if (s != null) {
                s.onGuiClosed();
            }
        }
        if (resizing) {
            CursorUtil.setCursor(CursorUtil.CURSOR_ARROW);
            resizing = false;
        }
    }

    public void drawWindow(int mouseX, int mouseY, boolean isTopFocus, int minIndex, float partialTicks) {
        if (isMinimized) {
            drawMinimized(mouseX, mouseY, isTopFocus, minIndex);
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        float screenW = mc.currentScreen != null ? mc.currentScreen.width : 854;
        float screenH = mc.currentScreen != null ? mc.currentScreen.height : 480;

        // 1. Process Window Dragging
        if (dragging && !isMaximized) {
            if (!Mouse.isButtonDown(0)) {
                dragging = false;
            } else {
                posX = mouseX - dragX;
                posY = mouseY - dragY;
            }
        }

        // 2. Process Window Resizing
        if (resizing && !isMaximized) {
            if (!Mouse.isButtonDown(0)) {
                resizing = false;
                CursorUtil.setCursor(CursorUtil.CURSOR_ARROW);
            } else {
                float deltaX = mouseX - resizeMouseStartX;
                float deltaY = mouseY - resizeMouseStartY;
                float newX = resizeStartPosX;
                float newY = resizeStartPosY;
                float newW = resizeStartW;
                float newH = resizeStartH;

                if (currentResizeDir == RESIZE_E || currentResizeDir == RESIZE_NE || currentResizeDir == RESIZE_SE) {
                    newW = Math.max(minWidth, resizeStartW + deltaX);
                }
                if (currentResizeDir == RESIZE_W || currentResizeDir == RESIZE_NW || currentResizeDir == RESIZE_SW) {
                    float targetW = resizeStartW - deltaX;
                    if (targetW < minWidth) {
                        newW = minWidth;
                        newX = resizeStartPosX + (resizeStartW - minWidth);
                    } else {
                        newW = targetW;
                        newX = resizeStartPosX + deltaX;
                    }
                }
                if (currentResizeDir == RESIZE_S || currentResizeDir == RESIZE_SW || currentResizeDir == RESIZE_SE) {
                    newH = Math.max(minHeight, resizeStartH + deltaY);
                }
                if (currentResizeDir == RESIZE_N || currentResizeDir == RESIZE_NW || currentResizeDir == RESIZE_NE) {
                    float targetH = resizeStartH - deltaY;
                    if (targetH < minHeight) {
                        newH = minHeight;
                        newY = resizeStartPosY + (resizeStartH - minHeight);
                    } else {
                        newH = targetH;
                        newY = resizeStartPosY + deltaY;
                    }
                }

                posX = newX;
                posY = newY;
                windowWidth = newW;
                windowHeight = newH;
            }
        }

        // 3. Screen bounds restriction
        if (!isMaximized) {
            if (posY < 0) posY = 0;
            if (posY > screenH - titlebarH) posY = screenH - titlebarH;
            if (posX + windowWidth < 40) posX = 40 - windowWidth;
            if (posX > screenW - 40) posX = screenW - 40;
        }

        float curX = isMaximized ? 0 : posX;
        float curY = isMaximized ? 0 : posY;
        float curW = isMaximized ? screenW : windowWidth;
        float curH = isMaximized ? screenH : windowHeight;
        float winRadius = isMaximized ? 0 : 5.0f;

        // 4. Update Hardware Cursor when hovering resize borders
        if (isTopFocus && !isMaximized && !dragging) {
            int dir = getResizeDirection(mouseX, mouseY);
            int activeDir = resizing ? currentResizeDir : dir;
            switch (activeDir) {
                case RESIZE_N:
                case RESIZE_S:
                    CursorUtil.setCursor(CursorUtil.CURSOR_SIZE_V);
                    break;
                case RESIZE_W:
                case RESIZE_E:
                    CursorUtil.setCursor(CursorUtil.CURSOR_SIZE_H);
                    break;
                case RESIZE_NW:
                case RESIZE_SE:
                    CursorUtil.setCursor(CursorUtil.CURSOR_SIZE_NWSE);
                    break;
                case RESIZE_NE:
                case RESIZE_SW:
                    CursorUtil.setCursor(CursorUtil.CURSOR_SIZE_NESW);
                    break;
                default:
                    if (isInsideWindow(mouseX, mouseY)) {
                        CursorUtil.setCursor(CursorUtil.CURSOR_ARROW);
                    }
                    break;
            }
        }

        // 5. Draw Window Surface & Drop Shadow (matching ClickGUI)
        if (!isMaximized) {
            int shadowCol = isTopFocus ? 0x65000000 : 0x40000000;
            RenderUtil.drawDropShadow(curX, curY, curW, curH, winRadius, 8.0f, shadowCol);
        }
        int winBg = 0xFF0C0C0C;
        RenderUtil.drawRoundedRect(curX, curY, curW, curH, winRadius, winBg);

        // 6. Draw Titlebar (Clean window titlebar without tab container)
        int tbBg = 0xFF202020;
        int tbSep = 0xFF303030;
        RenderUtil.drawRoundedRect(curX, curY, curW, titlebarH, isMaximized ? 0 : winRadius, isMaximized ? 0 : winRadius, 0, 0, tbBg);
        RenderUtil.drawRect(curX, curY + titlebarH - 1, curX + curW, curY + titlebarH, tbSep);

        // Left Icon & Title (10px icon + FontUtil 1x string, vertically centered)
        float textOffset = curX + 7.0f;
        int iconColor = isTopFocus ? 0xFF76B9ED : 0xFFB0B0B0;
        drawTypeIcon(textOffset, curY + (titlebarH - 10.0f) / 2.0f, iconColor);
        textOffset += 15.0f;

        int textColor = isTopFocus ? 0xFFFFFFFF : 0xFFD7D7D7;
        FontUtil.drawString1x(title, textOffset, curY + (titlebarH - 8.0f) / 2.0f, textColor);

        // 7. Right Control Buttons (Minimize, Maximize/Restore, Close - 23px wide, 20px high)
        float closeBtnX = curX + curW - controlW;
        float maxBtnX = closeBtnX - controlW;
        float minBtnX = maxBtnX - controlW;

        int ctrlHoverBg = 0xFF333333;
        int ctrlIcon = 0xFFDDDDDD;
        float iconOffX = (controlW - 5.0f) / 2.0f; // 9.0f
        float iconOffY = (titlebarH - 5.0f) / 2.0f; // 7.5f

        // Minimize button
        boolean minHover = isTopFocus && isHovered(mouseX, mouseY, minBtnX, curY, controlW, titlebarH);
        if (minHover) RenderUtil.drawRect(minBtnX, curY, minBtnX + controlW, curY + titlebarH, ctrlHoverBg);
        int minColor = minHover ? 0xFFFFFFFF : ctrlIcon;
        drawWin11MinimizeIcon(minBtnX + iconOffX, curY + iconOffY, minColor);

        // Maximize/Restore button
        boolean maxHover = isTopFocus && isHovered(mouseX, mouseY, maxBtnX, curY, controlW, titlebarH);
        if (maxHover) RenderUtil.drawRect(maxBtnX, curY, maxBtnX + controlW, curY + titlebarH, ctrlHoverBg);
        int maxColor = maxHover ? 0xFFFFFFFF : ctrlIcon;
        if (isMaximized) {
            drawWin11RestoreIcon(maxBtnX + iconOffX, curY + iconOffY, maxColor);
        } else {
            drawWin11MaximizeIcon(maxBtnX + iconOffX, curY + iconOffY, maxColor);
        }

        // Close button
        boolean closeHover = isTopFocus && isHovered(mouseX, mouseY, closeBtnX, curY, controlW, titlebarH);
        if (closeHover) {
            int closeHoverBg = 0xFFC42B1C;
            if (!isMaximized) {
                RenderUtil.drawRoundedRect(closeBtnX, curY, controlW, titlebarH, 0, winRadius, 0, 0, closeHoverBg);
            } else {
                RenderUtil.drawRect(closeBtnX, curY, closeBtnX + controlW, curY + titlebarH, closeHoverBg);
            }
        }
        int closeColor = closeHover ? 0xFFFFFFFF : ctrlIcon;
        drawWin11CloseIcon(closeBtnX + iconOffX, curY + iconOffY, closeColor);

        // 8. Content Area Rendering with Scissoring
        float contentX = curX;
        float contentY = curY + titlebarH;
        float contentW = curW;
        float contentH = curH - titlebarH;

        if (content != null) {
            if (content.width != (int) contentW || content.height != (int) contentH) {
                content.setWorldAndResolution(mc, (int) contentW, (int) contentH);
            }

            RenderUtil.enableScissor(contentX, contentY, contentW, contentH);
            GlStateManager.pushMatrix();
            GlStateManager.translate(contentX, contentY, 0);

            int relMouseX = (int) (mouseX - contentX);
            int relMouseY = (int) (mouseY - contentY);

            isRenderingEmbedded = true;
            currentContentX = contentX;
            currentContentY = contentY;
            try {
                content.drawScreen(relMouseX, relMouseY, partialTicks);
            } catch (Throwable t) {
                t.printStackTrace();
            } finally {
                isRenderingEmbedded = false;
                GlStateManager.popMatrix();
                RenderUtil.disableScissor();
            }
        }

        // 9. Window Border Outline
        if (!isMaximized) {
            int outlineColor = isTopFocus ? 0xFF454545 : 0xFF282828;
            RenderUtil.drawRoundedOutline(curX, curY, curW, curH, winRadius, 1.0f, outlineColor);
        }
    }

    private void drawMinimized(int mouseX, int mouseY, boolean isTopFocus, int minIndex) {
        Minecraft mc = Minecraft.getMinecraft();
        float screenW = mc.currentScreen != null ? mc.currentScreen.width : 854;
        float screenH = mc.currentScreen != null ? mc.currentScreen.height : 480;

        float minX = getMinimizedX(minIndex, screenW);
        float minY = getMinimizedY(minIndex, screenH);
        float minW = MINIMIZED_WIDTH;  // 160.0f
        float minH = MINIMIZED_HEIGHT; // 20.0f
        float winRadius = 4.0f;

        // 1. Drop Shadow
        int shadowColor = isTopFocus ? 0x65000000 : 0x40000000;
        RenderUtil.drawDropShadow(minX, minY, minW, minH, winRadius, 6.0f, shadowColor);

        // 2. Background
        int minBg = 0xF2202020;
        RenderUtil.drawRoundedRect(minX, minY, minW, minH, winRadius, minBg);

        // 3. Outline
        int outlineColor = isTopFocus ? 0xFF4C4C4C : 0xFF303030;
        RenderUtil.drawRoundedOutline(minX, minY, minW, minH, winRadius, 1.0f, outlineColor);

        // 4. Icon & Title (10px icon + FontUtil 1x string)
        int accent = 0xFF76B9ED;
        float textOffset = minX + 6.0f;
        drawTypeIcon(textOffset, minY + (minH - 10.0f) / 2.0f, accent);
        textOffset += 14.0f;

        float btnW = controlW; // 12.0f
        float closeX = minX + minW - btnW;
        float maxX = closeX - btnW;
        float restoreX = maxX - btnW;

        float maxTitleW = restoreX - textOffset - 4.0f;
        String displayTitle = title;
        if (FontUtil.getStringWidth1x(displayTitle) > maxTitleW) {
            while (displayTitle.length() > 3 && FontUtil.getStringWidth1x(displayTitle + "...") > maxTitleW) {
                displayTitle = displayTitle.substring(0, displayTitle.length() - 1);
            }
            displayTitle = displayTitle + "...";
        }
        FontUtil.drawString1x(displayTitle, textOffset, minY + (minH - 8.0f) / 2.0f, 0xFFFFFFFF);

        // 5. Right Control Buttons: Restore, Maximize, Close
        int ctrlHoverBg = 0xFF333333;
        int ctrlIcon = 0xFFDDDDDD;
        float iconOffX = (btnW - 5.0f) / 2.0f; // 9.0f
        float iconOffY = (minH - 5.0f) / 2.0f; // 7.5f

        // Restore
        boolean restoreHover = isHovered(mouseX, mouseY, restoreX, minY, btnW, minH);
        if (restoreHover) {
            RenderUtil.drawRect(restoreX, minY, restoreX + btnW, minY + minH, ctrlHoverBg);
        }
        int restoreColor = restoreHover ? 0xFFFFFFFF : ctrlIcon;
        drawWin11RestoreIcon(restoreX + iconOffX, minY + iconOffY, restoreColor);

        // Maximize
        boolean maxHover = isHovered(mouseX, mouseY, maxX, minY, btnW, minH);
        if (maxHover) {
            RenderUtil.drawRect(maxX, minY, maxX + btnW, minY + minH, ctrlHoverBg);
        }
        int maxColor = maxHover ? 0xFFFFFFFF : ctrlIcon;
        drawWin11MaximizeIcon(maxX + iconOffX, minY + iconOffY, maxColor);

        // Close
        boolean closeHover = isHovered(mouseX, mouseY, closeX, minY, btnW, minH);
        if (closeHover) {
            RenderUtil.drawRoundedRect(closeX, minY, btnW, minH, 0, winRadius, winRadius, 0, 0xFFC42B1C);
        }
        int closeColor = closeHover ? 0xFFFFFFFF : ctrlIcon;
        drawWin11CloseIcon(closeX + iconOffX, minY + iconOffY, closeColor);
    }

    public float getMinimizedX(int minIndex, float screenW) {
        Minecraft mc = Minecraft.getMinecraft();
        boolean onMainMenu = (mc.currentScreen instanceof client.m1ck3y.rattix.ui.client.RattixMainMenu);
        float margin = onMainMenu ? 204.0f : 16.0f;
        float gap = 8.0f;
        float availW = screenW - margin - 16.0f;
        int perRow = Math.max(1, (int) ((availW + gap) / (MINIMIZED_WIDTH + gap)));
        int col = Math.max(0, minIndex) % perRow;
        return margin + col * (MINIMIZED_WIDTH + gap);
    }

    public float getMinimizedY(int minIndex, float screenH) {
        float gap = 8.0f;
        Minecraft mc = Minecraft.getMinecraft();
        boolean onMainMenu = (mc.currentScreen instanceof client.m1ck3y.rattix.ui.client.RattixMainMenu);
        float marginX = onMainMenu ? 204.0f : 16.0f;
        float screenW = mc.currentScreen != null ? mc.currentScreen.width : 854;
        float availW = screenW - marginX - 16.0f;
        int perRow = Math.max(1, (int) ((availW + gap) / (MINIMIZED_WIDTH + gap)));
        int row = Math.max(0, minIndex) / perRow;
        return screenH - 12.0f - MINIMIZED_HEIGHT - row * (MINIMIZED_HEIGHT + gap);
    }

    public boolean isInsideWindow(float mx, float my) {
        if (isMinimized) return false;
        Minecraft mc = Minecraft.getMinecraft();
        float screenW = mc.currentScreen != null ? mc.currentScreen.width : 854;
        float screenH = mc.currentScreen != null ? mc.currentScreen.height : 480;
        float curX = isMaximized ? 0 : posX;
        float curY = isMaximized ? 0 : posY;
        float curW = isMaximized ? screenW : windowWidth;
        float curH = isMaximized ? screenH : windowHeight;

        return mx >= curX - RESIZE_BORDER && mx <= curX + curW + RESIZE_BORDER &&
               my >= curY - RESIZE_BORDER && my <= curY + curH + RESIZE_BORDER;
    }

    public boolean isInsideTitlebar(float mx, float my) {
        if (isMinimized) return false;
        Minecraft mc = Minecraft.getMinecraft();
        float screenW = mc.currentScreen != null ? mc.currentScreen.width : 854;
        float curX = isMaximized ? 0 : posX;
        float curY = isMaximized ? 0 : posY;
        float curW = isMaximized ? screenW : windowWidth;
        return mx >= curX && mx <= curX + curW && my >= curY && my <= curY + titlebarH;
    }

    public boolean isInsideContent(float mx, float my) {
        if (isMinimized) return false;
        Minecraft mc = Minecraft.getMinecraft();
        float screenW = mc.currentScreen != null ? mc.currentScreen.width : 854;
        float screenH = mc.currentScreen != null ? mc.currentScreen.height : 480;
        float curX = isMaximized ? 0 : posX;
        float curY = isMaximized ? 0 : posY;
        float curW = isMaximized ? screenW : windowWidth;
        float curH = isMaximized ? screenH : windowHeight;

        float contentX = curX;
        float contentY = curY + titlebarH;
        float contentW = curW;
        float contentH = curH - titlebarH;

        return mx >= contentX && mx <= contentX + contentW && my >= contentY && my <= contentY + contentH;
    }

    public int getResizeDirection(float mx, float my) {
        if (isMaximized || isMinimized) return RESIZE_NONE;

        float curX = posX;
        float curY = posY;
        float curW = windowWidth;
        float curH = windowHeight;

        boolean inX = mx >= curX - RESIZE_BORDER && mx <= curX + curW + RESIZE_BORDER;
        boolean inY = my >= curY - RESIZE_BORDER && my <= curY + curH + RESIZE_BORDER;
        if (!inX || !inY) return RESIZE_NONE;

        boolean nearLeft = mx <= curX + RESIZE_BORDER;
        boolean nearRight = mx >= curX + curW - RESIZE_BORDER;
        boolean nearTop = my <= curY + RESIZE_BORDER;
        boolean nearBottom = my >= curY + curH - RESIZE_BORDER;

        if (!nearLeft && !nearRight && !nearTop && !nearBottom) return RESIZE_NONE;

        boolean cornerLeft = mx <= curX + RESIZE_CORNER;
        boolean cornerRight = mx >= curX + curW - RESIZE_CORNER;
        boolean cornerTop = my <= curY + RESIZE_CORNER;
        boolean cornerBottom = my >= curY + curH - RESIZE_CORNER;

        if ((nearTop && cornerLeft) || (nearLeft && cornerTop)) return RESIZE_NW;
        if ((nearTop && cornerRight) || (nearRight && cornerTop)) return RESIZE_NE;
        if ((nearBottom && cornerLeft) || (nearLeft && cornerBottom)) return RESIZE_SW;
        if ((nearBottom && cornerRight) || (nearRight && cornerBottom)) return RESIZE_SE;

        if (nearTop) return RESIZE_N;
        if (nearBottom) return RESIZE_S;
        if (nearLeft) return RESIZE_W;
        if (nearRight) return RESIZE_E;

        return RESIZE_NONE;
    }

    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton, int minIndex, WindowManager manager) {
        if (isMinimized) {
            return mouseClickedMinimized(mouseX, mouseY, mouseButton, minIndex, manager);
        }

        Minecraft mc = Minecraft.getMinecraft();
        float screenW = mc.currentScreen != null ? mc.currentScreen.width : 854;
        float screenH = mc.currentScreen != null ? mc.currentScreen.height : 480;
        float curX = isMaximized ? 0 : posX;
        float curY = isMaximized ? 0 : posY;
        float curW = isMaximized ? screenW : windowWidth;
        float curH = isMaximized ? screenH : windowHeight;

        if (!isInsideWindow(mouseX, mouseY)) {
            return false;
        }

        manager.bringToFront(this);

        // 1. Check titlebar control buttons
        if (mouseY >= curY && mouseY <= curY + titlebarH) {
            float controlsTotalW = controlW * 3;
            float closeBtnX = curX + curW - controlW;
            float maxBtnX = closeBtnX - controlW;
            float minBtnX = maxBtnX - controlW;

            if (isHovered(mouseX, mouseY, closeBtnX, curY, controlW, titlebarH)) {
                manager.closeWindow(this);
                return true;
            }
            if (isHovered(mouseX, mouseY, maxBtnX, curY, controlW, titlebarH)) {
                toggleMaximize();
                return true;
            }
            if (isHovered(mouseX, mouseY, minBtnX, curY, controlW, titlebarH)) {
                setMinimized(true);
                return true;
            }

            // Clicked on titlebar (Drag or Double-Click Maximize)
            if (mouseButton == 0 && !isMaximized) {
                long now = System.currentTimeMillis();
                if (now - lastTitlebarClickTime < 280) {
                    toggleMaximize();
                    lastTitlebarClickTime = 0;
                    return true;
                }
                lastTitlebarClickTime = now;
                dragging = true;
                dragX = mouseX - posX;
                dragY = mouseY - posY;
                return true;
            }
            if (mouseButton == 0 && isMaximized) {
                long now = System.currentTimeMillis();
                if (now - lastTitlebarClickTime < 280) {
                    toggleMaximize();
                    lastTitlebarClickTime = 0;
                    return true;
                }
                lastTitlebarClickTime = now;
                return true;
            }
        }

        // 2. Check resize borders
        if (!isMaximized && mouseButton == 0) {
            int dir = getResizeDirection(mouseX, mouseY);
            if (dir != RESIZE_NONE) {
                resizing = true;
                currentResizeDir = dir;
                resizeMouseStartX = mouseX;
                resizeMouseStartY = mouseY;
                resizeStartPosX = posX;
                resizeStartPosY = posY;
                resizeStartW = windowWidth;
                resizeStartH = windowHeight;
                return true;
            }
        }

        // 3. Content click
        float contentX = curX;
        float contentY = curY + titlebarH;
        float contentW = curW;
        float contentH = curH - titlebarH;

        if (content != null && isHovered(mouseX, mouseY, contentX, contentY, contentW, contentH)) {
            int relX = (int) (mouseX - contentX);
            int relY = (int) (mouseY - contentY);
            try {
                GuiScreenBridge.mouseClicked(content, relX, relY, mouseButton);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }

        return true;
    }

    public boolean mouseClickedMinimized(int mouseX, int mouseY, int mouseButton, int minIndex, WindowManager manager) {
        Minecraft mc = Minecraft.getMinecraft();
        float screenW = mc.currentScreen != null ? mc.currentScreen.width : 854;
        float screenH = mc.currentScreen != null ? mc.currentScreen.height : 480;
        float curX = getMinimizedX(minIndex, screenW);
        float curY = getMinimizedY(minIndex, screenH);
        float curW = MINIMIZED_WIDTH;
        float curH = MINIMIZED_HEIGHT;

        if (!isHovered(mouseX, mouseY, curX, curY, curW, curH)) {
            return false;
        }

        float btnW = controlW;
        float closeX = curX + curW - btnW;
        float maxX = closeX - btnW;
        float restoreX = maxX - btnW;

        if (mouseButton == 0) {
            if (isHovered(mouseX, mouseY, closeX, curY, btnW, curH)) {
                manager.closeWindow(this);
                return true;
            }
            if (isHovered(mouseX, mouseY, maxX, curY, btnW, curH)) {
                setMinimized(false);
                setMaximized(true);
                manager.bringToFront(this);
                return true;
            }
            if (isHovered(mouseX, mouseY, restoreX, curY, btnW, curH)) {
                setMinimized(false);
                setMaximized(false);
                manager.bringToFront(this);
                return true;
            }
            setMinimized(false);
            manager.bringToFront(this);
            return true;
        }
        return true;
    }

    public void mouseReleased(int mouseX, int mouseY, int state) {
        if (dragging) {
            dragging = false;
        }
        if (resizing) {
            resizing = false;
            CursorUtil.setCursor(CursorUtil.CURSOR_ARROW);
        }
        if (content != null && !isMinimized) {
            float contentX = isMaximized ? 0 : posX;
            float contentY = (isMaximized ? 0 : posY) + titlebarH;
            int relX = (int) (mouseX - contentX);
            int relY = (int) (mouseY - contentY);
            GuiScreenBridge.mouseReleased(content, relX, relY, state);
        }
    }

    public void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (content != null && !isMinimized && !dragging && !resizing) {
            float contentX = isMaximized ? 0 : posX;
            float contentY = (isMaximized ? 0 : posY) + titlebarH;
            int relX = (int) (mouseX - contentX);
            int relY = (int) (mouseY - contentY);
            GuiScreenBridge.mouseClickMove(content, relX, relY, clickedMouseButton, timeSinceLastClick);
        }
    }

    public void handleMouseInput(int mouseX, int mouseY) throws IOException {
        if (content != null && !isMinimized) {
            if (isInsideContent(mouseX, mouseY)) {
                isRenderingEmbedded = true;
                currentContentX = isMaximized ? 0 : posX;
                currentContentY = (isMaximized ? 0 : posY) + titlebarH;
                try {
                    content.handleMouseInput();
                } finally {
                    isRenderingEmbedded = false;
                }
            }
        }
    }

    public boolean keyTyped(char typedChar, int keyCode) throws IOException {
        if (isMinimized) return false;
        if (keyCode == 1) { // Escape
            if (!history.isEmpty()) {
                popScreen();
                return true;
            }
            return false; // let window manager close window
        }
        if (content != null) {
            GuiScreenBridge.keyTyped(content, typedChar, keyCode);
            return true;
        }
        return false;
    }

    private void drawTypeIcon(float x, float y, int color) {
        if (type == null || type == WindowType.CUSTOM) {
            String cleanId = (id != null) ? id.toLowerCase().trim() : "";
            if (SvgIconHelper.hasIcon(cleanId)) {
                SvgIconHelper.drawIcon(cleanId, x, y, 10.0f, color);
            } else {
                RenderUtil.drawTerminalIcon(x, y, 10.0f, color);
            }
            return;
        }
        switch (type) {
            case MODULES:
                RenderUtil.drawModulesIcon(x, y, 10.0f, color);
                break;
            case SETTINGS:
                RenderUtil.drawTerminalIcon(x, y, 10.0f, color);
                break;
            case SINGLEPLAYER:
                SvgIconHelper.drawIcon("player", x, y, 10.0f, color);
                break;
            case MULTIPLAYER:
                SvgIconHelper.drawIcon("ghost", x, y, 10.0f, color);
                break;
            case OPTIONS:
                SvgIconHelper.drawIcon("other", x, y, 10.0f, color);
                break;
            default:
                RenderUtil.drawTerminalIcon(x, y, 10.0f, color);
                break;
        }
    }

    private void drawWin11MinimizeIcon(float x, float y, int color) {
        RenderUtil.drawLine(x + 0.5f, y + 2.5f, x + 4.5f, y + 2.5f, 1.0f, color);
    }

    private void drawWin11MaximizeIcon(float x, float y, int color) {
        RenderUtil.drawRectOutline(x + 0.5f, y + 0.5f, 4.0f, 4.0f, 1.0f, color);
    }

    private void drawWin11RestoreIcon(float x, float y, int color) {
        RenderUtil.drawRectOutline(x + 0.5f, y + 1.5f, 3.0f, 3.0f, 1.0f, color);
        RenderUtil.drawLine(x + 1.5f, y + 0.5f, x + 4.5f, y + 0.5f, 1.0f, color);
        RenderUtil.drawLine(x + 4.5f, y + 0.5f, x + 4.5f, y + 3.5f, 1.0f, color);
    }

    private void drawWin11CloseIcon(float x, float y, int color) {
        RenderUtil.drawLine(x + 0.5f, y + 0.5f, x + 4.5f, y + 4.5f, 1.0f, color);
        RenderUtil.drawLine(x + 4.5f, y + 0.5f, x + 0.5f, y + 4.5f, 1.0f, color);
    }

    private boolean isHovered(float mx, float my, float x, float y, float w, float h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }
}
