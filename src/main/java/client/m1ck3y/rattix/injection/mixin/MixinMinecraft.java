package client.m1ck3y.rattix.injection.mixin;

import client.m1ck3y.rattix.utils.StartupScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(Minecraft.class)
@SuppressWarnings("all")
public class MixinMinecraft {

    /**
     * @author Rattix Team (reference FPSMaster-Edge)
     * @reason Custom Loading Screen - Overwrites Minecraft.drawSplashScreen so all vanilla splash / resource reloads render black background + ricon
     */
    @Overwrite
    public void drawSplashScreen(TextureManager textureManagerInstance) {
        StartupScreen.renderSplashScreen();
    }
}
