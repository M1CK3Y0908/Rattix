package client.m1ck3y.rattix.utils.font;

import client.m1ck3y.rattix.modules.Font;
import client.m1ck3y.rattix.utils.FontUtil;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class GlobalFontRenderer extends FontRenderer {
    private final FontRenderer fallback;

    public GlobalFontRenderer(GameSettings gameSettingsIn, ResourceLocation location, TextureManager textureManagerIn, boolean unicode, FontRenderer fallback) {
        super(gameSettingsIn, location, textureManagerIn, unicode);
        this.fallback = fallback;
    }

    public FontRenderer getFallback() {
        return fallback;
    }

    private float getEffectiveScale() {
        Font fs = FontUtil.getFont();
        return fs != null ? fs.size.getValue().floatValue() / 100.0f : 1.0f;
    }

    public void renderPlainDirect(String text, float x, float y, int color) {
        if (text == null || text.isEmpty()) return;
        FontRenderer fr = (fallback != null) ? fallback : this;
        if (fr == this) {
            super.drawString(text, x, y, color, false);
        } else {
            fr.drawString(text, x, y, color, false);
        }
    }

    public void renderSolidDirect(String text, float x, float y, int color) {
        if (text == null || text.isEmpty()) return;
        String clean = FontUtil.stripColorCodes(text);
        FontRenderer fr = (fallback != null) ? fallback : this;
        if (fr == this) {
            super.drawString(clean, x, y, color, false);
        } else {
            fr.drawString(clean, x, y, color, false);
        }
    }

    @Override
    public int drawStringWithShadow(String text, float x, float y, int color) {
        return drawString(text, x, y, color, true);
    }

    @Override
    public int drawString(String text, int x, int y, int color) {
        return drawString(text, (float) x, (float) y, color, false);
    }

    @Override
    public int drawString(String text, float x, float y, int color, boolean dropShadow) {
        if (text == null) return 0;
        Font fontSettings = FontUtil.getFont();
        float sizeScale = getEffectiveScale();
        this.FONT_HEIGHT = (int) Math.max(1, Math.round(9 * sizeScale));

        if (FontUtil.isCustomFont()) {
            CustomFontRenderer cfr = FontUtil.getGlobalFont();
            if (cfr != null) {
                float renderedW = cfr.drawString(text, x, y, color, dropShadow, 0.5f);
                return (int) Math.ceil(x + renderedW);
            }
        }

        float offX = fontSettings != null ? fontSettings.offsetX.getValue().floatValue() : 0.0f;
        float offY = fontSettings != null ? fontSettings.offsetY.getValue().floatValue() : 0.0f;
        boolean shadowEnabled = dropShadow && (fontSettings == null || fontSettings.shadow.getValue());
        float shadowOffX = fontSettings != null ? fontSettings.shadowOffsetX.getValue().floatValue() : 1.0f;
        float shadowOffY = fontSettings != null ? fontSettings.shadowOffsetY.getValue().floatValue() : 1.0f;
        boolean customShadow = fontSettings != null && fontSettings.shadowMode.is("Custom");
        boolean outlineEnabled = fontSettings != null && fontSettings.outline.getValue();
        float outlineWidth = fontSettings != null ? fontSettings.outlineWidth.getValue().floatValue() : 1.0f;
        String outlineMode = fontSettings != null ? fontSettings.outlineColor.getValue() : "Black";

        int alpha = (color >> 24) & 0xFF;
        if (alpha == 0) alpha = 255;

        FontRenderer fr = (fallback != null) ? fallback : this;

        // If no custom offsets/scale/outline/custom shadow, pass directly through to vanilla font renderer
        if (sizeScale == 1.0f && offX == 0.0f && offY == 0.0f && !outlineEnabled && !customShadow && shadowOffX == 1.0f && shadowOffY == 1.0f) {
            if (fr == this) {
                return super.drawString(text, x, y, color, shadowEnabled);
            } else {
                return fr.drawString(text, x, y, color, shadowEnabled);
            }
        }

        GlStateManager.pushMatrix();
        GlStateManager.translate(x + offX, y + offY, 0);
        if (sizeScale != 1.0f) {
            GlStateManager.scale(sizeScale, sizeScale, 1.0f);
        }

        // Outline for vanilla font
        if (outlineEnabled) {
            int outColor;
            if ("Dark".equalsIgnoreCase(outlineMode)) {
                outColor = (alpha << 24) | ((color & 0xFCFCFC) >> 2);
            } else if ("50% Alpha".equalsIgnoreCase(outlineMode)) {
                outColor = ((alpha / 2) << 24) | 0x000000;
            } else {
                outColor = (alpha << 24) | 0x000000;
            }
            float ow = outlineWidth * 0.5f;
            renderSolidDirect(text, -ow, 0, outColor);
            renderSolidDirect(text, ow, 0, outColor);
            renderSolidDirect(text, 0, -ow, outColor);
            renderSolidDirect(text, 0, ow, outColor);
            renderSolidDirect(text, -ow, -ow, outColor);
            renderSolidDirect(text, ow, -ow, outColor);
            renderSolidDirect(text, -ow, ow, outColor);
            renderSolidDirect(text, ow, ow, outColor);
        }

        // Shadow & Main text for vanilla font
        if (shadowEnabled) {
            if (customShadow) {
                int shadowColor = fontSettings != null ? fontSettings.shadowColor.getRGB() : 0x80000000;
                renderSolidDirect(text, shadowOffX, shadowOffY, shadowColor);
                renderPlainDirect(text, 0, 0, color);
            } else if (shadowOffX == 1.0f && shadowOffY == 1.0f) {
                if (fr == this) {
                    super.drawString(text, 0, 0, color, true);
                } else {
                    fr.drawString(text, 0, 0, color, true);
                }
            } else {
                int shadowColor = (alpha << 24) | ((color & 0xFCFCFC) >> 2);
                renderSolidDirect(text, shadowOffX, shadowOffY, shadowColor);
                renderPlainDirect(text, 0, 0, color);
            }
        } else {
            renderPlainDirect(text, 0, 0, color);
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
        return (int) (x + getStringWidth(text));
    }

    @Override
    public int getStringWidth(String text) {
        if (text == null || text.isEmpty()) return 0;
        float sizeScale = getEffectiveScale();

        if (FontUtil.isCustomFont()) {
            CustomFontRenderer cfr = FontUtil.getGlobalFont();
            if (cfr != null) {
                return (int) Math.ceil(cfr.getStringWidth(text) * 0.5f * sizeScale);
            }
        }
        if (fallback != null) {
            return (int) Math.ceil(fallback.getStringWidth(text) * sizeScale);
        }
        return (int) Math.ceil(super.getStringWidth(text) * sizeScale);
    }

    @Override
    public int getCharWidth(char character) {
        float sizeScale = getEffectiveScale();

        if (FontUtil.isCustomFont()) {
            CustomFontRenderer cfr = FontUtil.getGlobalFont();
            if (cfr != null) {
                return (int) Math.ceil(cfr.getCharWidth(character) * 0.5f * sizeScale);
            }
        }
        if (fallback != null) {
            return (int) Math.ceil(fallback.getCharWidth(character) * sizeScale);
        }
        return (int) Math.ceil(super.getCharWidth(character) * sizeScale);
    }

    @Override
    public String trimStringToWidth(String text, int width) {
        return trimStringToWidth(text, width, false);
    }

    @Override
    public String trimStringToWidth(String text, int width, boolean reverse) {
        if (text == null) return "";
        float sizeScale = getEffectiveScale();

        if (FontUtil.isCustomFont()) {
            CustomFontRenderer cfr = FontUtil.getGlobalFont();
            if (cfr != null) {
                StringBuilder sb = new StringBuilder();
                float curW = 0;
                int start = reverse ? text.length() - 1 : 0;
                int step = reverse ? -1 : 1;
                boolean isColor = false;

                for (int i = start; i >= 0 && i < text.length() && curW < width; i += step) {
                    char c = text.charAt(i);
                    float charW = cfr.getCharWidth(c) * 0.5f * sizeScale;

                    if (isColor) {
                        isColor = false;
                    } else if (c == '\u00A7') {
                        isColor = true;
                    } else {
                        curW += charW;
                    }

                    if (curW > width) {
                        break;
                    }

                    if (reverse) {
                        sb.insert(0, c);
                    } else {
                        sb.append(c);
                    }
                }
                return sb.toString();
            }
        }
        if (fallback != null) {
            int unscaledWidth = (int) Math.ceil(width / sizeScale);
            return fallback.trimStringToWidth(text, unscaledWidth, reverse);
        }
        return super.trimStringToWidth(text, width, reverse);
    }

    @Override
    public void drawSplitString(String str, int x, int y, int wrapWidth, int textColor) {
        List<String> list = listFormattedStringToWidth(str, wrapWidth);
        for (String s : list) {
            drawString(s, (float) x, (float) y, textColor, false);
            y += FONT_HEIGHT;
        }
    }

    @Override
    public int splitStringWidth(String str, int max) {
        return listFormattedStringToWidth(str, max).size() * FONT_HEIGHT;
    }

    @Override
    public List<String> listFormattedStringToWidth(String str, int wrapWidth) {
        List<String> list = new ArrayList<>();
        if (str == null || str.isEmpty()) return list;

        String[] lines = str.split("\n");
        for (String line : lines) {
            while (!line.isEmpty()) {
                String trimmed = trimStringToWidth(line, wrapWidth);
                if (trimmed.length() >= line.length()) {
                    list.add(line);
                    break;
                } else {
                    int lastSpace = trimmed.lastIndexOf(' ');
                    if (lastSpace > 0) {
                        trimmed = trimmed.substring(0, lastSpace);
                        list.add(trimmed);
                        line = line.substring(lastSpace + 1);
                    } else {
                        list.add(trimmed);
                        line = line.substring(trimmed.length());
                    }
                }
            }
        }
        return list;
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
        if (fallback != null) {
            fallback.onResourceManagerReload(resourceManager);
        }
        super.onResourceManagerReload(resourceManager);
    }

    @Override
    public void setUnicodeFlag(boolean unicodeFlagIn) {
        if (fallback != null) fallback.setUnicodeFlag(unicodeFlagIn);
        super.setUnicodeFlag(unicodeFlagIn);
    }

    @Override
    public boolean getUnicodeFlag() {
        if (fallback != null) return fallback.getUnicodeFlag();
        return super.getUnicodeFlag();
    }

    @Override
    public void setBidiFlag(boolean bidiFlagIn) {
        if (fallback != null) fallback.setBidiFlag(bidiFlagIn);
        super.setBidiFlag(bidiFlagIn);
    }

    @Override
    public boolean getBidiFlag() {
        if (fallback != null) return fallback.getBidiFlag();
        return super.getBidiFlag();
    }
}
