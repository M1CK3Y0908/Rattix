package client.m1ck3y.rattix.injection.mixin;

import client.m1ck3y.rattix.features.module.modules.Foliage;
import net.minecraft.world.ColorizerGrass;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ColorizerGrass.class)
public abstract class MixinColorizerGrass {

    /**
     * 草地查找表颜色（草方块基础色与掉落物，参考 Miralune Foliage 实现）
     */
    @Inject(method = "getGrassColor", at = @At("HEAD"), cancellable = true)
    private static void onGetGrassColor(double temperature, double humidity, CallbackInfoReturnable<Integer> cir) {
        Foliage foliage = Foliage.getInstance();
        if (foliage != null && foliage.isEnabled()) {
            Integer color = foliage.getGrassColor();
            if (color != null) {
                cir.setReturnValue(color);
            }
        }
    }
}
