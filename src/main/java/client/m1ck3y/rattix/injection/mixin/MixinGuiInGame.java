package client.m1ck3y.rattix.injection.mixin;

import client.m1ck3y.rattix.RattixMod;
import net.minecraft.client.gui.GuiIngame;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiIngame.class)
@SuppressWarnings("all")
public abstract class MixinGuiInGame {

    @Inject(method = "renderGameOverlay", at = @At("TAIL"))
    private void onRenderGameOverlay(float partialTicks, CallbackInfo ci) {
        RattixMod.handleRender2D(partialTicks);
    }
}
