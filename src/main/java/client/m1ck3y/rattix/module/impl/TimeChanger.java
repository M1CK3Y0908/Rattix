package client.m1ck3y.rattix.module.impl;

import client.m1ck3y.rattix.module.Category;
import client.m1ck3y.rattix.module.Module;
import client.m1ck3y.rattix.module.clickgui.ModeSetting;
import client.m1ck3y.rattix.module.clickgui.NumberSetting;

public class TimeChanger extends Module {
    public ModeSetting mode = new ModeSetting("Mode", "Day", "Day", "Sunset", "Night", "Custom");
    public NumberSetting customTime = new NumberSetting("Custom Time", 6000, 0, 24000, 500);

    public TimeChanger() {
        super("Time Changer", "Changes client-side world time visually.", Category.RENDER);
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
