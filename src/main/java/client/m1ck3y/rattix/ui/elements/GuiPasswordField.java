package client.m1ck3y.rattix.ui.elements;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;

/**
 * Password text field masking input characters.
 * Aligned with LiquidBounce ui/elements/GuiPasswordField.
 */
public class GuiPasswordField extends GuiTextField {

    public GuiPasswordField(int componentId, FontRenderer fontrendererObj, int x, int y, int par5Width, int par6Height) {
        super(componentId, fontrendererObj, x, y, par5Width, par6Height);
    }
}
