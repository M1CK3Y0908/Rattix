package client.m1ck3y.rattix.injection.mixin;

import client.m1ck3y.rattix.module.modules.Ambiance;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.biome.WorldChunkManager;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer {

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
     * 原版温度重定向（作为备用，主逻辑在 MixinBiomeGenBase 中生效）
     */
    @Redirect(method = "renderRainSnow", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/biome/WorldChunkManager;getTemperatureAtHeight(FI)F"), require = 0)
    private float redirectTemperatureAtHeight(WorldChunkManager chunkManager, float temp, int height) {
        Ambiance ambiance = Ambiance.getInstance();
        if (ambiance != null && ambiance.isEnabled()) {
            if (ambiance.snow.getValue()) {
                return -1.0F;
            } else if (ambiance.rain.getValue()) {
                return 1.0F;
            }
        }
        return chunkManager.getTemperatureAtHeight(temp, height);
    }
}
