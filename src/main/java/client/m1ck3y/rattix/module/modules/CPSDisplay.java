package client.m1ck3y.rattix.module.modules;

import client.m1ck3y.rattix.module.Category;
import client.m1ck3y.rattix.module.Module;

import client.m1ck3y.rattix.module.modules.ClickGUI.BooleanSetting;
import client.m1ck3y.rattix.utils.FontUtil;
import client.m1ck3y.rattix.utils.RenderUtil;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;
import java.util.List;

public class CPSDisplay extends Module {
    public BooleanSetting showRMB = new BooleanSetting("Show Right Click", true);

    private final List<Long> leftClicks = new ArrayList<>();
    private final List<Long> rightClicks = new ArrayList<>();
    private boolean lastLeftState = false;
    private boolean lastRightState = false;

    public CPSDisplay() {
        super("Displays your Clicks Per Second in real time.", Category.VISUAL);
        addSettings(showRMB);
        initVisualSettings();
        setEnabled(true);
    }

    @Override
    public void onTick() {
        if (!isEnabled()) return;
        long time = System.currentTimeMillis();

        boolean leftDown = Mouse.isButtonDown(0);
        if (leftDown && !lastLeftState) {
            leftClicks.add(time);
        }
        lastLeftState = leftDown;

        boolean rightDown = Mouse.isButtonDown(1);
        if (rightDown && !lastRightState) {
            rightClicks.add(time);
        }
        lastRightState = rightDown;

        while (!leftClicks.isEmpty() && time - leftClicks.get(0) > 1000) {
            leftClicks.remove(0);
        }
        while (!rightClicks.isEmpty() && time - rightClicks.get(0) > 1000) {
            rightClicks.remove(0);
        }
    }

    @Override
    public void onRender2D() {
        if (!isEnabled()) return;
        int lCps = leftClicks.size();
        int rCps = rightClicks.size();

        String text = showRMB.getValue() ? "CPS: " + lCps + " | " + rCps : "CPS: " + lCps;

        float x = 4.0f;
        float y = 22.0f;
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
