package client.m1ck3y.rattix.injection.mixin;

import client.m1ck3y.rattix.utils.render.RenderUtil;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiTextField.class)
public abstract class MixinGuiTextField {
    @Shadow private int xPosition;
    @Shadow private int yPosition;
    @Shadow private int width;
    @Shadow private int height;
    @Shadow private boolean visible;
    @Shadow private boolean isFocused;
    @Inject(method = "drawTextBox", at = @At("HEAD"), cancellable = true)
    private void rattix$drawWin11TextField(CallbackInfo ci) {
        GuiTextField self = (GuiTextField) (Object) this;
        if (!visible) return;
        int bg = isFocused ? 0xE61F242B : 0xD02A2F36;
        int border = isFocused ? 0xFF76B9ED : 0x553FFFFF;
        RenderUtil.drawRoundedRect(xPosition, yPosition, width, height, 5.0f, bg);
        RenderUtil.drawRoundedOutline(xPosition, yPosition, width, height, 5.0f, 1.0f, border);
        FontRenderer font = net.minecraft.client.Minecraft.getMinecraft().fontRendererObj;
        if (font != null) {
            String text = self.getText();
            if (text != null && !text.isEmpty()) {
                font.drawStringWithShadow(text, xPosition + 7, yPosition + (height - 8) / 2, 0xFFE6E9ED);
            }
        }
        ci.cancel();
    }
}
