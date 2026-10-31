package client.m1ck3y.rattix.injection.mixin;

import client.m1ck3y.rattix.features.module.modules.Foliage;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.biome.BiomeColorHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BiomeColorHelper.class)
public abstract class MixinBiomeColorHelper {

    /**
     * 自定义植物颜色 - 草地（草方块顶面、草、双草、蕨、甘蔗等，参考 Miralune Foliage 实现）
     */
    @Inject(method = "getGrassColorAtPos", at = @At("HEAD"), cancellable = true)
    private static void onGetGrassColorAtPos(IBlockAccess worldIn, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        Foliage foliage = Foliage.getInstance();
        if (foliage != null && foliage.isEnabled()) {
            Integer color = foliage.getGrassColor();
            if (color != null) {
                cir.setReturnValue(color);
            }
        }
    }

    /**
     * 自定义植物颜色 - 树叶与藤蔓（橡木树叶、丛林树叶、金合欢、深色橡木、藤蔓、荷叶等，参考 Miralune Foliage 实现）
     */
    @Inject(method = "getFoliageColorAtPos", at = @At("HEAD"), cancellable = true)
    private static void onGetFoliageColorAtPos(IBlockAccess worldIn, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        Foliage foliage = Foliage.getInstance();
        if (foliage != null && foliage.isEnabled()) {
            Integer color = foliage.getLeavesColor();
            if (color != null) {
                cir.setReturnValue(color);
            }
        }
    }
}
