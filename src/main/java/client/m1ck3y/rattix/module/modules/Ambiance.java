package client.m1ck3y.rattix.module.modules;

import client.m1ck3y.rattix.module.Category;
import client.m1ck3y.rattix.module.Module;
import client.m1ck3y.rattix.module.modules.ClickGUI.CheckBoxSetting;
import client.m1ck3y.rattix.module.modules.ClickGUI.ColorSetting;
import client.m1ck3y.rattix.module.modules.ClickGUI.ModeSetting;
import client.m1ck3y.rattix.module.modules.ClickGUI.NumberSetting;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Augustus 与 RavenXD 风格融合优化的 Ambiance (环境与氛围) 模块。
 * 允许玩家自由定制世界时间、时间流速、天气（雨/雪/雷暴与动态雷击引擎）、植被颜色（草地/树叶等）、迷雾颜色与迷雾可视距离。
 */
public class Ambiance extends Module {
    private static Ambiance instance;
    private static final Random RANDOM = new Random();
    private static Field activeChunkSetField;

    static {
        try {
            activeChunkSetField = ReflectionHelper.findField(World.class, "activeChunkSet", "field_72993_I");
        } catch (Exception ignored) {
        }
    }

    // Time & Speed
    public final NumberSetting time = new NumberSetting("Time", 18000.0, 0.0, 24000.0, 100.0, "Time");
    public final NumberSetting timeSpeed = new NumberSetting("Time Speed", 0.0, 0.0, 20.0, 0.5, "Time");

    // Weather
    public final CheckBoxSetting rain = new CheckBoxSetting("Rain", false, "Weather");
    public final CheckBoxSetting snow = new CheckBoxSetting("Snow", false, "Weather");
    public final CheckBoxSetting thunder = new CheckBoxSetting("Thunder", false, "Weather");
    public final ModeSetting lightningMode = new ModeSetting("Lightning", "Frequent", "Weather", new String[]{"Vanilla", "Uncommon", "Common", "Frequent", "Intense", "Chaotic"}).withParent(thunder);
    public final CheckBoxSetting thunderSound = new CheckBoxSetting("Thunder Sound", true, "Weather").withParent(thunder);

    // World Color (植物与植被颜色：草地、树叶、藤蔓等)
    public final CheckBoxSetting customWorldColor = new CheckBoxSetting("Custom World Color", false, "World");
    public final ColorSetting worldColor = new ColorSetting("World Color", 0xFF38B6FF, "World").withParent(customWorldColor);

    // Fog
    public final CheckBoxSetting customFogColor = new CheckBoxSetting("Custom Fog Color", false, "Fog");
    public final ColorSetting fogColor = new ColorSetting("Fog Color", 0xFF808080, "Fog").withParent(customFogColor);
    public final CheckBoxSetting customFogDistance = new CheckBoxSetting("Custom Fog Distance", false, "Fog");
    public final NumberSetting fogDistance = new NumberSetting("Fog Distance", 100.0, 10.0, 500.0, 5.0, "Fog").withParent(customFogDistance);

    private double currentTime = 18000.0;
    private long savedTime = -1L;
    private long savedRain = -1L;
    private long savedThunder = -1L;
    private long lastThunderTime = 0L;

    private boolean prevWorldColorActive = false;
    private int prevWorldColor = 0xFF38B6FF;

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

