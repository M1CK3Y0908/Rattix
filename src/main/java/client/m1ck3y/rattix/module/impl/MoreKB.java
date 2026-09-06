package client.m1ck3y.rattix.module.impl;

import client.m1ck3y.rattix.module.Category;
import client.m1ck3y.rattix.module.Module;
import client.m1ck3y.rattix.module.clickgui.ModeSetting;

public class MoreKB extends Module {
    public ModeSetting mode = new ModeSetting("Mode", "SprintReset", "Miscellaneous", new String[]{"SprintReset", "WTap", "Packet"});

    public MoreKB() {
        super("MoreKB", "Deals additional knockback to opponents.", Category.COMBAT);
        addSettings(mode);
    }
}
