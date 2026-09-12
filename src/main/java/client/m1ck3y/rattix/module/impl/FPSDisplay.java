package client.m1ck3y.rattix.module.impl;

import client.m1ck3y.rattix.module.Category;
import client.m1ck3y.rattix.module.Module;
import client.m1ck3y.rattix.module.clickgui.BooleanSetting;
import client.m1ck3y.rattix.utils.FontUtil;
import client.m1ck3y.rattix.utils.RenderUtil;
import net.minecraft.client.Minecraft;

public class FPSDisplay extends Module {
    public BooleanSetting showBackground = new BooleanSetting("Background", true);
    public BooleanSetting showLabel = new BooleanSetting("Show 'FPS' text", true);

    public FPSDisplay() {
        super("FPS Display", "Shows current Frames Per Second.", Category.HUD);
        addSettings(showBackground, showLabel);
        setEnabled(true);
    }

    @Override
    public void onRender2D() {
        if (!isEnabled()) return;
        int fps = Minecraft.getDebugFPS();
        String text = showLabel.getValue() ? "FPS: §f" + fps : "§f" + fps;

        int x = 4;
        int y = 4;
        int width = (int) Math.ceil(FontUtil.getHudStringWidth(text)) + 8;
        int height = 14;

        if (showBackground.getValue()) {
            RenderUtil.drawRoundedRect(x, y, width, height, 4, 0x90000000);
            RenderUtil.drawRoundedOutline(x, y, width, height, 4, 1.0f, 0x25FFFFFF);
        }
        FontUtil.drawHudString("§b" + text, x + 4, y + 3, 0xFFFFFF);
    }
}
