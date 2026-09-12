package client.m1ck3y.rattix.modules;

import client.m1ck3y.rattix.modules.clickgui.ModeSetting;
import client.m1ck3y.rattix.modules.clickgui.NumberSetting;

public class Speed extends Register {
    public ModeSetting mode = new ModeSetting("Mode", "Watchdog", "Movement", new String[]{"Watchdog", "BHop", "Vanilla"});
    public NumberSetting speed = new NumberSetting("Speed", 1.2, 0.5, 3.0, 0.1, "Movement");

    public Speed() {
        super("Increases movement speed.", Category.MOVEMENT);
        addSettings(mode, speed);
    }
}
