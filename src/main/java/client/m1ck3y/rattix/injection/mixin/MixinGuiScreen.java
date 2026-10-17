package client.m1ck3y.rattix.injection.mixin;

import client.m1ck3y.rattix.module.modules.MenuShader;
import client.m1ck3y.rattix.utils.IconUtils;
import client.m1ck3y.rattix.utils.shader.ShaderManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.GuiConnecting;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiScreen.class)
public abstract class MixinGuiScreen extends Gui {

    /**
     * 将统一背景风格（动态着色器或纯黑背景）延伸到所有非局内界面（多人游戏、选项设置、世界选择、断开连接等）
     */
    @Inject(method = "drawBackground", at = @At("HEAD"), cancellable = true)
    private void onDrawBackground(int tint, CallbackInfo ci) {
        GuiScreen self = (GuiScreen) (Object) this;
        if (self.mc != null && self.mc.theWorld == null) {
            MenuShader ms = MenuShader.getInstance();
            boolean useShader = ms != null && ms.enableShader.isEnabled() && ms.allMenus.isEnabled() && ShaderManager.getInstance().isShaderActive();
            if (useShader) {
                int mouseX = Mouse.getX() * self.width / Math.max(1, self.mc.displayWidth);
                int mouseY = self.height - Mouse.getY() * self.height / Math.max(1, self.mc.displayHeight) - 1;
                ShaderManager.getInstance().render(self.width, self.height, mouseX, mouseY);
                // 柔和暗色遮罩，保证前景设置项、按钮、服务器列表等文本极其清晰易读
                drawRect(0, 0, self.width, self.height, 0x88000000);
            } else {
                // 纯黑暗色背景替代原版泥土平铺
                drawRect(0, 0, self.width, self.height, 0xFF141414);
            }
            ci.cancel();
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
}
