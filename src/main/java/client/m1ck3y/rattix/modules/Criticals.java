package client.m1ck3y.rattix.modules;

import client.m1ck3y.rattix.modules.manager.Category;
import client.m1ck3y.rattix.modules.manager.Register;

import client.m1ck3y.rattix.modules.clickgui.ModeSetting;

public class Criticals extends Register {
    public ModeSetting mode = new ModeSetting("Mode", "Packet", "Miscellaneous", new String[]{"Packet", "Jump", "MiniJump"});

    public Criticals() {
        super("Forces critical hits on every attack.", Category.COMBAT);
        addSettings(mode);
    }
}
