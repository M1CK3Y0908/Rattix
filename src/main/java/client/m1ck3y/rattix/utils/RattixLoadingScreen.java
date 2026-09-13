package client.m1ck3y.rattix.utils;

import net.minecraft.client.LoadingScreenRenderer;
import net.minecraft.client.Minecraft;

/**
 * RattixLoadingScreen - 替换原版世界加载和资源刷新的泥土背景，保持黑底 + ricon 样式
 */
public class RattixLoadingScreen extends LoadingScreenRenderer {
    private String currentText = "";
    private String currentDesc = "";

    public RattixLoadingScreen(Minecraft mcIn) {
        super(mcIn);
    }

    @Override
    public void resetProgressAndMessage(String message) {
        this.currentText = message != null ? message : "";
        this.currentDesc = "";
        StartupScreen.draw(this.currentText, 0.0f);
    }

    @Override
    public void displaySavingString(String message) {
        this.currentText = message != null ? message : "";
        StartupScreen.draw(this.currentText, 0.0f);
    }

    @Override
    public void displayLoadingString(String message) {
        this.currentDesc = message != null ? message : "";
        String display = !this.currentText.isEmpty() ? (this.currentText + " - " + this.currentDesc) : this.currentDesc;
        StartupScreen.draw(display, 0.0f);
    }

    @Override
    public void setLoadingProgress(int progress) {
        float p = Math.max(0.0f, Math.min(1.0f, progress / 100.0f));
        String display = !this.currentText.isEmpty() ? (this.currentText + (!this.currentDesc.isEmpty() ? (" (" + this.currentDesc + ")") : "")) : this.currentDesc;
        StartupScreen.draw(display, p);
    }

    @Override
    public void setDoneWorking() {
        // 完成加载
    }
}
