package client.m1ck3y.rattix.injection.mixin;

import client.m1ck3y.rattix.RattixMod;
import client.m1ck3y.rattix.ui.client.StartupScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiMainMenu;
import client.m1ck3y.rattix.ui.client.RattixMainMenu;
import net.minecraft.client.renderer.texture.TextureManager;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
@SuppressWarnings("all")
public abstract class MixinMinecraft {
    private static boolean rattix$redirectingMainMenu;

    /** Fallback handoff used when OptiFine replaces GuiMainMenu.initGui before its Mixin runs,
     * and windowed child-page routing when RattixMainMenu is active. */
    @Inject(method = "displayGuiScreen", at = @At("HEAD"), cancellable = true, require = 0)
    private void rattix$redirectDisplayedMainMenu(GuiScreen screen, CallbackInfo ci) {
        GuiScreen cur = ((Minecraft) (Object) this).currentScreen;
        if (cur instanceof RattixMainMenu) {
            RattixMainMenu menu = (RattixMainMenu) cur;
            if (menu.getWindowManager() != null) {
                if (!menu.getWindowManager().onDisplayGuiScreen(screen)) {
                    ci.cancel();
                    return;
                }
            }
        }
        if (rattix$redirectingMainMenu || !(screen instanceof GuiMainMenu)) return;
        rattix$redirectingMainMenu = true;
        try {
            Minecraft.getMinecraft().displayGuiScreen(new RattixMainMenu());
            ci.cancel();
        } finally {
            rattix$redirectingMainMenu = false;
        }
    }

    /**
     * @author Rattix Team (reference FPSMaster-Edge)
     * @reason Custom Loading Screen - Intercepts Minecraft.drawSplashScreen so all vanilla splash / resource reloads render black background + ricon
     */
    @Inject(method = "drawSplashScreen", at = @At("HEAD"), cancellable = true)
    public void onDrawSplashScreen(TextureManager textureManagerInstance, CallbackInfo ci) {
        StartupScreen.renderSplashScreen();
        ci.cancel();
    }

    /**
     * Ensure Rattix Client initializes even if Forge mod discovery is bypassed in development
     */
    @Inject(method = "startGame", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;checkGLError(Ljava/lang/String;)V", ordinal = 2, shift = At.Shift.AFTER))
    private void onStartGame(CallbackInfo ci) {
        RattixMod.startClient();
    }

    /**
     * Intercept key events for in-game shortcuts (such as ClickGUI on RSHIFT)
     */
    @Inject(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;dispatchKeypresses()V", shift = At.Shift.AFTER))
    private void onKeyInput(CallbackInfo ci) {
        if (Keyboard.getEventKeyState() && ((Minecraft) (Object) this).currentScreen == null) {
            int key = Keyboard.getEventKey();
            if (key == 0) {
                key = Keyboard.getEventCharacter() + 256;
            }
            RattixMod.handleKey(key);
        }
    }

    /**
     * Client tick event
     */
    @Inject(method = "runTick", at = @At("TAIL"))
    private void onClientTick(CallbackInfo ci) {
        RattixMod.handleTick();
    }

    /**
     * Client shutdown hook
     */
    @Inject(method = "shutdown", at = @At("HEAD"))
    private void onShutdown(CallbackInfo ci) {
        RattixMod.stopClient();
    }
}
