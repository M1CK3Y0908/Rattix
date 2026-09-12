package client.m1ck3y.rattix.modules.clickgui.comp;

import client.m1ck3y.rattix.modules.manager.Register;
import client.m1ck3y.rattix.modules.clickgui.BooleanSetting;
import client.m1ck3y.rattix.modules.clickgui.ModeSetting;
import client.m1ck3y.rattix.modules.clickgui.NumberSetting;
import client.m1ck3y.rattix.modules.clickgui.Setting;
import client.m1ck3y.rattix.utils.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import org.lwjgl.opengl.GL11;

public class ModCard {
    private final Register module;
    private boolean settingsExpanded = false;
    private Setting<?> draggingSlider = null;

    // Animation progress for switch (0.0 = off, 1.0 = on)
    private float toggleAnimation = 0.0f;

    public ModCard(Register module) {
        this.module = module;
        this.toggleAnimation = module.isEnabled() ? 1.0f : 0.0f;
    }

    public float getHeight() {
        if (!settingsExpanded || module.getSettings().isEmpty()) {
            return 38;
        }
        return 38 + module.getSettings().size() * 18 + 4;
    }

    public void draw(float x, float y, float width, int mouseX, int mouseY) {
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        float height = getHeight();

        // Update toggle animation smoothly
        float target = module.isEnabled() ? 1.0f : 0.0f;
        toggleAnimation += (target - toggleAnimation) * 0.25f;

        boolean hovered = RenderUtil.isHovered(mouseX, mouseY, x, y, width, 38);
        int cardBg = hovered ? 0xCC252B38 : 0xBB1E232E;

        // Draw card main body
        RenderUtil.drawRoundedRect(x, y, width, height, 5, cardBg);
        RenderUtil.drawRoundedOutline(x, y, width, height, 5, 1.0f, module.isEnabled() ? 0x3338B6FF : 0x1AFFFFFF);

        // Mod Title & Description
        fr.drawStringWithShadow(module.getName(), x + 8, y + 7, module.isEnabled() ? 0xFFFFFFFF : 0xFFB0B0B0);
        String desc = module.getDescription();
        if (fr.getStringWidth(desc) > width - 70) {
            desc = fr.trimStringToWidth(desc, (int) (width - 75)) + "...";
        }
        fr.drawStringWithShadow("§7" + desc, x + 8, y + 20, 0xFF888888);

        // Settings Gear Icon (if has settings)
        if (!module.getSettings().isEmpty()) {
            float gearX = x + width - 54;
            float gearY = y + 10;
            boolean gearHovered = RenderUtil.isHovered(mouseX, mouseY, gearX, gearY, 18, 18);
            int gearBg = settingsExpanded ? 0xFF38B6FF : (gearHovered ? 0x44FFFFFF : 0x22FFFFFF);
            RenderUtil.drawRoundedRect(gearX, gearY, 18, 18, 4, gearBg);
            fr.drawStringWithShadow("⚙", gearX + 4.5f, gearY + 5, settingsExpanded ? 0xFFFFFFFF : 0xFFCCCCCC);
        }

        // Toggle Switch (CloudClient Style Pill Toggle)
        float switchWidth = 26;
        float switchHeight = 14;
        float switchX = x + width - 32;
        float switchY = y + 12;

        int trackColor = interpolateColor(0xFF3B4354, 0xFF38B6FF, toggleAnimation);
        RenderUtil.drawRoundedRect(switchX, switchY, switchWidth, switchHeight, 7, trackColor);

        float knobX = switchX + 2 + toggleAnimation * (switchWidth - 14);
        float knobY = switchY + 2;
        RenderUtil.drawCircle(knobX + 5, knobY + 5, 5, 0xFFFFFFFF);

        // Draw expanded settings
        if (settingsExpanded && !module.getSettings().isEmpty()) {
            float settingY = y + 40;
            RenderUtil.drawRect(x + 8, settingY - 2, x + width - 8, settingY - 1, 0x22FFFFFF);

            for (Setting<?> setting : module.getSettings()) {
                if (setting instanceof BooleanSetting) {
                    BooleanSetting bs = (BooleanSetting) setting;
                    fr.drawStringWithShadow(bs.getName(), x + 10, settingY + 3, 0xFFD0D0D0);

                    float bToggleX = x + width - 24;
                    boolean bHover = RenderUtil.isHovered(mouseX, mouseY, bToggleX, settingY + 2, 14, 12);
                    RenderUtil.drawRoundedRect(bToggleX, settingY + 2, 14, 12, 3, bs.getValue() ? 0xFF38B6FF : (bHover ? 0x55FFFFFF : 0x2AFFFFFF));
                    if (bs.getValue()) {
                        fr.drawStringWithShadow("✔", bToggleX + 3, settingY + 4, 0xFFFFFFFF);
                    }
                } else if (setting instanceof NumberSetting) {
                    NumberSetting ns = (NumberSetting) setting;
                    String text = ns.getName() + ": §b" + String.format("%.1f", ns.getValue());
                    fr.drawStringWithShadow(text, x + 10, settingY + 3, 0xFFD0D0D0);

                    float sliderWidth = 70;
                    float sliderX = x + width - sliderWidth - 10;
                    float sliderY = settingY + 5;
                    RenderUtil.drawRoundedRect(sliderX, sliderY, sliderWidth, 6, 3, 0x44000000);

                    float progress = (float) ((ns.getValue() - ns.getMin()) / (ns.getMax() - ns.getMin()));
                    progress = Math.max(0.0f, Math.min(1.0f, progress));
                    RenderUtil.drawRoundedRect(sliderX, sliderY, sliderWidth * progress, 6, 3, 0xFF38B6FF);
                    RenderUtil.drawCircle(sliderX + sliderWidth * progress, sliderY + 3, 4, 0xFFFFFFFF);

                    // Update slider if currently dragging
                    if (draggingSlider == ns) {
                        double val = ns.getMin() + Math.max(0, Math.min(1, (mouseX - sliderX) / sliderWidth)) * (ns.getMax() - ns.getMin());
                        ns.setValue(val);
                    }
                } else if (setting instanceof ModeSetting) {
                    ModeSetting ms = (ModeSetting) setting;
                    fr.drawStringWithShadow(ms.getName(), x + 10, settingY + 3, 0xFFD0D0D0);

                    String modeStr = "§b" + ms.getValue();
                    float btnWidth = fr.getStringWidth(modeStr) + 12;
                    float btnX = x + width - btnWidth - 10;
                    boolean mHover = RenderUtil.isHovered(mouseX, mouseY, btnX, settingY + 1, btnWidth, 14);
                    RenderUtil.drawRoundedRect(btnX, settingY + 1, btnWidth, 14, 3, mHover ? 0x5538B6FF : 0x2AFFFFFF);
                    fr.drawStringWithShadow(modeStr, btnX + 6, settingY + 4, 0xFFFFFFFF);
                }

                settingY += 18;
            }
        }
    }

