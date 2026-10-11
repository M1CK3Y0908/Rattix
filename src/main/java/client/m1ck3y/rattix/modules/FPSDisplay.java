package client.m1ck3y.rattix.modules;

import client.m1ck3y.rattix.modules.manager.Category;
import client.m1ck3y.rattix.modules.manager.Register;

import client.m1ck3y.rattix.modules.ClickGUI.BooleanSetting;
import client.m1ck3y.rattix.utils.FontUtil;
import client.m1ck3y.rattix.utils.RenderUtil;
import net.minecraft.client.Minecraft;

public class FPSDisplay extends Register {
    public BooleanSetting showLabel = new BooleanSetting("Show 'FPS' text", true);

    public FPSDisplay() {
        super("Shows current Frames Per Second.", Category.VISUAL);
        addSettings(showLabel);
        initVisualSettings();
        setEnabled(true);
    }

    @Override
    public void onRender2D() {
        if (!isEnabled()) return;
        int fps = Minecraft.getDebugFPS();
        String text = showLabel.getValue() ? "FPS: " + fps : String.valueOf(fps);

        float x = 4.0f;
        float y = 4.0f;
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
