package client.m1ck3y.rattix.modules;

import client.m1ck3y.rattix.modules.clickgui.NumberSetting;

public class AutoGapple extends Register {
    public NumberSetting health = new NumberSetting("Health", 14.0, 1.0, 20.0, 1.0, "Miscellaneous");

    public AutoGapple() {
        super("Automatically eats Golden Apples when health is low.", Category.PLAYER);
        addSettings(health);
    }
}
