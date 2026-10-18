package client.m1ck3y.rattix.injection.mixin;

import client.m1ck3y.rattix.module.modules.Ambiance;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(World.class)
public abstract class MixinWorld {

    @Shadow
    public boolean isRemote;

    /**
     * 自定义世界时间：拦截客户端的所有 getWorldTime 调用，彻底避免服务器与客户端时间竞争导致天空闪烁与跳跃
     */
    @Inject(method = "getWorldTime", at = @At("HEAD"), cancellable = true)
    private void onGetWorldTime(CallbackInfoReturnable<Long> cir) {
        if (this.isRemote) {
            Ambiance ambiance = Ambiance.getInstance();
            if (ambiance != null && ambiance.isEnabled()) {
                cir.setReturnValue((long) ambiance.getCurrentTime());
            }
        }
    }

    /**
     * 阻止服务器时间数据包覆盖客户端时间，并同步备份最新服务端时间
     */
    @Inject(method = "setWorldTime", at = @At("HEAD"), cancellable = true)
    private void onSetWorldTime(long time, CallbackInfo ci) {
        if (this.isRemote) {
            Ambiance ambiance = Ambiance.getInstance();
            if (ambiance != null && ambiance.isEnabled()) {
                ambiance.setSavedTime(time);
                ci.cancel();
            }
        }
    }

    /**
     * 自定义天体角度：当时间流速为 0 时计算出完全静止稳定的天体角度，彻底杜绝天空微跳闪烁；时间流速大于 0 时平滑旋转
     */
    @Inject(method = "getCelestialAngle", at = @At("HEAD"), cancellable = true)
    private void onGetCelestialAngle(float partialTicks, CallbackInfoReturnable<Float> cir) {
        if (this.isRemote) {
            Ambiance ambiance = Ambiance.getInstance();
            if (ambiance != null && ambiance.isEnabled()) {
                cir.setReturnValue(ambiance.getCelestialAngle(partialTicks));
            }
        }
    }

    /**
     * 自定义降雨/降雪强度：根据 Weather 单选模式设定（Rain/Snow 恒定 1.0F，Clear 恒定 0.0F）
     */
    @Inject(method = "getRainStrength", at = @At("HEAD"), cancellable = true)
    private void onGetRainStrength(float partialTicks, CallbackInfoReturnable<Float> cir) {
        if (this.isRemote) {
            Ambiance ambiance = Ambiance.getInstance();
            if (ambiance != null && ambiance.isEnabled()) {
                if (ambiance.isWeatherRain() || ambiance.isWeatherSnow()) {
                    cir.setReturnValue(1.0F);
                } else if (ambiance.isWeatherClear()) {
                    cir.setReturnValue(0.0F);
                }
            }
        }
    }

    /**
     * 自定义雷暴强度
     */
    @Inject(method = "getThunderStrength", at = @At("HEAD"), cancellable = true)
    private void onGetThunderStrength(float partialTicks, CallbackInfoReturnable<Float> cir) {
        if (this.isRemote) {
            Ambiance ambiance = Ambiance.getInstance();
            if (ambiance != null && ambiance.isEnabled()) {
                if (ambiance.thunder.getValue()) {
                    cir.setReturnValue(1.0F);
                } else if (ambiance.isWeatherClear()) {
                    cir.setReturnValue(0.0F);
                }
            }
        }
    }
}
