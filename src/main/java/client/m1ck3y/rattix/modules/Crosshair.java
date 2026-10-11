package client.m1ck3y.rattix.modules;

import client.m1ck3y.rattix.modules.manager.Category;
import client.m1ck3y.rattix.modules.manager.Register;

import client.m1ck3y.rattix.modules.ClickGUI.BooleanSetting;
import client.m1ck3y.rattix.modules.ClickGUI.NumberSetting;
import client.m1ck3y.rattix.utils.RenderUtil;
import net.minecraft.client.gui.ScaledResolution;

public class Crosshair extends Register {
    public NumberSetting length = new NumberSetting("Length", 4, 1, 10, 1);
    public NumberSetting gap = new NumberSetting("Gap", 2, 0, 8, 1);
    public BooleanSetting dot = new BooleanSetting("Center Dot", true);

    public Crosshair() {
        super("Customizable screen crosshair.", Category.VISUAL);
        addSettings(length, gap, dot);
        initVisualSettings();
        this.visualBackground.setValue(false);
    }

    @Override
    public void onRender2D() {
        if (!isEnabled()) return;

        ScaledResolution sr = new ScaledResolution(mc);
        float cx = sr.getScaledWidth() / 2.0f;
        float cy = sr.getScaledHeight() / 2.0f;

        float s = length.getValue().floatValue();
        float g = gap.getValue().floatValue();
        float boxExtent = g + s + 2.0f;

        beginVisualTransform(cx, cy);
        drawVisualBackground(cx - boxExtent, cy - boxExtent, boxExtent * 2.0f, boxExtent * 2.0f);

        int color = visualTextColor != null ? visualTextColor.getRGB() : 0xFF38B6FF;

        // Top
        RenderUtil.drawRect(cx - 0.5f, cy - g - s, cx + 0.5f, cy - g, color);
        // Bottom
        RenderUtil.drawRect(cx - 0.5f, cy + g, cx + 0.5f, cy + g + s, color);
        // Left
        RenderUtil.drawRect(cx - g - s, cy - 0.5f, cx - g, cy + 0.5f, color);
        // Right
        RenderUtil.drawRect(cx + g, cy - 0.5f, cx + g + s, cy + 0.5f, color);

        if (dot.getValue()) {
            RenderUtil.drawRect(cx - 0.5f, cy - 0.5f, cx + 0.5f, cy + 0.5f, 0xFFFFFFFF);
        }
        endVisualTransform();
    }
}
