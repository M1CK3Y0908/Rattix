package client.m1ck3y.rattix.utils.render;

import client.m1ck3y.rattix.ui.client.RattixMainMenu;
import client.m1ck3y.rattix.ui.font.FontUtil;
import client.m1ck3y.rattix.ui.window.WindowManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;

import java.util.List;

/**
 * Modern minimalist launcher surface for the Rattix main menu.
 * Features a clean translucent black navigation bar on the left with 4x Rattix branding at the top,
 * vertically centered navigation buttons, and an uncluttered desktop canvas for background shaders
 * and floating subpage windows.
 */
public final class AugustusMainMenuRenderer {
    private AugustusMainMenuRenderer() {}

    public static void draw(GuiScreen screen, Minecraft mc, int mouseX, int mouseY, List<GuiButton> buttons) {
        draw(screen, mc, mouseX, mouseY, buttons, null);
    }

    public static void draw(GuiScreen screen, Minecraft mc, int mouseX, int mouseY, List<GuiButton> buttons, WindowManager windowManager) {
        if (screen == null || mc == null) return;
        int w = screen.width, h = screen.height;

        int railW = (screen instanceof RattixMainMenu) ? ((RattixMainMenu) screen).getRailWidth() : 180;

        // 1. Simplified background: translucent black rectangle for the left navigation rail
        RenderUtil.drawRect(0, 0, railW, h, 0x80000000);
        // Subtle 1px right glass border
        RenderUtil.drawRect(railW - 1, 0, railW, h, 0x1AFFFFFF);

        // 2. Top 4x "Rattix" text display
        int btnStartY = (screen instanceof RattixMainMenu) ? ((RattixMainMenu) screen).getButtonsStartY() : 140;
        float titleW = FontUtil.getStringWidth4x("Rattix");
        float titleX = (railW - titleW) / 2.0f;
        float titleY = (btnStartY > 65) ? (btnStartY - 28.0f) / 2.0f : 12.0f;

        if (titleW > railW - 16) {
            float scaleFit = Math.max(0.5f, (railW - 20.0f) / titleW);
            GlStateManager.pushMatrix();
            GlStateManager.translate(railW / 2.0f, titleY, 0);
            GlStateManager.scale(scaleFit, scaleFit, 1.0f);
            FontUtil.drawString4x("Rattix", -titleW / 2.0f, 0, 0xFFFFFFFF);
            GlStateManager.popMatrix();
        } else {
            FontUtil.drawString4x("Rattix", titleX, titleY, 0xFFFFFFFF);
        }

        // 3. Bottom subtle version watermark
        FontRenderer f = mc.fontRendererObj;
        if (f != null && h >= 220) {
            f.drawString("1.8.9 Forge", 16, h - 26, 0x55FFFFFF);
            f.drawString("Rattix 1.0.0", 16, h - 15, 0x33FFFFFF);
        }

        // Draw buttons if explicitly passed
        if (buttons != null) {
            for (GuiButton button : buttons) {
                button.drawButton(mc, mouseX, mouseY);
            }
        }
    }
}
