package client.m1ck3y.rattix.injection.mixin;

import client.m1ck3y.rattix.features.module.modules.Weather;
import net.minecraft.world.WorldProvider;
import net.minecraftforge.client.IRenderHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 对应 Miralune MixinEnvironmentProvider 实现：
 * 当天气系统启用时，拦截 WorldProvider.getWeatherRenderer() 返回 null，
 * 强制使游戏执行具备 Mixin 重定向钩子的 EntityRenderer.renderRainSnow 原版渲染管线，杜绝 OptiFine 绕过。
 */
@Mixin(WorldProvider.class)
public abstract class MixinWorldProvider {

    @Inject(method = "getWeatherRenderer", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void environmentWeatherRenderer(CallbackInfoReturnable<IRenderHandler> callback) {
        Weather weather = Weather.getInstance();
        if (weather != null && weather.isEnabled()) {
            callback.setReturnValue(null);
        }
    }
}
