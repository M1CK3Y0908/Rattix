package client.m1ck3y.rattix.injection.mixin;

import client.m1ck3y.rattix.utils.StartupScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
@SuppressWarnings("all")
public class MixinMinecraft {

    /**
     * @author Rattix Team (reference FPSMaster-Edge)
     * @reason Custom Loading Screen - Intercepts Minecraft.drawSplashScreen so all vanilla splash / resource reloads render black background + ricon
     */
    @Inject(method = "drawSplashScreen", at = @At("HEAD"), cancellable = true)
    public void onDrawSplashScreen(TextureManager textureManagerInstance, CallbackInfo ci) {
        StartupScreen.renderSplashScreen();
        ci.cancel();
    }
}
