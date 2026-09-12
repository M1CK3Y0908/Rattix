package client.m1ck3y.rattix.module.clickgui;

import client.m1ck3y.rattix.config.ClickGuiConfig;
import client.m1ck3y.rattix.utils.CursorUtil;
import client.m1ck3y.rattix.utils.FontUtil;
import client.m1ck3y.rattix.utils.RenderUtil;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class ClickGuiScreen extends GuiScreen {
    private final List<ClickGuiWindow> windows = new ArrayList<>();

    // Tab Dragging & Tear-Off / Docking State
    private TabInfo dragCandidateTab = null;
    private ClickGuiWindow dragCandidateWindow = null;
    private int dragCandidateIndex = -1;
    private float dragCandidateOffsetX = 0;
    private float dragStartX = 0;
    private float dragStartY = 0;
    private boolean isDraggingTab = false;
    private ClickGuiWindow hoverTargetWindow = null;
    private int hoverInsertIndex = -1;

    public ClickGuiScreen() {
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);

        if (windows.isEmpty()) {
            List<ClickGuiWindow> loaded = ClickGuiConfig.load();
            if (loaded != null && !loaded.isEmpty()) {
                windows.addAll(loaded);
            } else {
                float defaultW = 960;
                float defaultH = 600;
                float posX = Math.max(0, (mc.displayWidth - defaultW) / 2.0f);
                float posY = Math.max(0, (mc.displayHeight - defaultH) / 2.0f);
                ClickGuiWindow defaultWindow = new ClickGuiWindow(
                        UUID.randomUUID().toString(),
                        posX, posY, defaultW, defaultH, false,
                        Collections.singletonList(TabInfo.createPreset("console")),
                        0
                );
                windows.add(defaultWindow);
            }
        }
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
        ClickGuiConfig.save(windows);
        CursorUtil.resetCursor();
    }

    private int getRawMouseX() {
        return Mouse.getX();
    }

    private int getRawMouseY() {
        return mc.displayHeight - Mouse.getY() - 1;
    }

    private int getCursorForDir(int dir) {
        switch (dir) {
            case ClickGuiWindow.RESIZE_N:
            case ClickGuiWindow.RESIZE_S:
                return CursorUtil.CURSOR_SIZE_V;
            case ClickGuiWindow.RESIZE_W:
            case ClickGuiWindow.RESIZE_E:
                return CursorUtil.CURSOR_SIZE_H;
            case ClickGuiWindow.RESIZE_NW:
            case ClickGuiWindow.RESIZE_SE:
                return CursorUtil.CURSOR_SIZE_NWSE;
            case ClickGuiWindow.RESIZE_NE:
            case ClickGuiWindow.RESIZE_SW:
                return CursorUtil.CURSOR_SIZE_NESW;
            default:
                return CursorUtil.CURSOR_ARROW;
        }
    }

    private ScaledResolution cachedSr = null;
    private int lastDisplayWidth = -1;
    private int lastDisplayHeight = -1;
    private int lastGuiScale = -1;

    private ScaledResolution getScaledResolution() {
        int dw = mc.displayWidth;
        int dh = mc.displayHeight;
        int gs = mc.gameSettings.guiScale;
        if (cachedSr == null || dw != lastDisplayWidth || dh != lastDisplayHeight || gs != lastGuiScale) {
            cachedSr = new ScaledResolution(mc);
            lastDisplayWidth = dw;
            lastDisplayHeight = dh;
            lastGuiScale = gs;
        }
        return cachedSr;
    }

    @Override
    public void drawScreen(int scaledMouseX, int scaledMouseY, float partialTicks) {
        int mouseX = getRawMouseX();
        int mouseY = getRawMouseY();

        ScaledResolution sr = getScaledResolution();
        float scale = sr.getScaleFactor();

        GlStateManager.pushMatrix();
        GlStateManager.scale(1.0f / scale, 1.0f / scale, 1.0f / scale);

        // 1. 处理 Tab 拖拽、脱出 (Tear-off) 与合并逻辑
        updateTabDragState(mouseX, mouseY);

        // 2. 绘制所有窗口 (按 Z-Order 从底到顶)
        int minIndexCounter = 0;
        for (int i = 0; i < windows.size(); i++) {
            ClickGuiWindow win = windows.get(i);
            boolean isTop = (i == windows.size() - 1);
            int mIdx = win.isMinimized() ? minIndexCounter++ : -1;
            win.drawWindow(mouseX, mouseY, isTop, dragCandidateTab, hoverTargetWindow, hoverInsertIndex, mIdx);
        }

        // 3. 绘制正在脱出/拖拽的浮动 Tab 预览
        if (isDraggingTab && dragCandidateTab != null && (hoverTargetWindow == null || hoverTargetWindow != dragCandidateWindow)) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(0, 0, 400.0f);
            float ghostW = ClickGuiWindow.getTabWidth(dragCandidateTab.getTitle());
            float ghostX = mouseX - dragCandidateOffsetX;
            float ghostY = mouseY - 20;
            RenderUtil.drawDropShadow(ghostX, ghostY, ghostW, 40, 5, 12, 0x75000000);
            RenderUtil.drawRoundedRect(ghostX, ghostY, ghostW, 40, 5, 0xE5202020);
            RenderUtil.drawRoundedOutline(ghostX, ghostY, ghostW, 40, 5, 1.2f, 0xFF76B9ED);
            float textOff = ghostX + 11;
            if (dragCandidateTab.getCategory() == null) {
                RenderUtil.drawTerminalIcon(textOff, ghostY + 8.0f, 20.0f, 0xFF76B9ED);
                textOff += 27;
            } else {
                RenderUtil.drawCategoryIcon(dragCandidateTab.getCategory(), textOff, ghostY + 8.0f, 20.0f, 0xFF76B9ED);
                textOff += 27;
            }
            FontUtil.drawString2x(dragCandidateTab.getTitle(), textOff, ghostY + 11, 0xFFFFFFFF);
            GlStateManager.popMatrix();
        }

        // 4. 更新系统双箭头光标
        updateCursorState(mouseX, mouseY);

        GlStateManager.popMatrix();
        super.drawScreen(scaledMouseX, scaledMouseY, partialTicks);
    }

    private void updateTabDragState(int mouseX, int mouseY) {
        if (dragCandidateTab != null) {
            if (!Mouse.isButtonDown(0)) {
                // 鼠标已松开 -> 执行放下/合并逻辑
                handleTabDrop(mouseX, mouseY);
            } else {
                float dx = mouseX - dragStartX;
                float dy = mouseY - dragStartY;
                if (!isDraggingTab && (Math.abs(dx) > 6 || Math.abs(dy) > 6)) {
                    isDraggingTab = true;
                }

                if (isDraggingTab) {
                    // 检查是否脱出为新独立窗口
                    if (dragCandidateWindow != null && dragCandidateWindow.getOpenTabs().size() > 1) {
                        if (!dragCandidateWindow.isInsideTitlebar(mouseX, mouseY)) {
                            // 脱出 (Tear-off) 为新窗口
                            dragCandidateWindow.getOpenTabs().remove(dragCandidateTab);
                            dragCandidateWindow.setActiveTabIndex(Math.max(0, Math.min(dragCandidateWindow.getOpenTabs().size() - 1, dragCandidateWindow.getActiveTabIndex())));

                            float newW = dragCandidateWindow.getWindowWidth();
                            float newH = dragCandidateWindow.getWindowHeight();
                            float newX = mouseX - dragCandidateOffsetX - 10;
                            float newY = mouseY - 15;
                            ClickGuiWindow newWin = new ClickGuiWindow(
                                    UUID.randomUUID().toString(),
                                    newX, newY, newW, newH, false,
                                    new ArrayList<>(Collections.singletonList(dragCandidateTab)),
                                    0
                            );
                            newWin.setDragging(true);
                            newWin.setDragOffset(mouseX - newX, mouseY - newY);

                            windows.add(newWin);
                            dragCandidateWindow = newWin;
                            dragCandidateIndex = 0;
                        }
                    }

                    // 检查是否悬停在其他窗口的 Tab 栏上方准备合并
                    hoverTargetWindow = null;
                    hoverInsertIndex = -1;
                    for (int i = windows.size() - 1; i >= 0; i--) {
                        ClickGuiWindow win = windows.get(i);
                        int ins = win.getTabInsertionIndex(mouseX, mouseY);
                        if (ins >= 0) {
                            hoverTargetWindow = win;
                            hoverInsertIndex = ins;
                            break;
                        }
                    }
                }
            }
        }
    }

    private void handleTabDrop(int mouseX, int mouseY) {
        if (isDraggingTab && dragCandidateTab != null && dragCandidateWindow != null) {
            if (hoverTargetWindow != null) {
                if (hoverTargetWindow == dragCandidateWindow) {
                    // 同窗口内调整顺序
                    if (hoverInsertIndex >= 0 && hoverInsertIndex != dragCandidateIndex) {
                        dragCandidateWindow.getOpenTabs().remove(dragCandidateTab);
                        int ins = Math.max(0, Math.min(hoverInsertIndex, dragCandidateWindow.getOpenTabs().size()));
                        dragCandidateWindow.getOpenTabs().add(ins, dragCandidateTab);
                        dragCandidateWindow.setActiveTabIndex(ins);
                    }
                } else {
                    // 跨窗口合并
                    dragCandidateWindow.getOpenTabs().remove(dragCandidateTab);
                    int ins = Math.max(0, Math.min(hoverInsertIndex, hoverTargetWindow.getOpenTabs().size()));
                    hoverTargetWindow.getOpenTabs().add(ins, dragCandidateTab);
                    hoverTargetWindow.setActiveTabIndex(ins);

                    // 提升目标窗口到最前
                    windows.remove(hoverTargetWindow);
                    windows.add(hoverTargetWindow);

                    // 若原窗口已无标签，销毁原窗口
                    if (dragCandidateWindow.getOpenTabs().isEmpty()) {
                        windows.remove(dragCandidateWindow);
                    } else {
                        dragCandidateWindow.setActiveTabIndex(Math.max(0, Math.min(dragCandidateWindow.getOpenTabs().size() - 1, dragCandidateWindow.getActiveTabIndex())));
                    }
                }
            }
        }

        dragCandidateTab = null;
        dragCandidateWindow = null;
        dragCandidateIndex = -1;
        isDraggingTab = false;
        hoverTargetWindow = null;
        hoverInsertIndex = -1;
    }

    private void updateCursorState(int mouseX, int mouseY) {
        if (windows.isEmpty()) {
            CursorUtil.resetCursor();
            return;
        }

        // 1. 若正在拖拽 Tab 或有窗口正在拖拽位置，重置光标
        if (isDraggingTab) {
            CursorUtil.resetCursor();
            return;
        }
        for (ClickGuiWindow win : windows) {
            if (win.isDragging()) {
                CursorUtil.resetCursor();
                return;
            }
        }

        // 2. 检查是否有窗口正在缩放
        for (ClickGuiWindow win : windows) {
            if (win.isResizing()) {
                int dir = win.getCurrentResizeDir();
                CursorUtil.setCursor(getCursorForDir(dir));
                return;
            }
        }

        // 3. 从顶层到底层遍历所有窗口，检查边缘缩放指针（即使不在最前方，只要边缘未被上方窗口遮挡即可正常触发）
        for (int i = windows.size() - 1; i >= 0; i--) {
            ClickGuiWindow win = windows.get(i);
            int mIdx = getMinimizedIndex(win);

            if (win.isMinimized()) {
                if (win.isInsideWindow(mouseX, mouseY, mIdx)) {
                    // 悬停在最小化磁贴上方，磁贴遮挡了底层窗口
                    break;
                }
                continue;
            }

            // 检查鼠标是否在该窗口的缩放边缘上
            int hoverDir = win.getResizeDirection(mouseX, mouseY);
            if (hoverDir != ClickGuiWindow.RESIZE_NONE) {
                CursorUtil.setCursor(getCursorForDir(hoverDir));
                return;
            }

            // 若鼠标在该窗口内部（非边缘），因该顶层窗口是不透明的，会遮挡底层窗口的缩放边缘
            if (win.isInsideWindow(mouseX, mouseY, mIdx)) {
                break;
            }
        }

        CursorUtil.resetCursor();
    }

    private int getMinimizedIndex(ClickGuiWindow target) {
        if (!target.isMinimized()) return -1;
        int idx = 0;
        for (ClickGuiWindow win : windows) {
            if (win == target) return idx;
            if (win.isMinimized()) idx++;
        }
        return idx;
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int dWheel = Mouse.getEventDWheel();
        if (dWheel != 0 && !windows.isEmpty()) {
            int mouseX = getRawMouseX();
            int mouseY = getRawMouseY();
            // 从上到下传递滚轮事件
            for (int i = windows.size() - 1; i >= 0; i--) {
                ClickGuiWindow win = windows.get(i);
                int mIdx = getMinimizedIndex(win);
                if (win.isInsideWindow(mouseX, mouseY, mIdx)) {
                    if (!win.isMinimized()) {
                        win.handleMouseWheel(mouseX, mouseY, dWheel);
                    }
                    break;
                }
            }
        }
    }

    @Override
    protected void mouseClicked(int scaledMouseX, int scaledMouseY, int mouseButton) throws IOException {
        int mouseX = getRawMouseX();
        int mouseY = getRawMouseY();

        // 优先检查是否有悬浮菜单打开
        for (int i = windows.size() - 1; i >= 0; i--) {
            ClickGuiWindow win = windows.get(i);
            if (win.isMenuHovered(mouseX, mouseY)) {
                ClickGuiWindow.WindowClickResult res = win.mouseClicked(mouseX, mouseY, mouseButton);
                if (res.type == ClickGuiWindow.WindowClickResult.Type.CLOSE_WINDOW) {
                    windows.remove(i);
                    if (windows.isEmpty()) mc.displayGuiScreen(null);
                }
                return;
            }
        }

        // 遍历所有窗口 (从顶到底)
        for (int i = windows.size() - 1; i >= 0; i--) {
            ClickGuiWindow win = windows.get(i);
            int mIdx = getMinimizedIndex(win);
            if (win.isInsideWindow(mouseX, mouseY, mIdx)) {
                // 点击聚焦置顶
                windows.remove(i);
                windows.add(win);

                ClickGuiWindow.WindowClickResult res = win.isMinimized()
                        ? win.mouseClickedMinimized(mouseX, mouseY, mouseButton, mIdx)
                        : win.mouseClicked(mouseX, mouseY, mouseButton);

                if (res.type == ClickGuiWindow.WindowClickResult.Type.CLOSE_WINDOW) {
                    windows.remove(win);
                    if (windows.isEmpty()) {
                        mc.displayGuiScreen(null);
                    }
                } else if (res.type == ClickGuiWindow.WindowClickResult.Type.DRAG_TAB) {
                    dragCandidateTab = res.tab;
                    dragCandidateWindow = win;
                    dragCandidateIndex = res.tabIndex;
                    dragCandidateOffsetX = res.tabOffsetX;
                    dragStartX = mouseX;
                    dragStartY = mouseY;
                    isDraggingTab = false;
                }
                return;
            }
        }

        super.mouseClicked(scaledMouseX, scaledMouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        int rawX = getRawMouseX();
        int rawY = getRawMouseY();

        handleTabDrop(rawX, rawY);

        for (ClickGuiWindow win : windows) {
            win.mouseReleased(rawX, rawY, state);
        }
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE || keyCode == Keyboard.KEY_RSHIFT) {
            mc.displayGuiScreen(null);
            if (mc.currentScreen == null) {
                mc.setIngameFocus();
            }
            return;
        }

        if (!windows.isEmpty()) {
            ClickGuiWindow topWindow = windows.get(windows.size() - 1);
            if (topWindow.keyTyped(typedChar, keyCode)) {
                return;
            }
        }

        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
