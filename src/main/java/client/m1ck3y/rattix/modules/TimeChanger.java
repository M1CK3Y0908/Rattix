package client.m1ck3y.rattix.modules;

import client.m1ck3y.rattix.modules.manager.Category;
import client.m1ck3y.rattix.modules.manager.Register;

import client.m1ck3y.rattix.modules.ClickGUI.ModeSetting;
import client.m1ck3y.rattix.modules.ClickGUI.NumberSetting;

public class TimeChanger extends Register {
    public ModeSetting mode = new ModeSetting("Mode", "Day", "Day", "Sunset", "Night", "Custom");
    public NumberSetting customTime = new NumberSetting("Custom Time", 6000, 0, 24000, 500);

    public TimeChanger() {
        super("Changes client-side world time visually.", Category.VISUAL);
        addSettings(mode, customTime);
    }

    @Override
    public void onTick() {
        if (!isEnabled() || mc.theWorld == null) return;

        long timeToSet = 6000;
        if (mode.is("Day")) {
            timeToSet = 1000;
        } else if (mode.is("Sunset")) {
            timeToSet = 13000;
        } else if (mode.is("Night")) {
            timeToSet = 18000;
        } else if (mode.is("Custom")) {
            timeToSet = (long) customTime.getValue().doubleValue();
        }

        mc.theWorld.setWorldTime(timeToSet);
    }
}