    public Ambiance() {
        super("Ambiance", "Change the time, weather, worldcolor, fog and more.", Category.VISUAL);
        instance = this;
        addSettings(
                time, timeSpeed,
                rain, snow, thunder, lightningMode, thunderSound,
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

        // 备份服务端真实天气与时间状态
        if (mc.theWorld != null) {
            savedTime = mc.theWorld.getWorldTime();
            if (mc.theWorld.getWorldInfo() != null) {
                savedRain = mc.theWorld.getWorldInfo().isRaining() ? 1L : 0L;
                savedThunder = mc.theWorld.getWorldInfo().isThundering() ? 1L : 0L;
            }
        }

        prevWorldColorActive = customWorldColor.getValue();
        prevWorldColor = worldColor.getRGB();
        if (customWorldColor.getValue() && mc.renderGlobal != null) {
            mc.renderGlobal.loadRenderers();
        }
    }

    @Override
    public void onDisable() {
        MinecraftForge.EVENT_BUS.unregister(this);

        // 平滑复原服务端时间与天气状态（参考 RavenXD 实现）
        if (mc.theWorld != null) {
            if (savedTime != -1L) {
                mc.theWorld.setWorldTime(savedTime);
                savedTime = -1L;
            }
            if (savedRain != -1L && mc.theWorld.getWorldInfo() != null) {
                mc.theWorld.getWorldInfo().setRaining(savedRain == 1L);
                mc.theWorld.setRainStrength(savedRain == 1L ? 1.0F : 0.0F);
                savedRain = -1L;
            }
            if (savedThunder != -1L && mc.theWorld.getWorldInfo() != null) {
                mc.theWorld.getWorldInfo().setThundering(savedThunder == 1L);
                mc.theWorld.setThunderStrength(savedThunder == 1L ? 1.0F : 0.0F);
                savedThunder = -1L;
            }
        }

        if (customWorldColor.getValue() && mc.renderGlobal != null) {
            mc.renderGlobal.loadRenderers();
        }
    }

    @Override
    public void onTick() {
        if (!isEnabled() || mc.theWorld == null) return;

        // 1. 时间步进（时间流速大于 0 时平滑递增）
        double speed = timeSpeed.getValue();
        if (speed > 0.0) {
            currentTime = (currentTime + speed * 10.0) % 24000.0;
        } else {
            currentTime = time.getValue();
        }

        // 2. 天气状态同步
        if (mc.theWorld.getWorldInfo() != null) {
            if (rain.getValue() || snow.getValue()) {
                mc.theWorld.getWorldInfo().setRaining(true);
                mc.theWorld.setRainStrength(1.0F);
            }
            if (thunder.getValue()) {
                mc.theWorld.getWorldInfo().setThundering(true);
                mc.theWorld.setThunderStrength(1.0F);
            }
        }

        // 3. 动态雷暴雷击引擎（参考 RavenXD 实现）
        if (thunder.getValue()) {
            this.strike();
        }

        // 4. 植物/世界颜色变更时实时重构区块网格
        boolean currentActive = customWorldColor.getValue();
        int currentColor = worldColor.getRGB();
        if (currentActive != prevWorldColorActive || (currentActive && currentColor != prevWorldColor)) {
            prevWorldColorActive = currentActive;
            prevWorldColor = currentColor;
            if (mc.renderGlobal != null) {
                mc.renderGlobal.loadRenderers();
            }
        }
    }

    /**
     * 动态雷暴生成引擎（参考 RavenXD）：
     * 遍历玩家周围活跃区块，在露天实体或最高地表生成真实的闪电实体 EntityLightningBolt 与 3D 空间雷声音效。
     */
    @SuppressWarnings("unchecked")
    private void strike() {
        if (mc.theWorld == null || mc.thePlayer == null || mc.isGamePaused()) return;

        LightningMode mode = LightningMode.fromString(lightningMode.getValue());
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
            int radius = 5;
            for (int cx = px - radius; cx <= px + radius; cx++) {
                for (int cz = pz - radius; cz <= pz + radius; cz++) {
                    set.add(new ChunkCoordIntPair(cx, cz));
                }
            }
        }

        long currentTimeMs = System.currentTimeMillis();
        for (ChunkCoordIntPair c : set) {
            if (!mode.shouldStrike()) continue;
            int x = c.chunkXPos * 16;
            int z = c.chunkZPos * 16;
            BlockPos targetPos = this.target(new BlockPos(x + RANDOM.nextInt(16), 0, z + RANDOM.nextInt(16)));
            if (targetPos == null || !mc.theWorld.canSeeSky(targetPos)) continue;

            // 生成客户端视觉闪电（不造成真实破坏，具有真实光照、天际放电与地面火光视觉）
            mc.theWorld.addWeatherEffect(new EntityLightningBolt(mc.theWorld, targetPos.getX(), targetPos.getY(), targetPos.getZ()));

            // 播放带有随机声调与防爆音间隔的立体声雷鸣
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

    /**
     * 寻找垂直方向露天的生物实体（玩家/生物），若无则落雷在最高地表方块
     */
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

    /**
     * 记录服务端发来的真实时间，供关闭模块时恢复
     */
    public void setSavedTime(long time) {
        this.savedTime = time;
    }

    /**
     * 获取当前环境时间，供 MixinWorld.getWorldTime 拦截调用
     */
    public double getCurrentTime() {
        if (timeSpeed.getValue() > 0.0) {
            return currentTime;
        }
        return time.getValue();
    }

    /**
     * 计算天体角度，供 MixinWorld.getCelestialAngle 拦截调用
     * 当 timeSpeed == 0 时，角度严格基于 time.getValue()，不参与 partialTicks 抖动，天空绝对稳定不闪烁；
     * 当 timeSpeed > 0 时，结合 partialTicks 进行高帧率平滑插值旋转。
     */
    public float getCelestialAngle(float partialTicks) {
        double speed = timeSpeed.getValue();
        double t = speed > 0.0 ? (currentTime + speed * 10.0 * partialTicks) : time.getValue();
        t = t % 24000.0;
        if (t < 0.0) t += 24000.0;
        float f = (float) (t / 24000.0 - 0.25);
        if (f < 0.0F) f += 1.0F;
        if (f > 1.0F) f -= 1.0F;
        float f1 = 1.0F - (float) ((Math.cos((double) f * Math.PI) + 1.0) / 2.0);
        f = f + (f1 - f) / 3.0F;
        return f;
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
