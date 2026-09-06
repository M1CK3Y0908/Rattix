package client.m1ck3y.rattix.module.impl;

import client.m1ck3y.rattix.module.Category;
import client.m1ck3y.rattix.module.Module;
import client.m1ck3y.rattix.module.clickgui.NumberSetting;

public class AutoGapple extends Module {
    public NumberSetting health = new NumberSetting("Health", 14.0, 1.0, 20.0, 1.0, "Miscellaneous");

    public AutoGapple() {
        super("AutoGapple", "Automatically eats Golden Apples when health is low.", Category.PLAYER);
        addSettings(health);
    }
}
