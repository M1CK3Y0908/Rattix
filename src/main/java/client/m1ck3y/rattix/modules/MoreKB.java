package client.m1ck3y.rattix.modules;

import client.m1ck3y.rattix.modules.clickgui.ModeSetting;

public class MoreKB extends Register {
    public ModeSetting mode = new ModeSetting("Mode", "SprintReset", "Miscellaneous", new String[]{"SprintReset", "WTap", "Packet"});

    public MoreKB() {
        super("Deals additional knockback to opponents.", Category.COMBAT);
        addSettings(mode);
    }
}
