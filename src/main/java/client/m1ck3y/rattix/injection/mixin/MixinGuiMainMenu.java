package client.m1ck3y.rattix.injection.mixin;

import client.m1ck3y.rattix.ui.client.RattixMainMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Redirects vanilla GuiMainMenu to the standalone Rattix screen after normal setup. */
@Mixin(GuiMainMenu.class)
public abstract class MixinGuiMainMenu {
    private static boolean rattix$redirecting;

    @Inject(method = "initGui", at = @At("RETURN"), require = 0)
    private void rattix$redirectToCustomMenu(CallbackInfo ci) {
        if (rattix$redirecting) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.currentScreen == null || !(mc.currentScreen instanceof GuiMainMenu)) return;
        rattix$redirecting = true;
        try {
            mc.displayGuiScreen(new RattixMainMenu());
        } finally {
            rattix$redirecting = false;
        }
    }
}
