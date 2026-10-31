package client.m1ck3y.rattix.injection.mixin;

import client.m1ck3y.rattix.features.module.modules.Foliage;
import net.minecraft.util.BlockPos;
import net.minecraft.world.biome.BiomeGenBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 生物群系植被混色 Mixin：
 * 遵循 Miralune 架构原则，避免在此类中全局修改 getFloatTemperature / canRain 导致植被破坏；
 * 仅用于 Foliage 模块的自定义草地与树叶颜色拦截。
 */
@Mixin(BiomeGenBase.class)
public abstract class MixinBiomeGenBase {

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
