package client.m1ck3y.rattix.injection.mixin;

import client.m1ck3y.rattix.features.module.modules.WeatherAudio;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 1:1 还原 Miralune MixinEnvironmentAudio 实现：
 * 拦截 WorldClient.playSound，对雨声 (ambient.weather.rain) 与雷声 (ambient.weather.thunder) 动态缩放音量。
 */
@Mixin(WorldClient.class)
public abstract class MixinWorldClient {

    @Redirect(
            method = "playSound(DDDLjava/lang/String;FFZ)V",
            at = @At(value = "NEW", target = "net/minecraft/client/audio/PositionedSoundRecord"),
            require = 0
    )
    private PositionedSoundRecord weatherVolume(ResourceLocation location, float volume, float pitch, float x, float y, float z) {
        WeatherAudio audio = WeatherAudio.getInstance();
        if (audio != null && audio.isEnabled()) {
            String name = location.getResourcePath();
            if ("ambient.weather.rain".equals(name)) {
                volume *= (audio.rainVolume.getValue().floatValue() / 100.0F);
            } else if ("ambient.weather.thunder".equals(name)) {
                volume *= (audio.thunderVolume.getValue().floatValue() / 100.0F);
            }
        }
        return new PositionedSoundRecord(location, volume, pitch, x, y, z);
    }
}
