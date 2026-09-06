package client.m1ck3y.rattix.module.impl;

import client.m1ck3y.rattix.module.Category;
import client.m1ck3y.rattix.module.Module;
import client.m1ck3y.rattix.module.clickgui.NumberSetting;

public class Velocity extends Module {
    public NumberSetting horizontal = new NumberSetting("Horizontal", 0.0, 0.0, 100.0, 1.0, "Miscellaneous");
    public NumberSetting vertical = new NumberSetting("Vertical", 0.0, 0.0, 100.0, 1.0, "Miscellaneous");

    public Velocity() {
        super("Velocity", "Reduces incoming knockback.", Category.COMBAT);
        addSettings(horizontal, vertical);
    }
}
