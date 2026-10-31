package client.m1ck3y.rattix.features.module.modules;

import client.m1ck3y.rattix.features.module.Category;
import client.m1ck3y.rattix.features.module.Module;
import client.m1ck3y.rattix.features.module.modules.ClickGUI.CheckBoxSetting;
import client.m1ck3y.rattix.features.module.modules.ClickGUI.ColorSetting;
import client.m1ck3y.rattix.features.module.modules.ClickGUI.ModeSetting;
import client.m1ck3y.rattix.features.module.modules.ClickGUI.NumberSetting;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Field;
import java.util.*;

/**
 * 完整还原 Miralune Weather 架构的天气控制模块：
 * 1. 原版气候状态解耦：不修改 WorldInfo 或破坏服务端同步，仅通过细粒度 Mixin 拦截展现层；
 * 2. 模式与平滑过渡：Clear(1) / Rain(2) / Snow(3)，支持 TransitionSeconds 平滑过渡；
 * 3. 晶莹高亮纯白默认色彩 (0xFFFFFF)，杜绝过暗发灰或偏色，允许自由调色并真实保留原版光照层次；
 * 4. 纯净材质 UV 与顶点偏移：下落速度调整与风力偏移 (WindX/Z) 不破坏遮挡模型与多重剔除；
 * 5. 动态雷暴落雷引擎 (Thunder & Lightning)。
 */
public class Weather extends Module {
    private static Weather instance;
    private static final Random RANDOM = new Random();
    private static Field activeChunkSetField;

    static {
        try {
            activeChunkSetField = ReflectionHelper.findField(World.class, "activeChunkSet", "field_72993_I");
        } catch (Exception ignored) {
        }
    }

    // 1. Weather Mode & Transitions (Clear, Rain, Snow)
    public final ModeSetting mode = new ModeSetting("Mode", "Clear", "Weather", new String[]{"Clear", "Rain", "Snow"});
    public final NumberSetting intensity = new NumberSetting("Intensity", 100.0, 0.0, 100.0, 5.0, "Weather");
    public final NumberSetting transitionSeconds = new NumberSetting("TransitionSeconds", 0.0, 0.0, 30.0, 1.0, "Weather");

    // 2. Precipitation Visuals (1:1 还原 Miralune Weather，纯白默认色 0xFFFFFFFF)
    public final ColorSetting rainColor = new ColorSetting("RainColor", 0xFFFFFFFF, "Visuals");
    public final ColorSetting snowColor = new ColorSetting("SnowColor", 0xFFFFFFFF, "Visuals");
    public final NumberSetting fallSpeed = new NumberSetting("FallSpeed", 1.0, 0.1, 4.0, 0.1, "Visuals");
    public final NumberSetting windX = new NumberSetting("WindX", 0.0, -100.0, 100.0, 5.0, "Visuals");
    public final NumberSetting windZ = new NumberSetting("WindZ", 0.0, -100.0, 100.0, 5.0, "Visuals");
    public final NumberSetting density = new NumberSetting("Density", 100.0, 0.0, 100.0, 5.0, "Visuals");
    public final NumberSetting radius = new NumberSetting("Radius", 10.0, 2.0, 15.0, 1.0, "Visuals");

    // 3. Dynamic Thunder & Lightning
    public final CheckBoxSetting thunder = new CheckBoxSetting("Thunder", false, "Thunder");
    public final ModeSetting lightningMode = new ModeSetting("Lightning", "Frequent", "Thunder", new String[]{"Vanilla", "Uncommon", "Common", "Frequent", "Intense", "Chaotic"}).withParent(thunder);
    public final CheckBoxSetting thunderSound = new CheckBoxSetting("ThunderSound", true, "Thunder").withParent(thunder);

    // Weather strength animation state (Miralune 原理)
    private World strengthWorld;
    private float previousStrength;
    private float currentStrength;
    private long lastUpdateNanos;
    private long lastThunderTime = 0L;

