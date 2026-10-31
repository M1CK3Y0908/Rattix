package client.m1ck3y.rattix.injection.mixin;

import client.m1ck3y.rattix.features.module.modules.Fog;
import client.m1ck3y.rattix.features.module.modules.Weather;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManager;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer {

    @Unique
    private float environmentRainSpeed = 1.0F;
    @Unique
    private int environmentVertex;
    @Unique
    private int environmentDensity = 100;
    @Unique
    private int environmentWindX, environmentWindZ;
    @Unique
    private double environmentPrecipitationClock;
    @Unique
    private int environmentWeatherMode;
    @Unique
    private int environmentRainColor;
    @Unique
    private int environmentSnowColor;
    @Unique
    private boolean environmentSnowBatch;

    /**
     * 自定义迷雾控制 (Custom Fog Pipeline，参考 Miralune 架构实现)：
     * 支持控制迷雾是否可见、雾模型 (Linear / Exponential / Exponential_Squared)、雾密度、起止距离以及特殊水下/岩浆材质独立过滤。
     */
    @Inject(method = "setupFog", at = @At("RETURN"))
    private void onSetupFog(int startCoords, float partialTicks, CallbackInfo ci) {
        Fog fog = Fog.getInstance();
        if (fog == null || !fog.isEnabled()) {
            return;
        }

        if (!fog.visible.getValue()) {
            GlStateManager.disableFog();
            GL11.glDisable(GL11.GL_FOG);
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        if (mc != null && mc.theWorld != null) {
            Entity entity = mc.getRenderViewEntity();
            if (entity != null) {
                BlockPos pos = new BlockPos(entity.posX, entity.posY + (double) entity.getEyeHeight(), entity.posZ);
                IBlockState state = mc.theWorld.getBlockState(pos);
                if (state != null && state.getBlock() != null) {
                    Material material = state.getBlock().getMaterial();
                    if (material == Material.water && !fog.underwater.getValue()) {
                        return;
                    }
                    if (material == Material.lava && !fog.lava.getValue()) {
                        return;
                    }
                }
            }
        }

        int glMode = GL11.GL_LINEAR;
        String modelStr = fog.model.getValue();
        if ("Exponential".equalsIgnoreCase(modelStr)) {
            glMode = GL11.GL_EXP;
        } else if ("Exponential_Squared".equalsIgnoreCase(modelStr)) {
            glMode = GL11.GL_EXP2;
        }

        GlStateManager.enableFog();
        GL11.glEnable(GL11.GL_FOG);
        GlStateManager.setFog(glMode);
        GL11.glFogi(GL11.GL_FOG_MODE, glMode);
        GlStateManager.setFogDensity(fog.density.getValue().floatValue());
        GL11.glFogf(GL11.GL_FOG_DENSITY, fog.density.getValue().floatValue());

        float startDist = fog.getStartDistance(startCoords);
        float endDist = fog.getEndDistance(startCoords);

        GlStateManager.setFogStart(startDist);
        GlStateManager.setFogEnd(endDist);
        GL11.glFogf(GL11.GL_FOG_START, startDist);
        GL11.glFogf(GL11.GL_FOG_END, endDist);
    }

    // ==========================================
    // 现代降水渲染管线 (1:1 还原 Miralune MixinEnvironmentRenderer)
    // ==========================================

    @Inject(method = "renderRainSnow", at = @At("HEAD"))
    private void environmentWeatherClock(float partial, CallbackInfo ci) {
        Minecraft client = Minecraft.getMinecraft();
        environmentPrecipitationClock = (client.theWorld == null) ? 0.0 : ((client.theWorld.getTotalWorldTime() % 120000L) + partial) / 20.0;
    }

    @Inject(method = {"renderRainSnow", "addRainParticles"}, at = @At("HEAD"))
    private void environmentWeatherSnapshot(CallbackInfo callback) {
        Weather weather = Weather.getInstance();
        environmentWeatherMode = (weather == null) ? 0 : weather.activeMode();
        environmentRainColor = (weather != null && weather.isEnabled()) ? (weather.rainColor.getRGB() & 0xFFFFFF) : 0xFFFFFF;
        environmentSnowColor = (weather != null && weather.isEnabled()) ? (weather.snowColor.getRGB() & 0xFFFFFF) : 0xFFFFFF;
        environmentSnowBatch = false;
        environmentRainSpeed = (weather != null && weather.isEnabled()) ? weather.fallSpeed.getValue().floatValue() : 1.0F;
        environmentDensity = (weather != null && weather.isEnabled()) ? (int) Math.round(weather.density.getValue()) : 100;
        environmentWindX = (weather != null && weather.isEnabled()) ? (int) Math.round(weather.windX.getValue()) : 0;
        environmentWindZ = (weather != null && weather.isEnabled()) ? (int) Math.round(weather.windZ.getValue()) : 0;
        environmentVertex = 0;
    }

    @Redirect(method = {"renderRainSnow", "addRainParticles"}, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/biome/BiomeGenBase;canRain()Z"), require = 0)
    private boolean environmentCanRain(BiomeGenBase biome) {
        if (environmentWeatherMode == 2) return true;
        if (environmentWeatherMode == 3) return false;
        return biome.canRain();
    }

    @Redirect(method = "renderRainSnow", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/biome/BiomeGenBase;getEnableSnow()Z"), require = 0)
    private boolean environmentCanSnow(BiomeGenBase biome) {
        return environmentWeatherMode >= 2 ? environmentWeatherMode == 3 : biome.getEnableSnow();
    }

    @Redirect(method = "renderRainSnow", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/biome/WorldChunkManager;getTemperatureAtHeight(FI)F"), require = 0)
    private float environmentTemperatureAtHeight(WorldChunkManager manager, float temperature, int height) {
        if (environmentWeatherMode == 2) return 1.0F;
        if (environmentWeatherMode == 3) return 0.0F;
        return manager.getTemperatureAtHeight(temperature, height);
    }

    @Redirect(method = "addRainParticles", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/biome/BiomeGenBase;getFloatTemperature(Lnet/minecraft/util/BlockPos;)F"), require = 0)
    private float environmentSplashTemperature(BiomeGenBase biome, BlockPos pos) {
        if (environmentWeatherMode == 2) return 1.0F;
        if (environmentWeatherMode == 3) return 0.0F;
        return biome.getFloatTemperature(pos);
    }

    @Redirect(method = "renderRainSnow", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/texture/TextureManager;bindTexture(Lnet/minecraft/util/ResourceLocation;)V"), require = 0)
    private void environmentPrecipitationTexture(TextureManager textures, ResourceLocation texture) {
        environmentSnowBatch = texture != null && texture.getResourcePath().endsWith("snow.png");
        textures.bindTexture(texture);
    }

    @Redirect(method = "renderRainSnow", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/WorldRenderer;color(FFFF)Lnet/minecraft/client/renderer/WorldRenderer;"), require = 0)
    private WorldRenderer environmentPrecipitationColor(WorldRenderer renderer, float red, float green, float blue, float alpha) {
        int color = environmentSnowBatch ? environmentSnowColor : environmentRainColor;
        if (Math.floorMod(environmentVertex++ / 4 * 37, 100) >= environmentDensity) {
            alpha = 0.0F;
        }
        return renderer.color(
                red * ((color >> 16) & 255) / 255.0F,
                green * ((color >> 8) & 255) / 255.0F,
                blue * (color & 255) / 255.0F,
                alpha
        );
    }

    @Redirect(method = "renderRainSnow", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/WorldRenderer;tex(DD)Lnet/minecraft/client/renderer/WorldRenderer;"), require = 0)
    private WorldRenderer precipitationSpeed(WorldRenderer renderer, double u, double v) {
        double time = environmentPrecipitationClock;
        return renderer.tex(
                u + time * (environmentRainSpeed - 1.0) * 0.1,
                v + time * (environmentRainSpeed - 1.0) * (environmentSnowBatch ? 0.12 : 1.0)
        );
    }

    @Redirect(method = "renderRainSnow", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/WorldRenderer;pos(DDD)Lnet/minecraft/client/renderer/WorldRenderer;"), require = 0)
    private WorldRenderer precipitationWind(WorldRenderer renderer, double x, double y, double z) {
        if (environmentWindX != 0 || environmentWindZ != 0) {
            Entity view = Minecraft.getMinecraft().getRenderViewEntity();
            double base = view == null ? y : view.posY;
            x += (y - base) * environmentWindX / 200.0F;
            z += (y - base) * environmentWindZ / 200.0F;
        }
        return renderer.pos(x, y, z);
    }

    @ModifyConstant(method = "renderRainSnow", constant = @Constant(intValue = 5), require = 0)
    private int fastWeatherRadius(int radius) {
        Weather w = Weather.getInstance();
        return (w == null || !w.isEnabled()) ? radius : Math.max(2, (int) Math.round(w.radius.getValue() / 2.0));
    }

    @ModifyConstant(method = "renderRainSnow", constant = @Constant(intValue = 10), require = 0)
    private int fancyWeatherRadius(int radius) {
        Weather w = Weather.getInstance();
        return (w == null || !w.isEnabled()) ? radius : Math.min(15, (int) Math.round(w.radius.getValue()));
    }
}
