package client.m1ck3y.rattix.modules;

import client.m1ck3y.rattix.modules.clickgui.NumberSetting;

public class Velocity extends Register {
    public NumberSetting horizontal = new NumberSetting("Horizontal", 0.0, 0.0, 100.0, 1.0, "Miscellaneous");
    public NumberSetting vertical = new NumberSetting("Vertical", 0.0, 0.0, 100.0, 1.0, "Miscellaneous");

    public Velocity() {
        super("Reduces incoming knockback.", Category.COMBAT);
        addSettings(horizontal, vertical);
    }
}
