package client.m1ck3y.rattix.module.impl;

import client.m1ck3y.rattix.module.Category;
import client.m1ck3y.rattix.module.Module;
import client.m1ck3y.rattix.module.clickgui.BooleanSetting;
import client.m1ck3y.rattix.utils.FontUtil;
import client.m1ck3y.rattix.utils.RenderUtil;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;

public class ArmorStatus extends Module {
    public BooleanSetting showDamage = new BooleanSetting("Show Durability", true);
    public BooleanSetting showBackground = new BooleanSetting("Background", true);

    public ArmorStatus() {
        super("Armor Status", "Displays equipped armor and held items with durability.", Category.HUD);
        addSettings(showDamage, showBackground);
        setEnabled(false);
    }

    @Override
    public void onRender2D() {
        if (!isEnabled() || mc.thePlayer == null) return;

        ScaledResolution sr = new ScaledResolution(mc);
        int x = 6;
        int y = sr.getScaledHeight() / 2 + 10;

        int count = 0;
        int maxTextWidth = 0;
        for (int i = 3; i >= 0; i--) {
            ItemStack stack = mc.thePlayer.inventory.armorItemInSlot(i);
            if (stack != null) {
                count++;
                if (showDamage.getValue() && stack.isItemStackDamageable()) {
                    int max = stack.getMaxDamage();
                    int cur = max - stack.getItemDamage();
                    maxTextWidth = Math.max(maxTextWidth, (int) Math.ceil(FontUtil.getHudStringWidth(cur + "/" + max)));
                }
            }
        }

        if (count == 0) return;

        int totalH = count * 18 + 4;
        int boxW = (maxTextWidth > 0 ? 24 + maxTextWidth : 18) + 6;

        if (showBackground.getValue()) {
            RenderUtil.drawRoundedRect(x - 3, y - 3, boxW, totalH, 4, 0x90000000);
            RenderUtil.drawRoundedOutline(x - 3, y - 3, boxW, totalH, 4, 1.0f, 0x25FFFFFF);
        }

        RenderHelper.enableGUIStandardItemLighting();
        for (int i = 3; i >= 0; i--) {
            ItemStack stack = mc.thePlayer.inventory.armorItemInSlot(i);
            if (stack != null) {
                mc.getRenderItem().renderItemAndEffectIntoGUI(stack, x, y);
                mc.getRenderItem().renderItemOverlays(mc.fontRendererObj, stack, x, y);

                if (showDamage.getValue() && stack.isItemStackDamageable()) {
                    int max = stack.getMaxDamage();
                    int cur = max - stack.getItemDamage();
                    FontUtil.drawHudString(cur + "/" + max, x + 20, y + 4, 0xFFFFFF);
                }
                y += 18;
            }
        }
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableLighting();
    }
}
