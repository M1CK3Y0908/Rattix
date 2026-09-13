package client.m1ck3y.rattix.modules;

import client.m1ck3y.rattix.modules.manager.Category;
import client.m1ck3y.rattix.modules.manager.Register;

import client.m1ck3y.rattix.modules.ClickGUI.BooleanSetting;
import client.m1ck3y.rattix.utils.FontUtil;
import client.m1ck3y.rattix.utils.RenderUtil;
import org.lwjgl.input.Mouse;

public class Keystrokes extends Register {
    public BooleanSetting showMouse = new BooleanSetting("Show Mouse Buttons", true);

    public Keystrokes() {
        super("Displays movement keys and mouse presses on screen.", Category.VISUAL);
        addSettings(showMouse);
        setEnabled(false);
    }

    @Override
    public void onRender2D() {
        if (!isEnabled()) return;

        int startX = 4;
        int startY = 40;
        int keySize = 18;
        int gap = 2;

        // W Key
        drawKey("W", startX + keySize + gap, startY, keySize, keySize, mc.gameSettings.keyBindForward.isKeyDown());

        // A, S, D Keys
        int row2Y = startY + keySize + gap;
        drawKey("A", startX, row2Y, keySize, keySize, mc.gameSettings.keyBindLeft.isKeyDown());
        drawKey("S", startX + keySize + gap, row2Y, keySize, keySize, mc.gameSettings.keyBindBack.isKeyDown());
        drawKey("D", startX + (keySize + gap) * 2, row2Y, keySize, keySize, mc.gameSettings.keyBindRight.isKeyDown());

        // Mouse buttons
        if (showMouse.getValue()) {
            int row3Y = row2Y + keySize + gap;
            int mouseWidth = (keySize * 3 + gap * 2 - gap) / 2;
            drawKey("LMB", startX, row3Y, mouseWidth, keySize, Mouse.isButtonDown(0));
            drawKey("RMB", startX + mouseWidth + gap, row3Y, mouseWidth, keySize, Mouse.isButtonDown(1));
        }
    }

    private void drawKey(String text, int x, int y, int width, int height, boolean pressed) {
        int bgColor = pressed ? 0xCC38B6FF : 0x90000000;
        int textColor = pressed ? 0xFFFFFFFF : 0xFFE0E0E0;
        int outlineColor = pressed ? 0x8838B6FF : 0x25FFFFFF;

        RenderUtil.drawRoundedRect(x, y, width, height, 4, bgColor);
        RenderUtil.drawRoundedOutline(x, y, width, height, 4, 1.0f, outlineColor);
        float textWidth = FontUtil.getHudStringWidth(text);
        FontUtil.drawHudString(text, x + (width - textWidth) / 2.0f, y + (height - 8) / 2.0f, textColor);
    }
}
