package client.m1ck3y.rattix.ui.client;

import net.minecraft.client.gui.GuiScreen;

/**
 * Rattix 客户端基础配置界面容器，对齐 LiquidBounce ui/client 架构。
 */
public class GuiClientConfiguration extends GuiScreen {
    private final GuiScreen parentScreen;

    public GuiClientConfiguration(GuiScreen parentScreen) {
        this.parentScreen = parentScreen;
    }

    public GuiScreen getParentScreen() {
        return parentScreen;
    }
}
