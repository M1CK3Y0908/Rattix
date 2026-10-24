package client.m1ck3y.rattix.tabs;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;

/**
 * Creative tab for heads.
 * Aligned with LiquidBounce tabs/HeadsTab.
 */
public class HeadsTab extends CreativeTabs {
    public static final HeadsTab INSTANCE = new HeadsTab();

    public HeadsTab() {
        super("headsTab");
    }

    @Override
    public Item getTabIconItem() {
        return Items.skull;
    }
}
