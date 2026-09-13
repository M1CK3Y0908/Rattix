package client.m1ck3y.rattix.modules;

import client.m1ck3y.rattix.modules.clickgui.ClickGuiScreen;
import client.m1ck3y.rattix.modules.manager.Category;
import client.m1ck3y.rattix.modules.manager.Register;
import org.lwjgl.input.Keyboard;

public class ClickGUI extends Register {
    private boolean closing = false;

    public ClickGUI() {
        super("Opens the Graphical User Interface.", Category.VISUAL, Keyboard.KEY_RSHIFT);
        this.showInArrayList.setValue(false);
    }

    @Override
    public void onEnable() {
        if (mc.currentScreen == null) {
            mc.displayGuiScreen(new ClickGuiScreen());
        }
    }

    @Override
    public void onDisable() {
        if (closing) return;
        try {
            closing = true;
            if (mc.currentScreen instanceof ClickGuiScreen) {
                mc.displayGuiScreen(null);
            }
        } finally {
            closing = false;
        }
    }
}
