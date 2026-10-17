package client.m1ck3y.rattix.module.modules;

import client.m1ck3y.rattix.module.Category;
import client.m1ck3y.rattix.module.Module;

import client.m1ck3y.rattix.module.modules.ClickGUI.BooleanSetting;
import client.m1ck3y.rattix.utils.FontUtil;
import client.m1ck3y.rattix.utils.RenderUtil;
import net.minecraft.client.network.NetworkPlayerInfo;

public class PingDisplay extends Module {

    public PingDisplay() {
        super("Displays your server ping in milliseconds.", Category.VISUAL);
        initVisualSettings();
        setEnabled(true);
    }

    @Override
    public void onRender2D() {
        if (!isEnabled() || mc.thePlayer == null || mc.getNetHandler() == null) return;
        NetworkPlayerInfo info = mc.getNetHandler().getPlayerInfo(mc.thePlayer.getUniqueID());
        int ping = info != null ? info.getResponseTime() : 0;
        String text = "Ping: " + ping + "ms";

        float x = 4.0f;
        float y = 40.0f;
        float textW = getVisualTextWidth(text);
        float textH = getVisualTextHeight();
        float width = (float) Math.ceil(textW) + 8.0f;
        float height = Math.max(14.0f, textH + 6.0f);

        beginVisualTransform(x + width * 0.5f, y + height * 0.5f);
        drawVisualBackground(x, y, width, height);
        drawVisualText(text, x + 4.0f, y + (height - textH) * 0.5f);
        endVisualTransform();
    }
}
