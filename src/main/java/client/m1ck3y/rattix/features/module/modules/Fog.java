package client.m1ck3y.rattix.features.module.modules;

import client.m1ck3y.rattix.features.module.Category;
import client.m1ck3y.rattix.features.module.Module;
import client.m1ck3y.rattix.features.module.modules.ClickGUI.CheckBoxSetting;
import client.m1ck3y.rattix.features.module.modules.ClickGUI.ColorSetting;
import client.m1ck3y.rattix.features.module.modules.ClickGUI.ModeSetting;
import client.m1ck3y.rattix.features.module.modules.ClickGUI.NumberSetting;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * 参考 Miralune 架构实现的迷雾定制模块。
 * 支持控制迷雾是否可见、雾模型 (Linear / Exponential / Exponential_Squared)、雾密度、
 * 自定义雾起止距离、常态雾颜色以及水下/岩浆特殊材质下的独立迷雾色彩。
 */
public class Fog extends Module {
    private static Fog instance;

    public final CheckBoxSetting visible = new CheckBoxSetting("Visible", true, "Fog");
    public final ModeSetting model = new ModeSetting("Model", "Linear", "Fog", new String[]{"Linear", "Exponential", "Exponential_Squared"});
    public final NumberSetting density = new NumberSetting("Density", 0.02, 0.001, 0.2, 0.005, "Fog");
    public final NumberSetting startDistance = new NumberSetting("StartDistance", 0.0, 0.0, 200.0, 5.0, "Distance");
    public final NumberSetting endDistance = new NumberSetting("EndDistance", 100.0, 5.0, 500.0, 5.0, "Distance");

    public final ColorSetting color = new ColorSetting("Color", 0xFFC0D8FF, "Color");

    public final CheckBoxSetting underwater = new CheckBoxSetting("Underwater", true, "Water");
    public final ColorSetting underwaterColor = new ColorSetting("UnderwaterColor", 0xFF385A78, "Water").withParent(underwater);

    public final CheckBoxSetting lava = new CheckBoxSetting("Lava", true, "Lava");
    public final ColorSetting lavaColor = new ColorSetting("LavaColor", 0xFFFF5A1F, "Lava").withParent(lava);

    public Fog() {
        super("Fog", "Customise client fog visibility, distance, model and colors.", Category.VISUAL);
        instance = this;
        addSettings(
                visible, model, density,
                startDistance, endDistance,
                color,
                underwater, underwaterColor,
                lava, lavaColor
        );
    }

    public static Fog getInstance() {
        return instance;
    }

    public boolean applies() {
        return isEnabled() && visible.getValue();
    }

    public Integer colorFor(Material material) {
        if (!isEnabled()) return null;
        if (material == Material.water) {
            return underwater.getValue() ? underwaterColor.getRGB() : null;
        }
        if (material == Material.lava) {
            return lava.getValue() ? lavaColor.getRGB() : null;
        }
        return color.getRGB();
    }

    public float getStartDistance(int fogMode) {
        return fogMode == -1 ? 0.0F : startDistance.getValue().floatValue();
    }

    public float getEndDistance(int fogMode) {
        float distance = Math.max(startDistance.getValue().floatValue() + 1.0F, endDistance.getValue().floatValue());
        return fogMode == -1 ? distance * 0.8F : distance;
    }

    @Override
    public void onEnable() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void onDisable() {
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    @SubscribeEvent
    public void onFogColors(EntityViewRenderEvent.FogColors event) {
        if (!isEnabled()) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.theWorld == null) return;

        Material material = Material.air;
        Entity viewEntity = mc.getRenderViewEntity();
        if (viewEntity != null) {
            BlockPos pos = new BlockPos(viewEntity.posX, viewEntity.posY + (double) viewEntity.getEyeHeight(), viewEntity.posZ);
            material = mc.theWorld.getBlockState(pos).getBlock().getMaterial();
        }

        Integer c = colorFor(material);
        if (c != null) {
            event.red = ((c >> 16) & 0xFF) / 255.0f;
            event.green = ((c >> 8) & 0xFF) / 255.0f;
            event.blue = (c & 0xFF) / 255.0f;
        }
    }
}
