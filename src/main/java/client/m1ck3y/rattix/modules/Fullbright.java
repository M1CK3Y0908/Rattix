package client.m1ck3y.rattix.modules;

import client.m1ck3y.rattix.modules.manager.Category;
import client.m1ck3y.rattix.modules.manager.Register;


public class Fullbright extends Register {
    private float previousGamma = 1.0f;

    public Fullbright() {
        super("Brightens up everything in the world.", Category.VISUAL);
    }

    @Override
    public void onEnable() {
        if (mc.gameSettings != null) {
            previousGamma = mc.gameSettings.gammaSetting;
            mc.gameSettings.gammaSetting = 100.0f;
        }
    }

    @Override
    public void onTick() {
        if (isEnabled() && mc.gameSettings != null && mc.gameSettings.gammaSetting < 10.0f) {
            mc.gameSettings.gammaSetting = 100.0f;
        }
    }

    @Override
    public void onDisable() {
        if (mc.gameSettings != null) {
            mc.gameSettings.gammaSetting = previousGamma;
        }
    }
}