    public void mouseClicked(float x, float y, float width, int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0) {
            // Clicked toggle switch or card header
            float switchX = x + width - 32;
            float switchY = y + 12;
            if (RenderUtil.isHovered(mouseX, mouseY, switchX - 2, switchY - 2, 30, 18)) {
                module.toggle();
                return;
            }

            // Clicked settings gear
            if (!module.getSettings().isEmpty()) {
                float gearX = x + width - 54;
                float gearY = y + 10;
                if (RenderUtil.isHovered(mouseX, mouseY, gearX, gearY, 18, 18)) {
                    settingsExpanded = !settingsExpanded;
                    return;
                }
            }

            // Clicked settings items
            if (settingsExpanded && !module.getSettings().isEmpty()) {
                float settingY = y + 40;
                for (Setting<?> setting : module.getSettings()) {
                    if (setting instanceof BooleanSetting) {
                        float bToggleX = x + width - 24;
                        if (RenderUtil.isHovered(mouseX, mouseY, bToggleX, settingY + 2, 14, 12)) {
                            ((BooleanSetting) setting).toggle();
                        }
                    } else if (setting instanceof NumberSetting) {
                        float sliderWidth = 70;
                        float sliderX = x + width - sliderWidth - 10;
                        if (RenderUtil.isHovered(mouseX, mouseY, sliderX - 4, settingY, sliderWidth + 8, 12)) {
                            draggingSlider = setting;
                            NumberSetting ns = (NumberSetting) setting;
                            double val = ns.getMin() + Math.max(0, Math.min(1, (mouseX - sliderX) / sliderWidth)) * (ns.getMax() - ns.getMin());
                            ns.setValue(val);
                        }
                    } else if (setting instanceof ModeSetting) {
                        ModeSetting ms = (ModeSetting) setting;
                        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
                        String modeStr = ms.getValue();
                        float btnWidth = fr.getStringWidth(modeStr) + 12;
                        float btnX = x + width - btnWidth - 10;
                        if (RenderUtil.isHovered(mouseX, mouseY, btnX, settingY + 1, btnWidth, 14)) {
                            ms.cycle();
                        }
                    }
                    settingY += 18;
                }
            }
        } else if (mouseButton == 1) {
            // Right-click card to expand settings
            if (RenderUtil.isHovered(mouseX, mouseY, x, y, width, 38) && !module.getSettings().isEmpty()) {
                settingsExpanded = !settingsExpanded;
            }
        }
    }

    public void mouseReleased(int mouseX, int mouseY, int state) {
        draggingSlider = null;
    }

    private int interpolateColor(int color1, int color2, float fraction) {
        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;
        int a1 = (color1 >> 24) & 0xFF;

        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;
        int a2 = (color2 >> 24) & 0xFF;

        int r = (int) (r1 + (r2 - r1) * fraction);
        int g = (int) (g1 + (g2 - g1) * fraction);
        int b = (int) (b1 + (b2 - b1) * fraction);
        int a = (int) (a1 + (a2 - a1) * fraction);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public Register getModule() {
        return module;
    }
}
