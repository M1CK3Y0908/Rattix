package client.m1ck3y.rattix.ui.window;

import client.m1ck3y.rattix.utils.render.RenderUtil;
import client.m1ck3y.rattix.utils.ui.CursorUtil;
import client.m1ck3y.rattix.utils.ui.GuiScreenBridge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.Stack;

/**
 * SubpageWindow - Floating, resizable, draggable, maximizable, and minimizable secondary page window.
 * Matches ClickGUI's design language, multi-window focus, and Win11 control architecture.
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

    // Dock dimensions
    public static final float MINIMIZED_WIDTH = 150.0f;
    public static final float MINIMIZED_HEIGHT = 28.0f;

    private final String id;
    private String title;
    private final WindowType type;
    private GuiScreen content;
    private final Stack<GuiScreen> history = new Stack<>();

    private float posX;
    private float posY;
    private float windowWidth;
    private float windowHeight;
    private float minWidth = 420.0f;
    private float minHeight = 280.0f;

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

    // Resizing state
    private boolean resizing = false;
    private int currentResizeDir = RESIZE_NONE;
    private float resizeMouseStartX = 0;
    private float resizeMouseStartY = 0;
    private float resizeStartPosX = 0;
    private float resizeStartPosY = 0;
    private float resizeStartW = 0;
    private float resizeStartH = 0;

    // Layout
    private final float titlebarH = 30.0f;
    private final float controlW = 28.0f;

    public SubpageWindow(String id, WindowType type, String title, GuiScreen content, float posX, float posY, float width, float height) {
        this.id = id;
        this.type = type;
        this.title = (title != null && !title.isEmpty()) ? title : (type != null ? type.getDefaultTitle() : "Window");
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
        return isMaximized ? 0 : posX;
    }

    public float getPosY() {
        return isMaximized ? 0 : posY;
    }

    public float getWidth() {
        if (isMaximized) {
            Minecraft mc = Minecraft.getMinecraft();
            return mc.currentScreen != null ? mc.currentScreen.width : 854;
        }
        return windowWidth;
    }

    public float getHeight() {
        if (isMaximized) {
            Minecraft mc = Minecraft.getMinecraft();
            return mc.currentScreen != null ? mc.currentScreen.height : 480;
        }
        return windowHeight;
    }

    public float getContentW() {
        return Math.max(10, getWidth());
    }

    public float getContentH() {
        return Math.max(10, getHeight() - titlebarH);
    }

    public void pushScreen(GuiScreen newScreen) {
        if (newScreen == null) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (this.content != null) {
            this.history.push(this.content);
        }
        this.content = newScreen;
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
        float winRadius = isMaximized ? 0 : 8.0f;

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

        // 5. Draw Window Surface & Drop Shadow
        if (!isMaximized) {
            int shadowCol = isTopFocus ? 0x65000000 : 0x40000000;
            RenderUtil.drawDropShadow(curX, curY, curW, curH, winRadius, 12.0f, shadowCol);
        }
        RenderUtil.drawRoundedRect(curX, curY, curW, curH, winRadius, 0xF212151D);

        // 6. Draw Titlebar
        RenderUtil.drawRoundedRect(curX, curY, curW, titlebarH, isMaximized ? 0 : winRadius, isMaximized ? 0 : winRadius, 0, 0, 0xFF191D26);
        RenderUtil.drawRect(curX, curY + titlebarH - 1, curX + curW, curY + titlebarH, 0x303F6D94);

        // Icon & Title
        drawTypeIcon(curX + 10, curY + (titlebarH - 14) / 2, isTopFocus ? 0xFF76B9ED : 0xFF7E8A99);
        FontRenderer fr = mc.fontRendererObj;
        if (fr != null) {
            fr.drawStringWithShadow(title, curX + 30, curY + (titlebarH - 8) / 2, isTopFocus ? 0xFFFFFFFF : 0xFFB0B8C4);
        }

        // 7. Right Control Buttons (Minimize, Maximize/Restore, Close)
        float controlsTotalW = controlW * 3;
        float minBtnX = curX + curW - controlsTotalW;
        float maxBtnX = minBtnX + controlW;
        float closeBtnX = maxBtnX + controlW;

        // Minimize button
        boolean minHover = isTopFocus && isHovered(mouseX, mouseY, minBtnX, curY, controlW, titlebarH);
        if (minHover) RenderUtil.drawRect(minBtnX, curY, minBtnX + controlW, curY + titlebarH, 0x22FFFFFF);
        drawWin11MinimizeIcon(minBtnX + (controlW - 10) / 2, curY + (titlebarH - 10) / 2, minHover ? 0xFFFFFFFF : 0xFFB0B8C4);

        // Maximize/Restore button
        boolean maxHover = isTopFocus && isHovered(mouseX, mouseY, maxBtnX, curY, controlW, titlebarH);
        if (maxHover) RenderUtil.drawRect(maxBtnX, curY, maxBtnX + controlW, curY + titlebarH, 0x22FFFFFF);
        if (isMaximized) {
            drawWin11RestoreIcon(maxBtnX + (controlW - 10) / 2, curY + (titlebarH - 10) / 2, maxHover ? 0xFFFFFFFF : 0xFFB0B8C4);
        } else {
            drawWin11MaximizeIcon(maxBtnX + (controlW - 10) / 2, curY + (titlebarH - 10) / 2, maxHover ? 0xFFFFFFFF : 0xFFB0B8C4);
        }

        // Close button
        boolean closeHover = isTopFocus && isHovered(mouseX, mouseY, closeBtnX, curY, controlW, titlebarH);
        if (closeHover) {
            if (!isMaximized) {
                RenderUtil.drawRoundedRect(closeBtnX, curY, controlW, titlebarH, 0, winRadius, 0, 0, 0xFFC42B1C);
            } else {
                RenderUtil.drawRect(closeBtnX, curY, closeBtnX + controlW, curY + titlebarH, 0xFFC42B1C);
            }
        }
        drawWin11CloseIcon(closeBtnX + (controlW - 10) / 2, curY + (titlebarH - 10) / 2, closeHover ? 0xFFFFFFFF : 0xFFB0B8C4);

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
            int borderColor = isTopFocus ? 0xFF76B9ED : 0x303F6D94;
            RenderUtil.drawRoundedOutline(curX, curY, curW, curH, winRadius, 1.0f, borderColor);
        }
    }

    private void drawMinimized(int mouseX, int mouseY, boolean isTopFocus, int minIndex) {
        Minecraft mc = Minecraft.getMinecraft();
        float screenW = mc.currentScreen != null ? mc.currentScreen.width : 854;
        float screenH = mc.currentScreen != null ? mc.currentScreen.height : 480;

        float minX = getMinimizedX(minIndex, screenW);
        float minY = getMinimizedY(minIndex, screenH);
        float minW = MINIMIZED_WIDTH;
        float minH = MINIMIZED_HEIGHT;
        float radius = 6.0f;

        boolean hover = isHovered(mouseX, mouseY, minX, minY, minW, minH);

        // Drop shadow
        RenderUtil.drawDropShadow(minX, minY, minW, minH, radius, 8.0f, isTopFocus ? 0x65000000 : 0x40000000);
        // Surface
        RenderUtil.drawRoundedRect(minX, minY, minW, minH, radius, hover ? 0xF2222733 : 0xF2181C26);
        // Outline
        RenderUtil.drawRoundedOutline(minX, minY, minW, minH, radius, 1.0f, isTopFocus ? 0xFF76B9ED : (hover ? 0x8876B9ED : 0x403F6D94));

        // Icon + Title
        drawTypeIcon(minX + 8, minY + (minH - 14) / 2, isTopFocus ? 0xFF76B9ED : 0xFF9EABB9);
        FontRenderer fr = mc.fontRendererObj;
        if (fr != null) {
            String shortTitle = title;
            if (fr.getStringWidth(shortTitle) > minW - 56) {
                shortTitle = fr.trimStringToWidth(shortTitle, (int) (minW - 62)) + "...";
            }
            fr.drawStringWithShadow(shortTitle, minX + 26, minY + (minH - 8) / 2, hover ? 0xFFFFFFFF : 0xFFE0E5EC);
        }

        // Restore & Close buttons
        float ctrlW = 16.0f;
        float closeX = minX + minW - 20;
        float restoreX = closeX - 18;

        boolean resHover = isHovered(mouseX, mouseY, restoreX - 2, minY + (minH - ctrlW) / 2, ctrlW, ctrlW);
        if (resHover) RenderUtil.drawRoundedRect(restoreX - 2, minY + (minH - ctrlW) / 2, ctrlW, ctrlW, 3.0f, 0x33FFFFFF);
        drawWin11RestoreIcon(restoreX + 2, minY + (minH - 10) / 2, resHover ? 0xFFFFFFFF : 0xFF8F9BA8);

        boolean clsHover = isHovered(mouseX, mouseY, closeX - 2, minY + (minH - ctrlW) / 2, ctrlW, ctrlW);
        if (clsHover) RenderUtil.drawRoundedRect(closeX - 2, minY + (minH - ctrlW) / 2, ctrlW, ctrlW, 3.0f, 0xFFC42B1C);
        drawWin11CloseIcon(closeX + 2, minY + (minH - 10) / 2, clsHover ? 0xFFFFFFFF : 0xFF8F9BA8);
    }

    public float getMinimizedX(int minIndex, float screenW) {
        float startX = 204.0f;
        float gap = 8.0f;
        float availW = screenW - startX - 20.0f;
        int perRow = Math.max(1, (int) ((availW + gap) / (MINIMIZED_WIDTH + gap)));
        int col = Math.max(0, minIndex) % perRow;
        return startX + col * (MINIMIZED_WIDTH + gap);
    }

    public float getMinimizedY(int minIndex, float screenH) {
        float gap = 8.0f;
        float startX = 204.0f;
        float availW = screenH - startX - 20.0f;
        int perRow = Math.max(1, (int) ((availW + gap) / (MINIMIZED_WIDTH + gap)));
        int row = Math.max(0, minIndex) / perRow;
        return screenH - MINIMIZED_HEIGHT - 12.0f - row * (MINIMIZED_HEIGHT + gap);
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
            Minecraft mc = Minecraft.getMinecraft();
            float screenW = mc.currentScreen != null ? mc.currentScreen.width : 854;
            float screenH = mc.currentScreen != null ? mc.currentScreen.height : 480;
            float minX = getMinimizedX(minIndex, screenW);
            float minY = getMinimizedY(minIndex, screenH);
            float minW = MINIMIZED_WIDTH;
            float minH = MINIMIZED_HEIGHT;
            if (!isHovered(mouseX, mouseY, minX, minY, minW, minH)) {
                return false;
            }

            float closeX = minX + minW - 20;
            float restoreX = closeX - 18;
            if (isHovered(mouseX, mouseY, closeX - 2, minY + 4, 16, 16)) {
                manager.closeWindow(this);
                return true;
            }
            if (isHovered(mouseX, mouseY, restoreX - 2, minY + 4, 16, 16) || mouseButton == 0) {
                setMinimized(false);
                manager.bringToFront(this);
                return true;
            }
            return true;
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
            float minBtnX = curX + curW - controlsTotalW;
            float maxBtnX = minBtnX + controlW;
            float closeBtnX = maxBtnX + controlW;

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

            // Clicked on titlebar (Drag)
            if (mouseButton == 0 && !isMaximized) {
                dragging = true;
                dragX = mouseX - posX;
                dragY = mouseY - posY;
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
        if (type == null) {
            RenderUtil.drawRectOutline(x + 2.0f, y + 2.0f, 10.0f, 10.0f, 1.2f, color);
            return;
        }
        switch (type) {
            case SINGLEPLAYER:
                // Earth/World circle with latitude & longitude
                RenderUtil.drawCircleOutline(x + 7.0f, y + 7.0f, 6.0f, 1.2f, color);
                RenderUtil.drawLine(x + 1.0f, y + 7.0f, x + 13.0f, y + 7.0f, 1.0f, color);
                RenderUtil.drawLine(x + 7.0f, y + 1.0f, x + 7.0f, y + 13.0f, 1.0f, color);
                break;
            case MULTIPLAYER:
                // Server tower
                RenderUtil.drawRectOutline(x + 3.0f, y + 2.0f, 8.0f, 10.0f, 1.2f, color);
                RenderUtil.drawLine(x + 5.0f, y + 5.0f, x + 9.0f, y + 5.0f, 1.0f, color);
                RenderUtil.drawLine(x + 5.0f, y + 8.0f, x + 9.0f, y + 8.0f, 1.0f, color);
                break;
            case OPTIONS:
                // Gear / concentric circles
                RenderUtil.drawCircleOutline(x + 7.0f, y + 7.0f, 5.0f, 1.2f, color);
                RenderUtil.drawCircle(x + 7.0f, y + 7.0f, 2.0f, color);
                break;
            case SETTINGS:
                // Sliders icon
                RenderUtil.drawLine(x + 2.0f, y + 4.0f, x + 12.0f, y + 4.0f, 1.0f, color);
                RenderUtil.drawRect(x + 8.0f, y + 2.5f, x + 10.5f, y + 5.5f, color);
                RenderUtil.drawLine(x + 2.0f, y + 10.0f, x + 12.0f, y + 10.0f, 1.0f, color);
                RenderUtil.drawRect(x + 4.0f, y + 8.5f, x + 6.5f, y + 11.5f, color);
                break;
            case MODULES:
                RenderUtil.drawModulesIcon(x, y - 2.0f, 16.0f, color);
                break;
        }
    }

    private void drawWin11MinimizeIcon(float x, float y, int color) {
        RenderUtil.drawLine(x + 0.5f, y + 5.0f, x + 9.5f, y + 5.0f, 1.2f, color);
    }

    private void drawWin11MaximizeIcon(float x, float y, int color) {
        RenderUtil.drawRectOutline(x + 1.0f, y + 1.0f, 8.0f, 8.0f, 1.2f, color);
    }

    private void drawWin11RestoreIcon(float x, float y, int color) {
        RenderUtil.drawRectOutline(x + 1.0f, y + 3.0f, 6.5f, 6.5f, 1.2f, color);
        RenderUtil.drawLine(x + 3.0f, y + 1.0f, x + 9.5f, y + 1.0f, 1.2f, color);
        RenderUtil.drawLine(x + 9.5f, y + 1.0f, x + 9.5f, y + 7.5f, 1.2f, color);
    }

    private void drawWin11CloseIcon(float x, float y, int color) {
        RenderUtil.drawLine(x + 1.0f, y + 1.0f, x + 9.0f, y + 9.0f, 1.2f, color);
        RenderUtil.drawLine(x + 9.0f, y + 1.0f, x + 1.0f, y + 9.0f, 1.2f, color);
    }

    private boolean isHovered(float mx, float my, float x, float y, float w, float h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }
}
