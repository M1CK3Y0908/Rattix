package client.m1ck3y.rattix.module.impl;

import client.m1ck3y.rattix.module.Category;
import client.m1ck3y.rattix.module.Module;
import client.m1ck3y.rattix.module.clickgui.NumberSetting;

public class AutoPot extends Module {
    public NumberSetting health = new NumberSetting("Health", 12.0, 1.0, 20.0, 1.0, "Miscellaneous");

    public AutoPot() {
        super("AutoPot", "Automatically throws health and speed potions.", Category.PLAYER);
        addSettings(health);
    }
}
