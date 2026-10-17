package client.m1ck3y.rattix.modules;

import client.m1ck3y.rattix.modules.manager.Category;
import client.m1ck3y.rattix.modules.manager.Register;

import client.m1ck3y.rattix.modules.ClickGUI.BooleanSetting;
import client.m1ck3y.rattix.utils.FontUtil;
import client.m1ck3y.rattix.utils.RenderUtil;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;

public class ArmorStatus extends Register {
    public BooleanSetting showDamage = new BooleanSetting("Show Durability", true);

    public ArmorStatus() {
        super("Displays equipped armor and held items with durability.", Category.VISUAL);
        addSettings(showDamage);
        initVisualSettings();
        setEnabled(false);
    }

    @Override
    public void onRender2D() {
        if (!isEnabled() || mc.thePlayer == null) return;

        ScaledResolution sr = RenderUtil.getScaledResolution();
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
                    maxTextWidth = Math.max(maxTextWidth, (int) Math.ceil(getVisualTextWidth(cur + "/" + max)));
                }
            }
        }

        if (count == 0) return;

        int totalH = count * 18 + 4;
        int boxW = (maxTextWidth > 0 ? 24 + maxTextWidth : 18) + 6;

        beginVisualTransform(x - 3 + boxW * 0.5f, y - 3 + totalH * 0.5f);
        drawVisualBackground(x - 3, y - 3, boxW, totalH);

        RenderHelper.enableGUIStandardItemLighting();
        for (int i = 3; i >= 0; i--) {
            ItemStack stack = mc.thePlayer.inventory.armorItemInSlot(i);
            if (stack != null) {
                mc.getRenderItem().renderItemAndEffectIntoGUI(stack, x, y);
                mc.getRenderItem().renderItemOverlays(mc.fontRendererObj, stack, x, y);

                if (showDamage.getValue() && stack.isItemStackDamageable()) {
                    int max = stack.getMaxDamage();
                    int cur = max - stack.getItemDamage();
                    RenderHelper.disableStandardItemLighting();
                    GlStateManager.disableLighting();
                    drawVisualText(cur + "/" + max, x + 20, y + 4);
                    RenderHelper.enableGUIStandardItemLighting();
                }
                y += 18;
            }
        }
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableLighting();
        endVisualTransform();
    }
}