    public enum LightningMode {
        VANILLA, UNCOMMON, COMMON, FREQUENT, INTENSE, CHAOTIC;

        public boolean shouldStrike() {
            if (this == VANILLA) {
                return RANDOM.nextInt(100000) == 0;
            }
            double d = Math.random();
            switch (this) {
                case UNCOMMON: return d > 0.9999;
                case COMMON: return d > 0.999;
                case FREQUENT: return d > 0.99;
                case INTENSE: return d > 0.9;
                case CHAOTIC: return d > 0.3;
                default: return false;
            }
        }

        public static LightningMode fromString(String mode) {
            for (LightningMode m : values()) {
                if (m.name().equalsIgnoreCase(mode)) {
                    return m;
                }
            }
            return FREQUENT;
        }
    }

    public Weather() {
        super("Weather", "Customise client weather, rain/snow visuals, colors and fall dynamics.", Category.VISUAL);
        instance = this;
        addSettings(
                mode, intensity, transitionSeconds,
                rainColor, snowColor, fallSpeed, windX, windZ, density, radius,
                thunder, lightningMode, thunderSound
        );
    }

    public static Weather getInstance() {
        return instance;
    }

    /**
     * 1:1 对应 Miralune activeMode():
     * 0: VANILLA (保持原版气候状态)
     * 1: CLEAR (晴天无降水)
     * 2: RAIN (全群系强制降雨)
     * 3: SNOW (全群系强制降雪)
     */
    /**
     * 对应天气模式:
     * 0: 模块关闭 (保持原版气候状态)
     * 1: Clear (晴天无降水)
     * 2: Rain (全群系强制降雨)
     * 3: Snow (全群系强制降雪)
     */
    public int activeMode() {
        if (!isEnabled()) return 0;
        String m = mode.getValue();
        if ("Clear".equalsIgnoreCase(m)) return 1;
        if ("Rain".equalsIgnoreCase(m)) return 2;
        if ("Snow".equalsIgnoreCase(m)) return 3;
        return 1;
    }

    public boolean isWeatherVanilla() { return !isEnabled(); }
    public boolean isWeatherClear() { return isEnabled() && activeMode() == 1; }
    public boolean isWeatherRain() { return isEnabled() && activeMode() == 2; }
    public boolean isWeatherSnow() { return isEnabled() && activeMode() == 3; }

    public float targetStrength() {
        int m = activeMode();
        if (m == 1) return 0.0F;
        if (m >= 2) return (float) (intensity.getValue() / 100.0);
        return 0.0F;
    }

    private void bindWorld(World world) {
        if (strengthWorld == world) return;
        strengthWorld = world;
        if (transitionSeconds.getValue() <= 0.0) {
            previousStrength = currentStrength = targetStrength();
        } else {
            previousStrength = currentStrength = world.rainingStrength;
        }
        lastUpdateNanos = 0L;
    }

    /**
     * 展现层平滑插值强度 (1:1 还原 Miralune rainStrength)
     */
    public float rainStrength(World world, float partialTicks) {
        bindWorld(world);
        if (transitionSeconds.getValue() <= 0.0) return targetStrength();
        float partial = MathHelper.clamp_float(partialTicks, 0.0F, 1.0F);
        return previousStrength + (currentStrength - previousStrength) * partial;
    }

    private static float advanceStrength(float current, float target, float seconds, double duration) {
        if (duration <= 0.0) return target;
        float step = Math.max(0.0F, seconds) / (float) duration;
        return current < target ? Math.min(target, current + step) : Math.max(target, current - step);
    }

    @Override
    public void onEnable() {
        if (mc.theWorld != null) {
            bindWorld(mc.theWorld);
            if (transitionSeconds.getValue() <= 0.0) {
                previousStrength = currentStrength = targetStrength();
            }
        }
    }

    @Override
    public void onDisable() {
        resetStrength();
    }

    private void resetStrength() {
        strengthWorld = null;
        previousStrength = currentStrength = 0.0F;
        lastUpdateNanos = 0L;
    }

