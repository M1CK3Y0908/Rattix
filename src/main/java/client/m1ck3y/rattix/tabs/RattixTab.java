package client.m1ck3y.rattix.tabs;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;

/**
 * Rattix 客户端创造模式物品栏 Tab 架构，对齐 LiquidBounce tabs 架构。
 */
public class RattixTab extends CreativeTabs {
    public static final RattixTab INSTANCE = new RattixTab();

    public RattixTab() {
        super("Rattix");
    }

    @Override
    public Item getTabIconItem() {
        return Items.nether_star;
    }

    @Override
    public String getTranslatedTabLabel() {
        return "Rattix";
    }
}
