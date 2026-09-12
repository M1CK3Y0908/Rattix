package client.m1ck3y.rattix.modules;

import client.m1ck3y.rattix.modules.manager.Category;
import client.m1ck3y.rattix.modules.manager.Register;

import client.m1ck3y.rattix.modules.clickgui.BooleanSetting;
import client.m1ck3y.rattix.utils.FontUtil;
import client.m1ck3y.rattix.utils.RenderUtil;
import net.minecraft.client.network.NetworkPlayerInfo;

public class PingDisplay extends Register {
    public BooleanSetting showBackground = new BooleanSetting("Background", true);

    public PingDisplay() {
        super("Displays your server ping in milliseconds.", Category.HUD);
        addSettings(showBackground);
        setEnabled(true);
    }

    @Override
    public void onRender2D() {
        if (!isEnabled() || mc.thePlayer == null || mc.getNetHandler() == null) return;
        NetworkPlayerInfo info = mc.getNetHandler().getPlayerInfo(mc.thePlayer.getUniqueID());
        int ping = info != null ? info.getResponseTime() : 0;
        String text = "Ping: §f" + ping + "ms";

        int x = 4;
        int y = 40;
        int width = (int) Math.ceil(FontUtil.getHudStringWidth(text)) + 8;
        int height = 14;

        if (showBackground.getValue()) {
            RenderUtil.drawRoundedRect(x, y, width, height, 4, 0x90000000);
            RenderUtil.drawRoundedOutline(x, y, width, height, 4, 1.0f, 0x25FFFFFF);
        }
        FontUtil.drawHudString("§b" + text, x + 4, y + 3, 0xFFFFFF);
    }
}
