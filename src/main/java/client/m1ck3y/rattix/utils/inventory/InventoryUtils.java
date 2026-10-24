package client.m1ck3y.rattix.utils.inventory;

import net.minecraft.client.Minecraft;

/**
 * Inventory utility helper.
 * Aligned with LiquidBounce utils/inventory/InventoryUtils.
 */
public class InventoryUtils {
    private static final Minecraft mc = Minecraft.getMinecraft();

    public static int findItem(int startSlot, int endSlot, net.minecraft.item.Item item) {
        if (mc.thePlayer == null) return -1;
        for (int i = startSlot; i < endSlot; i++) {
            net.minecraft.item.ItemStack stack = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
            if (stack != null && stack.getItem() == item) {
                return i;
            }
        }
        return -1;
    }
}
