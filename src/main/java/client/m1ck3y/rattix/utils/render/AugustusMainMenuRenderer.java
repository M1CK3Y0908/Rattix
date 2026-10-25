package client.m1ck3y.rattix.utils.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.util.List;

/** Augustus-inspired dark launcher surface for the Minecraft main menu. */
public final class AugustusMainMenuRenderer {
    private AugustusMainMenuRenderer() {}

    public static void draw(GuiScreen screen, Minecraft mc, int mouseX, int mouseY, List<GuiButton> buttons) {
        if (screen == null || mc == null) return;
        int w = screen.width, h = screen.height;
        // Keep the active GLSL background visible through the launcher surface.
        RenderUtil.drawRect(0, 0, w, h, 0x550B0D12);
        // Left navigation rail
        RenderUtil.drawRect(0, 0, 188, h, 0xC6101219);
        RenderUtil.drawRect(187, 0, 188, h, 0x2438B6FF);
        RenderUtil.drawRoundedRect(24, 24, 42, 42, 12, 0xFF263A52);
        FontRenderer f = mc.fontRendererObj;
        if (f == null) return;
        f.drawStringWithShadow("R", 38, 35, 0xFF76B9ED);
        f.drawString("RATTIX", 76, 31, 0xFFF4F7FA);
        f.drawString("CLIENT", 76, 43, 0xFF7F8A98);
        drawNavItem(f, "Home", 82, true);
        drawNavItem(f, "Modules", 118, false);
        drawNavItem(f, "Settings", 154, false);
        f.drawString("1.8.9 Forge", 24, h - 34, 0xFF7F8A98);
        f.drawString("Rattix 1.0.0", 24, h - 21, 0xFF566170);

        // Main hero card
        int cardX = 222, cardY = 44, cardW = Math.max(300, w - 258), cardH = Math.max(175, h - 116);
        RenderUtil.drawRoundedRect(cardX, cardY, cardW, cardH, 14, 0xD9141821);
        RenderUtil.drawRoundedOutline(cardX, cardY, cardW, cardH, 14, 1.0f, 0x303F6D94);
        f.drawString("WELCOME BACK", cardX + 28, cardY + 28, 0xFF76B9ED);
        f.drawString("Rattix Client", cardX + 28, cardY + 55, 0xFFF4F7FA);
        f.drawString("A clean, fast and configurable Minecraft experience.", cardX + 28, cardY + 74, 0xFF9DA8B5);
        f.drawString("Choose an action to continue", cardX + 28, cardY + cardH - 44, 0xFF667384);

        // Buttons are laid out here but remain vanilla GuiButton instances so the
        // original GuiMainMenu actionPerformed logic keeps working.
        if (buttons != null) {
            for (GuiButton button : buttons) {
                button.drawButton(mc, mouseX, mouseY);
            }
        }
        f.drawString("Build your own visual profile in ClickGUI  •  Right Shift", cardX + 28, h - 24, 0xFF667384);
    }

    private static void drawNavItem(FontRenderer f, String text, int y, boolean selected) {
        if (selected) RenderUtil.drawRoundedRect(16, y - 9, 150, 28, 7, 0xFF20344B);
        f.drawString(text, 34, y, selected ? 0xFFF4F7FA : 0xFF929CAA);
        if (selected) RenderUtil.drawRoundedRect(16, y - 9, 3, 28, 1.5f, 0xFF76B9ED);
    }
}
