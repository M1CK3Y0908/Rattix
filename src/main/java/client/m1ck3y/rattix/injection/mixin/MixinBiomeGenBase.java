package client.m1ck3y.rattix.injection.mixin;

import client.m1ck3y.rattix.features.module.modules.Foliage;
import client.m1ck3y.rattix.features.module.modules.Weather;
import net.minecraft.util.BlockPos;
import net.minecraft.world.biome.BiomeGenBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BiomeGenBase.class)
public abstract class MixinBiomeGenBase {

    /**
     * 强制控制生物群系温度（参考 Miralune 与 Augustus 实现）：
     * 当开启雪天时强制返回负温度（< 0.15F），使游戏在任意群系（包括沙漠、平原等）均渲染降雪贴图与下雪效果；
     * 当开启雨天或自定义雨时强制返回高温（>= 0.15F），渲染降雨贴图。
     */
    @Inject(method = "getFloatTemperature", at = @At("HEAD"), cancellable = true)
    private void onGetFloatTemperature(BlockPos pos, CallbackInfoReturnable<Float> cir) {
        Weather weather = Weather.getInstance();
        if (weather != null && weather.isEnabled()) {
            if (weather.isWeatherSnow()) {
                cir.setReturnValue(-1.0F);
            } else if (weather.isRainingActive()) {
                cir.setReturnValue(1.0F);
            }
        }
    }

    /**
     * 开启雪天时强制允许群系下雪
     */
    @Inject(method = "getEnableSnow", at = @At("HEAD"), cancellable = true)
    private void onGetEnableSnow(CallbackInfoReturnable<Boolean> cir) {
        Weather weather = Weather.getInstance();
        if (weather != null && weather.isEnabled() && weather.isWeatherSnow()) {
            cir.setReturnValue(true);
        }
    }

    /**
     * 开启雪天时将群系判定为积雪群系
     */
    @Inject(method = "isSnowyBiome", at = @At("HEAD"), cancellable = true)
    private void onIsSnowyBiome(CallbackInfoReturnable<Boolean> cir) {
        Weather weather = Weather.getInstance();
        if (weather != null && weather.isEnabled() && weather.isWeatherSnow()) {
            cir.setReturnValue(true);
        }
    }

    /**
     * 控制群系是否允许降雨（参考 Miralune 与 Augustus 实现：支持全群系包括沙漠降雨）
     */
    @Inject(method = "canRain", at = @At("HEAD"), cancellable = true)
    private void onCanRain(CallbackInfoReturnable<Boolean> cir) {
        Weather weather = Weather.getInstance();
        if (weather != null && weather.isEnabled()) {
            if (weather.isWeatherClear() || weather.isWeatherSnow()) {
                cir.setReturnValue(false);
            } else if (weather.isRainingActive()) {
                cir.setReturnValue(true);
            }
        }
    }

    /**
     * 自定义植物颜色 - 群系草地颜色（参考 Miralune Foliage 实现）
     */
    @Inject(method = "getGrassColorAtPos", at = @At("HEAD"), cancellable = true)
    private void onGetGrassColorAtPos(BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        Foliage foliage = Foliage.getInstance();
        if (foliage != null && foliage.isEnabled()) {
            Integer color = foliage.getGrassColor();
            if (color != null) {
                cir.setReturnValue(color);
            }
        }
    }

    /**
     * 自定义植物颜色 - 群系树叶颜色（参考 Miralune Foliage 实现）
     */
    @Inject(method = "getFoliageColorAtPos", at = @At("HEAD"), cancellable = true)
    private void onGetFoliageColorAtPos(BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        Foliage foliage = Foliage.getInstance();
        if (foliage != null && foliage.isEnabled()) {
            Integer color = foliage.getLeavesColor();
            if (color != null) {
                cir.setReturnValue(color);
            }
        }
    }
}
