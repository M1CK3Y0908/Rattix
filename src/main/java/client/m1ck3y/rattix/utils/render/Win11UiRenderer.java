package client.m1ck3y.rattix.utils.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import java.util.List;

/** Lightweight Win11/UWP-inspired controls used by vanilla GUI mixins. */
public final class Win11UiRenderer {
    private Win11UiRenderer() {}

    public static void drawButton(GuiButton button, Minecraft mc, int mouseX, int mouseY) {
        if (button == null || !button.visible) return;
        boolean hover = mouseX >= button.xPosition && mouseY >= button.yPosition
                && mouseX < button.xPosition + button.width && mouseY < button.yPosition + button.height;
        boolean slider = button.getClass().getName().contains("GuiOptionSlider")
                || button.getClass().getName().contains("GuiSlider");
        int bg = !button.enabled ? 0x553A3A42 : (hover ? 0xE6383F4A : 0xD02A2F36);
        int border = hover ? 0xFF76B9ED : 0x553FFFFF;
        RenderUtil.drawRoundedRect(button.xPosition, button.yPosition, button.width, button.height, 5.0f, bg);
        RenderUtil.drawRoundedOutline(button.xPosition, button.yPosition, button.width, button.height, 5.0f, 1.0f, border);
        if (slider) {
            float trackX = button.xPosition + 10.0f;
            float trackY = button.yPosition + button.height - 5.0f;
            float trackW = Math.max(4.0f, button.width - 20.0f);
            RenderUtil.drawRoundedRect(trackX, trackY, trackW, 2.0f, 1.0f, 0x55FFFFFF);
            RenderUtil.drawRoundedRect(trackX + trackW * 0.5f - 3.0f, trackY - 3.0f, 6.0f, 8.0f, 3.0f,
                    hover ? 0xFF76B9ED : 0xFFE6E9ED);
        }
        FontRenderer font = mc.fontRendererObj;
        if (font != null && button.displayString != null) {
            int color = button.enabled ? (hover ? 0xFFFFFFFF : 0xFFE6E9ED) : 0xFF888D94;
            int x = button.xPosition + (button.width - font.getStringWidth(button.displayString)) / 2;
            int y = button.yPosition + (button.height - 8) / 2;
            font.drawStringWithShadow(button.displayString, x, y, color);
        }
    }

    public static boolean shouldStyle(GuiButton button) {
        return button != null && button.visible;
    }

    /** Draws after vanilla's button pass, guaranteeing a visible overlay even when
     * OptiFine or another coremod changes GuiButton's mapped draw method. */
    public static void drawButtons(GuiScreen screen, Minecraft mc, int mouseX, int mouseY, List<GuiButton> buttons) {
        if (screen == null || buttons == null) return;
        for (GuiButton button : buttons) {
            drawButton(button, mc, mouseX, mouseY);
        }
    }
}
