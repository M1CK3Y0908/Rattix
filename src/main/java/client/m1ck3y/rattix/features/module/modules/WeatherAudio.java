package client.m1ck3y.rattix.features.module.modules;

import client.m1ck3y.rattix.features.module.Category;
import client.m1ck3y.rattix.features.module.Module;
import client.m1ck3y.rattix.features.module.modules.ClickGUI.CheckBoxSetting;
import client.m1ck3y.rattix.features.module.modules.ClickGUI.NumberSetting;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.ResourceLocation;

/**
 * 参考 Miralune WeatherAudio 架构实现的天气音效模块：
 * 1. 雨声/雷声音量独立控制 (RainVolume / ThunderVolume)；
 * 2. 环境飘雪微风音效 (SnowAmbience)。
 */
public class WeatherAudio extends Module {
    private static WeatherAudio instance;
    private long lastAmbientTick = Long.MIN_VALUE;

    public final NumberSetting rainVolume = new NumberSetting("RainVolume", 100.0, 0.0, 100.0, 5.0, "Volume");
    public final NumberSetting thunderVolume = new NumberSetting("ThunderVolume", 100.0, 0.0, 100.0, 5.0, "Volume");
    public final CheckBoxSetting snowAmbience = new CheckBoxSetting("SnowAmbience", false, "Ambience");
    public final NumberSetting snowVolume = new NumberSetting("SnowVolume", 20.0, 0.0, 100.0, 5.0, "Ambience").withParent(snowAmbience);

    public WeatherAudio() {
        super("WeatherAudio", "Customise rain and thunder sound volume, and snow ambience.", Category.VISUAL);
        instance = this;
        addSettings(rainVolume, thunderVolume, snowAmbience, snowVolume);
    }

    public static WeatherAudio getInstance() {
        return instance;
    }

    @Override
    public void onTick() {
        if (!isEnabled() || !snowAmbience.getValue() || mc.theWorld == null || mc.thePlayer == null) return;
        Weather weather = Weather.getInstance();
        if (weather == null || weather.activeMode() != 3 || !mc.theWorld.canSeeSky(mc.thePlayer.getPosition())) return;
        long tick = mc.theWorld.getTotalWorldTime();
        if (lastAmbientTick != Long.MIN_VALUE && tick >= lastAmbientTick && tick - lastAmbientTick < 100) return;
        lastAmbientTick = tick;

        mc.getSoundHandler().playSound(new PositionedSoundRecord(
                new ResourceLocation("ambient.weather.rain"),
                (float) (snowVolume.getValue() / 400.0),
                0.5F,
                (float) mc.thePlayer.posX, (float) mc.thePlayer.posY, (float) mc.thePlayer.posZ
        ));
    }

    @Override
    public void onDisable() {
        lastAmbientTick = Long.MIN_VALUE;
    }
}
