package client.m1ck3y.rattix.ui.client;

import client.m1ck3y.rattix.RattixMod;
import client.m1ck3y.rattix.ui.elements.FluentGuiButton;
import client.m1ck3y.rattix.utils.render.RenderUtil;
import client.m1ck3y.rattix.utils.render.shader.ShaderManager;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

/**
 * GuiClientConfiguration - Modern client settings interface.
 * Configures background shaders, rendering pacing, ClickGUI shortcuts, and general preferences.
 */
public class GuiClientConfiguration extends GuiScreen {
    private final GuiScreen parentScreen;

    private static final int BTN_PREV_SHADER = 1;
    private static final int BTN_NEXT_SHADER = 2;
    private static final int BTN_CYCLE_SPEED = 3;
    private static final int BTN_CYCLE_RES = 4;
    private static final int BTN_CLICKGUI_KEY = 5;
    private static final int BTN_DONE = 200;

    private boolean listeningForKey = false;

    public GuiClientConfiguration(GuiScreen parentScreen) {
        this.parentScreen = parentScreen;
    }

    public GuiScreen getParentScreen() {
        return parentScreen;
    }

    @Override
    public void initGui() {
        buttonList.clear();

        int centerX = width / 2;
        int startY = Math.max(40, height / 2 - 100);

        // Shader switcher: Prev / Next
        buttonList.add(new FluentGuiButton(BTN_PREV_SHADER, centerX - 120, startY + 22, 28, 20, "<"));
        buttonList.add(new FluentGuiButton(BTN_NEXT_SHADER, centerX + 92, startY + 22, 28, 20, ">"));

        // Speed & Resolution
        buttonList.add(new FluentGuiButton(BTN_CYCLE_SPEED, centerX - 120, startY + 68, 115, 20, getSpeedString()));
        buttonList.add(new FluentGuiButton(BTN_CYCLE_RES, centerX + 5, startY + 68, 115, 20, getResolutionString()));

        // Keybind
        buttonList.add(new FluentGuiButton(BTN_CLICKGUI_KEY, centerX - 120, startY + 114, 240, 20, getKeybindString()));

        // Done button
        buttonList.add(new FluentGuiButton(BTN_DONE, centerX - 75, height - 38, 150, 24, "Done"));
    }

    private String getSpeedString() {
        return "Speed: " + String.format("%.1fx", ShaderManager.getInstance().getSpeed());
    }

    private String getResolutionString() {
        return "Res: " + (int) ShaderManager.getInstance().getResolution() + "%";
    }

    private String getKeybindString() {
        if (listeningForKey) {
            return "Press any key... (ESC to cancel)";
        }
        int code = RattixMod.clickGuiKey != null ? RattixMod.clickGuiKey.getKeyCode() : Keyboard.KEY_RSHIFT;
        return "ClickGUI Key: " + Keyboard.getKeyName(code);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        int centerX = width / 2;
        int startY = Math.max(40, height / 2 - 100);

        // Header Title
        drawCenteredString(fontRendererObj, "Rattix Client Settings", centerX, 16, 0xFF76B9ED);

        // Section 1: Background Shader
        drawCenteredString(fontRendererObj, "Background Shader", centerX, startY + 8, 0xFFE0E5EC);
        String shaderName = ShaderManager.getInstance().formatShaderName(ShaderManager.getInstance().getCurrentShader());
        RenderUtil.drawRoundedRect(centerX - 88, startY + 22, 176, 20, 4.0f, 0x33000000);
        RenderUtil.drawRoundedOutline(centerX - 88, startY + 22, 176, 20, 4.0f, 1.0f, 0x403F6D94);
        drawCenteredString(fontRendererObj, shaderName, centerX, startY + 28, 0xFFFFFFFF);

        // Section 2: Tuning
        drawCenteredString(fontRendererObj, "Shader Tuning", centerX, startY + 54, 0xFFE0E5EC);

        // Section 3: Keybinds
        drawCenteredString(fontRendererObj, "Keybinds & Controls", centerX, startY + 100, 0xFFE0E5EC);

        // Info footer
        drawCenteredString(fontRendererObj, "Press Right Shift anytime in-game to open ClickGUI", centerX, height - 56, 0xFF7E8A99);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        ShaderManager sm = ShaderManager.getInstance();
        switch (button.id) {
            case BTN_PREV_SHADER:
                sm.prevShader();
                break;
            case BTN_NEXT_SHADER:
                sm.nextShader();
                break;
            case BTN_CYCLE_SPEED: {
                double cur = sm.getSpeed();
                double next = (cur >= 3.0) ? 0.5 : cur + 0.5;
                sm.setSpeed(next);
                button.displayString = getSpeedString();
                break;
            }
            case BTN_CYCLE_RES: {
                double cur = sm.getResolution();
                double next;
                if (cur >= 200.0) next = 50.0;
                else if (cur >= 150.0) next = 200.0;
                else if (cur >= 100.0) next = 150.0;
                else if (cur >= 75.0) next = 100.0;
                else next = 75.0;
                sm.setResolution(next);
                button.displayString = getResolutionString();
                break;
            }
            case BTN_CLICKGUI_KEY:
                listeningForKey = true;
                button.displayString = getKeybindString();
                break;
            case BTN_DONE:
                mc.displayGuiScreen(parentScreen);
                break;
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (listeningForKey) {
            if (keyCode != Keyboard.KEY_ESCAPE && RattixMod.clickGuiKey != null) {
                RattixMod.clickGuiKey.setKeyCode(keyCode);
            }
            listeningForKey = false;
            for (GuiButton btn : buttonList) {
                if (btn.id == BTN_CLICKGUI_KEY) {
                    btn.displayString = getKeybindString();
                    break;
                }
            }
            return;
        }
        if (keyCode == 1) { // Escape
            mc.displayGuiScreen(parentScreen);
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }
}
