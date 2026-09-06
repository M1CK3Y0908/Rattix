package client.m1ck3y.rattix.module.impl;

import client.m1ck3y.rattix.module.Category;
import client.m1ck3y.rattix.module.Module;
import client.m1ck3y.rattix.module.clickgui.ModeSetting;
import client.m1ck3y.rattix.module.clickgui.NumberSetting;

public class Speed extends Module {
    public ModeSetting mode = new ModeSetting("Mode", "Watchdog", "Movement", new String[]{"Watchdog", "BHop", "Vanilla"});
    public NumberSetting speed = new NumberSetting("Speed", 1.2, 0.5, 3.0, 0.1, "Movement");

    public Speed() {
        super("Speed", "Increases movement speed.", Category.MOVEMENT);
        addSettings(mode, speed);
    }
}
