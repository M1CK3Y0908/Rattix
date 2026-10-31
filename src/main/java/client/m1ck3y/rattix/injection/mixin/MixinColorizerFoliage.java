package client.m1ck3y.rattix.injection.mixin;

import client.m1ck3y.rattix.features.module.modules.Foliage;
import net.minecraft.world.ColorizerFoliage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ColorizerFoliage.class)
public abstract class MixinColorizerFoliage {

    /**
     * 云杉树叶颜色
     */
    @Inject(method = "getFoliageColorPine", at = @At("HEAD"), cancellable = true)
    private static void onGetFoliageColorPine(CallbackInfoReturnable<Integer> cir) {
        Foliage foliage = Foliage.getInstance();
        if (foliage != null && foliage.isEnabled()) {
            Integer color = foliage.getLeavesColor();
            if (color != null) {
                cir.setReturnValue(color);
            }
        }
    }

    /**
     * 白桦树叶颜色
     */
    @Inject(method = "getFoliageColorBirch", at = @At("HEAD"), cancellable = true)
    private static void onGetFoliageColorBirch(CallbackInfoReturnable<Integer> cir) {
        Foliage foliage = Foliage.getInstance();
        if (foliage != null && foliage.isEnabled()) {
            Integer color = foliage.getLeavesColor();
            if (color != null) {
                cir.setReturnValue(color);
            }
        }
    }

    /**
     * 基础树叶颜色
     */
    @Inject(method = "getFoliageColorBasic", at = @At("HEAD"), cancellable = true)
    private static void onGetFoliageColorBasic(CallbackInfoReturnable<Integer> cir) {
        Foliage foliage = Foliage.getInstance();
        if (foliage != null && foliage.isEnabled()) {
            Integer color = foliage.getLeavesColor();
            if (color != null) {
                cir.setReturnValue(color);
            }
        }
    }

    /**
     * 树叶查找表颜色
     */
    @Inject(method = "getFoliageColor", at = @At("HEAD"), cancellable = true)
    private static void onGetFoliageColor(double temperature, double humidity, CallbackInfoReturnable<Integer> cir) {
        Foliage foliage = Foliage.getInstance();
        if (foliage != null && foliage.isEnabled()) {
            Integer color = foliage.getLeavesColor();
            if (color != null) {
                cir.setReturnValue(color);
            }
        }
    }
}
