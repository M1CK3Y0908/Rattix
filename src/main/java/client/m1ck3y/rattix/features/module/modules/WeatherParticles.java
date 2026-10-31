package client.m1ck3y.rattix.features.module.modules;

import client.m1ck3y.rattix.features.module.Category;
import client.m1ck3y.rattix.features.module.Module;
import client.m1ck3y.rattix.features.module.modules.ClickGUI.CheckBoxSetting;
import client.m1ck3y.rattix.features.module.modules.ClickGUI.ColorSetting;
import client.m1ck3y.rattix.features.module.modules.ClickGUI.NumberSetting;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumParticleTypes;

/**
 * 参考 Miralune WeatherParticles 架构实现的天气粒子模块：
 * 1. 地面雨滴溅水粒子 (Splash Particles / EntityRainFX)：自定义溅水颜色、生成数量与水花大小；
 * 2. 额外下雪微粒 (Extra Snowflakes)：下雪天在玩家周围生成飘落的三维雪花粒子。
 */
public class WeatherParticles extends Module {
    private static WeatherParticles instance;

    public final ColorSetting splashColor = new ColorSetting("SplashColor", 0xFFB7DFFF, "Splash");
    public final NumberSetting splashAmount = new NumberSetting("SplashAmount", 100.0, 0.0, 100.0, 5.0, "Splash");
    public final NumberSetting splashSize = new NumberSetting("SplashSize", 1.0, 0.1, 3.0, 0.1, "Splash");

    public final CheckBoxSetting snowflakes = new CheckBoxSetting("ExtraSnowflakes", false, "Snowflakes");
    public final NumberSetting snowAmount = new NumberSetting("SnowAmount", 2.0, 0.0, 8.0, 1.0, "Snowflakes").withParent(snowflakes);
    public final NumberSetting snowSize = new NumberSetting("SnowSize", 0.5, 0.1, 2.0, 0.1, "Snowflakes").withParent(snowflakes);

    public WeatherParticles() {
        super("WeatherParticles", "Customise rain splash particles and extra falling snowflakes.", Category.VISUAL);
        instance = this;
        addSettings(splashColor, splashAmount, splashSize, snowflakes, snowAmount, snowSize);
    }

    public static WeatherParticles getInstance() {
        return instance;
    }

    @Override
    public void onTick() {
        if (!isEnabled() || !snowflakes.getValue()) return;
        Weather weather = Weather.getInstance();
        if (weather == null || weather.activeMode() != 3) return;
        Entity view = mc.getRenderViewEntity();
        if (view == null || mc.theWorld == null || mc.effectRenderer == null) return;

        int amount = (int) Math.round(snowAmount.getValue());
        float size = snowSize.getValue().floatValue();
        int color = weather.snowColor.getRGB() & 0xFFFFFF;
        float r = ((color >> 16) & 255) / 255.0F;
        float g = ((color >> 8) & 255) / 255.0F;
        float b = (color & 255) / 255.0F;

        for (int i = 0; i < amount; i++) {
            double a = (mc.theWorld.getTotalWorldTime() * 7L + (long) i * 31L) * 2.399963D;
            double radius = 3.0D + (double) i;
            BlockPos pos = new BlockPos(view.posX + Math.cos(a) * radius, view.posY + 5.0D, view.posZ + Math.sin(a) * radius);
            if (!mc.theWorld.canSeeSky(pos)) continue;

            EntityFX snow = mc.effectRenderer.spawnEffectParticle(
                    EnumParticleTypes.SNOW_SHOVEL.getParticleID(),
                    pos.getX(), pos.getY(), pos.getZ(),
                    0.0D, -0.02D, 0.0D
            );
            if (snow != null) {
                snow.setRBGColorF(r, g, b);
                snow.multipleParticleScaleBy(size);
            }
        }
    }
}
