package client.m1ck3y.rattix.ui.client;

import net.minecraft.client.LoadingScreenRenderer;
import net.minecraft.client.Minecraft;

/**
 * RattixLoadingScreen - 继承 LoadingScreenRenderer，享受 MixinLoadingScreenRenderer 带来的现代纯黑/着色器背景、居中 ricon 图标与进度条
 */
public class RattixLoadingScreen extends LoadingScreenRenderer {
    public RattixLoadingScreen(Minecraft mcIn) {
        super(mcIn);
    }
}
