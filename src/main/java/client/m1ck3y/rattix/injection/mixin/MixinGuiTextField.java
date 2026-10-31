package client.m1ck3y.rattix.injection.mixin;

import client.m1ck3y.rattix.utils.render.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiTextField.class)
public abstract class MixinGuiTextField {

    @Inject(method = "drawTextBox", at = @At("HEAD"), cancellable = true)
    private void rattix$drawWin11TextField(CallbackInfo ci) {
        GuiTextField self = (GuiTextField) (Object) this;
        if (!self.getVisible()) return;
        boolean focused = self.isFocused();
        int bg = focused ? 0xE61F242B : 0xD02A2F36;
        int border = focused ? 0xFF76B9ED : 0x553FFFFF;
        RenderUtil.drawRoundedRect(self.xPosition, self.yPosition, self.width, self.height, 5.0f, bg);
        RenderUtil.drawRoundedOutline(self.xPosition, self.yPosition, self.width, self.height, 5.0f, 1.0f, border);
        FontRenderer font = Minecraft.getMinecraft().fontRendererObj;
        if (font != null) {
            String text = self.getText();
            if (text != null && !text.isEmpty()) {
                font.drawStringWithShadow(text, self.xPosition + 7, self.yPosition + (self.height - 8) / 2, 0xFFE6E9ED);
            }
        }
        ci.cancel();
    }
}
