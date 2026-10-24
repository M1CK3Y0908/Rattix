package client.m1ck3y.rattix.tabs;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;

/**
 * Creative tab for custom blocks.
 * Aligned with LiquidBounce tabs/BlocksTab.
 */
public class BlocksTab extends CreativeTabs {
    public static final BlocksTab INSTANCE = new BlocksTab();

    public BlocksTab() {
        super("blocksTab");
    }

    @Override
    public Item getTabIconItem() {
        return Item.getItemFromBlock(Blocks.command_block);
    }
}
