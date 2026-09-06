package client.m1ck3y.rattix.module.impl;

import client.m1ck3y.rattix.module.Category;
import client.m1ck3y.rattix.module.Module;
import client.m1ck3y.rattix.module.clickgui.BooleanSetting;
import client.m1ck3y.rattix.module.clickgui.NumberSetting;

public class BowAimbot extends Module {
    public NumberSetting range = new NumberSetting("Range", 30.0, 10.0, 60.0, 1.0, "Targets");
    public BooleanSetting silent = new BooleanSetting("Silent", true, "Movement");

    public BowAimbot() {
        super("BowAimbot", "Automatically aims bow at nearest target.", Category.COMBAT);
        addSettings(range, silent);
    }
}
