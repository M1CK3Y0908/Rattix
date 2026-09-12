package client.m1ck3y.rattix.modules;

import client.m1ck3y.rattix.modules.manager.Category;
import client.m1ck3y.rattix.modules.manager.Register;

import client.m1ck3y.rattix.modules.clickgui.NumberSetting;

public class AutoPot extends Register {
    public NumberSetting health = new NumberSetting("Health", 12.0, 1.0, 20.0, 1.0, "Miscellaneous");

    public AutoPot() {
        super("Automatically throws health and speed potions.", Category.PLAYER);
        addSettings(health);
    }
}
