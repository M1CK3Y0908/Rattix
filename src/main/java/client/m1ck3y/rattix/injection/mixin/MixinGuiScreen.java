package client.m1ck3y.rattix.injection.mixin;

import client.m1ck3y.rattix.utils.render.IconUtils;
import client.m1ck3y.rattix.utils.render.Win11UiRenderer;
import client.m1ck3y.rattix.utils.render.shader.ShaderBackgroundRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.multiplayer.GuiConnecting;
import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiScreen.class)
public abstract class MixinGuiScreen extends Gui {

    @Shadow public abstract void drawBackground(int tint);
    @Shadow protected List<GuiButton> buttonList;

    /**
     * 1.8.9 的大多数设置/列表界面通过 drawDefaultBackground() 绘制默认泥土背景，
     * 而不是直接调用 drawBackground()。在这里拦截才能覆盖 GuiOptions、GuiVideoSettings、
     * GuiLanguage、GuiControls 等界面的原版泥土纹理。
     */
    @Inject(method = "drawDefaultBackground", at = @At("HEAD"), cancellable = true)
    private void onDrawDefaultBackground(CallbackInfo ci) {
        if (client.m1ck3y.rattix.ui.window.SubpageWindow.isRenderingEmbedded) {
            GuiScreen self = (GuiScreen) (Object) this;
            drawRect(0, 0, self.width, self.height, 0xF212151D);
            ci.cancel();
            return;
        }
        if (ShaderBackgroundRenderer.shouldRenderShader()) {
            if (ShaderBackgroundRenderer.renderCurrentScreen(true)) {
                ci.cancel();
            }
        }
    }

    /**
     * 将统一背景风格（动态着色器或纯黑背景）延伸到所有非局内界面（多人游戏、选项设置、世界选择、断开连接等）
     */
    @Inject(method = "drawBackground", at = @At("HEAD"), cancellable = true)
    private void onDrawBackground(int tint, CallbackInfo ci) {
        if (client.m1ck3y.rattix.ui.window.SubpageWindow.isRenderingEmbedded) {
            GuiScreen self = (GuiScreen) (Object) this;
            drawRect(0, 0, self.width, self.height, 0xF212151D);
            ci.cancel();
            return;
        }
        if (ShaderBackgroundRenderer.shouldRenderShader()) {
            if (ShaderBackgroundRenderer.renderCurrentScreen()) {
                ci.cancel();
            }
        } else {
            GuiScreen self = (GuiScreen) (Object) this;
            if (self.mc == null || self.mc.theWorld == null || self instanceof GuiDownloadTerrain || self instanceof GuiConnecting) {
                // 纯黑暗色背景替代原版泥土平铺
                drawRect(0, 0, self.width, self.height, 0xFF141414);
                ci.cancel();
            }
        }
    }

    /**
     * 拦截世界背景绘制，确保下载地形与连接界面在 theWorld 非空时亦不会绘制原版暗色渐变
     */
    @Inject(method = "drawWorldBackground", at = @At("HEAD"), cancellable = true)
    private void onDrawWorldBackground(int tint, CallbackInfo ci) {
        if (client.m1ck3y.rattix.ui.window.SubpageWindow.isRenderingEmbedded) {
            ci.cancel();
            return;
        }
        if (ShaderBackgroundRenderer.shouldRenderShader()) {
            if (ShaderBackgroundRenderer.renderCurrentScreen()) {
                ci.cancel();
            }
        }
    }

    /**
     * 拦截嵌套窗口内的原生 handleMouseInput 按钮点击，防止与 SubpageWindow 的精准相对坐标分发发生重复触发
     */
    @Inject(method = "handleMouseInput", at = @At("HEAD"), cancellable = true)
    private void onHandleMouseInput(CallbackInfo ci) {
        if (client.m1ck3y.rattix.ui.window.SubpageWindow.isRenderingEmbedded) {
            if (org.lwjgl.input.Mouse.getEventButton() != -1) {
                ci.cancel();
            }
        }
    }

    /**
     * 在连接服务器和下载地形界面居中绘制 ricon 图标，延续启动与加载界面的视觉风格
     */
    @Inject(method = "drawScreen", at = @At("HEAD"))
    private void onDrawScreenHead(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        GuiScreen self = (GuiScreen) (Object) this;
        if (self instanceof GuiConnecting || self instanceof GuiDownloadTerrain) {
            float iconSize = 80.0f;
            float iconX = (self.width - iconSize) / 2.0f;
            float iconY = self.height / 2.0f - 115.0f;
            IconUtils.drawIconDirect(iconX, iconY, iconSize);
        }
    }

    /** Final overlay pass for vanilla/OptiFine menu buttons. */
    @Inject(method = "drawScreen", at = @At("TAIL"), require = 0)
    private void onDrawScreenWin11Buttons(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        Win11UiRenderer.drawButtons((GuiScreen) (Object) this, net.minecraft.client.Minecraft.getMinecraft(), mouseX, mouseY, buttonList);
    }
}
