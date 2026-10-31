package client.m1ck3y.rattix.features.module.modules;

import client.m1ck3y.rattix.features.module.Category;
import client.m1ck3y.rattix.features.module.Module;
import client.m1ck3y.rattix.features.module.modules.ClickGUI.CheckBoxSetting;
import client.m1ck3y.rattix.features.module.modules.ClickGUI.ColorSetting;

/**
 * 参考 Miralune 架构实现的植被与群系色彩 (Foliage) 模块。
 * 支持自由定制草地 (Grass)、树叶 (Leaves)、藤蔓 (Vines) 以及全局植物 (WorldColor) 颜色，
 * 并支持动态重新渲染区块网格。
 */
public class Foliage extends Module {
    private static Foliage instance;

    public final CheckBoxSetting customGrass = new CheckBoxSetting("CustomGrass", false, "Foliage");
    public final ColorSetting grassColor = new ColorSetting("GrassColor", 0xFF73AC45, "Foliage").withParent(customGrass);

    public final CheckBoxSetting customLeaves = new CheckBoxSetting("CustomLeaves", false, "Foliage");
    public final ColorSetting leavesColor = new ColorSetting("LeavesColor", 0xFF58A050, "Foliage").withParent(customLeaves);

    public final CheckBoxSetting customVines = new CheckBoxSetting("CustomVines", false, "Foliage");
    public final ColorSetting vinesColor = new ColorSetting("VinesColor", 0xFF518A42, "Foliage").withParent(customVines);

    public final CheckBoxSetting customWorldColor = new CheckBoxSetting("CustomWorldColor", false, "WorldColor");
    public final ColorSetting worldColor = new ColorSetting("WorldColor", 0xFF38B6FF, "WorldColor").withParent(customWorldColor);

    private boolean prevActive = false;
    private int prevGrassColor = 0;
    private int prevLeavesColor = 0;
    private int prevWorldColor = 0;

    public Foliage() {
        super("Foliage", "Customise client grass, leaves, vines and world vegetation colors.", Category.VISUAL);
        instance = this;
        addSettings(
                customGrass, grassColor,
                customLeaves, leavesColor,
                customVines, vinesColor,
                customWorldColor, worldColor
        );
    }

    public static Foliage getInstance() {
        return instance;
    }

    public Integer getGrassColor() {
        if (!isEnabled()) return null;
        if (customGrass.getValue()) return grassColor.getRGB();
        if (customWorldColor.getValue()) return worldColor.getRGB();
        return null;
    }

    public Integer getLeavesColor() {
        if (!isEnabled()) return null;
        if (customLeaves.getValue()) return leavesColor.getRGB();
        if (customWorldColor.getValue()) return worldColor.getRGB();
        return null;
    }

    public Integer getVinesColor() {
        if (!isEnabled()) return null;
        if (customVines.getValue()) return vinesColor.getRGB();
        if (customWorldColor.getValue()) return worldColor.getRGB();
        return null;
    }

    public Integer getWorldColor() {
        if (!isEnabled()) return null;
        if (customWorldColor.getValue()) return worldColor.getRGB();
        return null;
    }

    @Override
    public void onEnable() {
        reloadRenderers();
    }

    @Override
    public void onDisable() {
        reloadRenderers();
    }

    @Override
    public void onTick() {
        if (!isEnabled()) return;
        boolean active = customGrass.getValue() || customLeaves.getValue() || customVines.getValue() || customWorldColor.getValue();
        int g = grassColor.getRGB();
        int l = leavesColor.getRGB();
        int w = worldColor.getRGB();

        if (active != prevActive || g != prevGrassColor || l != prevLeavesColor || w != prevWorldColor) {
            prevActive = active;
            prevGrassColor = g;
            prevLeavesColor = l;
            prevWorldColor = w;
            reloadRenderers();
        }
    }

    private void reloadRenderers() {
        if (mc.renderGlobal != null) {
            mc.renderGlobal.loadRenderers();
        }
    }
}
