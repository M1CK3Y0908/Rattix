package client.m1ck3y.rattix.injection.mixin;

import client.m1ck3y.rattix.utils.shader.ShaderManager;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiMainMenu.class)
public abstract class MixinGuiMainMenu extends GuiScreen {

    @Inject(method = "renderSkybox", at = @At("HEAD"), cancellable = true)
    private void onRenderSkybox(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        if (ShaderManager.getInstance().isShaderActive()) {
            ShaderManager.getInstance().render(this.width, this.height, mouseX, mouseY);
            ci.cancel();
        }
    }

    @Redirect(
        method = "drawScreen",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiMainMenu;drawGradientRect(IIIIII)V"
        )
    )
    private void onDrawGradient(GuiMainMenu instance, int left, int top, int right, int bottom, int startColor, int endColor) {
        if (ShaderManager.getInstance().isShaderActive()) {
            if (startColor == -2130706433) {
                // Suppress white haze wash so GLSL colors are vibrant
                return;
            }
            // Keep subtle bottom darkening for button / text readability
            this.drawGradientRect(left, top, right, bottom, 0, 0x55000000);
            return;
        }
        this.drawGradientRect(left, top, right, bottom, startColor, endColor);
    }

    @Inject(method = "drawScreen", at = @At("TAIL"))
    private void onDrawScreenTail(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        ShaderManager.getInstance().renderUI(mouseX, mouseY, this.width, this.height);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void onMouseClicked(int mouseX, int mouseY, int mouseButton, CallbackInfo ci) {
        if (ShaderManager.getInstance().mouseClicked(mouseX, mouseY, mouseButton)) {
            ci.cancel();
        }
    }
}
