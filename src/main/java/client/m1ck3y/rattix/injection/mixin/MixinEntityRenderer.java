package client.m1ck3y.rattix.injection.mixin;

import client.m1ck3y.rattix.module.modules.Ambiance;
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
     * 自定义迷雾可视距离 (Custom Fog Distance)：
     * 在非天空渲染通行阶段（startCoords >= 0），强制开启 GL 迷雾并设定线性雾起止距离。
     */
    @Inject(method = "setupFog", at = @At("RETURN"))
    private void onSetupFog(int startCoords, float partialTicks, CallbackInfo ci) {
        Ambiance ambiance = Ambiance.getInstance();
        if (ambiance != null && ambiance.isEnabled() && ambiance.customFogDistance.getValue() && startCoords >= 0) {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc != null && mc.theWorld != null) {
                Entity entity = mc.getRenderViewEntity();
                if (entity != null) {
                    BlockPos pos = new BlockPos(entity.posX, entity.posY + (double) entity.getEyeHeight(), entity.posZ);
                    IBlockState state = mc.theWorld.getBlockState(pos);
                    if (state != null && state.getBlock() != null) {
                        Material material = state.getBlock().getMaterial();
                        if (material == Material.water || material == Material.lava) {
                            return; // 不覆盖水下与岩浆下的特殊迷雾
                        }
                    }
                }
            }
            float distance = ambiance.fogDistance.getValue().floatValue();
            GlStateManager.enableFog();
            GL11.glEnable(GL11.GL_FOG);
            GlStateManager.setFog(9729); // GL_LINEAR
            GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_LINEAR);
            GlStateManager.setFogStart(0.0F);
            GlStateManager.setFogEnd(distance);
            GL11.glFogf(GL11.GL_FOG_START, 0.0F);
            GL11.glFogf(GL11.GL_FOG_END, distance);
        }
    }

    /**
     * 自定义雨滴渲染管线 (Custom Rain Rendering):
     * 支持用户自定义雨滴大小 (RainSize)、颜色 (RainColor)、不透明度 (RainOpacity) 与密度 (RainDensity)。
     */
    @Inject(method = "renderRainSnow", at = @At("HEAD"), cancellable = true)
    private void onRenderRainSnow(float partialTicks, CallbackInfo ci) {
        Ambiance ambiance = Ambiance.getInstance();
        if (ambiance != null && ambiance.isEnabled() && ambiance.customRain.getValue() && !ambiance.isWeatherClear()) {
            this.renderCustomRainSnow(partialTicks, ambiance);
            ci.cancel();
        }
    }

    private void renderCustomRainSnow(float partialTicks, Ambiance ambiance) {
        if (this.mc == null || this.mc.theWorld == null) {
            return;
        }

        IRenderHandler renderer = this.mc.theWorld.provider.getWeatherRenderer();
        if (renderer != null) {
            renderer.render(partialTicks, this.mc.theWorld, this.mc);
            return;
        }

        float f = this.mc.theWorld.getRainStrength(partialTicks);
        if (ambiance.isRainingActive()) {
            f = Math.max(f, 1.0F);
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
        int i1 = this.mc.gameSettings.fancyGraphics ? 10 : 5;

        int j1 = -1;
        float f1 = (float) this.rendererUpdateCount + partialTicks;
        worldrenderer.setTranslation(-d0, -d1, -d2);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        BlockPos.MutableBlockPos blockpos = new BlockPos.MutableBlockPos();

        float rainSize = Math.max(0.1F, ambiance.rainSize.getValue().floatValue());
        float rainOpacity = MathHelper.clamp_float(ambiance.rainOpacity.getValue().floatValue() / 100.0F, 0.0F, 1.0F);
        float rainDensity = Math.max(0.1F, ambiance.rainDensity.getValue().floatValue());
        int rainColorRGB = ambiance.rainColor.getRGB();
        float rainR = ((rainColorRGB >> 16) & 0xFF) / 255.0F;
        float rainG = ((rainColorRGB >> 8) & 0xFF) / 255.0F;
        float rainB = (rainColorRGB & 0xFF) / 255.0F;
        float rainA = ((rainColorRGB >>> 24) & 0xFF) / 255.0F;
        if (rainA <= 0.0F) rainA = 1.0F;

        for (int k1 = k - i1; k1 <= k + i1; ++k1) {
            for (int l1 = i - i1; l1 <= i + i1; ++l1) {
                int i2 = (k1 - k + 16) * 32 + l1 - i + 16;
                double baseD3 = (double) RAIN_X[i2] * 0.5D;
                double baseD4 = (double) RAIN_Y[i2] * 0.5D;
                blockpos.set(l1, 0, k1);
                BiomeGenBase biomegenbase = world.getBiomeGenForCoords(blockpos);

                if (biomegenbase.canRain() || biomegenbase.getEnableSnow() || ambiance.isRainingActive()) {
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
                        if (ambiance.isWeatherSnow()) {
                            isRain = false;
                        } else if (ambiance.isWeatherRain() || ambiance.customRain.getValue()) {
                            isRain = true;
                        }

                        if (isRain) {
                            if (rainDensity < 1.0F) {
                                long colHash = ((long) l1 * 3121L + (long) k1 * 418711L) & 0xFFFFL;
                                if ((float) colHash / 65535.0F > rainDensity) {
                                    continue;
                                }
                            }

                            int dropCount = (int) Math.floor(rainDensity);
                            float remainder = rainDensity - (float) dropCount;
                            if (dropCount < 1) {
                                dropCount = 1;
                            } else if (remainder > 0.001F) {
                                long remHash = ((long) l1 * 13761L + (long) k1 * 45238971L) & 0xFFFFL;
                                if ((float) remHash / 65535.0F < remainder) {
                                    dropCount++;
                                }
                            }

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

                            for (int drop = 0; drop < dropCount; drop++) {
                                double offX = 0.0D;
                                double offZ = 0.0D;
                                double d5;
                                if (drop == 0) {
                                    long colSeed = (long) (l1 * l1 * 3121 + l1 * 45238971 ^ k1 * k1 * 418711 + k1 * 13761);
                                    double speedFactor = 3.0D + ((double) ((colSeed >> 16) & 1023) / 1024.0D);
                                    d5 = ((double) (this.rendererUpdateCount + (int) (colSeed & 31)) + (double) partialTicks) / 32.0D * speedFactor;
                                } else {
                                    long dropSeed = (long) (l1 * 73856093L ^ k1 * 19349663L ^ (long) drop * 84611L);
                                    offX = (((double) ((dropSeed >> 4) & 1023) / 1024.0D) - 0.5D) * 0.85D;
                                    offZ = (((double) ((dropSeed >> 14) & 1023) / 1024.0D) - 0.5D) * 0.85D;
                                    double speedFactorDrop = 3.0D + ((double) ((dropSeed >> 24) & 1023) / 1024.0D);
                                    d5 = ((double) (this.rendererUpdateCount + (int) (dropSeed & 31)) + (double) partialTicks) / 32.0D * speedFactorDrop;
                                }

                                worldrenderer.pos((double) l1 + offX - d3 + 0.5D, (double) k2, (double) k1 + offZ - d4 + 0.5D).tex(0.0D, (double) k2 * 0.25D + d5).color(rainR, rainG, rainB, f4).lightmap(k3, l3).endVertex();
                                worldrenderer.pos((double) l1 + offX + d3 + 0.5D, (double) k2, (double) k1 + offZ + d4 + 0.5D).tex(1.0D, (double) k2 * 0.25D + d5).color(rainR, rainG, rainB, f4).lightmap(k3, l3).endVertex();
                                worldrenderer.pos((double) l1 + offX + d3 + 0.5D, (double) l2, (double) k1 + offZ + d4 + 0.5D).tex(1.0D, (double) l2 * 0.25D + d5).color(rainR, rainG, rainB, f4).lightmap(k3, l3).endVertex();
                                worldrenderer.pos((double) l1 + offX - d3 + 0.5D, (double) l2, (double) k1 + offZ - d4 + 0.5D).tex(0.0D, (double) l2 * 0.25D + d5).color(rainR, rainG, rainB, f4).lightmap(k3, l3).endVertex();
                            }
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
                            double d8 = (double) (((float) (this.rendererUpdateCount & 511) + partialTicks) / 512.0F);
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
                            worldrenderer.pos((double) l1 - baseD3 + 0.5D, (double) k2, (double) k1 - baseD4 + 0.5D).tex(0.0D + d9, (double) k2 * 0.25D + d8 + d10).color(1.0F, 1.0F, 1.0F, f5).lightmap(j4, k4).endVertex();
                            worldrenderer.pos((double) l1 + baseD3 + 0.5D, (double) k2, (double) k1 + baseD4 + 0.5D).tex(1.0D + d9, (double) k2 * 0.25D + d8 + d10).color(1.0F, 1.0F, 1.0F, f5).lightmap(j4, k4).endVertex();
                            worldrenderer.pos((double) l1 + baseD3 + 0.5D, (double) l2, (double) k1 + baseD4 + 0.5D).tex(1.0D + d9, (double) l2 * 0.25D + d8 + d10).color(1.0F, 1.0F, 1.0F, f5).lightmap(j4, k4).endVertex();
                            worldrenderer.pos((double) l1 - baseD3 + 0.5D, (double) l2, (double) k1 - baseD4 + 0.5D).tex(0.0D + d9, (double) l2 * 0.25D + d8 + d10).color(1.0F, 1.0F, 1.0F, f5).lightmap(j4, k4).endVertex();
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
        Ambiance ambiance = Ambiance.getInstance();
        if (ambiance != null && ambiance.isEnabled()) {
            if (ambiance.isWeatherSnow()) {
                return -1.0F;
            } else if (ambiance.isWeatherRain()) {
                return 1.0F;
            }
        }
        return chunkManager.getTemperatureAtHeight(temp, height);
    }
}
