package client.m1ck3y.rattix.injection.mixin;

import client.m1ck3y.rattix.utils.render.Win11UiRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraftforge.fml.client.config.GuiButtonExt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GuiButtonExt.class, remap = false)
public abstract class MixinGuiButtonExt extends GuiButton {
    public MixinGuiButtonExt(int buttonId, int x, int y, String buttonText) {
        super(buttonId, x, y, buttonText);
    }

    /**
     * Intercepts Forge GuiButtonExt to render unified Fluent button styling
     */
    @Inject(method = "drawButton", at = @At("HEAD"), cancellable = true)
    public void onDrawButton(Minecraft mc, int mouseX, int mouseY, CallbackInfo ci) {
        if (!visible) {
            ci.cancel();
            return;
        }
        Win11UiRenderer.drawFluentButton(this, mc, mouseX, mouseY);
        mouseDragged(mc, mouseX, mouseY);
        ci.cancel();
    }
}
