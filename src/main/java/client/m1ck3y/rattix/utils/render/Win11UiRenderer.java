package client.m1ck3y.rattix.utils.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiOptionSlider;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.MathHelper;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Win11UiRenderer - Fluent Design (Windows 11 inspired) control renderer for Rattix Client.
 * Provides dark acrylic surfaces, reveal lighting on hover, smooth 1px borders,
 * dynamic slider tracks/thumbs, and clean typography across all Minecraft and Forge screens.
 */
public final class Win11UiRenderer {
    public static volatile boolean mixinApplied = false;
    private static volatile long lastMixinDrawTime = 0;

    private static final Map<GuiButton, Long> hoverStartMap = new WeakHashMap<>();
    private static final Map<GuiButton, Boolean> lastHoverMap = new WeakHashMap<>();

    private static Field optionSliderValueField;
    private static boolean optionSliderFieldInit = false;

    private static Field forgeSliderValueField;
    private static boolean forgeSliderFieldInit = false;

    private Win11UiRenderer() {}

    public static void markMixinDrawn() {
        mixinApplied = true;
        lastMixinDrawTime = System.currentTimeMillis();
    }

    public static boolean isMixinActive() {
        return mixinApplied && (System.currentTimeMillis() - lastMixinDrawTime < 1000);
    }

    public static float getHoverProgress(GuiButton button, boolean hovered) {
        Boolean lastHover = lastHoverMap.get(button);
        if (lastHover == null || lastHover != hovered) {
            lastHoverMap.put(button, hovered);
            hoverStartMap.put(button, System.currentTimeMillis());
        }
        Long start = hoverStartMap.get(button);
        if (start == null) return hovered ? 1.0f : 0.0f;
        return MathHelper.clamp_float((System.currentTimeMillis() - start) / 120.0f, 0.0f, 1.0f);
    }

    public static boolean isSliderButton(GuiButton button) {
        if (button == null) return false;
        if (button instanceof GuiOptionSlider) return true;
        String name = button.getClass().getName();
        return name.contains("GuiSlider") || name.contains("Slider");
    }

    public static float getSliderValue(GuiButton button) {
        if (button instanceof GuiOptionSlider) {
            if (!optionSliderFieldInit) {
                optionSliderFieldInit = true;
                for (Field f : GuiOptionSlider.class.getDeclaredFields()) {
                    if (f.getType() == float.class && !Modifier.isFinal(f.getModifiers())) {
                        f.setAccessible(true);
                        optionSliderValueField = f;
                        break;
                    }
                }
            }
            if (optionSliderValueField != null) {
                try {
                    return optionSliderValueField.getFloat(button);
                } catch (Throwable ignored) {}
            }
        } else if (button != null) {
            if (!forgeSliderFieldInit) {
                forgeSliderFieldInit = true;
                try {
                    for (Field f : button.getClass().getFields()) {
                        if (f.getName().equals("sliderValue")) {
                            forgeSliderValueField = f;
                            break;
                        }
                    }
                } catch (Throwable ignored) {}
            }
            if (forgeSliderValueField != null) {
                try {
                    Object val = forgeSliderValueField.get(button);
                    if (val instanceof Number) {
                        return ((Number) val).floatValue();
                    }
                } catch (Throwable ignored) {}
            }
        }
        return 0.5f;
    }

    public static void drawFluentButton(GuiButton button, Minecraft mc, int mouseX, int mouseY) {
        if (button == null || !button.visible) return;

        boolean hovered = mouseX >= button.xPosition && mouseY >= button.yPosition
                && mouseX < button.xPosition + button.width && mouseY < button.yPosition + button.height;

        float hoverState = getHoverProgress(button, hovered);

        int background = button.enabled ? (hovered ? 0xFF1F242B : 0xFF1A1D23) : 0xFF16181D;
        int border = button.enabled ? (hovered ? 0xFF76B9ED : 0x553A424E) : 0x333A424E;

        // Base rounded background
        RenderUtil.drawRoundedRect(button.xPosition, button.yPosition, button.width, button.height, 4.0f, background);

        // Hover reveal highlight
        if (button.enabled && hovered) {
            int revealAlpha = 18 + (int) (18.0f * hoverState);
            RenderUtil.drawRoundedRect(button.xPosition + 1, button.yPosition + 1, button.width - 2, button.height - 2, 3.0f,
                    (revealAlpha << 24) | 0xFFFFFF);
        }

        // Outline border
        RenderUtil.drawRoundedOutline(button.xPosition, button.yPosition, button.width, button.height, 4.0f, 1.0f, border);

        // Bottom accent line on hover
        if (button.enabled && hovered) {
            RenderUtil.drawRoundedRect(button.xPosition + 4, button.yPosition + button.height - 2, button.width - 8, 2.0f, 1.0f, 0xFF76B9ED);
        }

        // Slider track and thumb
        if (isSliderButton(button)) {
            float val = getSliderValue(button);
            float trackX = button.xPosition + 8.0f;
            float trackY = button.yPosition + button.height - 5.0f;
            float trackW = Math.max(4.0f, button.width - 16.0f);
            float thumbX = trackX + trackW * MathHelper.clamp_float(val, 0.0f, 1.0f);

            RenderUtil.drawRoundedRect(trackX, trackY, trackW, 2.0f, 1.0f, button.enabled ? 0x889AA7B5 : 0x445C6672);
            RenderUtil.drawRoundedRect(thumbX - 3.0f, button.yPosition + button.height - 8.0f, 6.0f, 8.0f, 3.0f, button.enabled ? 0xFF76B9ED : 0xFF6D737B);
        }

        // Text rendering
        FontRenderer font = mc != null ? mc.fontRendererObj : null;
        if (font != null && button.displayString != null) {
            int textColor;
            if (button.packedFGColour != 0) {
                textColor = button.packedFGColour;
            } else if (!button.enabled) {
                textColor = 0xFF8C929A;
            } else if (hovered) {
                textColor = 0xFFFFFFFF;
            } else {
                textColor = 0xFFF1F4F7;
            }

            String text = button.displayString;
            int textWidth = font.getStringWidth(text);
            int maxW = button.width - 8;
            if (textWidth > maxW && maxW > 20) {
                text = font.trimStringToWidth(text, maxW - font.getStringWidth("...")) + "...";
                textWidth = font.getStringWidth(text);
            }

            int x = button.xPosition + (button.width - textWidth) / 2;
            int y = button.yPosition + (button.height - 8) / 2;
            font.drawStringWithShadow(text, x, y, textColor);
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void drawButton(GuiButton button, Minecraft mc, int mouseX, int mouseY) {
        drawFluentButton(button, mc, mouseX, mouseY);
    }

    public static boolean shouldStyle(GuiButton button) {
        return button != null && button.visible;
    }

    public static void drawButtons(GuiScreen screen, Minecraft mc, int mouseX, int mouseY, List<GuiButton> buttons) {
        if (screen == null || buttons == null) return;
        for (GuiButton button : buttons) {
            drawFluentButton(button, mc, mouseX, mouseY);
        }
    }
}
