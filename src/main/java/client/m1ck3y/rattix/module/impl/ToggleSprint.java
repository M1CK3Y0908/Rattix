package client.m1ck3y.rattix.module.impl;

import client.m1ck3y.rattix.module.Category;
import client.m1ck3y.rattix.module.Module;
import client.m1ck3y.rattix.module.clickgui.BooleanSetting;
import client.m1ck3y.rattix.util.FontUtil;
import client.m1ck3y.rattix.util.RenderUtil;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.settings.KeyBinding;

public class ToggleSprint extends Module {
    public BooleanSetting showText = new BooleanSetting("Show HUD Status", true);
    public BooleanSetting showBackground = new BooleanSetting("Background", true);

    public ToggleSprint() {
        super("ToggleSprint", "Automatically toggles sprint without holding key.", Category.MOVEMENT);
        addSettings(showText, showBackground);
        setEnabled(true);
    }

    @Override
    public void onTick() {
        if (!isEnabled()) return;
        if (mc.thePlayer != null && mc.thePlayer.movementInput.moveForward > 0 && !mc.thePlayer.isSneaking() && !mc.thePlayer.isCollidedHorizontally) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), true);
        }
    }

    @Override
    public void onDisable() {
        if (mc.gameSettings != null) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), false);
        }
    }

    @Override
    public void onRender2D() {
        if (!isEnabled() || !showText.getValue() || mc.thePlayer == null) return;
        ScaledResolution sr = new ScaledResolution(mc);
        String text = "[Sprinting (Toggled)]";
        int x = 4;
        int y = sr.getScaledHeight() / 2 - 20;
        int width = (int) Math.ceil(FontUtil.getHudStringWidth(text)) + 8;
        int height = 14;

        if (showBackground.getValue()) {
            RenderUtil.drawRoundedRect(x, y, width, height, 4, 0x90000000);
            RenderUtil.drawRoundedOutline(x, y, width, height, 4, 1.0f, 0x25FFFFFF);
            FontUtil.drawHudString("§b" + text, x + 4, y + 3, 0xFFFFFF);
        } else {
            FontUtil.drawHudString("§7" + text, x, y, 0xFFFFFF);
        }
    }
}
