package client.m1ck3y.rattix.injection.mixin;

import client.m1ck3y.rattix.module.modules.Ambiance;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.world.biome.WorldChunkManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer {

    @Shadow
    private int[] lightmapColors;

    @Shadow
    private DynamicTexture lightmapTexture;

    /**
     * 自定义世界光照色调 (Custom World Color)
     */
    @Inject(method = "updateLightmap", at = @At("RETURN"))
    private void onUpdateLightmap(float partialTicks, CallbackInfo ci) {
        Ambiance ambiance = Ambiance.getInstance();
        if (ambiance != null && ambiance.isEnabled() && ambiance.customWorldColor.getValue() && this.lightmapColors != null && this.lightmapTexture != null) {
            int worldColor = ambiance.worldColor.getRGB();
            int wr = (worldColor >> 16) & 0xFF;
            int wg = (worldColor >> 8) & 0xFF;
            int wb = worldColor & 0xFF;
            for (int i = 0; i < this.lightmapColors.length; i++) {
                int c = this.lightmapColors[i];
                int a = (c >> 24) & 0xFF;
                int r = (c >> 16) & 0xFF;
                int g = (c >> 8) & 0xFF;
                int b = c & 0xFF;
                r = (r * wr) / 255;
                g = (g * wg) / 255;
                b = (b * wb) / 255;
                this.lightmapColors[i] = (a << 24) | (r << 16) | (g << 8) | b;
            }
            this.lightmapTexture.updateDynamicTexture();
        }
    }

    /**
     * 自定义迷雾可视距离 (Custom Fog Distance)
     */
    @Inject(method = "setupFog", at = @At("RETURN"))
    private void onSetupFog(int startCoords, float partialTicks, CallbackInfo ci) {
        Ambiance ambiance = Ambiance.getInstance();
        if (ambiance != null && ambiance.isEnabled() && ambiance.customFogDistance.getValue()) {
            GlStateManager.setFog(9729); // GL_LINEAR
            GlStateManager.setFogStart(0.0F);
            GlStateManager.setFogEnd(ambiance.fogDistance.getValue().floatValue());
        }
    }

    /**
     * 强制下雪效果 (Snow Option in Ambiance)
     */
    @Redirect(method = "renderRainSnow", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/biome/WorldChunkManager;getTemperatureAtHeight(FI)F"), require = 0)
    private float redirectTemperatureAtHeight(WorldChunkManager chunkManager, float temp, int height) {
        Ambiance ambiance = Ambiance.getInstance();
        if (ambiance != null && ambiance.isEnabled()) {
            if (ambiance.snow.getValue()) {
                return -1.0F; // 温度 < 0.15F 将天气贴图与粒子强制切换为雪花
            } else if (ambiance.rain.getValue()) {
                return 1.0F; // 强制保持下雨
            }
        }
        return chunkManager.getTemperatureAtHeight(temp, height);
    }
}
