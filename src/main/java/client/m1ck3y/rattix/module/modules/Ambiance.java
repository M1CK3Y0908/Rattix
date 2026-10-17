package client.m1ck3y.rattix.module.modules;

import client.m1ck3y.rattix.module.Category;
import client.m1ck3y.rattix.module.Module;
import client.m1ck3y.rattix.module.modules.ClickGUI.CheckBoxSetting;
import client.m1ck3y.rattix.module.modules.ClickGUI.ColorSetting;
import client.m1ck3y.rattix.module.modules.ClickGUI.NumberSetting;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Augustus 风格的 Ambiance (环境与氛围) 模块。
 * 允许玩家自由定制世界时间、时间流速、天气（雨/雪/雷暴）、世界光照色调、迷雾颜色与迷雾可视距离。
 */
public class Ambiance extends Module {
    private static Ambiance instance;

    // Time & Speed
    public final NumberSetting time = new NumberSetting("Time", 18000.0, 0.0, 24000.0, 100.0, "Time");
    public final NumberSetting timeSpeed = new NumberSetting("Time Speed", 0.0, 0.0, 20.0, 0.5, "Time");

    // Weather
    public final CheckBoxSetting rain = new CheckBoxSetting("Rain", false, "Weather");
    public final CheckBoxSetting snow = new CheckBoxSetting("Snow", false, "Weather");
    public final CheckBoxSetting thunder = new CheckBoxSetting("Thunder", false, "Weather");

    // World Color
    public final CheckBoxSetting customWorldColor = new CheckBoxSetting("Custom World Color", false, "World");
    public final ColorSetting worldColor = new ColorSetting("World Color", 0xFF38B6FF, "World").withParent(customWorldColor);

    // Fog
    public final CheckBoxSetting customFogColor = new CheckBoxSetting("Custom Fog Color", false, "Fog");
    public final ColorSetting fogColor = new ColorSetting("Fog Color", 0xFF808080, "Fog").withParent(customFogColor);
    public final CheckBoxSetting customFogDistance = new CheckBoxSetting("Custom Fog Distance", false, "Fog");
    public final NumberSetting fogDistance = new NumberSetting("Fog Distance", 100.0, 10.0, 500.0, 5.0, "Fog").withParent(customFogDistance);

    private double currentTime = 18000.0;

    public Ambiance() {
        super("Ambiance", "Change the time, worldcolor, fogcolor and more.", Category.VISUAL);
        instance = this;
        addSettings(
                time, timeSpeed,
                rain, snow, thunder,
                customWorldColor, worldColor,
                customFogColor, fogColor,
                customFogDistance, fogDistance
        );
    }

    public static Ambiance getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        MinecraftForge.EVENT_BUS.register(this);
        currentTime = time.getValue();
    }

    @Override
    public void onDisable() {
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    @Override
    public void onTick() {
        if (!isEnabled() || mc.theWorld == null) return;

        // 1. 时间与时间流速控制
        double speed = timeSpeed.getValue();
        if (speed > 0.0) {
            currentTime = (currentTime + speed) % 24000.0;
            mc.theWorld.setWorldTime((long) currentTime);
        } else {
            currentTime = time.getValue();
            mc.theWorld.setWorldTime((long) currentTime);
        }

        // 2. 天气控制
        if (rain.getValue() || snow.getValue()) {
            mc.theWorld.setRainStrength(1.0F);
        } else {
            mc.theWorld.setRainStrength(0.0F);
        }

        if (thunder.getValue()) {
            mc.theWorld.setThunderStrength(1.0F);
        } else {
            mc.theWorld.setThunderStrength(0.0F);
        }
    }

    @SubscribeEvent
    public void onFogColors(EntityViewRenderEvent.FogColors event) {
        if (isEnabled() && customFogColor.getValue()) {
            int color = fogColor.getRGB();
            event.red = ((color >> 16) & 0xFF) / 255.0f;
            event.green = ((color >> 8) & 0xFF) / 255.0f;
            event.blue = (color & 0xFF) / 255.0f;
        }
    }
}
