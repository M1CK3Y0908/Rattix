package client.m1ck3y.rattix.injection.mixin;

import client.m1ck3y.rattix.utils.render.IconUtils;
import client.m1ck3y.rattix.utils.render.shader.ShaderBackgroundRenderer;
import net.minecraft.client.LoadingScreenRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.lwjgl.opengl.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;

@Mixin(LoadingScreenRenderer.class)
public abstract class MixinLoadingScreenRenderer {

    @Unique private static Field messageField;
    @Unique private static Field currentlyDisplayedTextField;
    @Unique private long rattix$systemTime;
    @Unique private Framebuffer rattix$framebuffer;

    static {
        try {
            messageField = ReflectionHelper.findField(LoadingScreenRenderer.class, "message", "field_73727_a");
            messageField.setAccessible(true);
        } catch (Throwable ignored) {}
        try {
            currentlyDisplayedTextField = ReflectionHelper.findField(LoadingScreenRenderer.class, "currentlyDisplayedText", "field_73726_c");
            currentlyDisplayedTextField.setAccessible(true);
        } catch (Throwable ignored) {}
    }

    /**
     * 将原生无压暗动态着色器 + ricon + 现代进度条风格延续到单人世界加载、生成、保存与资源重载等所有加载界面
     */
    @Inject(method = "setLoadingProgress", at = @At("HEAD"), cancellable = true)
    private void onSetLoadingProgress(int progress, CallbackInfo ci) {
        long now = Minecraft.getSystemTime();
        if (now - this.rattix$systemTime < 100L) {
            ci.cancel();
            return;
        }
        this.rattix$systemTime = now;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || !Display.isCreated()) {
            ci.cancel();
            return;
        }

        ScaledResolution scaledResolution = new ScaledResolution(mc);
        int scale = scaledResolution.getScaleFactor();
        int width = scaledResolution.getScaledWidth();
        int height = scaledResolution.getScaledHeight();

        if (width <= 0 || height <= 0 || scale <= 0) {
            ci.cancel();
            return;
        }

        int dispW = width * scale;
        int dispH = height * scale;

        if (this.rattix$framebuffer == null) {
            this.rattix$framebuffer = new Framebuffer(dispW, dispH, false);
        } else if (this.rattix$framebuffer.framebufferWidth != dispW || this.rattix$framebuffer.framebufferHeight != dispH) {
            this.rattix$framebuffer.createBindFramebuffer(dispW, dispH);
        }

        this.rattix$framebuffer.bindFramebuffer(false);
        GlStateManager.matrixMode(5889);
        GlStateManager.loadIdentity();
        GlStateManager.ortho(0.0D, width, height, 0.0D, 100.0D, 300.0D);
        GlStateManager.matrixMode(5888);
        GlStateManager.loadIdentity();
        GlStateManager.translate(0.0F, 0.0F, -200.0F);
        GlStateManager.disableLighting();
        GlStateManager.disableFog();
        GlStateManager.disableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.resetColor();

        // 1. 背景：启用着色器时绘制动态背景，完全无压暗遮罩；未启用时为极简纯黑暗色背景
        if (ShaderBackgroundRenderer.shouldRenderShader()) {
            ShaderBackgroundRenderer.renderLoadingScreen(width, height);
        } else {
            Gui.drawRect(0, 0, width, height, 0xFF141414);
        }

        // 2. 屏幕居中绘制 ricon 图标
        float iconSize = 96.0f;
        float iconX = (width - iconSize) / 2.0f;
        float iconY = (height - iconSize) / 2.0f - 24.0f;
        IconUtils.drawIconDirect(iconX, iconY, iconSize);

        // 3. 现代化细条进度条
        float barWidth = 220.0f;
        float barHeight = 4.0f;
        float barX = (width - barWidth) / 2.0f;
        float barY = iconY + iconSize + 22.0f;

        Gui.drawRect((int) (barX - 1), (int) (barY - 1), (int) (barX + barWidth + 1), (int) (barY + barHeight + 1), 0xB430363D);
        Gui.drawRect((int) barX, (int) barY, (int) (barX + barWidth), (int) (barY + barHeight), 0xFF1E232E);
        if (progress >= 0) {
            float pct = Math.max(0.0f, Math.min(1.0f, progress / 100.0f));
            Gui.drawRect((int) barX, (int) barY, (int) (barX + barWidth * pct), (int) (barY + barHeight), 0xFF38B6FF);
        }

        // 4. 文字提示与百分比
        FontRenderer font = mc.fontRendererObj;
        if (font != null) {
            if (progress >= 0) {
                String pctStr = progress + "%";
                font.drawString(pctStr, (width - font.getStringWidth(pctStr)) / 2.0f, barY - 12.0f, 0xFFFFFFFF, false);
            }
            String message = null;
            String currentlyDisplayedText = null;
            try {
                if (messageField != null) message = (String) messageField.get(this);
                if (currentlyDisplayedTextField != null) currentlyDisplayedText = (String) currentlyDisplayedTextField.get(this);
            } catch (Throwable ignored) {}

            if (currentlyDisplayedText != null && !currentlyDisplayedText.isEmpty()) {
                font.drawString(currentlyDisplayedText, (width - font.getStringWidth(currentlyDisplayedText)) / 2.0f, barY + barHeight + 10.0f, 0xFFD7D7D7, false);
            } else if (message != null && !message.isEmpty()) {
                font.drawString(message, (width - font.getStringWidth(message)) / 2.0f, barY + barHeight + 10.0f, 0xFFD7D7D7, false);
            }
        }

        this.rattix$framebuffer.unbindFramebuffer();
        this.rattix$framebuffer.framebufferRender(dispW, dispH);
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(516, 0.1F);
        mc.updateDisplay();
        ci.cancel();
    }
}
