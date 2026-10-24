package client.m1ck3y.rattix.features.module.modules;

import client.m1ck3y.rattix.features.module.Category;
import client.m1ck3y.rattix.features.module.Module;

import client.m1ck3y.rattix.features.module.modules.ClickGUI.BooleanSetting;
import client.m1ck3y.rattix.ui.font.FontUtil;
import client.m1ck3y.rattix.utils.render.RenderUtil;
import org.lwjgl.input.Mouse;

public class Keystrokes extends Module {
    public BooleanSetting showMouse = new BooleanSetting("ShowMouseButtons", true);

    public Keystrokes() {
        super("Displays movement keys and mouse presses on screen.", Category.VISUAL);
        addSettings(showMouse);
        initVisualSettings();
        setEnabled(true);
    }

    @Override
    public void onRender2D() {
        if (!isEnabled()) return;

        int startX = 4;
        int startY = 58;
        int keySize = 18;
        int gap = 2;
        int totalW = keySize * 3 + gap * 2;
        int totalH = showMouse.getValue() ? (keySize * 3 + gap * 2) : (keySize * 2 + gap);

        beginVisualTransform(startX + totalW * 0.5f, startY + totalH * 0.5f);

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

        endVisualTransform();
    }

    private void drawKey(String text, int x, int y, int width, int height, boolean pressed) {
        drawVisualBackground(x, y, width, height);
        if (pressed) {
            float radius = visualBgRadius != null ? visualBgRadius.getValue().floatValue() : 4.0f;
            RenderUtil.drawRoundedRect(x, y, width, height, radius, 0x5538B6FF);
        }
        float textWidth = getVisualTextWidth(text);
        float textHeight = getVisualTextHeight();
        drawVisualText(text, x + (width - textWidth) / 2.0f, y + (height - textHeight) / 2.0f);
    }
}
