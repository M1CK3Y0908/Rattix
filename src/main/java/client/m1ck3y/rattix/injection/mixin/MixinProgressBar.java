package client.m1ck3y.rattix.injection.mixin;

import client.m1ck3y.rattix.utils.StartupScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = net.minecraftforge.fml.common.ProgressManager.ProgressBar.class, remap = false)
public class MixinProgressBar {

    @Inject(method = "step(Ljava/lang/String;)V", at = @At("HEAD"), remap = false)
    private void onStep(String message, CallbackInfo ci) {
        StartupScreen.pumpFrame();
    }
}
