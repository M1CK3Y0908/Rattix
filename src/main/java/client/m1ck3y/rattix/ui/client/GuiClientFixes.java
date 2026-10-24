package client.m1ck3y.rattix.ui.client;

import net.minecraft.client.gui.GuiScreen;

/**
 * Client fixes settings screen.
 * Aligned with LiquidBounce ui/client/GuiClientFixes.
 */
public class GuiClientFixes extends GuiScreen {
    private final GuiScreen prevGui;

    public GuiClientFixes(GuiScreen prevGui) {
        this.prevGui = prevGui;
    }
}
