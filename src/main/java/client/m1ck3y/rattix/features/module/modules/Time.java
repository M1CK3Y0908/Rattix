package client.m1ck3y.rattix.features.module.modules;

import client.m1ck3y.rattix.features.module.Category;
import client.m1ck3y.rattix.features.module.Module;
import client.m1ck3y.rattix.features.module.modules.ClickGUI.ModeSetting;
import client.m1ck3y.rattix.features.module.modules.ClickGUI.NumberSetting;

import java.util.Locale;

/**
 * 参考 Miralune 与 Augustus 架构实现的时间定制模块。
 * 支持 Dawn, Noon, Sunset, Midnight 预设及 Custom 自定义时间，
 * 并支持平滑时间流速加速与平滑天体角度计算，不修改服务端真实天数与月相。
 */
public class Time extends Module {
    private static Time instance;

    public final ModeSetting mode = new ModeSetting("Mode", "Custom", "Time", new String[]{"Dawn", "Noon", "Sunset", "Midnight", "Custom"});
    public final NumberSetting customTime = new NumberSetting("Time", 18000.0, 0.0, 24000.0, 100.0, "Time");
    public final NumberSetting timeSpeed = new NumberSetting("TimeSpeed", 0.0, 0.0, 20.0, 0.5, "Time");

    private double currentTime = 18000.0;
    private long savedTime = -1L;

    public Time() {
        super("Time", "Customise client world time and time speed.", Category.VISUAL);
        instance = this;
        addSettings(mode, customTime, timeSpeed);
    }

    public static Time getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        currentTime = getTargetTime();
        if (mc.theWorld != null) {
            savedTime = mc.theWorld.getWorldTime();
        }
    }

    @Override
    public void onDisable() {
        if (mc.theWorld != null && savedTime != -1L) {
            mc.theWorld.setWorldTime(savedTime);
            savedTime = -1L;
        }
    }

    @Override
    public void onTick() {
        if (!isEnabled() || mc.theWorld == null) return;

        double speed = timeSpeed.getValue();
        if (speed > 0.0) {
            currentTime = (currentTime + speed * 10.0) % 24000.0;
        } else {
            currentTime = getTargetTime();
        }
    }

    public double getTargetTime() {
        String m = mode.getValue().toUpperCase(Locale.ROOT);
        switch (m) {
            case "DAWN":
                return 0.0;
            case "NOON":
                return 6000.0;
            case "SUNSET":
                return 12000.0;
            case "MIDNIGHT":
                return 18000.0;
            case "CUSTOM":
            default:
                return customTime.getValue();
        }
    }

    /**
     * 计算视觉呈现的世界时间：保留原版天数与月相循环（参考 Miralune 实现）
     */
    public long visualTime(long vanillaTime) {
        if (!isEnabled()) return vanillaTime;
        long t = (long) (getCurrentTime() % 24000.0);
        if (t < 0) t += 24000L;
        return Math.floorDiv(vanillaTime, 24000L) * 24000L + t;
    }

    public double getCurrentTime() {
        if (timeSpeed.getValue() > 0.0) {
            return currentTime;
        }
        return getTargetTime();
    }

    public void setSavedTime(long time) {
        this.savedTime = time;
    }

    /**
     * 计算天体角度，供 MixinWorld.getCelestialAngle 调用
     */
    public float getCelestialAngle(float partialTicks) {
        double speed = timeSpeed.getValue();
        double t = speed > 0.0 ? (currentTime + speed * 10.0 * partialTicks) : getTargetTime();
        t = t % 24000.0;
        if (t < 0.0) t += 24000.0;
        float f = (float) (t / 24000.0 - 0.25);
        if (f < 0.0F) f += 1.0F;
        if (f > 1.0F) f -= 1.0F;
        float f1 = 1.0F - (float) ((Math.cos((double) f * Math.PI) + 1.0) / 2.0);
        f = f + (f1 - f) / 3.0F;
        return f;
    }
}
