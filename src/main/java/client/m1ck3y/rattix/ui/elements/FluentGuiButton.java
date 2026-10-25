package client.m1ck3y.rattix.ui.elements;

import client.m1ck3y.rattix.utils.render.Win11UiRenderer;
import client.m1ck3y.rattix.utils.ui.GuiScreenBridge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

/**
 * FluentGuiButton - Standalone Win11 Fluent Design button control.
 * Overrides drawButton directly to render modern Fluent UI styles without
 * depending on Mixin bytecode transformations, providing 100% reliability
 * across external launchers and modified runtimes.
 */
public class FluentGuiButton extends GuiButton {

    public FluentGuiButton(int buttonId, int x, int y, String buttonText) {
        super(buttonId, x, y, buttonText);
    }

    public FluentGuiButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText) {
        super(buttonId, x, y, widthIn, heightIn, buttonText);
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (!this.visible) return;
        this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition
                && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
        Win11UiRenderer.drawFluentButton(this, mc, mouseX, mouseY);
        GuiScreenBridge.mouseDragged(this, mc, mouseX, mouseY);
    }
}