    @Override
    public void onTick() {
        // 遵循 Miralune 规范：在 activeMode == 0 或世界为空时重置动画并退出，绝不调用 setRaining 污染服务端
        if (activeMode() == 0 || mc.theWorld == null) {
            resetStrength();
            if (thunder.getValue() && isEnabled()) {
                this.strike();
            }
            return;
        }

        bindWorld(mc.theWorld);

        if (transitionSeconds.getValue() <= 0.0) {
            previousStrength = currentStrength = targetStrength();
        } else {
            long now = System.nanoTime();
            float deltaSeconds = lastUpdateNanos == 0L ? 0.05F : Math.min(0.25F, (now - lastUpdateNanos) / 1000000000.0F);
            lastUpdateNanos = now;
            previousStrength = currentStrength;
            currentStrength = advanceStrength(currentStrength, targetStrength(), deltaSeconds, transitionSeconds.getValue());
        }

        // 动态落雷
        if (thunder.getValue()) {
            this.strike();
        }
    }

    @SuppressWarnings("unchecked")
    private void strike() {
        if (mc.theWorld == null || mc.thePlayer == null || mc.isGamePaused()) return;

        LightningMode lMode = LightningMode.fromString(lightningMode.getValue());
        Set<ChunkCoordIntPair> set = null;
        if (activeChunkSetField != null) {
            try {
                set = (Set<ChunkCoordIntPair>) activeChunkSetField.get(mc.theWorld);
            } catch (Exception ignored) {
            }
        }
        if (set == null || set.isEmpty()) {
            set = new HashSet<>();
            int px = (int) mc.thePlayer.posX >> 4;
            int pz = (int) mc.thePlayer.posZ >> 4;
            int rad = 5;
            for (int cx = px - rad; cx <= px + rad; cx++) {
                for (int cz = pz - rad; cz <= pz + rad; cz++) {
                    set.add(new ChunkCoordIntPair(cx, cz));
                }
            }
        }

        long currentTimeMs = System.currentTimeMillis();
        for (ChunkCoordIntPair c : set) {
            if (!lMode.shouldStrike()) continue;
            int x = c.chunkXPos * 16;
            int z = c.chunkZPos * 16;
            BlockPos targetPos = this.target(new BlockPos(x + RANDOM.nextInt(16), 0, z + RANDOM.nextInt(16)));
            if (targetPos == null || !mc.theWorld.canSeeSky(targetPos)) continue;

            mc.theWorld.addWeatherEffect(new EntityLightningBolt(mc.theWorld, targetPos.getX(), targetPos.getY(), targetPos.getZ()));

            if (thunderSound.getValue() && Math.abs(currentTimeMs - lastThunderTime) > 250L) {
                WeatherAudio audio = WeatherAudio.getInstance();
                float vol = (audio != null && audio.isEnabled()) ? (10.0F * audio.thunderVolume.getValue().floatValue() / 100.0F) : 10.0F;
                mc.theWorld.playSound(
                        targetPos.getX(), targetPos.getY(), targetPos.getZ(),
                        "ambient.weather.thunder", vol,
                        0.8F + RANDOM.nextFloat() * 0.2F, false
                );
                lastThunderTime = currentTimeMs;
            }
        }
    }

    private BlockPos target(BlockPos blockPos) {
        BlockPos top = mc.theWorld.getPrecipitationHeight(blockPos);
        BlockPos sky = new BlockPos(top.getX(), mc.theWorld.getHeight(), top.getZ());
        AxisAlignedBB box = new AxisAlignedBB(top, sky).expand(3.0, 3.0, 3.0);
        List<EntityLivingBase> list = mc.theWorld.getEntitiesWithinAABB(EntityLivingBase.class, box, this::canStrike);
        return !list.isEmpty() ? list.get(RANDOM.nextInt(list.size())).getPosition() : top;
    }

    private boolean canStrike(EntityLivingBase e) {
        return e != null && e.isEntityAlive() && mc.theWorld != null && mc.theWorld.canSeeSky(e.getPosition());
    }
}
