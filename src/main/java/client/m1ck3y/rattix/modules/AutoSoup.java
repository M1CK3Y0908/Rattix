package client.m1ck3y.rattix.modules;

import client.m1ck3y.rattix.modules.clickgui.NumberSetting;

public class AutoSoup extends Register {
    public NumberSetting health = new NumberSetting("Health", 13.0, 1.0, 20.0, 1.0, "Miscellaneous");

    public AutoSoup() {
        super("Automatically consumes mushroom soup.", Category.PLAYER);
        addSettings(health);
    }
}
