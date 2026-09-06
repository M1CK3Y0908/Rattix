package client.m1ck3y.rattix.module.impl;

import client.m1ck3y.rattix.module.Category;
import client.m1ck3y.rattix.module.Module;
import client.m1ck3y.rattix.module.clickgui.ModeSetting;

public class Criticals extends Module {
    public ModeSetting mode = new ModeSetting("Mode", "Packet", "Miscellaneous", new String[]{"Packet", "Jump", "MiniJump"});

    public Criticals() {
        super("Criticals", "Forces critical hits on every attack.", Category.COMBAT);
        addSettings(mode);
    }
}
