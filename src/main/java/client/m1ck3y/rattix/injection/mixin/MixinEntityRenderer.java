package client.m1ck3y.rattix.injection.mixin;

import client.m1ck3y.rattix.features.module.modules.Fog;
import client.m1ck3y.rattix.features.module.modules.Weather;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraftforge.client.IRenderHandler;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer {

    @Shadow private Minecraft mc;
    @Shadow private Random random;
    @Shadow private int rendererUpdateCount;
    @Shadow public abstract void enableLightmap();
    @Shadow public abstract void disableLightmap();

    private static final float[] RAIN_X = new float[1024];
    private static final float[] RAIN_Y = new float[1024];
    private static final ResourceLocation CUSTOM_RAIN_TEXTURE = new ResourceLocation("textures/environment/rain.png");
    private static final ResourceLocation CUSTOM_SNOW_TEXTURE = new ResourceLocation("textures/environment/snow.png");

    static {
        for (int i = 0; i < 32; ++i) {
            for (int j = 0; j < 32; ++j) {
                float f = (float)(j - 16);
                float f1 = (float)(i - 16);
                float f2 = MathHelper.sqrt_float(f * f + f1 * f1);
                RAIN_X[i << 5 | j] = -f1 / f2;
                RAIN_Y[i << 5 | j] = f / f2;
            }
        }
    }

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

    /**
     * 自定义降雨/降雪渲染管线 (Custom Precipitation Pipeline，参考 Miralune 与 Augustus 架构实现)：
     * 支持雨雪颜色、下落速度 (FallSpeed)、风力偏角 (WindX/Z)、密度、渲染半径、尺寸与不透明度。
     */
    @Inject(method = "renderRainSnow", at = @At("HEAD"), cancellable = true)
    private void onRenderRainSnow(float partialTicks, CallbackInfo ci) {
        Weather weather = Weather.getInstance();
        if (weather != null && weather.isEnabled() && weather.customRain.getValue() && !weather.isWeatherClear()) {
            this.renderCustomRainSnow(partialTicks, weather);
            ci.cancel();
        }
    }

    private void renderCustomRainSnow(float partialTicks, Weather weather) {
        if (this.mc == null || this.mc.theWorld == null) {
            return;
        }

        IRenderHandler renderer = this.mc.theWorld.provider.getWeatherRenderer();
        if (renderer != null) {
            renderer.render(partialTicks, this.mc.theWorld, this.mc);
            return;
        }

        float f = this.mc.theWorld.getRainStrength(partialTicks);
        if (weather.isRainingActive()) {
            f = Math.max(f, 0.05F);
        }

        if (f <= 0.0F) {
            return;
        }

        this.enableLightmap();
        Entity entity = this.mc.getRenderViewEntity();
        if (entity == null) {
            this.disableLightmap();
            return;
        }

        World world = this.mc.theWorld;
        int i = MathHelper.floor_double(entity.posX);
        int j = MathHelper.floor_double(entity.posY);
        int k = MathHelper.floor_double(entity.posZ);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        GlStateManager.disableCull();
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.alphaFunc(516, 0.1F);
        double d0 = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double) partialTicks;
        double d1 = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double) partialTicks;
        double d2 = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double) partialTicks;
        int l = MathHelper.floor_double(d1);
        int i1 = Math.max(2, (int) Math.round(weather.radius.getValue()));

        int j1 = -1;
        float f1 = (float) this.rendererUpdateCount + partialTicks;
        worldrenderer.setTranslation(-d0, -d1, -d2);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        BlockPos.MutableBlockPos blockpos = new BlockPos.MutableBlockPos();

        float rainSize = Math.max(0.1F, weather.rainSize.getValue().floatValue());
        float rainOpacity = MathHelper.clamp_float(weather.rainOpacity.getValue().floatValue() / 100.0F, 0.0F, 1.0F);
        float densityPct = MathHelper.clamp_float(weather.density.getValue().floatValue() / 100.0F, 0.0F, 1.0F);
        float fallSpeed = Math.max(0.1F, weather.fallSpeed.getValue().floatValue());
        float windX = weather.windX.getValue().floatValue();
        float windZ = weather.windZ.getValue().floatValue();

        int rainColorRGB = weather.rainColor.getRGB();
        float rainR = ((rainColorRGB >> 16) & 0xFF) / 255.0F;
        float rainG = ((rainColorRGB >> 8) & 0xFF) / 255.0F;
        float rainB = (rainColorRGB & 0xFF) / 255.0F;
        float rainA = ((rainColorRGB >>> 24) & 0xFF) / 255.0F;

        int snowColorRGB = weather.snowColor.getRGB();
        float snowR = ((snowColorRGB >> 16) & 0xFF) / 255.0F;
        float snowG = ((snowColorRGB >> 8) & 0xFF) / 255.0F;
        float snowB = (snowColorRGB & 0xFF) / 255.0F;

        for (int k1 = k - i1; k1 <= k + i1; ++k1) {
            for (int l1 = i - i1; l1 <= i + i1; ++l1) {
                int idx = (k1 - k + 16) * 32 + l1 - i + 16;
                if (idx < 0 || idx >= 1024) continue;
                double baseD3 = (double) RAIN_X[idx] * 0.5D;
                double baseD4 = (double) RAIN_Y[idx] * 0.5D;
                blockpos.set(l1, 0, k1);
                BiomeGenBase biomegenbase = world.getBiomeGenForCoords(blockpos);

                if (biomegenbase.canRain() || biomegenbase.getEnableSnow() || weather.isRainingActive()) {
                    int j2 = world.getPrecipitationHeight(blockpos).getY();
                    int k2 = j - i1;
                    int l2 = j + i1;

                    if (k2 < j2) k2 = j2;
                    if (l2 < j2) l2 = j2;

                    int i3 = j2;
                    if (j2 < l) i3 = l;

                    if (k2 != l2) {
                        blockpos.set(l1, k2, k1);
                        float f2 = biomegenbase.getFloatTemperature(blockpos);

                        boolean isRain = world.getWorldChunkManager().getTemperatureAtHeight(f2, j2) >= 0.15F;
                        if (weather.isWeatherSnow()) {
                            isRain = false;
                        } else if (weather.isWeatherRain() || weather.customRain.getValue()) {
                            isRain = true;
                        }

                        // 密度过滤（参考 Miralune 算法）
                        if (densityPct < 1.0F) {
                            long colHash = ((long) l1 * 3121L + (long) k1 * 418711L) & 0xFFFFL;
                            if ((float) colHash / 65535.0F > densityPct) {
                                continue;
                            }
                        }

                        double baseViewY = entity.posY;

                        if (isRain) {
                            if (j1 != 0) {
                                if (j1 >= 0) {
                                    tessellator.draw();
                                }
                                j1 = 0;
                                this.mc.getTextureManager().bindTexture(CUSTOM_RAIN_TEXTURE);
                                worldrenderer.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
                            }

                            double d6 = (double) ((float) l1 + 0.5F) - entity.posX;
                            double d7 = (double) ((float) k1 + 0.5F) - entity.posZ;
                            float f3 = MathHelper.sqrt_double(d6 * d6 + d7 * d7) / (float) i1;
                            float f4 = MathHelper.clamp_float(((1.0F - f3 * f3) * 0.5F + 0.5F) * f * rainOpacity * rainA, 0.0F, 1.0F);

                            blockpos.set(l1, i3, k1);
                            int j3 = world.getCombinedLight(blockpos, 0);
                            int k3 = j3 >> 16 & 65535;
                            int l3 = j3 & 65535;

                            double d3 = baseD3 * (double) rainSize;
                            double d4 = baseD4 * (double) rainSize;

                            long colSeed = (long) (l1 * l1 * 3121 + l1 * 45238971 ^ k1 * k1 * 418711 + k1 * 13761);
                            double speedFactor = (3.0D + ((double) ((colSeed >> 16) & 1023) / 1024.0D)) * (double) fallSpeed;
                            double d5 = ((double) (this.rendererUpdateCount + (int) (colSeed & 31)) + (double) partialTicks) / 32.0D * speedFactor;

                            // 风力偏移（参考 Miralune 实现）
                            double windOffK2X = (k2 - baseViewY) * (double) windX / 200.0D;
                            double windOffK2Z = (k2 - baseViewY) * (double) windZ / 200.0D;
                            double windOffL2X = (l2 - baseViewY) * (double) windX / 200.0D;
                            double windOffL2Z = (l2 - baseViewY) * (double) windZ / 200.0D;

                            worldrenderer.pos((double) l1 - d3 + 0.5D + windOffK2X, (double) k2, (double) k1 - d4 + 0.5D + windOffK2Z).tex(0.0D, (double) k2 * 0.25D + d5).color(rainR, rainG, rainB, f4).lightmap(k3, l3).endVertex();
                            worldrenderer.pos((double) l1 + d3 + 0.5D + windOffK2X, (double) k2, (double) k1 + d4 + 0.5D + windOffK2Z).tex(1.0D, (double) k2 * 0.25D + d5).color(rainR, rainG, rainB, f4).lightmap(k3, l3).endVertex();
                            worldrenderer.pos((double) l1 + d3 + 0.5D + windOffL2X, (double) l2, (double) k1 + d4 + 0.5D + windOffL2Z).tex(1.0D, (double) l2 * 0.25D + d5).color(rainR, rainG, rainB, f4).lightmap(k3, l3).endVertex();
                            worldrenderer.pos((double) l1 - d3 + 0.5D + windOffL2X, (double) l2, (double) k1 - d4 + 0.5D + windOffL2Z).tex(0.0D, (double) l2 * 0.25D + d5).color(rainR, rainG, rainB, f4).lightmap(k3, l3).endVertex();
                        } else {
                            // Snow
                            if (j1 != 1) {
                                if (j1 >= 0) {
                                    tessellator.draw();
                                }
                                j1 = 1;
                                this.mc.getTextureManager().bindTexture(CUSTOM_SNOW_TEXTURE);
                                worldrenderer.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
                            }

                            this.random.setSeed((long) (l1 * l1 * 3121 + l1 * 45238971 ^ k1 * k1 * 418711 + k1 * 13761));
                            double d8 = (double) (((float) (this.rendererUpdateCount & 511) + partialTicks) / 512.0F) * (double) fallSpeed;
                            double d9 = this.random.nextDouble() + (double) f1 * 0.01D * (double) ((float) this.random.nextGaussian());
                            double d10 = this.random.nextDouble() + (double) (f1 * (float) this.random.nextGaussian()) * 0.001D;
                            double d11 = (double) ((float) l1 + 0.5F) - entity.posX;
                            double d12 = (double) ((float) k1 + 0.5F) - entity.posZ;
                            float f6 = MathHelper.sqrt_double(d11 * d11 + d12 * d12) / (float) i1;
                            float f5 = ((1.0F - f6 * f6) * 0.3F + 0.5F) * f;
                            blockpos.set(l1, i3, k1);
                            int i4 = (world.getCombinedLight(blockpos, 0) * 3 + 15728880) / 4;
                            int j4 = i4 >> 16 & 65535;
                            int k4 = i4 & 65535;

                            double windOffK2X = (k2 - baseViewY) * (double) windX / 200.0D;
                            double windOffK2Z = (k2 - baseViewY) * (double) windZ / 200.0D;
                            double windOffL2X = (l2 - baseViewY) * (double) windX / 200.0D;
                            double windOffL2Z = (l2 - baseViewY) * (double) windZ / 200.0D;

                            worldrenderer.pos((double) l1 - baseD3 + 0.5D + windOffK2X, (double) k2, (double) k1 - baseD4 + 0.5D + windOffK2Z).tex(0.0D + d9, (double) k2 * 0.25D + d8 + d10).color(snowR, snowG, snowB, f5).lightmap(j4, k4).endVertex();
                            worldrenderer.pos((double) l1 + baseD3 + 0.5D + windOffK2X, (double) k2, (double) k1 + baseD4 + 0.5D + windOffK2Z).tex(1.0D + d9, (double) k2 * 0.25D + d8 + d10).color(snowR, snowG, snowB, f5).lightmap(j4, k4).endVertex();
                            worldrenderer.pos((double) l1 + baseD3 + 0.5D + windOffL2X, (double) l2, (double) k1 + baseD4 + 0.5D + windOffL2Z).tex(1.0D + d9, (double) l2 * 0.25D + d8 + d10).color(snowR, snowG, snowB, f5).lightmap(j4, k4).endVertex();
                            worldrenderer.pos((double) l1 - baseD3 + 0.5D + windOffL2X, (double) l2, (double) k1 - baseD4 + 0.5D + windOffL2Z).tex(0.0D + d9, (double) l2 * 0.25D + d8 + d10).color(snowR, snowG, snowB, f5).lightmap(j4, k4).endVertex();
                        }
                    }
                }
            }
        }

        if (j1 >= 0) {
            tessellator.draw();
        }

        worldrenderer.setTranslation(0.0D, 0.0D, 0.0D);
        GlStateManager.enableCull();
        GlStateManager.disableBlend();
        GlStateManager.alphaFunc(516, 0.1F);
        this.disableLightmap();
    }

    /**
     * 原版温度重定向（作为备用，主逻辑在 MixinBiomeGenBase 中生效）
     */
    @Redirect(method = "renderRainSnow", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/biome/WorldChunkManager;getTemperatureAtHeight(FI)F"), require = 0)
    private float redirectTemperatureAtHeight(WorldChunkManager chunkManager, float temp, int height) {
        Weather weather = Weather.getInstance();
        if (weather != null && weather.isEnabled()) {
            if (weather.isWeatherSnow()) {
                return -1.0F;
            } else if (weather.isWeatherRain()) {
                return 1.0F;
            }
        }
        return chunkManager.getTemperatureAtHeight(temp, height);
    }
}
