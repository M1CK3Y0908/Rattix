package client.m1ck3y.rattix.modules;

import client.m1ck3y.rattix.modules.manager.Category;
import client.m1ck3y.rattix.modules.manager.Register;

import client.m1ck3y.rattix.modules.clickgui.BooleanSetting;
import client.m1ck3y.rattix.utils.FontUtil;
import client.m1ck3y.rattix.utils.RenderUtil;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;
import java.util.List;

public class CPSDisplay extends Register {
    public BooleanSetting showRMB = new BooleanSetting("Show Right Click", true);
    public BooleanSetting showBackground = new BooleanSetting("Background", true);

    private final List<Long> leftClicks = new ArrayList<>();
    private final List<Long> rightClicks = new ArrayList<>();
    private boolean lastLeftState = false;
    private boolean lastRightState = false;

    public CPSDisplay() {
        super("Displays your Clicks Per Second in real time.", Category.VISUAL);
        addSettings(showRMB, showBackground);
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

        leftClicks.removeIf(clickTime -> time - clickTime > 1000);
        rightClicks.removeIf(clickTime -> time - clickTime > 1000);
    }

    @Override
    public void onRender2D() {
        if (!isEnabled()) return;
        int lCps = leftClicks.size();
        int rCps = rightClicks.size();

        String text = showRMB.getValue() ? "CPS: §f" + lCps + " §7| §f" + rCps : "CPS: §f" + lCps;

        int x = 4;
        int y = 22;
        int width = (int) Math.ceil(FontUtil.getHudStringWidth(text)) + 8;
        int height = 14;

        if (showBackground.getValue()) {
            RenderUtil.drawRoundedRect(x, y, width, height, 4, 0x90000000);
            RenderUtil.drawRoundedOutline(x, y, width, height, 4, 1.0f, 0x25FFFFFF);
        }
        FontUtil.drawHudString("§b" + text, x + 4, y + 3, 0xFFFFFF);
    }
}
