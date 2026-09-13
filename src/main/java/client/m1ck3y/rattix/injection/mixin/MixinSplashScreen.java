package client.m1ck3y.rattix.injection.mixin;

import client.m1ck3y.rattix.utils.StartupScreen;
import net.minecraftforge.fml.client.SplashProgress;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(SplashProgress.class)
@SuppressWarnings("all")
public class MixinSplashScreen {

    /**
     * @author Rattix Team (reference FPSMaster-Edge)
     * @reason Custom Loading Screen - Overwrites Forge's SplashProgress.start to prevent Forge background thread, anvil, and default bars
     */
    @Overwrite(remap = false)
    public static void start() {
        StartupScreen.renderSplashScreen();
    }
}
