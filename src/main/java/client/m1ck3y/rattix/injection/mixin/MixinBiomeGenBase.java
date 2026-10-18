package client.m1ck3y.rattix.injection.mixin;

import client.m1ck3y.rattix.module.modules.Ambiance;
import net.minecraft.util.BlockPos;
import net.minecraft.world.biome.BiomeGenBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BiomeGenBase.class)
public abstract class MixinBiomeGenBase {

    /**
     * 强制控制生物群系温度：
     * 当开启雪天时强制返回负温度（< 0.15F），使游戏在任意群系（包括沙漠、平原等）均渲染降雪贴图与下雪效果；
     * 当开启雨天时强制返回高温（>= 0.15F），渲染降雨贴图。
     */
    @Inject(method = "getFloatTemperature", at = @At("HEAD"), cancellable = true)
    private void onGetFloatTemperature(BlockPos pos, CallbackInfoReturnable<Float> cir) {
        Ambiance ambiance = Ambiance.getInstance();
        if (ambiance != null && ambiance.isEnabled()) {
            if (ambiance.isWeatherSnow()) {
                cir.setReturnValue(-1.0F);
            } else if (ambiance.isWeatherRain()) {
                cir.setReturnValue(1.0F);
            }
        }
    }

    /**
     * 开启雪天时强制允许群系下雪
     */
    @Inject(method = "getEnableSnow", at = @At("HEAD"), cancellable = true)
    private void onGetEnableSnow(CallbackInfoReturnable<Boolean> cir) {
        Ambiance ambiance = Ambiance.getInstance();
        if (ambiance != null && ambiance.isEnabled() && ambiance.isWeatherSnow()) {
            cir.setReturnValue(true);
        }
    }

    /**
     * 开启雪天时将群系判定为积雪群系
     */
    @Inject(method = "isSnowyBiome", at = @At("HEAD"), cancellable = true)
    private void onIsSnowyBiome(CallbackInfoReturnable<Boolean> cir) {
        Ambiance ambiance = Ambiance.getInstance();
        if (ambiance != null && ambiance.isEnabled() && ambiance.isWeatherSnow()) {
            cir.setReturnValue(true);
        }
    }

    /**
     * 控制群系是否允许降雨
     */
    @Inject(method = "canRain", at = @At("HEAD"), cancellable = true)
    private void onCanRain(CallbackInfoReturnable<Boolean> cir) {
        Ambiance ambiance = Ambiance.getInstance();
        if (ambiance != null && ambiance.isEnabled()) {
            if (ambiance.isWeatherRain()) {
                cir.setReturnValue(true);
            } else if (ambiance.isWeatherSnow() || ambiance.isWeatherClear()) {
                cir.setReturnValue(false);
            }
        }
    }

    /**
     * 自定义植物颜色 - 群系草地颜色
     */
    @Inject(method = "getGrassColorAtPos", at = @At("HEAD"), cancellable = true)
    private void onGetGrassColorAtPos(BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        Ambiance ambiance = Ambiance.getInstance();
        if (ambiance != null && ambiance.isEnabled() && ambiance.customWorldColor.getValue()) {
            cir.setReturnValue(ambiance.worldColor.getRGB());
        }
    }

    /**
     * 自定义植物颜色 - 群系树叶颜色
     */
    @Inject(method = "getFoliageColorAtPos", at = @At("HEAD"), cancellable = true)
    private void onGetFoliageColorAtPos(BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        Ambiance ambiance = Ambiance.getInstance();
        if (ambiance != null && ambiance.isEnabled() && ambiance.customWorldColor.getValue()) {
            cir.setReturnValue(ambiance.worldColor.getRGB());
        }
    }
}
