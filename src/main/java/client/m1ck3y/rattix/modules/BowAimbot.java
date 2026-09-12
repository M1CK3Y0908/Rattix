package client.m1ck3y.rattix.modules;

import client.m1ck3y.rattix.modules.clickgui.BooleanSetting;
import client.m1ck3y.rattix.modules.clickgui.NumberSetting;

public class BowAimbot extends Register {
    public NumberSetting range = new NumberSetting("Range", 30.0, 10.0, 60.0, 1.0, "Targets");
    public BooleanSetting silent = new BooleanSetting("Silent", true, "Movement");

    public BowAimbot() {
        super("Automatically aims bow at nearest target.", Category.COMBAT);
        addSettings(range, silent);
    }
}
