package client.m1ck3y.rattix.injection.mixin;

import client.m1ck3y.rattix.module.modules.MenuShader;
import client.m1ck3y.rattix.utils.IconUtils;
import client.m1ck3y.rattix.utils.shader.ShaderManager;
import net.minecraft.client.LoadingScreenRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(LoadingScreenRenderer.class)
public abstract class MixinLoadingScreenRenderer {

    @Shadow private Minecraft mc;
    @Shadow private String message;
    @Shadow private String currentlyDisplayedText;
    @Shadow private long systemTime;
    @Shadow private Framebuffer framebuffer;
    @Shadow private ScaledResolution scaledResolution;

    /**
     * @author Rattix
     * @reason 将纯黑/动态着色器 + ricon + 现代进度条风格延续到单人世界加载、生成、保存与资源重载等所有加载界面
     */
    @Overwrite
    public void setLoadingProgress(int progress) {
        long now = Minecraft.getSystemTime();
        if (now - this.systemTime < 100L) {
            return;
        }
        this.systemTime = now;

        if (this.mc == null || !Display.isCreated()) return;

        this.scaledResolution = new ScaledResolution(this.mc);
        int scale = this.scaledResolution.getScaleFactor();
        int width = this.scaledResolution.getScaledWidth();
        int height = this.scaledResolution.getScaledHeight();

        if (width <= 0 || height <= 0 || scale <= 0) return;

        int dispW = width * scale;
        int dispH = height * scale;

        if (this.framebuffer == null) {
            this.framebuffer = new Framebuffer(dispW, dispH, false);
        } else if (this.framebuffer.framebufferWidth != dispW || this.framebuffer.framebufferHeight != dispH) {
            this.framebuffer.createBindFramebuffer(dispW, dispH);
        }

        this.framebuffer.bindFramebuffer(false);
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

        // 1. 背景：启用着色器时绘制动态背景，否则为极简纯黑暗色背景
        MenuShader ms = MenuShader.getInstance();
        boolean useShader = ms != null && ms.enableShader.isEnabled() && ms.loadingScreens.isEnabled() && ShaderManager.getInstance().isShaderActive();
        if (useShader) {
            ShaderManager.getInstance().render(width, height, width / 2, height / 2);
            Gui.drawRect(0, 0, width, height, 0x88000000);
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
        FontRenderer font = this.mc.fontRendererObj;
        if (font != null) {
            if (progress >= 0) {
                String pctStr = progress + "%";
                font.drawString(pctStr, (width - font.getStringWidth(pctStr)) / 2.0f, barY - 12.0f, 0xFFFFFFFF, false);
            }
            if (this.currentlyDisplayedText != null && !this.currentlyDisplayedText.isEmpty()) {
                font.drawString(this.currentlyDisplayedText, (width - font.getStringWidth(this.currentlyDisplayedText)) / 2.0f, barY + barHeight + 10.0f, 0xFFD7D7D7, false);
            } else if (this.message != null && !this.message.isEmpty()) {
                font.drawString(this.message, (width - font.getStringWidth(this.message)) / 2.0f, barY + barHeight + 10.0f, 0xFFD7D7D7, false);
            }
        }

        this.framebuffer.unbindFramebuffer();
        this.framebuffer.framebufferRender(dispW, dispH);
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(516, 0.1F);
        this.mc.updateDisplay();
    }
}
