package client.m1ck3y.rattix.injection.mixin;

import client.m1ck3y.rattix.features.module.modules.Time;
import client.m1ck3y.rattix.features.module.modules.Weather;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(World.class)
public abstract class MixinWorld {

    /**
     * 自定义世界时间：拦截客户端的所有 getWorldTime 调用，彻底避免服务器与客户端时间竞争导致天空闪烁与跳跃
     */
    @Inject(method = "getWorldTime", at = @At("HEAD"), cancellable = true)
    private void onGetWorldTime(CallbackInfoReturnable<Long> cir) {
        if (((World) (Object) this).isRemote) {
            Time time = Time.getInstance();
            if (time != null && time.isEnabled()) {
                cir.setReturnValue((long) time.getCurrentTime());
            }
        }
    }

    /**
     * 阻止服务器时间数据包覆盖客户端时间，并同步备份最新服务端时间
     */
    @Inject(method = "setWorldTime", at = @At("HEAD"), cancellable = true)
    private void onSetWorldTime(long time, CallbackInfo ci) {
        if (((World) (Object) this).isRemote) {
            Time timeMod = Time.getInstance();
            if (timeMod != null && timeMod.isEnabled()) {
                timeMod.setSavedTime(time);
                ci.cancel();
            }
        }
    }

    /**
     * 自定义天体角度：当时间流速为 0 时计算出完全静止稳定的天体角度，彻底杜绝天空微跳闪烁；时间流速大于 0 时平滑旋转
     */
    @Inject(method = "getCelestialAngle", at = @At("HEAD"), cancellable = true)
    private void onGetCelestialAngle(float partialTicks, CallbackInfoReturnable<Float> cir) {
        if (((World) (Object) this).isRemote) {
            Time time = Time.getInstance();
            if (time != null && time.isEnabled()) {
                cir.setReturnValue(time.getCelestialAngle(partialTicks));
            }
        }
    }

    /**
     * 自定义降雨/降雪强度：平滑插值降雨强度与天气状态控制（参考 Miralune 实现）
     */
    @Inject(method = "getRainStrength", at = @At("HEAD"), cancellable = true)
    private void onGetRainStrength(float partialTicks, CallbackInfoReturnable<Float> cir) {
        if (((World) (Object) this).isRemote) {
            Weather weather = Weather.getInstance();
            if (weather != null && weather.isEnabled() && weather.activeMode() != 0) {
                cir.setReturnValue(weather.rainStrength((World) (Object) this, partialTicks));
            }
        }
    }

    /**
     * 阻止服务器天气数据包在 Weather 生效时重置客户端降雨强度
     */
    @Inject(method = "setRainStrength", at = @At("HEAD"), cancellable = true)
    private void onSetRainStrength(float strength, CallbackInfo ci) {
        if (((World) (Object) this).isRemote) {
            Weather weather = Weather.getInstance();
            if (weather != null && weather.isEnabled() && weather.activeMode() != 0) {
                ci.cancel();
            }
        }
    }

    /**
     * 自定义雷暴强度
     */
    @Inject(method = "getThunderStrength", at = @At("HEAD"), cancellable = true)
    private void onGetThunderStrength(float partialTicks, CallbackInfoReturnable<Float> cir) {
        if (((World) (Object) this).isRemote) {
            Weather weather = Weather.getInstance();
            if (weather != null && weather.isEnabled()) {
                if (weather.thunder.getValue()) {
                    cir.setReturnValue(1.0F);
                } else if (weather.activeMode() != 0) {
                    cir.setReturnValue(0.0F);
                }
            }
        }
    }
}
