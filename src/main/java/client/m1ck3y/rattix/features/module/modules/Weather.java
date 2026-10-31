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
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Field;
import java.util.*;

/**
 * 参考 Miralune, Augustus 与 RavenXD 架构实现的天气系统 (Weather) 模块。
 * 支持 Vanilla / Clear / Rain / Snow 模式切换与平滑强度过渡 (TransitionSeconds)，
 * 支持自定义雨雪颜色、下落速度、风力偏移 (WindX/Z)、密度、渲染半径，
 * 并内置逼真的动态雷暴落雷引擎 (Lightning) 与立体声雷鸣。
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

    // Weather mode & strength transitions (参考 Miralune)
    public final ModeSetting mode = new ModeSetting("Weather", "Clear", "Weather", new String[]{"Vanilla", "Clear", "Rain", "Snow"});
    public final NumberSetting intensity = new NumberSetting("Intensity", 100.0, 0.0, 100.0, 5.0, "Weather");
    public final NumberSetting transitionSeconds = new NumberSetting("TransitionSeconds", 2.0, 0.0, 30.0, 1.0, "Weather");

    // Dynamic Thunder & Lightning
    public final CheckBoxSetting thunder = new CheckBoxSetting("Thunder", false, "Weather");
    public final ModeSetting lightningMode = new ModeSetting("Lightning", "Frequent", "Weather", new String[]{"Vanilla", "Uncommon", "Common", "Frequent", "Intense", "Chaotic"}).withParent(thunder);
    public final CheckBoxSetting thunderSound = new CheckBoxSetting("ThunderSound", true, "Weather").withParent(thunder);

    // Rain & Snow Customization (参考 Miralune)
    public final CheckBoxSetting customRain = new CheckBoxSetting("CustomRain", false, "Precipitation");
    public final ColorSetting rainColor = new ColorSetting("RainColor", 0xFF80A0FF, "Precipitation").withParent(customRain);
    public final ColorSetting snowColor = new ColorSetting("SnowColor", 0xFFFFFFFF, "Precipitation").withParent(customRain);
    public final NumberSetting fallSpeed = new NumberSetting("FallSpeed", 1.0, 0.1, 4.0, 0.1, "Precipitation").withParent(customRain);
    public final NumberSetting rainSize = new NumberSetting("RainSize", 1.0, 0.1, 5.0, 0.1, "Precipitation").withParent(customRain);
    public final NumberSetting rainOpacity = new NumberSetting("RainOpacity", 100.0, 0.0, 100.0, 1.0, "Precipitation").withParent(customRain);
    public final NumberSetting density = new NumberSetting("Density", 100.0, 0.0, 100.0, 5.0, "Precipitation").withParent(customRain);
    public final NumberSetting windX = new NumberSetting("WindX", 0.0, -100.0, 100.0, 5.0, "Precipitation").withParent(customRain);
    public final NumberSetting windZ = new NumberSetting("WindZ", 0.0, -100.0, 100.0, 5.0, "Precipitation").withParent(customRain);
    public final NumberSetting radius = new NumberSetting("Radius", 10.0, 2.0, 15.0, 1.0, "Precipitation").withParent(customRain);

    // Weather strength animation state (参考 Miralune 实现)
    private World strengthWorld;
    private float previousStrength;
    private float currentStrength;
    private long lastUpdateNanos;

    private long savedRain = -1L;
    private long savedThunder = -1L;
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
        super("Weather", "Customise client weather, rain/snow visuals and dynamic lightning.", Category.VISUAL);
        instance = this;
        addSettings(
                mode, intensity, transitionSeconds,
                thunder, lightningMode, thunderSound,
                customRain, rainColor, snowColor, fallSpeed, rainSize, rainOpacity, density, windX, windZ, radius
        );
    }

    public static Weather getInstance() {
        return instance;
    }

    public boolean isWeatherVanilla() {
        return isEnabled() && "Vanilla".equalsIgnoreCase(mode.getValue());
    }

    public boolean isWeatherClear() {
        return isEnabled() && "Clear".equalsIgnoreCase(mode.getValue());
    }

    public boolean isWeatherRain() {
        return isEnabled() && "Rain".equalsIgnoreCase(mode.getValue());
    }

    public boolean isWeatherSnow() {
        return isEnabled() && "Snow".equalsIgnoreCase(mode.getValue());
    }

    public boolean isRainingActive() {
        if (!isEnabled()) return false;
        if (isWeatherClear() || isWeatherVanilla()) return false;
        return isWeatherRain() || isWeatherSnow() || customRain.getValue();
    }

    private float targetStrength() {
        if (!isEnabled() || isWeatherVanilla() || isWeatherClear()) return 0.0F;
        return (float) (intensity.getValue() / 100.0);
    }

    private void bindWorld(World world) {
        if (strengthWorld == world) return;
        strengthWorld = world;
        previousStrength = currentStrength = world.rainingStrength;
        lastUpdateNanos = 0L;
    }

    /**
     * 计算并平滑插值降雨强度（参考 Miralune 实现）
     */
    public float rainStrength(World world, float partialTicks) {
        if (!isEnabled() || isWeatherVanilla()) return world.rainingStrength;
        bindWorld(world);
        if (transitionSeconds.getValue() <= 0.0) return targetStrength();
        float partial = Math.max(0.0F, Math.min(1.0F, partialTicks));
        return previousStrength + (currentStrength - previousStrength) * partial;
    }

    private static float advanceStrength(float current, float target, float seconds, double duration) {
        if (duration <= 0.0) return target;
        float step = Math.max(0.0F, seconds) / (float) duration;
        return current < target ? Math.min(target, current + step) : Math.max(target, current - step);
    }

    @Override
    public void onEnable() {
        if (mc.theWorld != null && mc.theWorld.getWorldInfo() != null) {
            savedRain = mc.theWorld.getWorldInfo().isRaining() ? 1L : 0L;
            savedThunder = mc.theWorld.getWorldInfo().isThundering() ? 1L : 0L;
        }
        resetStrength();
    }

    @Override
    public void onDisable() {
        if (mc.theWorld != null && mc.theWorld.getWorldInfo() != null) {
            if (savedRain != -1L) {
                mc.theWorld.getWorldInfo().setRaining(savedRain == 1L);
                mc.theWorld.setRainStrength(savedRain == 1L ? 1.0F : 0.0F);
                savedRain = -1L;
            }
            if (savedThunder != -1L) {
                mc.theWorld.getWorldInfo().setThundering(savedThunder == 1L);
                mc.theWorld.setThunderStrength(savedThunder == 1L ? 1.0F : 0.0F);
                savedThunder = -1L;
            }
        }
        resetStrength();
    }

    private void resetStrength() {
        strengthWorld = null;
        previousStrength = currentStrength = 0.0F;
        lastUpdateNanos = 0L;
    }

    @Override
    public void onTick() {
        if (!isEnabled() || mc.theWorld == null) {
            resetStrength();
            return;
        }

        bindWorld(mc.theWorld);

        // 1. 平滑推进降水强度
        long now = System.nanoTime();
        float deltaSeconds = lastUpdateNanos == 0L ? 0.05F : Math.min(0.25F, (now - lastUpdateNanos) / 1000000000.0F);
        lastUpdateNanos = now;
        previousStrength = currentStrength;
        currentStrength = advanceStrength(currentStrength, targetStrength(), deltaSeconds, transitionSeconds.getValue());

        // 2. 同步更新世界雨雪与雷暴标记
        if (mc.theWorld.getWorldInfo() != null) {
            if (isRainingActive()) {
                mc.theWorld.getWorldInfo().setRaining(true);
                mc.theWorld.setRainStrength(currentStrength);
            } else if (isWeatherClear()) {
                mc.theWorld.getWorldInfo().setRaining(false);
                mc.theWorld.setRainStrength(0.0F);
            }

            if (thunder.getValue() && !isWeatherClear()) {
                mc.theWorld.getWorldInfo().setThundering(true);
                mc.theWorld.setThunderStrength(1.0F);
            } else if (isWeatherClear()) {
                mc.theWorld.getWorldInfo().setThundering(false);
                mc.theWorld.setThunderStrength(0.0F);
            }
        }

        // 3. 动态落雷引擎
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
                mc.theWorld.playSound(
                        targetPos.getX(), targetPos.getY(), targetPos.getZ(),
                        "ambient.weather.thunder", 10.0F,
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
