package client.m1ck3y.rattix.injection.mixin;

import client.m1ck3y.rattix.utils.render.RenderUtil;
import client.m1ck3y.rattix.utils.render.shader.ShaderBackgroundRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.renderer.Tessellator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiSlot.class)
public abstract class MixinGuiSlot {

    @Shadow @Final protected Minecraft mc;
    @Shadow public int width;
    @Shadow public int height;
    @Shadow public int top;
    @Shadow public int bottom;
    @Shadow public int right;
    @Shadow public int left;

    /**
     * 在滚动列表（单人世界选择、多人服务器列表、材质包、键位等）渲染之初，
     * 自动绘制全屏动态 GLSL 着色器背景，呈现与主菜单完全一致的生动视觉。
     * 当处于内嵌子页面窗口（SubpageWindow）时跳过全屏着色器，保留窗口半透明亚克力质感。
     */
    @Inject(method = "drawScreen", at = @At("HEAD"))
    private void onDrawScreenHead(int mouseXIn, int mouseYIn, float partialTicks, CallbackInfo ci) {
        if (client.m1ck3y.rattix.ui.window.SubpageWindow.isRenderingEmbedded) {
            return;
        }
        if (ShaderBackgroundRenderer.shouldRenderShader()) {
            ShaderBackgroundRenderer.renderCurrentScreen();
        }
    }

    /**
     * 取消原版滚动列表默认背景调用
     */
    @Inject(method = "drawBackground", at = @At("HEAD"), cancellable = true)
    private void onDrawBackground(CallbackInfo ci) {
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
     * 取消 Forge 为滚动列表容器所铺设的纯泥土背景（drawContainerBackground）
     */
    @Inject(method = "drawContainerBackground", at = @At("HEAD"), cancellable = true, remap = false)
    private void onDrawContainerBackground(Tessellator tessellator, CallbackInfo ci) {
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
     * 取消原版滚动列表顶部 Header 与底部 Footer 的上下泥土遮罩层
     */
    @Inject(method = "overlayBackground", at = @At("HEAD"), cancellable = true)
    private void onOverlayBackground(int startY, int endY, int startAlpha, int endAlpha, CallbackInfo ci) {
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
     * 在绘制列表项前启用精确硬件视口裁切，防止滚动条目超出 header 与 footer。
     * 若处于内嵌子页面窗口中，则精确叠加窗口内部偏移坐标，由 RenderUtil 裁切栈自动求交集保护边界。
     */
    @Inject(method = "drawSelectionBox", at = @At("HEAD"))
    private void onDrawSelectionBoxHead(int p_148120_1_, int p_148120_2_, int mouseXIn, int mouseYIn, CallbackInfo ci) {
        if (ShaderBackgroundRenderer.shouldRenderShader() || client.m1ck3y.rattix.ui.window.SubpageWindow.isRenderingEmbedded) {
            float scissorX = client.m1ck3y.rattix.ui.window.SubpageWindow.isRenderingEmbedded ? (client.m1ck3y.rattix.ui.window.SubpageWindow.currentContentX + this.left) : this.left;
            float scissorY = client.m1ck3y.rattix.ui.window.SubpageWindow.isRenderingEmbedded ? (client.m1ck3y.rattix.ui.window.SubpageWindow.currentContentY + this.top) : this.top;
            RenderUtil.enableScissor(scissorX, scissorY, this.width, this.bottom - this.top);
        }
    }

    /**
     * 列表项绘制完成后关闭裁切，恢复外部窗口视口
     */
    @Inject(method = "drawSelectionBox", at = @At("RETURN"))
    private void onDrawSelectionBoxReturn(int p_148120_1_, int p_148120_2_, int mouseXIn, int mouseYIn, CallbackInfo ci) {
        if (ShaderBackgroundRenderer.shouldRenderShader() || client.m1ck3y.rattix.ui.window.SubpageWindow.isRenderingEmbedded) {
            RenderUtil.disableScissor();
        }
    }
}
