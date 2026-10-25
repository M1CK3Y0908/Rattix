package client.m1ck3y.rattix.ui.client;

import client.m1ck3y.rattix.utils.render.AugustusMainMenuRenderer;
import client.m1ck3y.rattix.utils.render.shader.ShaderBackgroundRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSelectWorld;
import net.minecraft.client.gui.GuiMultiplayer;

import java.io.IOException;

/** Standalone main menu screen; vanilla GuiMainMenu is only the Mixin redirect target. */
public class RattixMainMenu extends GuiScreen {
    private static final int SINGLEPLAYER = 1, MULTIPLAYER = 2, OPTIONS = 3, QUIT = 4;

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
        if (ShaderBackgroundRenderer.shouldRenderShader()) ShaderBackgroundRenderer.renderCurrentScreen(true);
        else drawDefaultBackground();
        AugustusMainMenuRenderer.draw(this, mc, mouseX, mouseY, null);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        switch (button.id) {
            case SINGLEPLAYER: mc.displayGuiScreen(new GuiSelectWorld(this)); break;
            case MULTIPLAYER: mc.displayGuiScreen(new GuiMultiplayer(this)); break;
            case OPTIONS: mc.displayGuiScreen(new GuiOptions(this, mc.gameSettings)); break;
            case QUIT: mc.shutdown(); break;
            default: break;
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) {
            // Main menu ignores Escape key
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }
}
