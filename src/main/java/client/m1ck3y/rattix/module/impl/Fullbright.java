package client.m1ck3y.rattix.module.impl;

import client.m1ck3y.rattix.module.Category;
import client.m1ck3y.rattix.module.Module;

public class Fullbright extends Module {
    private float previousGamma = 1.0f;

    public Fullbright() {
        super("Fullbright", "Brightens up everything in the world.", Category.RENDER);
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
