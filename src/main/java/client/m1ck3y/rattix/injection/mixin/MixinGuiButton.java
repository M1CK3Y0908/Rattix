package client.m1ck3y.rattix.injection.mixin;

import client.m1ck3y.rattix.utils.render.Win11UiRenderer;
import client.m1ck3y.rattix.utils.ui.GuiScreenBridge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiButton.class)
public abstract class MixinGuiButton extends Gui {

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
        GuiButton self = (GuiButton) (Object) this;
        if (!self.visible) {
            ci.cancel();
            return;
        }
        Win11UiRenderer.drawFluentButton(self, mc, mouseX, mouseY);
        GuiScreenBridge.mouseDragged(self, mc, mouseX, mouseY);
        ci.cancel();
    }
}
