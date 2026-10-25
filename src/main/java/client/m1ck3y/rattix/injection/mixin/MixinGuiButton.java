package client.m1ck3y.rattix.injection.mixin;

import client.m1ck3y.rattix.utils.render.Win11UiRenderer;
import client.m1ck3y.rattix.utils.ui.GuiScreenBridge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiButton.class)
public abstract class MixinGuiButton extends Gui {
    @Shadow public boolean visible;
    @Shadow public int xPosition;
    @Shadow public int yPosition;
    @Shadow public int width;
    @Shadow public int height;
    @Shadow protected boolean hovered;

    static {
        Win11UiRenderer.mixinApplied = true;
    }

    /**
     * @author Rattix Team
     * @reason Fluent Design button states with full Searge & Notch refmap generation
     */
    @Inject(method = "drawButton", at = @At("HEAD"), cancellable = true)
    public void onDrawButton(Minecraft mc, int mouseX, int mouseY, CallbackInfo ci) {
        Win11UiRenderer.markMixinDrawn();
        if (!visible) {
            ci.cancel();
            return;
        }
        hovered = mouseX >= xPosition && mouseY >= yPosition && mouseX < xPosition + width && mouseY < yPosition + height;
        GuiButton self = (GuiButton) (Object) this;
        Win11UiRenderer.drawFluentButton(self, mc, mouseX, mouseY);
        GuiScreenBridge.mouseDragged(self, mc, mouseX, mouseY);
        ci.cancel();
    }
}
