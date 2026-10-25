package client.m1ck3y.rattix.injection.mixin;

import client.m1ck3y.rattix.utils.render.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiOptionSlider;
import net.minecraft.util.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(GuiButton.class)
public abstract class MixinGuiButton extends Gui {
    @Shadow public boolean visible;
    @Shadow public int xPosition;
    @Shadow public int yPosition;
    @Shadow public int width;
    @Shadow public int height;
    @Shadow protected boolean hovered;
    @Shadow public boolean enabled;
    @Shadow public String displayString;
    @Shadow protected abstract void mouseDragged(Minecraft mc, int mouseX, int mouseY);
    @Unique private long rattix$hoverStart;
    @Unique private boolean rattix$lastHover;
    @Unique private float rattix$progress;

    /**
     * @author Rattix Team
     * @reason Fluent button states: base, hover, disabled and slider accent
     */
    @Overwrite
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (!visible) return;
        hovered = mouseX >= xPosition && mouseY >= yPosition && mouseX < xPosition + width && mouseY < yPosition + height;
        boolean slider = (Object) this instanceof GuiOptionSlider;
        // Fluent uses a subtle reveal overlay rather than a full saturated fill.
        if (hovered != rattix$lastHover) { rattix$hoverStart = System.currentTimeMillis(); rattix$lastHover = hovered; }
        float state = MathHelper.clamp_float((System.currentTimeMillis() - rattix$hoverStart) / 120.0f, 0.0f, 1.0f);
        int background = enabled ? (hovered ? 0xE61F242B : 0xD61A1D23) : 0x661A1D23;
        int border = enabled ? (hovered ? 0xFF76B9ED : 0x553A424E) : 0x333A424E;
        RenderUtil.drawRoundedRect(xPosition, yPosition, width, height, 4.0f, background);
        if (enabled && hovered) {
            int revealAlpha = 18 + (int) (18.0f * state);
            RenderUtil.drawRoundedRect(xPosition + 1, yPosition + 1, width - 2, height - 2, 3.0f,
                    (revealAlpha << 24) | 0xFFFFFF);
        }
        RenderUtil.drawRoundedOutline(xPosition, yPosition, width, height, 4.0f, 1.0f, border);
        if (enabled && hovered) RenderUtil.drawRoundedRect(xPosition + 4, yPosition + height - 2, width - 8, 2.0f, 1.0f, 0xFF76B9ED);
        if (slider) {
            float trackX = xPosition + 10.0f, trackW = Math.max(4.0f, width - 20.0f);
            float thumbX = trackX + trackW * (hovered ? 1.0f : 0.0f);
            RenderUtil.drawRoundedRect(trackX, yPosition + height - 5.0f, trackW, 2.0f, 1.0f, enabled ? 0x889AA7B5 : 0x445C6672);
            RenderUtil.drawRoundedRect(thumbX - 3.0f, yPosition + height - 8.0f, 6.0f, 8.0f, 3.0f, enabled ? 0xFF76B9ED : 0xFF6D737B);
        }
        if (mc.fontRendererObj != null && displayString != null) mc.fontRendererObj.drawStringWithShadow(displayString, xPosition + (width - mc.fontRendererObj.getStringWidth(displayString)) / 2, yPosition + (height - 8) / 2, enabled ? 0xFFF1F4F7 : 0xFF8C929A);
        mouseDragged(mc, mouseX, mouseY);
    }
}
