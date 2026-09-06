package client.m1ck3y.rattix.module.clickgui.comp;

import client.m1ck3y.rattix.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.ChatAllowedCharacters;
import org.lwjgl.input.Keyboard;

public class SearchBar {
    private String text = "";
    private boolean focused = false;

    public void draw(float x, float y, float width, float height, int mouseX, int mouseY) {
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        boolean hovered = RenderUtil.isHovered(mouseX, mouseY, x, y, width, height);

        int bg = focused ? 0xDD2A3140 : (hovered ? 0xBB232833 : 0x991E232E);
        int border = focused ? 0xFF38B6FF : 0x22FFFFFF;

        RenderUtil.drawRoundedRect(x, y, width, height, 4, bg);
        RenderUtil.drawRoundedOutline(x, y, width, height, 4, 1.0f, border);

        // Search icon
        fr.drawStringWithShadow("⌕", x + 6, y + (height - 8) / 2.0f, 0xFF888888);

        // Text or Placeholder
        if (text.isEmpty() && !focused) {
            fr.drawStringWithShadow("§7Search mods...", x + 18, y + (height - 8) / 2.0f, 0xFF777777);
        } else {
            String cursor = focused && (System.currentTimeMillis() / 500 % 2 == 0) ? "_" : "";
            fr.drawStringWithShadow(text + cursor, x + 18, y + (height - 8) / 2.0f, 0xFFFFFFFF);
        }
    }

    public void mouseClicked(float x, float y, float width, float height, int mouseX, int mouseY, int button) {
        if (button == 0) {
            focused = RenderUtil.isHovered(mouseX, mouseY, x, y, width, height);
        }
    }

    public void keyTyped(char typedChar, int keyCode) {
        if (!focused) return;

        if (keyCode == Keyboard.KEY_BACK) {
            if (!text.isEmpty()) {
                text = text.substring(0, text.length() - 1);
            }
        } else if (keyCode == Keyboard.KEY_ESCAPE) {
            focused = false;
        } else if (ChatAllowedCharacters.isAllowedCharacter(typedChar)) {
            text += typedChar;
        }
    }

    public String getText() {
        return text;
    }

    public boolean isFocused() {
        return focused;
    }
}
