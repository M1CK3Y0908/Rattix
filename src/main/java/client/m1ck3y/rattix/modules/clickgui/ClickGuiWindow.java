package client.m1ck3y.rattix.modules.clickgui;

import client.m1ck3y.rattix.RattixMod;
import client.m1ck3y.rattix.modules.Category;
import client.m1ck3y.rattix.modules.Register;
import client.m1ck3y.rattix.manager.FileManager;
import client.m1ck3y.rattix.utils.CursorUtil;
import client.m1ck3y.rattix.utils.FontUtil;
import client.m1ck3y.rattix.utils.RenderUtil;
import client.m1ck3y.rattix.utils.font.CustomFontRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ChatAllowedCharacters;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

public class ClickGuiWindow {
    public static final float MIN_WINDOW_WIDTH = 640;
    public static final float MIN_WINDOW_HEIGHT = 400;

    public static final int RESIZE_NONE = 0;
    public static final int RESIZE_N = 1;
    public static final int RESIZE_S = 2;
    public static final int RESIZE_W = 3;
    public static final int RESIZE_E = 4;
    public static final int RESIZE_NW = 5;
    public static final int RESIZE_NE = 6;
    public static final int RESIZE_SW = 7;
    public static final int RESIZE_SE = 8;

    public static final float RESIZE_BORDER = 6.0f;
    public static final float RESIZE_CORNER = 14.0f;

    private final String id;
    private float posX;
    private float posY;
    private float windowWidth;
    private float windowHeight;
    private boolean isMaximized;
    private boolean isMinimized = false;

    public static final float MINIMIZED_WIDTH = 300.0f;
    public static final float MINIMIZED_HEIGHT = 40.0f;

    private final List<TabInfo> openTabs = new ArrayList<>();
    private int activeTabIndex = 0;

    // Window Drag State
    private boolean dragging = false;
    private float dragX = 0;
    private float dragY = 0;

    // Window Resize State
    private boolean resizing = false;
    private int currentResizeDir = RESIZE_NONE;
    private float resizeMouseStartX = 0;
    private float resizeMouseStartY = 0;
    private float resizeStartPosX = 0;
    private float resizeStartPosY = 0;
    private float resizeStartW = 0;
    private float resizeStartH = 0;

    // New Tab Menu State
    private boolean showNewTabMenu = false;
    private float newTabMenuX = 0;
    private float newTabMenuY = 0;
    private final float newTabMenuW = 215;
    public float getNewTabMenuH() {
        return AVAILABLE_PRESETS.size() * 32.0f;
    }

    // Tab Scrolling
    private float tabScrollX = 0;
    private float maxTabScroll = 0;

    // UI Slider Dragging
    private NumberSetting draggingSlider = null;
    private ColorSetting draggingColorSetting = null;
    public enum ColorDragType { HUE, SATURATION, BRIGHTNESS, ALPHA }
    private ColorDragType draggingColorType = null;

    // Available Presets
    public static class TabPreset {
        public final String type;
        public final String title;
        public final Category category;

        public TabPreset(String type, String title, Category category) {
            this.type = type;
            this.title = title;
            this.category = category;
        }
    }

    public static List<TabPreset> getAvailablePresets() {
        List<TabPreset> presets = new ArrayList<>();
        presets.add(new TabPreset("modules", "Modules", null));
        presets.add(new TabPreset("console", "Console", null));
        for (Category cat : Category.values()) {
            presets.add(new TabPreset(cat.name().toLowerCase(), cat.getDisplayName(), cat));
        }
        return Collections.unmodifiableList(presets);
    }

    public static final List<TabPreset> AVAILABLE_PRESETS = getAvailablePresets();

    public ClickGuiWindow(String id, float posX, float posY, float windowWidth, float windowHeight, boolean isMaximized, boolean isMinimized, List<TabInfo> tabs, int activeTabIndex) {
        this.id = (id != null && !id.isEmpty()) ? id : UUID.randomUUID().toString();
        this.posX = posX;
        this.posY = posY;
        this.windowWidth = Math.max(MIN_WINDOW_WIDTH, windowWidth);
        this.windowHeight = Math.max(MIN_WINDOW_HEIGHT, windowHeight);
        this.isMaximized = isMaximized;
        this.isMinimized = isMinimized;
        if (tabs != null && !tabs.isEmpty()) {
            this.openTabs.addAll(tabs);
        } else {
            this.openTabs.add(TabInfo.createPreset("console"));
        }
        this.activeTabIndex = Math.max(0, Math.min(this.openTabs.size() - 1, activeTabIndex));
    }

    public ClickGuiWindow(String id, float posX, float posY, float windowWidth, float windowHeight, boolean isMaximized, List<TabInfo> tabs, int activeTabIndex) {
        this(id, posX, posY, windowWidth, windowHeight, isMaximized, false, tabs, activeTabIndex);
    }

    public String getId() { return id; }
    public float getPosX() { return posX; }
    public void setPosX(float posX) { this.posX = posX; }
    public float getPosY() { return posY; }
    public void setPosY(float posY) { this.posY = posY; }
    public float getWindowWidth() { return windowWidth; }
    public void setWindowWidth(float windowWidth) { this.windowWidth = windowWidth; }
    public float getWindowHeight() { return windowHeight; }
    public void setWindowHeight(float windowHeight) { this.windowHeight = windowHeight; }
    public boolean isMaximized() { return isMaximized; }
    public void setMaximized(boolean maximized) { isMaximized = maximized; }
    public boolean isMinimized() { return isMinimized; }
    public void setMinimized(boolean minimized) { isMinimized = minimized; }
    public List<TabInfo> getOpenTabs() { return openTabs; }
    public int getActiveTabIndex() { return activeTabIndex; }
    public void setActiveTabIndex(int activeTabIndex) { this.activeTabIndex = Math.max(0, Math.min(openTabs.size() - 1, activeTabIndex)); }
    public boolean isDragging() { return dragging; }
    public void setDragging(boolean dragging) { this.dragging = dragging; }
    public void setDragOffset(float dragX, float dragY) { this.dragX = dragX; this.dragY = dragY; }
    public boolean isResizing() { return resizing; }
    public int getCurrentResizeDir() { return currentResizeDir; }

    public TabInfo getActiveTab() {
        if (activeTabIndex >= 0 && activeTabIndex < openTabs.size()) {
            return openTabs.get(activeTabIndex);
        }
        return openTabs.isEmpty() ? null : openTabs.get(0);
    }

