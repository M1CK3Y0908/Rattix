package client.m1ck3y.rattix.injection.mixin;

import client.m1ck3y.rattix.ui.client.StartupScreen;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = FMLCommonHandler.class, remap = false)
public class MixinFMLCommonHandler {

    @Inject(method = "processWindowMessages", at = @At("HEAD"), remap = false)
    private void onProcessWindowMessages(CallbackInfo ci) {
        StartupScreen.pumpFrame();
    }
}
