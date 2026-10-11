package client.m1ck3y.rattix.utils;

import net.minecraft.client.LoadingScreenRenderer;
import net.minecraft.client.Minecraft;

/**
 * RattixLoadingScreen - 继承原版 LoadingScreenRenderer，保持原版世界加载与进入服务器界面
 */
public class RattixLoadingScreen extends LoadingScreenRenderer {
    public RattixLoadingScreen(Minecraft mcIn) {
        super(mcIn);
    }
}