    private String getPlayerName() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc != null) {
            if (mc.thePlayer != null) return mc.thePlayer.getName();
            if (mc.getSession() != null && mc.getSession().getUsername() != null) return mc.getSession().getUsername();
        }
        return "Player";
    }

    private String getPrompt() {
        return "C:\\" + getPlayerName() + "\\Rattix> ";
    }

    private void drawString2x(String text, float x, float y, int color) {
        FontUtil.drawString2x(text, x, y, color);
    }

    private float getStringWidth2x(String text) {
        return FontUtil.getStringWidth2x(text);
    }

    private void drawTreeBranch(float trunkX, float itemX, float startY, float centerY) {
        RenderUtil.beginUntexturedDraw();
        RenderUtil.drawRectNoState(trunkX - 1.0f, centerY - 1.0f, itemX - 4.0f, centerY + 1.0f, 0xFFFFAA00);
        if (startY >= 0 && startY < centerY) {
            RenderUtil.drawRectNoState(trunkX - 1.0f, startY - 1.0f, trunkX + 1.0f, centerY + 1.0f, 0xFFFFAA00);
        }
        RenderUtil.endUntexturedDraw();
    }

    private void drawTreeTrunk(float trunkX, float startY, float endY) {
        if (startY >= 0 && startY < endY) {
            RenderUtil.beginUntexturedDraw();
            RenderUtil.drawRectNoState(trunkX - 1.0f, startY - 1.0f, trunkX + 1.0f, endY + 1.0f, 0xFFFFAA00);
            RenderUtil.endUntexturedDraw();
        }
    }

    public static float getTabWidth(String title) {
        return FontUtil.getStringWidth2x(title) + 70.0f;
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

    public float getMinimizedX(int minIndex) {
        Minecraft mc = Minecraft.getMinecraft();
        float margin = 16.0f;
        float gap = 10.0f;
        float availW = mc.displayWidth - margin * 2;
        int perRow = Math.max(1, (int) ((availW + gap) / (MINIMIZED_WIDTH + gap)));
        int col = Math.max(0, minIndex) % perRow;
        return margin + col * (MINIMIZED_WIDTH + gap);
    }

    public float getMinimizedY(int minIndex) {
        Minecraft mc = Minecraft.getMinecraft();
        float margin = 16.0f;
        float gap = 10.0f;
        float availW = mc.displayWidth - margin * 2;
        int perRow = Math.max(1, (int) ((availW + gap) / (MINIMIZED_WIDTH + gap)));
        int row = Math.max(0, minIndex) / perRow;
        return mc.displayHeight - margin - MINIMIZED_HEIGHT - row * (MINIMIZED_HEIGHT + gap);
    }

    public boolean isMenuHovered(int mouseX, int mouseY) {
        return !isMinimized && showNewTabMenu && RenderUtil.isHovered(mouseX, mouseY, newTabMenuX - 4, newTabMenuY - 4, newTabMenuW + 8, getNewTabMenuH() + 8);
    }

    public int getResizeDirection(float mx, float my) {
        if (isMaximized || isMinimized) return RESIZE_NONE;

        Minecraft mc = Minecraft.getMinecraft();
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

    public boolean isInsideWindow(float mx, float my, int minIndex) {
        if (isMinimized) {
            float minX = getMinimizedX(minIndex);
            float minY = getMinimizedY(minIndex);
            return mx >= minX && mx <= minX + MINIMIZED_WIDTH && my >= minY && my <= minY + MINIMIZED_HEIGHT;
        }
        Minecraft mc = Minecraft.getMinecraft();
        float curX = isMaximized ? 0 : posX;
        float curY = isMaximized ? 0 : posY;
        float curW = isMaximized ? mc.displayWidth : windowWidth;
        float curH = isMaximized ? mc.displayHeight : windowHeight;
        return mx >= curX - RESIZE_BORDER && mx <= curX + curW + RESIZE_BORDER &&
               my >= curY - RESIZE_BORDER && my <= curY + curH + RESIZE_BORDER;
    }

    public boolean isInsideTitlebar(float mx, float my) {
        if (isMinimized) return false;
        Minecraft mc = Minecraft.getMinecraft();
        float curX = isMaximized ? 0 : posX;
        float curY = isMaximized ? 0 : posY;
        float curW = isMaximized ? mc.displayWidth : windowWidth;
        float titlebarH = 40;
        return mx >= curX && mx <= curX + curW && my >= curY && my <= curY + titlebarH;
    }

    public int getTabInsertionIndex(float mx, float my) {
        if (isMinimized) return -1;
        Minecraft mc = Minecraft.getMinecraft();
        float curX = isMaximized ? 0 : posX;
        float curY = isMaximized ? 0 : posY;
        float curW = isMaximized ? mc.displayWidth : windowWidth;
        float titlebarH = 40;
        float controlW = 46;
        float tabsViewportW = curW - (controlW * 3);

        if (my < curY || my > curY + titlebarH || mx < curX || mx > curX + tabsViewportW) {
            return -1;
        }

        float tabX = curX + tabScrollX;
        for (int i = 0; i < openTabs.size(); i++) {
            TabInfo tab = openTabs.get(i);
            float tabW = getTabWidth(tab.getTitle());
            if (mx < tabX + tabW / 2.0f) {
                return i;
            }
            tabX += tabW;
        }
        return openTabs.size();
    }

    public float getTabInsertionX(int insertIndex) {
        Minecraft mc = Minecraft.getMinecraft();
        float curX = isMaximized ? 0 : posX;
        float tabX = curX + tabScrollX;
        for (int i = 0; i < openTabs.size(); i++) {
            if (i == insertIndex) return tabX;
            TabInfo tab = openTabs.get(i);
            float tabW = getTabWidth(tab.getTitle());
            tabX += tabW;
        }
        return tabX;
    }

    private void drawMinimizedWindow(int mouseX, int mouseY, boolean isTopFocus, int minIndex) {
        float curX = getMinimizedX(minIndex);
        float curY = getMinimizedY(minIndex);
        float curW = MINIMIZED_WIDTH;
        float curH = MINIMIZED_HEIGHT;
        float winRadius = 6.0f;

        // 1. 柔和阴影
        RenderUtil.drawDropShadow(curX, curY, curW, curH, winRadius, 10.0f, isTopFocus ? 0x65000000 : 0x40000000);

        // 2. 导航栏底栏背景 (#202020)
        RenderUtil.drawRoundedRect(curX, curY, curW, curH, winRadius, 0xF2202020);

        // 3. 边框
        int outlineColor = isTopFocus ? 0xFF4C4C4C : 0xFF303030;
        RenderUtil.drawRoundedOutline(curX, curY, curW, curH, winRadius, 1.0f, outlineColor);

        // 4. 左侧图标与窗口/激活标签名称
        TabInfo activeTab = getActiveTab();
        float textOffset = curX + 12;
        if (activeTab != null && activeTab.isModulesTab()) {
            RenderUtil.drawModulesIcon(textOffset, curY + 8.0f, 20.0f, 0xFF76B9ED);
            textOffset += 27;
        } else if (activeTab != null && activeTab.getCategory() == null) {
            RenderUtil.drawTerminalIcon(textOffset, curY + 8.0f, 20.0f, 0xFF76B9ED);
            textOffset += 27;
        } else if (activeTab != null) {
            RenderUtil.drawCategoryIcon(activeTab.getCategory(), textOffset, curY + 8.0f, 20.0f, 0xFF76B9ED);
            textOffset += 27;
        } else {
            RenderUtil.drawTerminalIcon(textOffset, curY + 8.0f, 20.0f, 0xFF76B9ED);
            textOffset += 27;
        }

        String title = (activeTab != null) ? activeTab.getTitle() : "Window";
        drawString2x(title, textOffset, curY + 11, 0xFFFFFFFF);

        // 5. 右侧控制按钮: 还原、最大化、关闭 (以默认窗口 46px 为基准，无边距全填充)
        float btnW = 46.0f;
        float closeX = curX + curW - btnW;
        float maxX = closeX - btnW;
        float restoreX = maxX - btnW;

        // 还原按钮 (Restore)
        boolean restoreHover = RenderUtil.isHovered(mouseX, mouseY, restoreX, curY, btnW, curH);
        if (restoreHover) {
            RenderUtil.drawRect(restoreX, curY, restoreX + btnW, curY + curH, 0xFF333333);
        }
        int restoreColor = restoreHover ? 0xFFFFFFFF : 0xFFDDDDDD;
        drawWin11RestoreIcon(restoreX + 18.0f, curY + 15.0f, restoreColor);

        // 最大化按钮 (Maximize)
        boolean maxHover = RenderUtil.isHovered(mouseX, mouseY, maxX, curY, btnW, curH);
        if (maxHover) {
            RenderUtil.drawRect(maxX, curY, maxX + btnW, curY + curH, 0xFF333333);
        }
        int maxColor = maxHover ? 0xFFFFFFFF : 0xFFDDDDDD;
        drawWin11MaximizeIcon(maxX + 18.0f, curY + 15.0f, maxColor);

        // 关闭按钮 (Close) - 右上和右下角圆角延伸至边缘
        boolean closeHover = RenderUtil.isHovered(mouseX, mouseY, closeX, curY, btnW, curH);
        if (closeHover) {
            RenderUtil.drawRoundedRect(closeX, curY, btnW, curH, 0, winRadius, winRadius, 0, 0xFFC42B1C);
        }
        int closeColor = closeHover ? 0xFFFFFFFF : 0xFFDDDDDD;
        drawWin11CloseIcon(closeX + 18.0f, curY + 15.0f, closeColor);
    }

    public void drawWindow(int mouseX, int mouseY, boolean isTopFocus, TabInfo draggingTab, ClickGuiWindow hoverTargetWindow, int hoverInsertIndex, int minIndex) {
        if (isMinimized) {
            drawMinimizedWindow(mouseX, mouseY, isTopFocus, minIndex);
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        float curX = isMaximized ? 0 : posX;
        float curY = isMaximized ? 0 : posY;
        float curW = isMaximized ? mc.displayWidth : windowWidth;
        float curH = isMaximized ? mc.displayHeight : windowHeight;
        float winRadius = isMaximized ? 0 : 8.0f;

        // 1. 处理拖拽
        if (dragging && !isMaximized) {
            if (!Mouse.isButtonDown(0)) {
                dragging = false;
            } else {
                posX = mouseX - dragX;
                posY = mouseY - dragY;
                curX = posX;
                curY = posY;
            }
        }

        // 2. 处理无极缩放
        if (resizing && !isMaximized) {
            if (!Mouse.isButtonDown(0)) {
                resizing = false;
            } else {
                float deltaX = mouseX - resizeMouseStartX;
                float deltaY = mouseY - resizeMouseStartY;

                float newX = resizeStartPosX;
                float newY = resizeStartPosY;
                float newW = resizeStartW;
                float newH = resizeStartH;

                if (currentResizeDir == RESIZE_E || currentResizeDir == RESIZE_NE || currentResizeDir == RESIZE_SE) {
                    newW = Math.max(MIN_WINDOW_WIDTH, resizeStartW + deltaX);
                }
                if (currentResizeDir == RESIZE_W || currentResizeDir == RESIZE_NW || currentResizeDir == RESIZE_SW) {
                    float targetW = resizeStartW - deltaX;
                    if (targetW < MIN_WINDOW_WIDTH) {
                        newW = MIN_WINDOW_WIDTH;
                        newX = resizeStartPosX + (resizeStartW - MIN_WINDOW_WIDTH);
                    } else {
                        newW = targetW;
                        newX = resizeStartPosX + deltaX;
                    }
                }
                if (currentResizeDir == RESIZE_S || currentResizeDir == RESIZE_SW || currentResizeDir == RESIZE_SE) {
                    newH = Math.max(MIN_WINDOW_HEIGHT, resizeStartH + deltaY);
                }
                if (currentResizeDir == RESIZE_N || currentResizeDir == RESIZE_NW || currentResizeDir == RESIZE_NE) {
                    float targetH = resizeStartH - deltaY;
                    if (targetH < MIN_WINDOW_HEIGHT) {
                        newH = MIN_WINDOW_HEIGHT;
                        newY = resizeStartPosY + (resizeStartH - MIN_WINDOW_HEIGHT);
                    } else {
                        newH = targetH;
                        newY = resizeStartPosY + deltaY;
                    }
                }

                posX = newX;
                posY = newY;
                windowWidth = newW;
                windowHeight = newH;
                curX = posX;
                curY = posY;
                curW = windowWidth;
                curH = windowHeight;
            }
        }

        // 3. 柔和阴影与主体背景 (#0C0C0C)
        if (!isMaximized) {
            RenderUtil.drawDropShadow(curX, curY, curW, curH, winRadius, 14.0f, isTopFocus ? 0x65000000 : 0x40000000);
        }
        RenderUtil.drawRoundedRect(curX, curY, curW, curH, winRadius, 0xFF0C0C0C);

        // 4. Titlebar 顶部栏 (#202020)
        float titlebarH = 40;
        RenderUtil.drawRoundedRect(curX, curY, curW, titlebarH, winRadius, winRadius, 0, 0, 0xFF202020);
        RenderUtil.drawRect(curX, curY + titlebarH - 1, curX + curW, curY + titlebarH, 0xFF303030);

        float controlW = 46;
        float controlsTotalW = controlW * 3;
        float tabsViewportW = curW - controlsTotalW;

        // 计算所有 Tab 总宽度 (使用索引循环避免 Iterator 分配)
        float totalTabsWidth = 0;
        int tabCount = openTabs.size();
        for (int i = 0; i < tabCount; i++) {
            totalTabsWidth += getTabWidth(openTabs.get(i).getTitle());
        }
        totalTabsWidth += 40; // '+' 按钮宽度
        maxTabScroll = Math.max(0, totalTabsWidth - tabsViewportW);
        tabScrollX = Math.min(0, Math.max(-maxTabScroll, tabScrollX));

        // 5. 渲染 Titlebar 选项卡（Scissor 遮罩）
        RenderUtil.enablePixelScissor(curX, curY, tabsViewportW, titlebarH);

        float tabX = curX + tabScrollX;
        for (int i = 0; i < openTabs.size(); i++) {
            TabInfo tab = openTabs.get(i);
            String name = tab.getTitle();
            float tabW = getTabWidth(name);
            boolean isActive = (activeTabIndex == i);
            boolean isHover = !isMenuHovered(mouseX, mouseY) && isTopFocus && RenderUtil.isHovered(mouseX, mouseY, tabX, curY, tabW, titlebarH) && mouseX < curX + tabsViewportW;

            int tabBg = isActive ? 0xFF0C0C0C : (isHover ? 0xFF282828 : 0xFF181818);
            RenderUtil.drawRoundedRect(tabX + 1, curY + 3, tabW - 2, titlebarH - 4, 5, 5, 0, 0, tabBg);
            RenderUtil.drawRect(tabX + tabW - 1, curY + 6, tabX + tabW, curY + titlebarH - 6, 0xFF2C2C2C);

            if (isActive) {
                RenderUtil.drawRoundedRect(tabX + 4, curY + titlebarH - 3, tabW - 8, 2, 1, 0xFF76B9ED);
            }

            float textOffset = tabX + 11;
            int iconColor = isActive ? 0xFF76B9ED : (isHover ? 0xFFFFFFFF : 0xFFB0B0B0);
            if (tab.isModulesTab()) {
                RenderUtil.drawModulesIcon(textOffset, curY + 8.0f, 20.0f, iconColor);
                textOffset += 27;
            } else if (tab.getCategory() == null) {
                RenderUtil.drawTerminalIcon(textOffset, curY + 8.0f, 20.0f, 0xFF76B9ED);
                textOffset += 27;
            } else {
                RenderUtil.drawCategoryIcon(tab.getCategory(), textOffset, curY + 8.0f, 20.0f, iconColor);
                textOffset += 27;
            }
            int textColor = isActive ? 0xFFFFFFFF : (isHover ? 0xFFE0E0E0 : 0xFFD7D7D7);
            drawString2x(name, textOffset, curY + 11, textColor);

            // Tab 关闭图标 (X)
            float closeX = tabX + tabW - 18;
            boolean closeHover = !isMenuHovered(mouseX, mouseY) && isTopFocus && RenderUtil.isHovered(mouseX, mouseY, closeX - 4, curY + 10, 18, 18) && mouseX < curX + tabsViewportW;
            if (closeHover) {
                RenderUtil.drawRoundedRect(closeX - 2, curY + 12, 14, 14, 3, 0x33FFFFFF);
            }
            int tabCloseColor = closeHover ? 0xFFFF4444 : 0xFF888888;
            drawWin11CloseIcon(closeX, curY + 15, tabCloseColor);

            tabX += tabW;
        }

        // 新建 Tab 按钮 (+)
        float newTabW = 40;
        boolean isHoverPlusBtn = isTopFocus && (RenderUtil.isHovered(mouseX, mouseY, tabX, curY, newTabW, titlebarH) && mouseX < curX + tabsViewportW);
        newTabMenuX = Math.max(curX + 5, Math.min(tabX, curX + curW - newTabMenuW - 5));
        newTabMenuY = Math.min(curY + titlebarH, mc.displayHeight - getNewTabMenuH() - 5);
        boolean isHoverMenu = showNewTabMenu && RenderUtil.isHovered(mouseX, mouseY, newTabMenuX - 4, newTabMenuY - 4, newTabMenuW + 8, getNewTabMenuH() + 8);

        if (isHoverPlusBtn) {
            showNewTabMenu = true;
        } else if (!isHoverMenu) {
            showNewTabMenu = false;
        }

        boolean newTabHover = isHoverPlusBtn || showNewTabMenu;
        if (newTabHover) {
            RenderUtil.drawRoundedRect(tabX + 4, curY + 6, newTabW - 8, titlebarH - 12, 4, 0xFF2A2A2A);
        }
        drawString2x("+", tabX + 14, curY + 10, newTabHover ? 0xFFFFFFFF : 0xFFCCCCCC);

        // 如果是跨窗口拖拽合并目标，绘制高亮插入指示线
        if (hoverTargetWindow == this && hoverInsertIndex >= 0) {
            float insX = getTabInsertionX(hoverInsertIndex);
            RenderUtil.drawRoundedRect(insX - 1.5f, curY + 4, 3.0f, titlebarH - 8, 1.5f, 0xFF76B9ED);
        }

        RenderUtil.disableScissor();

        // 6. 右侧控制按钮区域（最小化、最大化、关闭）
        float controlsStartX = curX + curW - controlsTotalW;
        RenderUtil.drawRoundedRect(controlsStartX, curY, controlsTotalW, titlebarH, 0, winRadius, 0, 0, 0xFF202020);
        RenderUtil.drawRect(controlsStartX, curY + titlebarH - 1, curX + curW, curY + titlebarH, 0xFF303030);

        float closeBtnX = curX + curW - controlW;
        float maxBtnX = closeBtnX - controlW;
        float minBtnX = maxBtnX - controlW;

        boolean minHover = isTopFocus && !isMenuHovered(mouseX, mouseY) && RenderUtil.isHovered(mouseX, mouseY, minBtnX, curY, controlW, titlebarH);
        if (minHover) RenderUtil.drawRect(minBtnX, curY, minBtnX + controlW, curY + titlebarH, 0xFF333333);
        int minColor = minHover ? 0xFFFFFFFF : 0xFFDDDDDD;
        drawWin11MinimizeIcon(minBtnX + 18.0f, curY + 15.0f, minColor);

        boolean maxHover = isTopFocus && !isMenuHovered(mouseX, mouseY) && RenderUtil.isHovered(mouseX, mouseY, maxBtnX, curY, controlW, titlebarH);
        if (maxHover) RenderUtil.drawRect(maxBtnX, curY, maxBtnX + controlW, curY + titlebarH, 0xFF333333);
        int maxColor = maxHover ? 0xFFFFFFFF : 0xFFDDDDDD;
        if (isMaximized) {
            drawWin11RestoreIcon(maxBtnX + 18.0f, curY + 15.0f, maxColor);
        } else {
            drawWin11MaximizeIcon(maxBtnX + 18.0f, curY + 15.0f, maxColor);
        }

        boolean closeHover = isTopFocus && !isMenuHovered(mouseX, mouseY) && RenderUtil.isHovered(mouseX, mouseY, closeBtnX, curY, controlW, titlebarH);
        if (closeHover) {
            if (!isMaximized) {
                RenderUtil.drawRoundedRect(closeBtnX, curY, controlW, titlebarH, 0, winRadius, 0, 0, 0xFFC42B1C);
            } else {
                RenderUtil.drawRect(closeBtnX, curY, closeBtnX + controlW, curY + titlebarH, 0xFFC42B1C);
            }
        }
        int closeColor = closeHover ? 0xFFFFFFFF : 0xFFDDDDDD;
        drawWin11CloseIcon(closeBtnX + 18.0f, curY + 15.0f, closeColor);

        // 7. 终端或分类页面主体
        float bodyY = curY + titlebarH;
        float bodyH = curH - titlebarH - 24;

        TabInfo currentTab = getActiveTab();
        if (currentTab != null) {
            if (currentTab.isModulesTab() || currentTab.getCategory() != null) {
                drawModuleManagement(currentTab, curX, bodyY, curW, bodyH, mouseX, mouseY);
            } else {
                drawTerminalConsole(currentTab, curX, bodyY, curW, bodyH, mouseX, mouseY);
            }
        }

        // 8. 底部状态栏
        float statusY = curY + curH - 24;
        RenderUtil.drawRoundedRect(curX, statusY, curW, 24, 0, 0, winRadius, winRadius, 0xFF141414);
        RenderUtil.drawRect(curX, statusY, curX + curW, statusY + 1, 0xFF252525);
        String tabName = (currentTab != null) ? currentTab.getTitle() : "Console";
        drawString2x("\u00A78UTF-8   |   " + tabName + "   |   Rattix 1.8.9", curX + 16, statusY + 3, 0xFF777777);

        String statusText = tabName + " | Rattix";
        drawString2x("\u00A78" + statusText, curX + curW - getStringWidth2x(statusText) - 16, statusY + 3, 0xFF777777);

        // 9. 外轮廓圆角边框
        int outlineColor = isTopFocus ? 0xFF454545 : 0xFF282828;
        RenderUtil.drawRoundedOutline(curX, curY, curW, curH, winRadius, 1.0f, outlineColor);

        // 10. 新建标签下拉浮层菜单
        if (showNewTabMenu) {
            drawNewTabContextMenu(newTabMenuX, newTabMenuY, mouseX, mouseY);
        }
    }

    private void drawNewTabContextMenu(float mx, float my, int mouseX, int mouseY) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 0, 500.0f);
        GlStateManager.disableDepth();
        RenderUtil.disableScissor();

        float menuRadius = 6.0f;
        float menuH = getNewTabMenuH();
        RenderUtil.drawDropShadow(mx, my, newTabMenuW, menuH, menuRadius, 10.0f, 0x55000000);
        RenderUtil.drawRoundedRect(mx, my, newTabMenuW, menuH, menuRadius, 0xF5202020);

        float itemH = 32.0f;
        float itemY = my;
        for (int i = 0; i < AVAILABLE_PRESETS.size(); i++) {
            TabPreset preset = AVAILABLE_PRESETS.get(i);
            boolean isHover = RenderUtil.isHovered(mouseX, mouseY, mx, itemY, newTabMenuW, itemH);

            if (isHover) {
                float tl = (i == 0) ? menuRadius : 0;
                float tr = (i == 0) ? menuRadius : 0;
                float br = (i == AVAILABLE_PRESETS.size() - 1) ? menuRadius : 0;
                float bl = (i == AVAILABLE_PRESETS.size() - 1) ? menuRadius : 0;
                RenderUtil.drawRoundedRect(mx, itemY, newTabMenuW, itemH, tl, tr, br, bl, 0xFF303030);
            }

            int iconColor = isHover ? 0xFF76B9ED : 0xFFB0B0B0;
            if ("modules".equalsIgnoreCase(preset.type)) {
                RenderUtil.drawModulesIcon(mx + 12, itemY + 4.0f, 20.0f, iconColor);
            } else if (preset.category == null) {
                RenderUtil.drawTerminalIcon(mx + 12, itemY + 4.0f, 20.0f, 0xFF76B9ED);
            } else {
                RenderUtil.drawCategoryIcon(preset.category, mx + 12, itemY + 4.0f, 20.0f, iconColor);
            }

            int textColor = isHover ? 0xFFFFFFFF : 0xFFCCCCCC;
            drawString2x(preset.title, mx + 40, itemY + 6, textColor);

            itemY += itemH;
        }

        RenderUtil.drawRoundedOutline(mx, my, newTabMenuW, menuH, menuRadius, 1.0f, 0xFF3C3C3C);

        GlStateManager.enableDepth();
        GlStateManager.popMatrix();
    }

    private void drawTerminalConsole(TabInfo tab, float x, float y, float w, float h, int mouseX, int mouseY) {
        float paddingX = x + 20;
        float paddingY = y + 16;
        float contentW = w - 40;
        float contentH = h - 32;

        RenderUtil.enablePixelScissor(x, y, w, h);

        float lineHeight = 22;
        List<String> outputLines = tab.getTerminalOutput();
        int lineCount = outputLines.size();

        float startRelY = y - paddingY - tab.getTerminalScrollY();
        int firstVisibleLine = Math.max(0, (int) Math.floor(startRelY / lineHeight));
        int lastVisibleLine = Math.min(lineCount, (int) Math.ceil((startRelY + h) / lineHeight));

        float lineY = paddingY + tab.getTerminalScrollY() + firstVisibleLine * lineHeight;
        for (int li = firstVisibleLine; li < lastVisibleLine; li++) {
            drawString2x(outputLines.get(li), paddingX, lineY, 0xFFF2F2F2);
            lineY += lineHeight;
        }

        float promptY = paddingY + tab.getTerminalScrollY() + lineCount * lineHeight;
        if (promptY + lineHeight >= y && promptY <= y + h) {
            String prompt = getPrompt();
            String cursor = (System.currentTimeMillis() / 500 % 2 == 0) ? "_" : "";
            drawString2x(prompt + "\u00A7f" + tab.getCurrentInput() + cursor, paddingX, promptY, 0xFFF2F2F2);
        }

        RenderUtil.disableScissor();

        float totalHeight = (lineCount + 1) * lineHeight;
        tab.setMaxTerminalScroll(Math.max(0, totalHeight - contentH));

        if (tab.getMaxTerminalScroll() > 0) {
            float barH = Math.max(30, contentH * (contentH / totalHeight));
            float progress = -tab.getTerminalScrollY() / tab.getMaxTerminalScroll();
            float barY = paddingY + progress * (contentH - barH);
            RenderUtil.drawRoundedRect(x + w - 12, barY, 6, barH, 3, 0xFF424242);
        }
    }

    private void drawModuleManagement(TabInfo tab, float x, float y, float w, float h, int mouseX, int mouseY) {
        Category cat = tab.getCategory();
        List<Register> mods = tab.isModulesTab()
                ? Register.getAlphabeticalModules()
                : (cat != null ? cat.getModules() : Collections.emptyList());

        float sidebarW = 240;
        RenderUtil.drawRect(x, y, x + sidebarW, y + h, 0xFF101010);
        RenderUtil.drawRect(x + sidebarW, y, x + sidebarW + 1, y + h, 0xFF2A2A2A);

        RenderUtil.enablePixelScissor(x, y, sidebarW, h);
        float itemY = y + 14 + tab.getModuleListScrollY();

        if (cat == Category.THEME) {
            String selectedItem = tab.getSelectedThemeItem();
            boolean isSelected = "Font".equalsIgnoreCase(selectedItem);
            String displayName = isSelected ? "> Font" : "Font";
            int color = isSelected ? 0xFF76B9ED : 0xFFD7D7D7;
            drawString2x(displayName, x + 32, itemY + 2, color);
        } else {
            int modCount = mods.size();
            for (int mi = 0; mi < modCount; mi++) {
                Register m = mods.get(mi);
                if (itemY + 28 >= y && itemY <= y + h) {
                    boolean isSelected = (tab.getSelectedModule() == m);

                    String displayName = isSelected ? ("> " + m.getName()) : m.getName();
                    int color = m.isEnabled() ? 0xFF76B9ED : 0xFFD7D7D7;
                    drawString2x(displayName, x + 32, itemY + 2, color);
                }

                itemY += 28;
            }
        }
        RenderUtil.disableScissor();

        float rightX = x + sidebarW + 24;
        if (cat == Category.THEME) {
            if ("Font".equalsIgnoreCase(tab.getSelectedThemeItem())) {
                drawThemeFontManagement(tab, x, y, w, h, rightX, sidebarW, mouseX, mouseY);
            }
            return;
        }

        Register selectedModule = tab.getSelectedModule();
        if (selectedModule != null) {
            float modTitleY = y + 20;
            drawString2x("\u00A7b" + selectedModule.getName().toUpperCase(), rightX, modTitleY, 0xFF76B9ED);

            float metaY = modTitleY + 30;
            RenderUtil.drawRect(rightX, metaY - 6, x + w - 20, metaY - 5, 0xFF252525);

            float metaGap = getStringWidth2x("    ");
            float curMetaX = rightX;
            float curMetaY = metaY;
            float featureStartX = rightX;
            float maxRightX = x + w - 20;

            boolean isBinding = (tab.getBindingModule() == selectedModule);
            String keyLabel = "Key: ";
            String keyVal = isBinding ? "..." : (selectedModule.getKeyCode() == 0 ? "None" : Keyboard.getKeyName(selectedModule.getKeyCode()));
            drawString2x(keyLabel, rightX, metaY, 0xFFD7D7D7);
            float keyLabelW = getStringWidth2x(keyLabel);
            int keyValColor = isBinding ? 0xFFD7D7D7 : 0xFFFFFFFF;
            drawString2x(keyVal, rightX + keyLabelW, metaY, keyValColor);
            float keyItemW = keyLabelW + getStringWidth2x(keyVal);

            featureStartX = rightX + keyItemW + metaGap;
            curMetaX = featureStartX;

            List<CheckBoxSetting> metaOpts = selectedModule.getMetaOptions();
            int metaOptCount = metaOpts.size();
            for (int oi = 0; oi < metaOptCount; oi++) {
                CheckBoxSetting opt = metaOpts.get(oi);
                String text = opt.getDisplayText();
                float totalW = getStringWidth2x(text);

                if (curMetaX + totalW > maxRightX && curMetaX > featureStartX) {
                    curMetaY += 22.0f;
                    curMetaX = featureStartX;
                }

                drawString2x(text, curMetaX, curMetaY, 0xFFD7D7D7);

                curMetaX += totalW + metaGap;
            }

            float rowH = 22.0f;
            float lineGap = 10.0f;
            float lineStep = rowH + lineGap;
            float indentStep = getStringWidth2x("    ");
            float sW = 200.0f;
            float sH = 22.0f;

            float settingY = curMetaY + 30;
            float scissorY = settingY - 4;
            float scissorH = h - (settingY - y) + 4;
            RenderUtil.enablePixelScissor(rightX - 6, scissorY, w - sidebarW - 14, scissorH);

            float clipTop = scissorY - 30;
            float clipBottom = scissorY + scissorH + 30;

            List<String> groupOrder = selectedModule.getGroupNames();
            int totalGroups = groupOrder.size();
            for (int gi = 0; gi < totalGroups; gi++) {
                String groupName = groupOrder.get(gi);
                boolean isCollapsed = tab.getCollapsedGroups().getOrDefault(groupName, false);
                String arrow = isCollapsed ? " \u25BC" : " \u25B2";

                float groupBottomY = settingY + rowH;
                if (settingY >= clipTop && settingY <= clipBottom) {
                    int groupColor = 0xFFFFAA00;
                    drawString2x(groupName, rightX, settingY + 2, groupColor);
                    float groupNameW = getStringWidth2x(groupName);

                    FontUtil.drawConsolasString2x(arrow, rightX + groupNameW, settingY + 2, groupColor);
                }
                settingY += lineStep;

                if (!isCollapsed) {
                    List<Setting<?>> topSettings = selectedModule.getTopSettingsByGroup(groupName);
                    int topCount = topSettings.size();
                    float trunkX1 = rightX + indentStep / 2.0f;
                    float prevTopExitY = groupBottomY;

                    for (int ti = 0; ti < topCount; ti++) {
                        Setting<?> s = topSettings.get(ti);
                        boolean isLastTop = (ti == topCount - 1);
                        float itemX = rightX + indentStep;
                        float centerY1 = settingY + rowH / 2.0f;

                        boolean topVisible = (settingY >= clipTop && settingY <= clipBottom);
                        if (topVisible) {
                            drawTreeBranch(trunkX1, itemX, prevTopExitY, centerY1);
                        }

                        float lastTopRowCenterY = centerY1;

                        if (s instanceof CheckBoxSetting) {
                            if (topVisible) {
                                CheckBoxSetting bs = (CheckBoxSetting) s;
                                drawString2x(bs.getDisplayText(), itemX, settingY + 2, 0xFFD7D7D7);
                            }
                            settingY += lineStep;
                        } else if (s instanceof NumberSetting) {
                            NumberSetting ns = (NumberSetting) s;
                            if (topVisible) {
                                String val = ns.getDisplayValue();
                                String label = ns.getLabel();
                                float labelW = getStringWidth2x(label);
                                drawString2x(label, itemX, settingY + 2, 0xFFD7D7D7);

                                float sX = itemX + labelW;
                                float sY = settingY;

                                double progress = (ns.getValue() - ns.getMin()) / (ns.getMax() - ns.getMin());
                                float filledW = (float) (sW * Math.max(0, Math.min(1, progress)));
                                RenderUtil.drawRect(sX, sY, sX + sW, sY + sH, 0x1A000000);
                                if (filledW > 0) {
                                    RenderUtil.drawRect(sX, sY, sX + filledW, sY + sH, 0xFF76B9ED);
                                }
                                RenderUtil.drawRectOutline(sX, sY, sW, sH, 1.0f, 0xFF000000);

                                float valW = getStringWidth2x(val);
                                float textX = sX + (sW - valW) / 2.0f;
                                float textY = sY + (sH - 14.0f) / 2.0f;
                                drawString2x(val, textX, textY, 0xFFFFFFFF);
                            }

                            if (draggingSlider == ns) {
                                String label = ns.getLabel();
                                float labelW = getStringWidth2x(label);
                                float sX = itemX + labelW;
                                double v = ns.getMin() + Math.max(0, Math.min(1, (mouseX - sX) / sW)) * (ns.getMax() - ns.getMin());
                                ns.setValue(v);
                            }
                            settingY += lineStep;
                        } else if (s instanceof ModeSetting) {
                            ModeSetting ms = (ModeSetting) s;
                            String prefix = ms.getName() + ": ";
                            float prefixW = getStringWidth2x(prefix);
                            float mCurX = itemX;
                            float mCurY = settingY;

                            if (topVisible) {
                                drawString2x(prefix, mCurX, mCurY + 2, 0xFFD7D7D7);
                            }
                            mCurX += prefixW;

                            List<String> modes = ms.getModes();
                            if (modes != null) {
                                float spaceW = getStringWidth2x(" ");
                                float commaW = getStringWidth2x(",");
                                float wrapIndent = itemX + prefixW;

                                for (int mi = 0; mi < modes.size(); mi++) {
                                    String mode = modes.get(mi);
                                    boolean isLast = (mi == modes.size() - 1);
                                    boolean isSelected = mode != null && mode.equalsIgnoreCase(ms.getValue());

                                    float modeW = getStringWidth2x(mode);
                                    float itemBlockW = modeW + (isLast ? 0 : commaW);

                                    if (mCurX + itemBlockW > maxRightX && mCurX > wrapIndent) {
                                        mCurX = wrapIndent;
                                        float prevLineY = mCurY + rowH / 2.0f;
                                        mCurY += lineStep;
                                        float nextLineY = mCurY + rowH / 2.0f;
                                        if (mCurY >= clipTop && mCurY <= clipBottom) {
                                            drawTreeTrunk(trunkX1, prevLineY, nextLineY);
                                        }
                                    }

                                    if (mCurY >= clipTop && mCurY <= clipBottom) {
                                        int modeColor = isSelected ? 0xFF76B9ED : 0xFFD7D7D7;
                                        drawString2x(mode, mCurX, mCurY + 2, modeColor);
                                        if (!isLast) {
                                            drawString2x(",", mCurX + modeW, mCurY + 2, 0xFFD7D7D7);
                                        }
                                    }

                                    mCurX += itemBlockW + spaceW;
                                }
                            }
                            lastTopRowCenterY = mCurY + rowH / 2.0f;
                            settingY = mCurY + lineStep;
                        }

                        List<Setting<?>> subSettings = selectedModule.getChildrenSettings(s);
                        if (subSettings != null && !subSettings.isEmpty()) {
                            boolean showSubs = !(s instanceof CheckBoxSetting) || ((CheckBoxSetting) s).getValue();
                            if (showSubs) {
                                int subCount = subSettings.size();
                                float trunkX2 = itemX + indentStep / 2.0f;
                                float subItemX = itemX + indentStep;
                                float prevSubY = lastTopRowCenterY + rowH / 2.0f;
                                float prevL1Y = centerY1;

                                for (int subI = 0; subI < subCount; subI++) {
                                    Setting<?> sub = subSettings.get(subI);
                                    boolean subVisible = (settingY >= clipTop && settingY <= clipBottom);

                                    if (sub instanceof CheckBoxSetting) {
                                        float subCenterY = settingY + rowH / 2.0f;
                                        if (subVisible) {
                                            drawTreeBranch(trunkX2, subItemX, prevSubY, subCenterY);
                                            if (!isLastTop) drawTreeTrunk(trunkX1, prevL1Y, subCenterY);
                                            CheckBoxSetting bs = (CheckBoxSetting) sub;
                                            drawString2x(bs.getDisplayText(), subItemX, settingY + 2, 0xFFD7D7D7);
                                        }

                                        prevSubY = subCenterY;
                                        prevL1Y = subCenterY;
                                        settingY += lineStep;
                                    } else if (sub instanceof NumberSetting) {
                                        float subCenterY = settingY + rowH / 2.0f;
                                        NumberSetting ns = (NumberSetting) sub;

                                        if (subVisible) {
                                            drawTreeBranch(trunkX2, subItemX, prevSubY, subCenterY);
                                            if (!isLastTop) drawTreeTrunk(trunkX1, prevL1Y, subCenterY);

                                            String val = ns.getDisplayValue();
                                            String label = ns.getLabel();
                                            float labelW = getStringWidth2x(label);
                                            drawString2x(label, subItemX, settingY + 2, 0xFFD7D7D7);

                                            float sX = subItemX + labelW;
                                            float sY = settingY;

                                            double progress = (ns.getValue() - ns.getMin()) / (ns.getMax() - ns.getMin());
                                            float filledW = (float) (sW * Math.max(0, Math.min(1, progress)));
                                            RenderUtil.drawRect(sX, sY, sX + sW, sY + sH, 0x1A000000);
                                            if (filledW > 0) {
                                                RenderUtil.drawRect(sX, sY, sX + filledW, sY + sH, 0xFF76B9ED);
                                            }
                                            RenderUtil.drawRectOutline(sX, sY, sW, sH, 1.0f, 0xFF000000);

                                            float valW = getStringWidth2x(val);
                                            float textX = sX + (sW - valW) / 2.0f;
                                            float textY = sY + (sH - 14.0f) / 2.0f;
                                            drawString2x(val, textX, textY, 0xFFFFFFFF);
                                        }

                                        if (draggingSlider == ns) {
                                            String label = ns.getLabel();
                                            float labelW = getStringWidth2x(label);
                                            float sX = subItemX + labelW;
                                            double v = ns.getMin() + Math.max(0, Math.min(1, (mouseX - sX) / sW)) * (ns.getMax() - ns.getMin());
                                            ns.setValue(v);
                                        }

                                        prevSubY = subCenterY;
                                        prevL1Y = subCenterY;
                                        settingY += lineStep;
                                    } else if (sub instanceof ModeSetting) {
                                        float subCenterY = settingY + rowH / 2.0f;
                                        if (subVisible) {
                                            drawTreeBranch(trunkX2, subItemX, prevSubY, subCenterY);
                                            if (!isLastTop) drawTreeTrunk(trunkX1, prevL1Y, subCenterY);
                                        }

                                        ModeSetting ms = (ModeSetting) sub;
                                        String prefix = ms.getName() + ": ";
                                        float prefixW = getStringWidth2x(prefix);
                                        float mCurX = subItemX;
                                        float mCurY = settingY;

                                        if (subVisible) {
                                            drawString2x(prefix, mCurX, mCurY + 2, 0xFFD7D7D7);
                                        }
                                        mCurX += prefixW;

                                        List<String> modes = ms.getModes();
                                        if (modes != null) {
                                            float spaceW = getStringWidth2x(" ");
                                            float commaW = getStringWidth2x(",");
                                            float wrapIndent = subItemX + prefixW;

                                            for (int mi = 0; mi < modes.size(); mi++) {
                                                String mode = modes.get(mi);
                                                boolean isLast = (mi == modes.size() - 1);
                                                boolean isSelected = mode != null && mode.equalsIgnoreCase(ms.getValue());

                                                float modeW = getStringWidth2x(mode);
                                                float itemBlockW = modeW + (isLast ? 0 : commaW);

                                                if (mCurX + itemBlockW > maxRightX && mCurX > wrapIndent) {
                                                    mCurX = wrapIndent;
                                                    float prevLineY = mCurY + rowH / 2.0f;
                                                    mCurY += lineStep;
                                                    float nextLineY = mCurY + rowH / 2.0f;
                                                    if (mCurY >= clipTop && mCurY <= clipBottom) {
                                                        drawTreeTrunk(trunkX2, prevLineY, nextLineY);
                                                        if (!isLastTop) drawTreeTrunk(trunkX1, prevLineY, nextLineY);
                                                    }
                                                }

                                                if (mCurY >= clipTop && mCurY <= clipBottom) {
                                                    int modeColor = isSelected ? 0xFF76B9ED : 0xFFD7D7D7;
                                                    drawString2x(mode, mCurX, mCurY + 2, modeColor);
                                                    if (!isLast) {
                                                        drawString2x(",", mCurX + modeW, mCurY + 2, 0xFFD7D7D7);
                                                    }
                                                }

                                                mCurX += itemBlockW + spaceW;
                                            }
                                        }
                                        prevSubY = mCurY + rowH / 2.0f;
                                        prevL1Y = prevSubY;
                                        settingY = mCurY + lineStep;
                                    } else if (sub instanceof ColorSetting) {
                                        ColorSetting cs = (ColorSetting) sub;

                                        float subCenterY1 = settingY + rowH / 2.0f;
                                        float sX1 = subItemX;
                                        float sY1 = settingY;
                                        if (settingY >= clipTop && settingY <= clipBottom) {
                                            drawTreeBranch(trunkX2, subItemX, prevSubY, subCenterY1);
                                            if (!isLastTop) drawTreeTrunk(trunkX1, prevL1Y, subCenterY1);
                                            RenderUtil.drawHorizontalRainbow(sX1, sY1, sW, sH);
                                            RenderUtil.drawRectOutline(sX1, sY1, sW, sH, 1.0f, 0xFF000000);
                                            float thumbX1 = sX1 + cs.getHue() * sW;
                                            RenderUtil.drawRect(thumbX1 - 1.0f, sY1, thumbX1 + 1.0f, sY1 + sH, 0xFF000000);
                                            float togX1 = sX1 + sW + 14.0f;
                                            drawString2x(cs.rainbow.getDisplayText(), togX1, settingY + 2, 0xFFD7D7D7);
                                        }

                                        if (draggingColorSetting == cs && draggingColorType == ColorDragType.HUE) {
                                            cs.setHue((mouseX - sX1) / sW);
                                        }

                                        prevSubY = subCenterY1;
                                        prevL1Y = subCenterY1;
                                        settingY += lineStep;

                                        float subCenterY2 = settingY + rowH / 2.0f;
                                        float sX2 = subItemX;
                                        float sY2 = settingY;
                                        if (settingY >= clipTop && settingY <= clipBottom) {
                                            drawTreeBranch(trunkX2, subItemX, prevSubY, subCenterY2);
                                            if (!isLastTop) drawTreeTrunk(trunkX1, prevL1Y, subCenterY2);
                                            int satLeft = 0xFFFFFFFF;
                                            int satRight = 0xFF000000 | java.awt.Color.HSBtoRGB(cs.getHue(), 1.0f, 1.0f);
                                            RenderUtil.drawHorizontalGradient(sX2, sY2, sW, sH, satLeft, satRight);
                                            RenderUtil.drawRectOutline(sX2, sY2, sW, sH, 1.0f, 0xFF000000);
                                            float thumbX2 = sX2 + cs.getSaturation() * sW;
                                            RenderUtil.drawRect(thumbX2 - 1.0f, sY2, thumbX2 + 1.0f, sY2 + sH, 0xFF000000);
                                            float togX2 = sX2 + sW + 14.0f;
                                            drawString2x(cs.fade.getDisplayText(), togX2, settingY + 2, 0xFFD7D7D7);
                                        }

                                        if (draggingColorSetting == cs && draggingColorType == ColorDragType.SATURATION) {
                                            cs.setSaturation((mouseX - sX2) / sW);
                                        }

                                        prevSubY = subCenterY2;
                                        prevL1Y = subCenterY2;
                                        settingY += lineStep;

                                        float subCenterY3 = settingY + rowH / 2.0f;
                                        float sX3 = subItemX;
                                        float sY3 = settingY;
                                        if (settingY >= clipTop && settingY <= clipBottom) {
                                            drawTreeBranch(trunkX2, subItemX, prevSubY, subCenterY3);
                                            if (!isLastTop) drawTreeTrunk(trunkX1, prevL1Y, subCenterY3);
                                            int briLeft = 0xFF000000;
                                            int briRight = 0xFF000000 | java.awt.Color.HSBtoRGB(cs.getHue(), cs.getSaturation(), 1.0f);
                                            RenderUtil.drawHorizontalGradient(sX3, sY3, sW, sH, briLeft, briRight);
                                            RenderUtil.drawRectOutline(sX3, sY3, sW, sH, 1.0f, 0xFF000000);
                                            float thumbX3 = sX3 + cs.getBrightness() * sW;
                                            RenderUtil.drawRect(thumbX3 - 1.0f, sY3, thumbX3 + 1.0f, sY3 + sH, 0xFF000000);
                                            float togX3 = sX3 + sW + 14.0f;
                                            drawString2x(cs.astolfo.getDisplayText(), togX3, settingY + 2, 0xFFD7D7D7);
                                        }

                                        if (draggingColorSetting == cs && draggingColorType == ColorDragType.BRIGHTNESS) {
                                            cs.setBrightness((mouseX - sX3) / sW);
                                        }

                                        prevSubY = subCenterY3;
                                        prevL1Y = subCenterY3;
                                        settingY += lineStep;

                                        float subCenterY4 = settingY + rowH / 2.0f;
                                        float sX4 = subItemX;
                                        float sY4 = settingY;
                                        if (settingY >= clipTop && settingY <= clipBottom) {
                                            drawTreeBranch(trunkX2, subItemX, prevSubY, subCenterY4);
                                            if (!isLastTop) drawTreeTrunk(trunkX1, prevL1Y, subCenterY4);
                                            int rgbBase = java.awt.Color.HSBtoRGB(cs.getHue(), cs.getSaturation(), cs.getBrightness()) & 0x00FFFFFF;
                                            int alpLeft = 0x00000000 | rgbBase;
                                            int alpRight = 0xFF000000 | rgbBase;
                                            RenderUtil.drawHorizontalGradient(sX4, sY4, sW, sH, alpLeft, alpRight);
                                            RenderUtil.drawRectOutline(sX4, sY4, sW, sH, 1.0f, 0xFF000000);
                                            float thumbX4 = sX4 + (cs.getAlpha() / 255.0f) * sW;
                                            RenderUtil.drawRect(thumbX4 - 1.0f, sY4, thumbX4 + 1.0f, sY4 + sH, 0xFF000000);
                                            float togX4 = sX4 + sW + 14.0f;
                                            drawString2x(cs.clientColor.getDisplayText(), togX4, settingY + 2, 0xFFD7D7D7);
                                        }

                                        if (draggingColorSetting == cs && draggingColorType == ColorDragType.ALPHA) {
                                            cs.setAlpha((int) (((mouseX - sX4) / sW) * 255));
                                        }

                                        prevSubY = subCenterY4;
                                        prevL1Y = subCenterY4;
                                        settingY += lineStep;
                                    }
                                }
                                prevTopExitY = prevL1Y;
                            } else {
                                prevTopExitY = lastTopRowCenterY;
                            }
                        } else {
                            prevTopExitY = lastTopRowCenterY;
                        }
                    }
                }
            }

            RenderUtil.disableScissor();
        }
    }

    private void drawThemeFontManagement(TabInfo tab, float x, float y, float w, float h, float rightX, float sidebarW, int mouseX, int mouseY) {
        float modTitleY = y + 20;
        drawString2x("\u00A7bFONT", rightX, modTitleY, 0xFF76B9ED);

        float metaY = modTitleY + 30;
        RenderUtil.drawRect(rightX, metaY - 6, x + w - 20, metaY - 5, 0xFF252525);

        float rowH = 22.0f;
        float lineGap = 10.0f;
        float lineStep = rowH + lineGap;
        float indentStep = getStringWidth2x("    ");
        float maxRightX = x + w - 20;
        float sW = 200.0f;
        float sH = 22.0f;

        float settingY = metaY + 16;
        float scissorY = settingY - 4;
        float scissorH = h - (settingY - y) + 4;
        RenderUtil.enablePixelScissor(rightX - 6, scissorY, w - sidebarW - 14, scissorH);

        float clipTop = scissorY - 30;
        float clipBottom = scissorY + scissorH + 30;

        client.m1ck3y.rattix.modules.Font font = client.m1ck3y.rattix.modules.Font.getInstance();
        List<String> groupOrder = font.getGroupNames();
        int totalGroups = groupOrder.size();
        for (int gi = 0; gi < totalGroups; gi++) {
            String groupName = groupOrder.get(gi);
            boolean isCollapsed = tab.getCollapsedGroups().getOrDefault(groupName, false);
            String arrow = isCollapsed ? " \u25BC" : " \u25B2";

            float groupBottomY = settingY + rowH;
            if (settingY >= clipTop && settingY <= clipBottom) {
                int groupColor = 0xFFFFAA00;
                drawString2x(groupName, rightX, settingY + 2, groupColor);
                float groupNameW = getStringWidth2x(groupName);
                FontUtil.drawConsolasString2x(arrow, rightX + groupNameW, settingY + 2, groupColor);
            }
            settingY += lineStep;

            if (!isCollapsed) {
                if ("Font Manager".equals(groupName)) {
                    List<File> ttfFiles = FileManager.getTtfFiles();
                    int childCount = 1 + (ttfFiles != null ? ttfFiles.size() : 0);
                    float trunkX1 = rightX + indentStep / 2.0f;
                    float itemX = rightX + indentStep;
                    float prevCenterY = groupBottomY;

                    for (int ci = 0; ci < childCount; ci++) {
                        float centerY = settingY + rowH / 2.0f;
                        if (settingY >= clipTop && settingY <= clipBottom) {
                            drawTreeBranch(trunkX1, itemX, prevCenterY, centerY);
                            if (ci == 0) {
                                boolean isCur = "Minecraft".equalsIgnoreCase(font.getSelectedFont());
                                FontUtil.drawMinecraftString2x("Minecraft", itemX, settingY + 2, isCur ? 0xFF76B9ED : 0xFFCCCCCC);
                            } else {
                                File ttf = ttfFiles.get(ci - 1);
                                String fName = ttf.getName();
                                boolean isCur = fName.equalsIgnoreCase(font.getSelectedFont());
                                int fColor = isCur ? 0xFF76B9ED : 0xFFCCCCCC;
                                CustomFontRenderer cfr = FontUtil.getFontRenderer(fName);
                                if (cfr != null) {
                                    cfr.drawStringWithShadow(fName, itemX, settingY + 2, fColor);
                                } else {
                                    drawString2x(fName, itemX, settingY + 2, fColor);
                                }
                            }
                        }
                        prevCenterY = centerY;
                        settingY += lineStep;
                    }
                } else if ("Global Font".equals(groupName)) {
                    List<File> ttfFiles = FileManager.getTtfFiles();
                    float trunkX1 = rightX + indentStep / 2.0f;
                    float itemX = rightX + indentStep;
                    float centerY = settingY + rowH / 2.0f;

                    boolean visible = (settingY >= clipTop && settingY <= clipBottom);
                    if (visible) {
                        drawTreeBranch(trunkX1, itemX, groupBottomY, centerY);
                    }

                    String prefix = "Font: ";
                    float prefixW = getStringWidth2x(prefix);
                    float mCurX = itemX;
                    float mCurY = settingY;

                    if (visible) {
                        drawString2x(prefix, mCurX, mCurY + 2, 0xFFD7D7D7);
                    }
                    mCurX += prefixW;

                    int optionCount = 1 + (ttfFiles != null ? ttfFiles.size() : 0);
                    float spaceW = getStringWidth2x(" ");
                    float commaW = getStringWidth2x(",");
                    float wrapIndent = itemX + prefixW;

                    for (int mi = 0; mi < optionCount; mi++) {
                        String fName = (mi == 0) ? "Minecraft" : ttfFiles.get(mi - 1).getName();
                        boolean isLast = (mi == optionCount - 1);
                        boolean isSelected = fName.equalsIgnoreCase(font.getSelectedFont());

                        float modeW = getStringWidth2x(fName);
                        float itemBlockW = modeW + (isLast ? 0 : commaW);

                        if (mCurX + itemBlockW > maxRightX && mCurX > wrapIndent) {
                            mCurX = wrapIndent;
                            float prevLineY = mCurY + rowH / 2.0f;
                            mCurY += lineStep;
                            float nextLineY = mCurY + rowH / 2.0f;
                            if (mCurY >= clipTop && mCurY <= clipBottom) {
                                drawTreeTrunk(trunkX1, prevLineY, nextLineY);
                            }
                        }

                        if (mCurY >= clipTop && mCurY <= clipBottom) {
                            int modeColor = isSelected ? 0xFF76B9ED : 0xFFD7D7D7;
                            drawString2x(fName, mCurX, mCurY + 2, modeColor);
                            if (!isLast) {
                                drawString2x(",", mCurX + modeW, mCurY + 2, 0xFFD7D7D7);
                            }
                        }

                        mCurX += itemBlockW + spaceW;
                    }
                    settingY = mCurY + lineStep;
                } else {
                    List<Setting<?>> topSettings = font.getTopSettingsByGroup(groupName);
                    if (topSettings != null) {
                        int topCount = topSettings.size();
                        float trunkX1 = rightX + indentStep / 2.0f;
                        float prevTopExitY = groupBottomY;

                        for (int ti = 0; ti < topCount; ti++) {
                            Setting<?> s = topSettings.get(ti);
                            boolean isLastTop = (ti == topCount - 1);
                            float itemX = rightX + indentStep;
                            float centerY1 = settingY + rowH / 2.0f;

                            boolean topVisible = (settingY >= clipTop && settingY <= clipBottom);
                            if (topVisible) {
                                drawTreeBranch(trunkX1, itemX, prevTopExitY, centerY1);
                            }

                            float lastTopRowCenterY = centerY1;

                            if (s instanceof CheckBoxSetting) {
                                if (topVisible) {
                                    CheckBoxSetting bs = (CheckBoxSetting) s;
                                    drawString2x(bs.getDisplayText(), itemX, settingY + 2, 0xFFD7D7D7);
                                }
                                settingY += lineStep;
                            } else if (s instanceof NumberSetting) {
                                NumberSetting ns = (NumberSetting) s;
                                if (topVisible) {
                                    String val = ns.getDisplayValue();
                                    String label = ns.getLabel();
                                    float labelW = getStringWidth2x(label);
                                    drawString2x(label, itemX, settingY + 2, 0xFFD7D7D7);

                                    float sX = itemX + labelW;
                                    float sY = settingY;

                                    double progress = (ns.getValue() - ns.getMin()) / (ns.getMax() - ns.getMin());
                                    float filledW = (float) (sW * Math.max(0, Math.min(1, progress)));
                                    RenderUtil.drawRect(sX, sY, sX + sW, sY + sH, 0x1A000000);
                                    if (filledW > 0) {
                                        RenderUtil.drawRect(sX, sY, sX + filledW, sY + sH, 0xFF76B9ED);
                                    }
                                    RenderUtil.drawRectOutline(sX, sY, sW, sH, 1.0f, 0xFF000000);

                                    float valW = getStringWidth2x(val);
                                    float textX = sX + (sW - valW) / 2.0f;
                                    float textY = sY + (sH - 14.0f) / 2.0f;
                                    drawString2x(val, textX, textY, 0xFFFFFFFF);
                                }

                                if (draggingSlider == ns) {
                                    String label = ns.getLabel();
                                    float labelW = getStringWidth2x(label);
                                    float sX = itemX + labelW;
                                    double v = ns.getMin() + Math.max(0, Math.min(1, (mouseX - sX) / sW)) * (ns.getMax() - ns.getMin());
                                    ns.setValue(v);
                                }
                                settingY += lineStep;
                            } else if (s instanceof ModeSetting) {
                                ModeSetting ms = (ModeSetting) s;
                                String prefix = ms.getName() + ": ";
                                float prefixW = getStringWidth2x(prefix);
                                float mCurX = itemX;
                                float mCurY = settingY;

                                if (topVisible) {
                                    drawString2x(prefix, mCurX, mCurY + 2, 0xFFD7D7D7);
                                }
                                mCurX += prefixW;

                                List<String> modes = ms.getModes();
                                if (modes != null) {
                                    float spaceW = getStringWidth2x(" ");
                                    float commaW = getStringWidth2x(",");
                                    float wrapIndent = itemX + prefixW;

                                    for (int mi = 0; mi < modes.size(); mi++) {
                                        String mode = modes.get(mi);
                                        boolean isLast = (mi == modes.size() - 1);
                                        boolean isSelected = mode != null && mode.equalsIgnoreCase(ms.getValue());

                                        float modeW = getStringWidth2x(mode);
                                        float itemBlockW = modeW + (isLast ? 0 : commaW);

                                        if (mCurX + itemBlockW > maxRightX && mCurX > wrapIndent) {
                                            mCurX = wrapIndent;
                                            float prevLineY = mCurY + rowH / 2.0f;
                                            mCurY += lineStep;
                                            float nextLineY = mCurY + rowH / 2.0f;
                                            if (mCurY >= clipTop && mCurY <= clipBottom) {
                                                drawTreeTrunk(trunkX1, prevLineY, nextLineY);
                                            }
                                        }

                                        if (mCurY >= clipTop && mCurY <= clipBottom) {
                                            int modeColor = isSelected ? 0xFF76B9ED : 0xFFD7D7D7;
                                            drawString2x(mode, mCurX, mCurY + 2, modeColor);
                                            if (!isLast) {
                                                drawString2x(",", mCurX + modeW, mCurY + 2, 0xFFD7D7D7);
                                            }
                                        }

                                        mCurX += itemBlockW + spaceW;
                                    }
                                }
                                lastTopRowCenterY = mCurY + rowH / 2.0f;
                                settingY = mCurY + lineStep;
                            }

                            List<Setting<?>> subSettings = font.getChildrenSettings(s);
                            if (subSettings != null && !subSettings.isEmpty()) {
                                boolean showSubs = !(s instanceof CheckBoxSetting) || ((CheckBoxSetting) s).getValue();
                                if (showSubs) {
                                    int subCount = subSettings.size();
                                    float trunkX2 = itemX + indentStep / 2.0f;
                                    float subItemX = itemX + indentStep;
                                    float prevSubY = lastTopRowCenterY + rowH / 2.0f;
                                    float prevL1Y = centerY1;

                                    for (int subI = 0; subI < subCount; subI++) {
                                        Setting<?> sub = subSettings.get(subI);
                                        if (sub instanceof ColorSetting && font.shadowMode.is("Vanilla")) {
                                            continue;
                                        }
                                        boolean subVisible = (settingY >= clipTop && settingY <= clipBottom);

                                        if (sub instanceof CheckBoxSetting) {
                                            float subCenterY = settingY + rowH / 2.0f;
                                            if (subVisible) {
                                                drawTreeBranch(trunkX2, subItemX, prevSubY, subCenterY);
                                                if (!isLastTop) drawTreeTrunk(trunkX1, prevL1Y, subCenterY);
                                                CheckBoxSetting bs = (CheckBoxSetting) sub;
                                                drawString2x(bs.getDisplayText(), subItemX, settingY + 2, 0xFFD7D7D7);
                                            }

                                            prevSubY = subCenterY;
                                            prevL1Y = subCenterY;
                                            settingY += lineStep;
                                        } else if (sub instanceof NumberSetting) {
                                            float subCenterY = settingY + rowH / 2.0f;
                                            NumberSetting ns = (NumberSetting) sub;

                                            if (subVisible) {
                                                drawTreeBranch(trunkX2, subItemX, prevSubY, subCenterY);
                                                if (!isLastTop) drawTreeTrunk(trunkX1, prevL1Y, subCenterY);

                                                String val = ns.getDisplayValue();
                                                String label = ns.getLabel();
                                                float labelW = getStringWidth2x(label);
                                                drawString2x(label, subItemX, settingY + 2, 0xFFD7D7D7);

                                                float sX = subItemX + labelW;
                                                float sY = settingY;

                                                double progress = (ns.getValue() - ns.getMin()) / (ns.getMax() - ns.getMin());
                                                float filledW = (float) (sW * Math.max(0, Math.min(1, progress)));
                                                RenderUtil.drawRect(sX, sY, sX + sW, sY + sH, 0x1A000000);
                                                if (filledW > 0) {
                                                    RenderUtil.drawRect(sX, sY, sX + filledW, sY + sH, 0xFF76B9ED);
                                                }
                                                RenderUtil.drawRectOutline(sX, sY, sW, sH, 1.0f, 0xFF000000);

                                                float valW = getStringWidth2x(val);
                                                float textX = sX + (sW - valW) / 2.0f;
                                                float textY = sY + (sH - 14.0f) / 2.0f;
                                                drawString2x(val, textX, textY, 0xFFFFFFFF);
                                            }

                                            if (draggingSlider == ns) {
                                                String label = ns.getLabel();
                                                float labelW = getStringWidth2x(label);
                                                float sX = subItemX + labelW;
                                                double v = ns.getMin() + Math.max(0, Math.min(1, (mouseX - sX) / sW)) * (ns.getMax() - ns.getMin());
                                                ns.setValue(v);
                                            }

                                            prevSubY = subCenterY;
                                            prevL1Y = subCenterY;
                                            settingY += lineStep;
                                        } else if (sub instanceof ModeSetting) {
                                            float subCenterY = settingY + rowH / 2.0f;
                                            if (subVisible) {
                                                drawTreeBranch(trunkX2, subItemX, prevSubY, subCenterY);
                                                if (!isLastTop) drawTreeTrunk(trunkX1, prevL1Y, subCenterY);
                                            }

                                            ModeSetting ms = (ModeSetting) sub;
                                            String prefix = ms.getName() + ": ";
                                            float prefixW = getStringWidth2x(prefix);
                                            float mCurX = subItemX;
                                            float mCurY = settingY;

                                            if (subVisible) {
                                                drawString2x(prefix, mCurX, mCurY + 2, 0xFFD7D7D7);
                                            }
                                            mCurX += prefixW;

                                            List<String> modes = ms.getModes();
                                            if (modes != null) {
                                                float spaceW = getStringWidth2x(" ");
                                                float commaW = getStringWidth2x(",");
                                                float wrapIndent = subItemX + prefixW;

                                                for (int mi = 0; mi < modes.size(); mi++) {
                                                    String mode = modes.get(mi);
                                                    boolean isLast = (mi == modes.size() - 1);
                                                    boolean isSelected = mode != null && mode.equalsIgnoreCase(ms.getValue());

                                                    float modeW = getStringWidth2x(mode);
                                                    float itemBlockW = modeW + (isLast ? 0 : commaW);

                                                    if (mCurX + itemBlockW > maxRightX && mCurX > wrapIndent) {
                                                        mCurX = wrapIndent;
                                                        float prevLineY = mCurY + rowH / 2.0f;
                                                        mCurY += lineStep;
                                                        float nextLineY = mCurY + rowH / 2.0f;
                                                        if (mCurY >= clipTop && mCurY <= clipBottom) {
                                                            drawTreeTrunk(trunkX2, prevLineY, nextLineY);
                                                            if (!isLastTop) drawTreeTrunk(trunkX1, prevLineY, nextLineY);
                                                        }
                                                    }

                                                    if (mCurY >= clipTop && mCurY <= clipBottom) {
                                                        int modeColor = isSelected ? 0xFF76B9ED : 0xFFD7D7D7;
                                                        drawString2x(mode, mCurX, mCurY + 2, modeColor);
                                                        if (!isLast) {
                                                            drawString2x(",", mCurX + modeW, mCurY + 2, 0xFFD7D7D7);
                                                        }
                                                    }

                                                    mCurX += itemBlockW + spaceW;
                                                }
                                            }
                                            prevSubY = mCurY + rowH / 2.0f;
                                            prevL1Y = prevSubY;
                                            settingY = mCurY + lineStep;
                                        } else if (sub instanceof ColorSetting) {
                                            ColorSetting cs = (ColorSetting) sub;

                                            float subCenterY1 = settingY + rowH / 2.0f;
                                            float sX1 = subItemX;
                                            float sY1 = settingY;
                                            if (settingY >= clipTop && settingY <= clipBottom) {
                                                drawTreeBranch(trunkX2, subItemX, prevSubY, subCenterY1);
                                                if (!isLastTop) drawTreeTrunk(trunkX1, prevL1Y, subCenterY1);
                                                RenderUtil.drawHorizontalRainbow(sX1, sY1, sW, sH);
                                                RenderUtil.drawRectOutline(sX1, sY1, sW, sH, 1.0f, 0xFF000000);
                                                float thumbX1 = sX1 + cs.getHue() * sW;
                                                RenderUtil.drawRect(thumbX1 - 1.0f, sY1, thumbX1 + 1.0f, sY1 + sH, 0xFF000000);
                                                float togX1 = sX1 + sW + 14.0f;
                                                drawString2x(cs.rainbow.getDisplayText(), togX1, settingY + 2, 0xFFD7D7D7);
                                            }

                                            if (draggingColorSetting == cs && draggingColorType == ColorDragType.HUE) {
                                                cs.setHue((mouseX - sX1) / sW);
                                            }

                                            prevSubY = subCenterY1;
                                            prevL1Y = subCenterY1;
                                            settingY += lineStep;

                                            float subCenterY2 = settingY + rowH / 2.0f;
                                            float sX2 = subItemX;
                                            float sY2 = settingY;
                                            if (settingY >= clipTop && settingY <= clipBottom) {
                                                drawTreeBranch(trunkX2, subItemX, prevSubY, subCenterY2);
                                                if (!isLastTop) drawTreeTrunk(trunkX1, prevL1Y, subCenterY2);
                                                int satLeft = 0xFFFFFFFF;
                                                int satRight = 0xFF000000 | java.awt.Color.HSBtoRGB(cs.getHue(), 1.0f, 1.0f);
                                                RenderUtil.drawHorizontalGradient(sX2, sY2, sW, sH, satLeft, satRight);
                                                RenderUtil.drawRectOutline(sX2, sY2, sW, sH, 1.0f, 0xFF000000);
                                                float thumbX2 = sX2 + cs.getSaturation() * sW;
                                                RenderUtil.drawRect(thumbX2 - 1.0f, sY2, thumbX2 + 1.0f, sY2 + sH, 0xFF000000);
                                                float togX2 = sX2 + sW + 14.0f;
                                                drawString2x(cs.fade.getDisplayText(), togX2, settingY + 2, 0xFFD7D7D7);
                                            }

                                            if (draggingColorSetting == cs && draggingColorType == ColorDragType.SATURATION) {
                                                cs.setSaturation((mouseX - sX2) / sW);
                                            }

                                            prevSubY = subCenterY2;
                                            prevL1Y = subCenterY2;
                                            settingY += lineStep;

                                            float subCenterY3 = settingY + rowH / 2.0f;
                                            float sX3 = subItemX;
                                            float sY3 = settingY;
                                            if (settingY >= clipTop && settingY <= clipBottom) {
                                                drawTreeBranch(trunkX2, subItemX, prevSubY, subCenterY3);
                                                if (!isLastTop) drawTreeTrunk(trunkX1, prevL1Y, subCenterY3);
                                                int briLeft = 0xFF000000;
                                                int briRight = 0xFF000000 | java.awt.Color.HSBtoRGB(cs.getHue(), cs.getSaturation(), 1.0f);
                                                RenderUtil.drawHorizontalGradient(sX3, sY3, sW, sH, briLeft, briRight);
                                                RenderUtil.drawRectOutline(sX3, sY3, sW, sH, 1.0f, 0xFF000000);
                                                float thumbX3 = sX3 + cs.getBrightness() * sW;
                                                RenderUtil.drawRect(thumbX3 - 1.0f, sY3, thumbX3 + 1.0f, sY3 + sH, 0xFF000000);
                                                float togX3 = sX3 + sW + 14.0f;
                                                drawString2x(cs.astolfo.getDisplayText(), togX3, settingY + 2, 0xFFD7D7D7);
                                            }

                                            if (draggingColorSetting == cs && draggingColorType == ColorDragType.BRIGHTNESS) {
                                                cs.setBrightness((mouseX - sX3) / sW);
                                            }

                                            prevSubY = subCenterY3;
                                            prevL1Y = subCenterY3;
                                            settingY += lineStep;

                                            float subCenterY4 = settingY + rowH / 2.0f;
                                            float sX4 = subItemX;
                                            float sY4 = settingY;
                                            if (settingY >= clipTop && settingY <= clipBottom) {
                                                drawTreeBranch(trunkX2, subItemX, prevSubY, subCenterY4);
                                                if (!isLastTop) drawTreeTrunk(trunkX1, prevL1Y, subCenterY4);
                                                int rgbBase = java.awt.Color.HSBtoRGB(cs.getHue(), cs.getSaturation(), cs.getBrightness()) & 0x00FFFFFF;
                                                int alpLeft = 0x00000000 | rgbBase;
                                                int alpRight = 0xFF000000 | rgbBase;
                                                RenderUtil.drawHorizontalGradient(sX4, sY4, sW, sH, alpLeft, alpRight);
                                                RenderUtil.drawRectOutline(sX4, sY4, sW, sH, 1.0f, 0xFF000000);
                                                float thumbX4 = sX4 + (cs.getAlpha() / 255.0f) * sW;
                                                RenderUtil.drawRect(thumbX4 - 1.0f, sY4, thumbX4 + 1.0f, sY4 + sH, 0xFF000000);
                                                float togX4 = sX4 + sW + 14.0f;
                                                drawString2x(cs.clientColor.getDisplayText(), togX4, settingY + 2, 0xFFD7D7D7);
                                            }

                                            if (draggingColorSetting == cs && draggingColorType == ColorDragType.ALPHA) {
                                                cs.setAlpha((int) (((mouseX - sX4) / sW) * 255));
                                            }

                                            prevSubY = subCenterY4;
                                            prevL1Y = subCenterY4;
                                            settingY += lineStep;
                                        }
                                    }
                                    prevTopExitY = prevL1Y;
                                } else {
                                    prevTopExitY = lastTopRowCenterY;
                                }
                            } else {
                                prevTopExitY = lastTopRowCenterY;
                            }
                        }
                    }
                }
            }
        }

        RenderUtil.disableScissor();
    }

    public static class WindowClickResult {
        public enum Type { NONE, CLOSE_WINDOW, DRAG_TAB, CONSUMED }
        public static final WindowClickResult NONE = new WindowClickResult(Type.NONE);
        public static final WindowClickResult CLOSE_WINDOW = new WindowClickResult(Type.CLOSE_WINDOW);
        public static final WindowClickResult CONSUMED = new WindowClickResult(Type.CONSUMED);

        public final Type type;
        public final TabInfo tab;
        public final int tabIndex;
        public final float tabOffsetX;

        public WindowClickResult(Type type) {
            this(type, null, -1, 0);
        }

        public WindowClickResult(Type type, TabInfo tab, int tabIndex, float tabOffsetX) {
            this.type = type;
            this.tab = tab;
            this.tabIndex = tabIndex;
            this.tabOffsetX = tabOffsetX;
        }
    }

    public WindowClickResult mouseClickedMinimized(int mouseX, int mouseY, int mouseButton, int minIndex) {
        float curX = getMinimizedX(minIndex);
        float curY = getMinimizedY(minIndex);
        float curW = MINIMIZED_WIDTH;
        float curH = MINIMIZED_HEIGHT;

        float btnW = 46.0f;
        float closeX = curX + curW - btnW;
        float maxX = closeX - btnW;
        float restoreX = maxX - btnW;

        if (mouseButton == 0) {
            if (RenderUtil.isHovered(mouseX, mouseY, closeX, curY, btnW, curH)) {
                return WindowClickResult.CLOSE_WINDOW;
            }
            if (RenderUtil.isHovered(mouseX, mouseY, maxX, curY, btnW, curH)) {
                isMinimized = false;
                isMaximized = true;
                return WindowClickResult.CONSUMED;
            }
            if (RenderUtil.isHovered(mouseX, mouseY, restoreX, curY, btnW, curH)) {
                isMinimized = false;
                isMaximized = false;
                return WindowClickResult.CONSUMED;
            }
            if (RenderUtil.isHovered(mouseX, mouseY, curX, curY, curW, curH)) {
                isMinimized = false;
                return WindowClickResult.CONSUMED;
            }
        }
        return WindowClickResult.CONSUMED;
    }

    public WindowClickResult mouseClicked(int mouseX, int mouseY, int mouseButton) {
        Minecraft mc = Minecraft.getMinecraft();
        float curX = isMaximized ? 0 : posX;
        float curY = isMaximized ? 0 : posY;
        float curW = isMaximized ? mc.displayWidth : windowWidth;
        float curH = isMaximized ? mc.displayHeight : windowHeight;
        float titlebarH = 40;

        float controlW = 46;
        float closeBtnX = curX + curW - controlW;
        float maxBtnX = closeBtnX - controlW;
        float minBtnX = maxBtnX - controlW;
        float tabsViewportW = curW - (controlW * 3);

        // 1. New Tab 下拉菜单拦截
        if (showNewTabMenu) {
            if (RenderUtil.isHovered(mouseX, mouseY, newTabMenuX, newTabMenuY, newTabMenuW, getNewTabMenuH())) {
                if (mouseButton == 0) {
                    float itemH = 32.0f;
                    float itemY = newTabMenuY;
                    for (TabPreset preset : AVAILABLE_PRESETS) {
                        if (RenderUtil.isHovered(mouseX, mouseY, newTabMenuX, itemY, newTabMenuW, itemH)) {
                            TabInfo newTab = TabInfo.createPreset(preset.type);
                            openTabs.add(newTab);
                            activeTabIndex = openTabs.size() - 1;
                            showNewTabMenu = false;
                            return WindowClickResult.CONSUMED;
                        }
                        itemY += itemH;
                    }
                }
                return WindowClickResult.CONSUMED;
            } else {
                showNewTabMenu = false;
                if (!RenderUtil.isHovered(mouseX, mouseY, curX, curY, curW, titlebarH)) {
                    return WindowClickResult.CONSUMED;
                }
            }
        }

        if (mouseButton == 0) {
            // 2. 窗口缩放边缘点击拦截
            if (!isMaximized) {
                int resizeDir = getResizeDirection(mouseX, mouseY);
                if (resizeDir != RESIZE_NONE) {
                    resizing = true;
                    currentResizeDir = resizeDir;
                    resizeMouseStartX = mouseX;
                    resizeMouseStartY = mouseY;
                    resizeStartPosX = posX;
                    resizeStartPosY = posY;
                    resizeStartW = windowWidth;
                    resizeStartH = windowHeight;
                    return WindowClickResult.CONSUMED;
                }
            }

            // 3. 窗口控制按钮点击
            if (RenderUtil.isHovered(mouseX, mouseY, closeBtnX, curY, controlW, titlebarH)) {
                return WindowClickResult.CLOSE_WINDOW;
            }
            if (RenderUtil.isHovered(mouseX, mouseY, maxBtnX, curY, controlW, titlebarH)) {
                isMaximized = !isMaximized;
                return WindowClickResult.CONSUMED;
            }
            if (RenderUtil.isHovered(mouseX, mouseY, minBtnX, curY, controlW, titlebarH)) {
                isMinimized = true;
                return WindowClickResult.CONSUMED;
            }

            // 4. Tab 选项卡及关闭小叉点击
            if (mouseX >= curX && mouseX < curX + tabsViewportW) {
                float tabX = curX + tabScrollX;
                for (int i = 0; i < openTabs.size(); i++) {
                    TabInfo tab = openTabs.get(i);
                    float tabW = getTabWidth(tab.getTitle());

                    // 关闭小叉
                    float closeX = tabX + tabW - 18;
                    if (RenderUtil.isHovered(mouseX, mouseY, closeX - 4, curY + 10, 18, 18)) {
                        openTabs.remove(i);
                        if (openTabs.isEmpty()) {
                            return WindowClickResult.CLOSE_WINDOW;
                        }
                        activeTabIndex = Math.max(0, Math.min(openTabs.size() - 1, activeTabIndex));
                        return WindowClickResult.CONSUMED;
                    }

                    // 点击 Tab 项 -> 激活并准备长按脱出/拖拽
                    if (RenderUtil.isHovered(mouseX, mouseY, tabX, curY, tabW, titlebarH)) {
                        activeTabIndex = i;
                        return new WindowClickResult(WindowClickResult.Type.DRAG_TAB, tab, i, mouseX - tabX);
                    }
                    tabX += tabW;
                }

                // 点击 '+' 按钮
                if (RenderUtil.isHovered(mouseX, mouseY, tabX, curY, 40, titlebarH)) {
                    showNewTabMenu = !showNewTabMenu;
                    return WindowClickResult.CONSUMED;
                }
            }

            // 5. Titlebar 空白区域拖拽
            if (RenderUtil.isHovered(mouseX, mouseY, curX, curY, curW - (controlW * 3), titlebarH)) {
                if (!isMaximized) {
                    dragging = true;
                    dragX = mouseX - posX;
                    dragY = mouseY - posY;
                }
                return WindowClickResult.CONSUMED;
            }
        } else if (mouseButton == 1) {
            if (RenderUtil.isHovered(mouseX, mouseY, curX, curY, curW - (controlW * 3), titlebarH)) {
                showNewTabMenu = true;
                return WindowClickResult.CONSUMED;
            }
        }

        // 6. 内容面板交互
        TabInfo activeTab = getActiveTab();
        if (activeTab != null && (activeTab.isModulesTab() || activeTab.getCategory() != null)) {
            Category cat = activeTab.getCategory();
            float sidebarW = 240;
            float bodyY = curY + titlebarH;
            float bodyH = curH - titlebarH - 24;

            if (RenderUtil.isHovered(mouseX, mouseY, curX, bodyY, sidebarW, bodyH)) {
                if (cat == Category.THEME) {
                    float itemY = bodyY + 14 + activeTab.getModuleListScrollY();
                    if (RenderUtil.isHovered(mouseX, mouseY, curX + 6, itemY - 2, sidebarW - 12, 24)) {
                        activeTab.setSelectedThemeItem("Font");
                        return WindowClickResult.CONSUMED;
                    }
                } else {
                    List<Register> mods = activeTab.isModulesTab()
                            ? Register.getAlphabeticalModules()
                            : (cat != null ? cat.getModules() : Collections.emptyList());
                    float itemY = bodyY + 14 + activeTab.getModuleListScrollY();

                    for (Register m : mods) {
                        if (RenderUtil.isHovered(mouseX, mouseY, curX + 6, itemY - 2, sidebarW - 12, 24)) {
                            if (mouseButton == 0) {
                                m.toggle();
                            } else if (mouseButton == 1) {
                                activeTab.setSelectedModule(m);
                                activeTab.setBindingModule(null);
                            }
                            return WindowClickResult.CONSUMED;
                        }
                        itemY += 28;
                    }
                }
            }

            float rightX = curX + sidebarW + 24;
            if (cat == Category.THEME) {
                if ("Font".equalsIgnoreCase(activeTab.getSelectedThemeItem()) && RenderUtil.isHovered(mouseX, mouseY, rightX, bodyY, curW - sidebarW - 24, bodyH)) {
                    return mouseClickedThemeFont(activeTab, mouseX, mouseY, mouseButton, curX, curW, rightX, bodyY);
                }
                return WindowClickResult.CONSUMED;
            }

            Register selectedModule = activeTab.getSelectedModule();
            if (selectedModule != null && RenderUtil.isHovered(mouseX, mouseY, rightX, bodyY, curW - sidebarW - 24, bodyH)) {
                float modTitleY = bodyY + 20;
                float metaY = modTitleY + 30;

                float metaGap = getStringWidth2x("    ");
                float curMetaX = rightX;
                float curMetaY = metaY;
                float featureStartX = rightX;
                float maxRightX = curX + curW - 20;

                boolean isBinding = (activeTab.getBindingModule() == selectedModule);
                String keyLabel = "Key: ";
                String keyVal = isBinding ? "..." : (selectedModule.getKeyCode() == 0 ? "None" : Keyboard.getKeyName(selectedModule.getKeyCode()));
                float keyLabelW = getStringWidth2x(keyLabel);
                float keyValW = getStringWidth2x(keyVal);
                float keyItemW = keyLabelW + keyValW;

                if (mouseButton == 0 && RenderUtil.isHovered(mouseX, mouseY, rightX, metaY - 2, keyItemW + 4, 18)) {
                    activeTab.setBindingModule(isBinding ? null : selectedModule);
                    return WindowClickResult.CONSUMED;
                }

                featureStartX = rightX + keyItemW + metaGap;
                curMetaX = featureStartX;

                for (CheckBoxSetting opt : selectedModule.getMetaOptions()) {
                    String text = opt.getDisplayText();
                    float totalW = getStringWidth2x(text);

                    if (curMetaX + totalW > maxRightX && curMetaX > featureStartX) {
                        curMetaY += 22.0f;
                        curMetaX = featureStartX;
                    }

                    if (mouseButton == 0 && RenderUtil.isHovered(mouseX, mouseY, curMetaX - 2, curMetaY - 2, totalW + 4, 18)) {
                        opt.toggle();
                        return WindowClickResult.CONSUMED;
                    }

                    curMetaX += totalW + metaGap;
                }

                float settingY = curMetaY + 30;
                float rowH = 22.0f;
                float lineGap = 10.0f;
                float lineStep = rowH + lineGap;
                float indentStep = getStringWidth2x("    ");
                float sW = 200.0f;
                float sH = 22.0f;

                List<String> groupNames = selectedModule.getGroupNames();
                int totalGroups = groupNames.size();
                for (int gi = 0; gi < totalGroups; gi++) {
                    String groupName = groupNames.get(gi);
                    boolean isCollapsed = activeTab.getCollapsedGroups().getOrDefault(groupName, false);

                    if (mouseButton == 0 && RenderUtil.isHovered(mouseX, mouseY, rightX, settingY, 220, (int) rowH)) {
                        activeTab.getCollapsedGroups().put(groupName, !isCollapsed);
                        return WindowClickResult.CONSUMED;
                    }
                    settingY += lineStep;

                    if (!isCollapsed) {
                        List<Setting<?>> topSettings = selectedModule.getTopSettingsByGroup(groupName);
                        if (topSettings != null) {
                            int topCount = topSettings.size();
                            for (int ti = 0; ti < topCount; ti++) {
                                Setting<?> s = topSettings.get(ti);
                                float itemX = rightX + indentStep;

                                if (s instanceof CheckBoxSetting) {
                                    CheckBoxSetting bs = (CheckBoxSetting) s;
                                    if (mouseButton == 0 && RenderUtil.isHovered(mouseX, mouseY, itemX, settingY, 260, (int) rowH)) {
                                        bs.toggle();
                                        return WindowClickResult.CONSUMED;
                                    }
                                    settingY += lineStep;
                                } else if (s instanceof NumberSetting) {
                                    NumberSetting ns = (NumberSetting) s;
                                    String label = ns.getName() + ": ";
                                    float labelW = getStringWidth2x(label);
                                    float sX = itemX + labelW;

                                    if (mouseButton == 0 && RenderUtil.isHovered(mouseX, mouseY, sX, settingY, sW, (int) rowH)) {
                                        draggingSlider = ns;
                                        double v = ns.getMin() + Math.max(0, Math.min(1, (mouseX - sX) / sW)) * (ns.getMax() - ns.getMin());
                                        ns.setValue(v);
                                        return WindowClickResult.CONSUMED;
                                    }
                                    settingY += lineStep;
                                } else if (s instanceof ModeSetting) {
                                    ModeSetting ms = (ModeSetting) s;
                                    String prefix = ms.getName() + ": ";
                                    float prefixW = getStringWidth2x(prefix);
                                    float mCurX = itemX + prefixW;
                                    float mCurY = settingY;
                                    float wrapIndent = itemX + prefixW;

                                    List<String> modes = ms.getModes();
                                    if (modes != null) {
                                        float spaceW = getStringWidth2x(" ");
                                        float commaW = getStringWidth2x(",");

                                        for (int mi = 0; mi < modes.size(); mi++) {
                                            String mode = modes.get(mi);
                                            boolean isLast = (mi == modes.size() - 1);
                                            float modeW = getStringWidth2x(mode);
                                            float itemBlockW = modeW + (isLast ? 0 : commaW);

                                            if (mCurX + itemBlockW > maxRightX && mCurX > wrapIndent) {
                                                mCurX = wrapIndent;
                                                mCurY += lineStep;
                                            }

                                            if (mouseButton == 0 && RenderUtil.isHovered(mouseX, mouseY, mCurX - 2, mCurY, modeW + 4, (int) rowH)) {
                                                ms.setValue(mode);
                                                return WindowClickResult.CONSUMED;
                                            }

                                            mCurX += itemBlockW + spaceW;
                                        }
                                    }
                                    settingY = mCurY + lineStep;
                                }

                                List<Setting<?>> subSettings = selectedModule.getChildrenSettings(s);
                                if (subSettings != null && !subSettings.isEmpty()) {
                                    boolean showSubs = !(s instanceof CheckBoxSetting) || ((CheckBoxSetting) s).getValue();
                                    if (showSubs) {
                                        int subCount = subSettings.size();
                                        for (int subI = 0; subI < subCount; subI++) {
                                            Setting<?> sub = subSettings.get(subI);
                                            float subItemX = itemX + indentStep;

                                            if (sub instanceof CheckBoxSetting) {
                                                CheckBoxSetting bs = (CheckBoxSetting) sub;
                                                if (mouseButton == 0 && RenderUtil.isHovered(mouseX, mouseY, subItemX, settingY, 260, (int) rowH)) {
                                                    bs.toggle();
                                                    return WindowClickResult.CONSUMED;
                                                }
                                                settingY += lineStep;
                                            } else if (sub instanceof NumberSetting) {
                                                NumberSetting ns = (NumberSetting) sub;
                                                String label = ns.getName() + ": ";
                                                float labelW = getStringWidth2x(label);
                                                float sX = subItemX + labelW;

                                                if (mouseButton == 0 && RenderUtil.isHovered(mouseX, mouseY, sX, settingY, sW, (int) rowH)) {
                                                    draggingSlider = ns;
                                                    double v = ns.getMin() + Math.max(0, Math.min(1, (mouseX - sX) / sW)) * (ns.getMax() - ns.getMin());
                                                    ns.setValue(v);
                                                    return WindowClickResult.CONSUMED;
                                                }
                                                settingY += lineStep;
                                            } else if (sub instanceof ModeSetting) {
                                                ModeSetting ms = (ModeSetting) sub;
                                                String prefix = ms.getName() + ": ";
                                                float prefixW = getStringWidth2x(prefix);
                                                float mCurX = subItemX + prefixW;
                                                float mCurY = settingY;
                                                float wrapIndent = subItemX + prefixW;

                                                List<String> modes = ms.getModes();
                                                if (modes != null) {
                                                    float spaceW = getStringWidth2x(" ");
                                                    float commaW = getStringWidth2x(",");

                                                    for (int mi = 0; mi < modes.size(); mi++) {
                                                        String mode = modes.get(mi);
                                                        boolean isLast = (mi == modes.size() - 1);
                                                        float modeW = getStringWidth2x(mode);
                                                        float itemBlockW = modeW + (isLast ? 0 : commaW);

                                                        if (mCurX + itemBlockW > maxRightX && mCurX > wrapIndent) {
                                                            mCurX = wrapIndent;
                                                            mCurY += lineStep;
                                                        }

                                                        if (mouseButton == 0 && RenderUtil.isHovered(mouseX, mouseY, mCurX - 2, mCurY, modeW + 4, (int) rowH)) {
                                                            ms.setValue(mode);
                                                            return WindowClickResult.CONSUMED;
                                                        }

                                                        mCurX += itemBlockW + spaceW;
                                                    }
                                                }
                                                settingY = mCurY + lineStep;
                                            } else if (sub instanceof ColorSetting) {
                                                ColorSetting cs = (ColorSetting) sub;

                                                float sX1 = subItemX;
                                                float togX1 = sX1 + sW + 14.0f;
                                                if (mouseButton == 0 && RenderUtil.isHovered(mouseX, mouseY, sX1, settingY, sW, (int) rowH)) {
                                                    draggingColorSetting = cs;
                                                    draggingColorType = ColorDragType.HUE;
                                                    cs.setHue((mouseX - sX1) / sW);
                                                    return WindowClickResult.CONSUMED;
                                                }
                                                if (mouseButton == 0 && RenderUtil.isHovered(mouseX, mouseY, togX1 - 2, settingY, 140, (int) rowH)) {
                                                    cs.rainbow.toggle();
                                                    return WindowClickResult.CONSUMED;
                                                }
                                                settingY += lineStep;

                                                float sX2 = subItemX;
                                                float togX2 = sX2 + sW + 14.0f;
                                                if (mouseButton == 0 && RenderUtil.isHovered(mouseX, mouseY, sX2, settingY, sW, (int) rowH)) {
                                                    draggingColorSetting = cs;
                                                    draggingColorType = ColorDragType.SATURATION;
                                                    cs.setSaturation((mouseX - sX2) / sW);
                                                    return WindowClickResult.CONSUMED;
                                                }
                                                if (mouseButton == 0 && RenderUtil.isHovered(mouseX, mouseY, togX2 - 2, settingY, 140, (int) rowH)) {
                                                    cs.fade.toggle();
                                                    return WindowClickResult.CONSUMED;
                                                }
                                                settingY += lineStep;

                                                float sX3 = subItemX;
                                                float togX3 = sX3 + sW + 14.0f;
                                                if (mouseButton == 0 && RenderUtil.isHovered(mouseX, mouseY, sX3, settingY, sW, (int) rowH)) {
                                                    draggingColorSetting = cs;
                                                    draggingColorType = ColorDragType.BRIGHTNESS;
                                                    cs.setBrightness((mouseX - sX3) / sW);
                                                    return WindowClickResult.CONSUMED;
                                                }
                                                if (mouseButton == 0 && RenderUtil.isHovered(mouseX, mouseY, togX3 - 2, settingY, 140, (int) rowH)) {
                                                    cs.astolfo.toggle();
                                                    return WindowClickResult.CONSUMED;
                                                }
                                                settingY += lineStep;

                                                float sX4 = subItemX;
                                                float togX4 = sX4 + sW + 14.0f;
                                                if (mouseButton == 0 && RenderUtil.isHovered(mouseX, mouseY, sX4, settingY, sW, (int) rowH)) {
                                                    draggingColorSetting = cs;
                                                    draggingColorType = ColorDragType.ALPHA;
                                                    cs.setAlpha((int) (((mouseX - sX4) / sW) * 255));
                                                    return WindowClickResult.CONSUMED;
                                                }
                                                if (mouseButton == 0 && RenderUtil.isHovered(mouseX, mouseY, togX4 - 2, settingY, 140, (int) rowH)) {
                                                    cs.clientColor.toggle();
                                                    return WindowClickResult.CONSUMED;
                                                }
                                                settingY += lineStep;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return WindowClickResult.CONSUMED;
    }

    public void mouseReleased(int mouseX, int mouseY, int state) {
        dragging = false;
        resizing = false;
        draggingSlider = null;
        draggingColorSetting = null;
        draggingColorType = null;
    }

    public void handleMouseWheel(int mouseX, int mouseY, int dWheel) {
        if (dWheel == 0) return;
        if (isMenuHovered(mouseX, mouseY)) return;

        Minecraft mc = Minecraft.getMinecraft();
        float curX = isMaximized ? 0 : posX;
        float curY = isMaximized ? 0 : posY;
        float curW = isMaximized ? mc.displayWidth : windowWidth;
        float titlebarH = 40;
        float controlW = 46;
        float tabsViewportW = curW - (controlW * 3);

        if (mouseY >= curY && mouseY <= curY + titlebarH && mouseX >= curX && mouseX <= curX + tabsViewportW) {
            if (dWheel > 0) tabScrollX += 36;
            else tabScrollX -= 36;
            tabScrollX = Math.min(0, Math.max(-maxTabScroll, tabScrollX));
        } else {
            TabInfo activeTab = getActiveTab();
            if (activeTab != null) {
                if (!activeTab.isModulesTab() && activeTab.getCategory() == null) {
                    if (dWheel > 0) activeTab.setTerminalScrollY(activeTab.getTerminalScrollY() + 28);
                    else activeTab.setTerminalScrollY(activeTab.getTerminalScrollY() - 28);
                    activeTab.setTerminalScrollY(Math.min(0, Math.max(-activeTab.getMaxTerminalScroll(), activeTab.getTerminalScrollY())));
                } else {
                    if (dWheel > 0) activeTab.setModuleListScrollY(activeTab.getModuleListScrollY() + 28);
                    else activeTab.setModuleListScrollY(activeTab.getModuleListScrollY() - 28);
                    activeTab.setModuleListScrollY(Math.min(0, activeTab.getModuleListScrollY()));
                }
            }
        }
    }

    public void executeCommand(TabInfo tab, String rawCmd) {
        String clean = rawCmd.trim();
        if (clean.isEmpty()) return;

        tab.getCommandHistory().add(clean);
        tab.setHistoryPointer(tab.getCommandHistory().size());

        tab.getTerminalOutput().add(getPrompt() + clean);

        String[] parts = clean.split(" ");
        String cmd = parts[0].toLowerCase();

        if (cmd.equals("cls") || cmd.equals("clear")) {
            tab.getTerminalOutput().clear();
        } else if (cmd.equals("help")) {
            tab.getTerminalOutput().add("Rattix Console Commands:");
            tab.getTerminalOutput().add("  help                      - Show available commands");
            tab.getTerminalOutput().add("  list / modules            - List all modules and status");
            tab.getTerminalOutput().add("  toggle <module> / t <mod> - Toggle a module on/off");
            tab.getTerminalOutput().add("  bind <mod> <key>          - Bind key to module");
            tab.getTerminalOutput().add("  font [list | set <name>]  - View or configure typography fonts");
            tab.getTerminalOutput().add("  info <mod>                - View module settings");
            tab.getTerminalOutput().add("  ver                       - Display version");
            tab.getTerminalOutput().add("  date / time               - Print date / time");
            tab.getTerminalOutput().add("  echo <text>               - Print text");
            tab.getTerminalOutput().add("  cls / clear               - Clear terminal screen");
        } else if (cmd.equals("list") || cmd.equals("modules") || cmd.equals("mods")) {
            tab.getTerminalOutput().add("------- Modules List -------");
            for (Register m : Register.getRegisteredModules()) {
                String st = m.isEnabled() ? "\u00A7a[ENABLED]\u00A7r" : "\u00A78[DISABLED]\u00A7r";
                String key = (m.getKeyCode() == 0) ? "None" : Keyboard.getKeyName(m.getKeyCode());
                tab.getTerminalOutput().add("  " + String.format("%-18s", m.getName()) + " " + st + " (Key: " + key + ") - " + m.getDescription());
            }
        } else if (cmd.equals("toggle") || cmd.equals("t")) {
            if (parts.length > 1) {
                String name = parts[1];
                Register m = Register.getModuleByName(name);
                if (m != null) {
                    m.toggle();
                    tab.getTerminalOutput().add("\u00A7aModule '" + m.getName() + "' is now " + (m.isEnabled() ? "\u00A7aENABLED" : "\u00A7cDISABLED"));
                } else {
                    tab.getTerminalOutput().add("\u00A7cError: Module '" + name + "' not found.");
                }
            } else {
                tab.getTerminalOutput().add("\u00A7eUsage: toggle <module>");
            }
        } else if (cmd.equals("bind")) {
            if (parts.length > 2) {
                String name = parts[1];
                String keyStr = parts[2].toUpperCase();
                Register m = Register.getModuleByName(name);
                if (m != null) {
                    int key = Keyboard.getKeyIndex(keyStr);
                    m.setKeyCode(key);
                    tab.getTerminalOutput().add("\u00A7aBound '" + m.getName() + "' to key \u00A7f" + keyStr);
                } else {
                    tab.getTerminalOutput().add("\u00A7cError: Module '" + name + "' not found.");
                }
            } else {
                tab.getTerminalOutput().add("\u00A7eUsage: bind <module> <key>");
            }
        } else if (cmd.equals("font")) {
            if (parts.length > 1) {
                String subCmd = parts[1].toLowerCase();
                if (subCmd.equals("list") || subCmd.equals("ls")) {
                    tab.getTerminalOutput().add("------- Available Fonts -------");
                    String cur = FontUtil.getGlobalFontName();
                    tab.getTerminalOutput().add((cur.equalsIgnoreCase("Minecraft") ? " \u00A7a* " : "   ") + "Minecraft (Vanilla)");
                    for (File ttf : FileManager.getTtfFiles()) {
                        String fn = ttf.getName();
                        tab.getTerminalOutput().add((cur.equalsIgnoreCase(fn) ? " \u00A7a* " : "   ") + fn);
                    }
                } else if (subCmd.equals("set") && parts.length > 2) {
                    String targetFont = parts[2];
                    if (targetFont.equalsIgnoreCase("minecraft") || targetFont.equalsIgnoreCase("vanilla")) {
                        FontUtil.setSelectedFont("Minecraft");
                        tab.getTerminalOutput().add("\u00A7aFont changed to \u00A7fMinecraft");
                    } else {
                        boolean found = false;
                        for (File ttf : FileManager.getTtfFiles()) {
                            if (ttf.getName().equalsIgnoreCase(targetFont) || ttf.getName().toLowerCase().startsWith(targetFont.toLowerCase())) {
                                FontUtil.setSelectedFont(ttf.getName());
                                tab.getTerminalOutput().add("\u00A7aFont changed to \u00A7f" + ttf.getName());
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            tab.getTerminalOutput().add("\u00A7cError: Font '" + targetFont + "' not found in fonts directory.");
                        }
                    }
                } else {
                    tab.getTerminalOutput().add("\u00A7eUsage: font list | font set <name>");
                }
            } else {
                tab.getTerminalOutput().add("Current Font: \u00A7a" + FontUtil.getGlobalFontName() + "\u00A7r | Scale: \u00A7e" + FontUtil.getFontSettings().size.getValue() + "%");
                tab.getTerminalOutput().add("Use \u00A7efont list\u00A7r to view available fonts or \u00A7efont set <name>\u00A7r to switch.");
            }
        } else if (cmd.equals("ver")) {
            tab.getTerminalOutput().add("Microsoft Windows [Version 11.0.26100.3194]");
            tab.getTerminalOutput().add("Rattix Client [Version 1.0.0]");
        } else if (cmd.equals("date")) {
            tab.getTerminalOutput().add(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        } else if (cmd.equals("time")) {
            tab.getTerminalOutput().add(new SimpleDateFormat("HH:mm:ss").format(new Date()));
        } else if (cmd.startsWith("echo")) {
            tab.getTerminalOutput().add(clean.length() > 5 ? clean.substring(5) : "");
        } else {
            tab.getTerminalOutput().add("'" + clean + "' is not recognized as the name of a cmdlet, function, script file, or operable program.");
        }

        tab.setCurrentInput("");
        tab.setTerminalScrollY(-tab.getMaxTerminalScroll());
    }

    public boolean keyTyped(char typedChar, int keyCode) {
        TabInfo activeTab = getActiveTab();
        if (activeTab == null) return false;

        if (activeTab.getBindingModule() != null) {
            if (keyCode == Keyboard.KEY_ESCAPE || keyCode == Keyboard.KEY_DELETE) {
                activeTab.getBindingModule().setKeyCode(0);
            } else {
                activeTab.getBindingModule().setKeyCode(keyCode);
            }
            activeTab.setBindingModule(null);
            return true;
        }

        if (!activeTab.isModulesTab() && activeTab.getCategory() == null) {
            if (keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_NUMPADENTER) {
                executeCommand(activeTab, activeTab.getCurrentInput());
                return true;
            } else if (keyCode == Keyboard.KEY_BACK) {
                String input = activeTab.getCurrentInput();
                if (!input.isEmpty()) {
                    activeTab.setCurrentInput(input.substring(0, input.length() - 1));
                }
                return true;
            } else if (keyCode == Keyboard.KEY_UP) {
                List<String> hist = activeTab.getCommandHistory();
                int ptr = activeTab.getHistoryPointer();
                if (!hist.isEmpty() && ptr > 0) {
                    activeTab.setHistoryPointer(ptr - 1);
                    activeTab.setCurrentInput(hist.get(activeTab.getHistoryPointer()));
                }
                return true;
            } else if (keyCode == Keyboard.KEY_DOWN) {
                List<String> hist = activeTab.getCommandHistory();
                int ptr = activeTab.getHistoryPointer();
                if (!hist.isEmpty() && ptr < hist.size() - 1) {
                    activeTab.setHistoryPointer(ptr + 1);
                    activeTab.setCurrentInput(hist.get(activeTab.getHistoryPointer()));
                } else {
                    activeTab.setHistoryPointer(hist.size());
                    activeTab.setCurrentInput("");
                }
                return true;
            } else if (ChatAllowedCharacters.isAllowedCharacter(typedChar)) {
                activeTab.setCurrentInput(activeTab.getCurrentInput() + typedChar);
                return true;
            }
        }
        return false;
    }

    private WindowClickResult mouseClickedThemeFont(TabInfo activeTab, int mouseX, int mouseY, int mouseButton, float curX, float curW, float rightX, float bodyY) {
        float modTitleY = bodyY + 20;
        float metaY = modTitleY + 30;
        float maxRightX = curX + curW - 20;

        float settingY = metaY + 16;
        float rowH = 22.0f;
        float lineGap = 10.0f;
        float lineStep = rowH + lineGap;
        float indentStep = getStringWidth2x("    ");
        float sW = 200.0f;

        client.m1ck3y.rattix.modules.Font font = client.m1ck3y.rattix.modules.Font.getInstance();
        List<String> groupNames = font.getGroupNames();
        int totalGroups = groupNames.size();
        for (int gi = 0; gi < totalGroups; gi++) {
            String groupName = groupNames.get(gi);
            boolean isCollapsed = activeTab.getCollapsedGroups().getOrDefault(groupName, false);

            if (mouseButton == 0 && RenderUtil.isHovered(mouseX, mouseY, rightX, settingY, 220, (int) rowH)) {
                activeTab.getCollapsedGroups().put(groupName, !isCollapsed);
                return WindowClickResult.CONSUMED;
            }
            settingY += lineStep;

            if (!isCollapsed) {
                if ("Font Manager".equals(groupName)) {
                    List<File> ttfFiles = FileManager.getTtfFiles();
                    int childCount = 1 + (ttfFiles != null ? ttfFiles.size() : 0);
                    float itemX = rightX + indentStep;
                    for (int ci = 0; ci < childCount; ci++) {
                        if (mouseButton == 0 && RenderUtil.isHovered(mouseX, mouseY, itemX, settingY, 300, (int) rowH)) {
                            if (ci == 0) {
                                font.setSelectedFont("Minecraft");
                            } else {
                                font.setSelectedFont(ttfFiles.get(ci - 1).getName());
                            }
                            return WindowClickResult.CONSUMED;
                        }
                        settingY += lineStep;
                    }
                } else if ("Global Font".equals(groupName)) {
                    List<File> ttfFiles = FileManager.getTtfFiles();
                    float itemX = rightX + indentStep;
                    String prefix = "Font: ";
                    float prefixW = getStringWidth2x(prefix);
                    float mCurX = itemX + prefixW;
                    float mCurY = settingY;
                    float wrapIndent = itemX + prefixW;

                    int totalFontCount = 1 + (ttfFiles != null ? ttfFiles.size() : 0);
                    float spaceW = getStringWidth2x(" ");
                    float commaW = getStringWidth2x(",");

                    for (int mi = 0; mi < totalFontCount; mi++) {
                        String fName = (mi == 0) ? "Minecraft" : ttfFiles.get(mi - 1).getName();
                        boolean isLast = (mi == totalFontCount - 1);
                        float modeW = getStringWidth2x(fName);
                        float itemBlockW = modeW + (isLast ? 0 : commaW);

                        if (mCurX + itemBlockW > maxRightX && mCurX > wrapIndent) {
                            mCurX = wrapIndent;
                            mCurY += lineStep;
                        }

                        if (mouseButton == 0 && RenderUtil.isHovered(mouseX, mouseY, mCurX - 2, mCurY, modeW + 4, (int) rowH)) {
                            font.setSelectedFont(fName);
                            return WindowClickResult.CONSUMED;
                        }

                        mCurX += itemBlockW + spaceW;
                    }
                    settingY = mCurY + lineStep;
                } else {
                    List<Setting<?>> topSettings = font.getTopSettingsByGroup(groupName);
                    if (topSettings != null) {
                        int topCount = topSettings.size();
                        for (int ti = 0; ti < topCount; ti++) {
                            Setting<?> s = topSettings.get(ti);
                            float itemX = rightX + indentStep;

                            if (s instanceof CheckBoxSetting) {
                                CheckBoxSetting bs = (CheckBoxSetting) s;
                                if (mouseButton == 0 && RenderUtil.isHovered(mouseX, mouseY, itemX, settingY, 260, (int) rowH)) {
                                    bs.toggle();
                                    return WindowClickResult.CONSUMED;
                                }
                                settingY += lineStep;
                            } else if (s instanceof NumberSetting) {
                                NumberSetting ns = (NumberSetting) s;
                                String label = ns.getName() + ": ";
                                float labelW = getStringWidth2x(label);
                                float sX = itemX + labelW;

                                if (mouseButton == 0 && RenderUtil.isHovered(mouseX, mouseY, sX, settingY, sW, (int) rowH)) {
                                    draggingSlider = ns;
                                    double v = ns.getMin() + Math.max(0, Math.min(1, (mouseX - sX) / sW)) * (ns.getMax() - ns.getMin());
                                    ns.setValue(v);
                                    return WindowClickResult.CONSUMED;
                                }
                                settingY += lineStep;
                            } else if (s instanceof ModeSetting) {
                                ModeSetting ms = (ModeSetting) s;
                                String prefix = ms.getName() + ": ";
                                float prefixW = getStringWidth2x(prefix);
                                float mCurX = itemX + prefixW;
                                float mCurY = settingY;
                                float wrapIndent = itemX + prefixW;

                                List<String> modes = ms.getModes();
                                if (modes != null) {
                                    float spaceW = getStringWidth2x(" ");
                                    float commaW = getStringWidth2x(",");

                                    for (int mi = 0; mi < modes.size(); mi++) {
                                        String mode = modes.get(mi);
                                        boolean isLast = (mi == modes.size() - 1);
                                        float modeW = getStringWidth2x(mode);
                                        float itemBlockW = modeW + (isLast ? 0 : commaW);

                                        if (mCurX + itemBlockW > maxRightX && mCurX > wrapIndent) {
                                            mCurX = wrapIndent;
                                            mCurY += lineStep;
                                        }

                                        if (mouseButton == 0 && RenderUtil.isHovered(mouseX, mouseY, mCurX - 2, mCurY, modeW + 4, (int) rowH)) {
                                            ms.setValue(mode);
                                            return WindowClickResult.CONSUMED;
                                        }

                                        mCurX += itemBlockW + spaceW;
                                    }
                                }
                                settingY = mCurY + lineStep;
                            }

                            List<Setting<?>> subSettings = font.getChildrenSettings(s);
                            if (subSettings != null && !subSettings.isEmpty()) {
                                boolean showSubs = !(s instanceof CheckBoxSetting) || ((CheckBoxSetting) s).getValue();
                                if (showSubs) {
                                    int subCount = subSettings.size();
                                    for (int subI = 0; subI < subCount; subI++) {
                                        Setting<?> sub = subSettings.get(subI);
                                        if (sub instanceof ColorSetting && font.shadowMode.is("Vanilla")) {
                                            continue;
                                        }
                                        float subItemX = itemX + indentStep;

                                        if (sub instanceof CheckBoxSetting) {
                                            CheckBoxSetting bs = (CheckBoxSetting) sub;
                                            if (mouseButton == 0 && RenderUtil.isHovered(mouseX, mouseY, subItemX, settingY, 260, (int) rowH)) {
                                                bs.toggle();
                                                return WindowClickResult.CONSUMED;
                                            }
                                            settingY += lineStep;
                                        } else if (sub instanceof NumberSetting) {
                                            NumberSetting ns = (NumberSetting) sub;
                                            String label = ns.getName() + ": ";
                                            float labelW = getStringWidth2x(label);
                                            float sX = subItemX + labelW;

                                            if (mouseButton == 0 && RenderUtil.isHovered(mouseX, mouseY, sX, settingY, sW, (int) rowH)) {
                                                draggingSlider = ns;
                                                double v = ns.getMin() + Math.max(0, Math.min(1, (mouseX - sX) / sW)) * (ns.getMax() - ns.getMin());
                                                ns.setValue(v);
                                                return WindowClickResult.CONSUMED;
                                            }
                                            settingY += lineStep;
                                        } else if (sub instanceof ModeSetting) {
                                            ModeSetting ms = (ModeSetting) sub;
                                            String prefix = ms.getName() + ": ";
                                            float prefixW = getStringWidth2x(prefix);
                                            float mCurX = subItemX + prefixW;
                                            float mCurY = settingY;
                                            float wrapIndent = subItemX + prefixW;

                                            List<String> modes = ms.getModes();
                                            if (modes != null) {
                                                float spaceW = getStringWidth2x(" ");
                                                float commaW = getStringWidth2x(",");

                                                for (int mi = 0; mi < modes.size(); mi++) {
                                                    String mode = modes.get(mi);
                                                    boolean isLast = (mi == modes.size() - 1);
                                                    float modeW = getStringWidth2x(mode);
                                                    float itemBlockW = modeW + (isLast ? 0 : commaW);

                                                    if (mCurX + itemBlockW > maxRightX && mCurX > wrapIndent) {
                                                        mCurX = wrapIndent;
                                                        mCurY += lineStep;
                                                    }

                                                    if (mouseButton == 0 && RenderUtil.isHovered(mouseX, mouseY, mCurX - 2, mCurY, modeW + 4, (int) rowH)) {
                                                        ms.setValue(mode);
                                                        return WindowClickResult.CONSUMED;
                                                    }

                                                    mCurX += itemBlockW + spaceW;
                                                }
                                            }
                                            settingY = mCurY + lineStep;
                                        } else if (sub instanceof ColorSetting) {
                                            ColorSetting cs = (ColorSetting) sub;

                                            float sX1 = subItemX;
                                            float togX1 = sX1 + sW + 14.0f;
                                            if (mouseButton == 0 && RenderUtil.isHovered(mouseX, mouseY, sX1, settingY, sW, (int) rowH)) {
                                                draggingColorSetting = cs;
                                                draggingColorType = ColorDragType.HUE;
                                                cs.setHue((mouseX - sX1) / sW);
                                                return WindowClickResult.CONSUMED;
                                            }
                                            if (mouseButton == 0 && RenderUtil.isHovered(mouseX, mouseY, togX1 - 2, settingY, 140, (int) rowH)) {
                                                cs.rainbow.toggle();
                                                return WindowClickResult.CONSUMED;
                                            }
                                            settingY += lineStep;

                                            float sX2 = subItemX;
                                            float togX2 = sX2 + sW + 14.0f;
                                            if (mouseButton == 0 && RenderUtil.isHovered(mouseX, mouseY, sX2, settingY, sW, (int) rowH)) {
                                                draggingColorSetting = cs;
                                                draggingColorType = ColorDragType.SATURATION;
                                                cs.setSaturation((mouseX - sX2) / sW);
                                                return WindowClickResult.CONSUMED;
                                            }
                                            if (mouseButton == 0 && RenderUtil.isHovered(mouseX, mouseY, togX2 - 2, settingY, 140, (int) rowH)) {
                                                cs.fade.toggle();
                                                return WindowClickResult.CONSUMED;
                                            }
                                            settingY += lineStep;

                                            float sX3 = subItemX;
                                            float togX3 = sX3 + sW + 14.0f;
                                            if (mouseButton == 0 && RenderUtil.isHovered(mouseX, mouseY, sX3, settingY, sW, (int) rowH)) {
                                                draggingColorSetting = cs;
                                                draggingColorType = ColorDragType.BRIGHTNESS;
                                                cs.setBrightness((mouseX - sX3) / sW);
                                                return WindowClickResult.CONSUMED;
                                            }
                                            if (mouseButton == 0 && RenderUtil.isHovered(mouseX, mouseY, togX3 - 2, settingY, 140, (int) rowH)) {
                                                cs.astolfo.toggle();
                                                return WindowClickResult.CONSUMED;
                                            }
                                            settingY += lineStep;

                                            float sX4 = subItemX;
                                            float togX4 = sX4 + sW + 14.0f;
                                            if (mouseButton == 0 && RenderUtil.isHovered(mouseX, mouseY, sX4, settingY, sW, (int) rowH)) {
                                                draggingColorSetting = cs;
                                                draggingColorType = ColorDragType.ALPHA;
                                                cs.setAlpha((int) (((mouseX - sX4) / sW) * 255));
                                                return WindowClickResult.CONSUMED;
                                            }
                                            if (mouseButton == 0 && RenderUtil.isHovered(mouseX, mouseY, togX4 - 2, settingY, 140, (int) rowH)) {
                                                cs.clientColor.toggle();
                                                return WindowClickResult.CONSUMED;
                                            }
                                            settingY += lineStep;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return WindowClickResult.CONSUMED;
    }
}
