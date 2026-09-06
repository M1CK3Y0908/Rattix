package client.m1ck3y.rattix.module.impl;

import client.m1ck3y.rattix.module.Category;
import client.m1ck3y.rattix.module.Module;
import client.m1ck3y.rattix.module.clickgui.NumberSetting;

public class AutoSoup extends Module {
    public NumberSetting health = new NumberSetting("Health", 13.0, 1.0, 20.0, 1.0, "Miscellaneous");

    public AutoSoup() {
        super("AutoSoup", "Automatically consumes mushroom soup.", Category.PLAYER);
        addSettings(health);
    }
}
