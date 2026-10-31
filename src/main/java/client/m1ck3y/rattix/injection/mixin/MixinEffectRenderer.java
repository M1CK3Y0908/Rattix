package client.m1ck3y.rattix.injection.mixin;

import client.m1ck3y.rattix.features.module.modules.WeatherParticles;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.particle.EntityRainFX;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 1:1 还原 Miralune MixinEnvironmentParticles 实现：
 * 拦截 EffectRenderer.addEffect，对下雨溅水粒子 (EntityRainFX) 动态注入自定义溅水颜色、粒子生成数量与缩放比例。
 */
@Mixin(EffectRenderer.class)
public abstract class MixinEffectRenderer {

    @Unique
    private int environmentParticleSequence;

    @Inject(method = "addEffect", at = @At("HEAD"), cancellable = true)
    private void environmentParticle(EntityFX particle, CallbackInfo ci) {
        if (particle == null) return;
        WeatherParticles weather = WeatherParticles.getInstance();
        int color = -1;
        int amount = 100;
        float size = 1.0F;

        if (weather != null && weather.isEnabled() && particle instanceof EntityRainFX) {
            color = weather.splashColor.getRGB() & 0xFFFFFF;
            amount = (int) Math.round(weather.splashAmount.getValue());
            size = weather.splashSize.getValue().floatValue();
        }

        if (amount < 100 && Math.floorMod(environmentParticleSequence++ * 37, 100) >= amount) {
            ci.cancel();
            return;
        }

        if (color != -1) {
            float r = ((color >> 16) & 255) / 255.0F;
            float g = ((color >> 8) & 255) / 255.0F;
            float b = (color & 255) / 255.0F;
            particle.setRBGColorF(r, g, b);
        }

        if (size != 1.0F) {
            particle.multipleParticleScaleBy(size);
        }
    }
}
