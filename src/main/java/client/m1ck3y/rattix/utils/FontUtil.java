package client.m1ck3y.rattix.utils;

import client.m1ck3y.rattix.manager.FileManager;
import client.m1ck3y.rattix.module.impl.Font;
import client.m1ck3y.rattix.utils.font.CustomFontRenderer;
import client.m1ck3y.rattix.utils.font.GlobalFontRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

public class FontUtil {
    private static final Map<String, CustomFontRenderer> fontCache = new HashMap<>();
    public static FontRenderer vanillaFontRenderer = null;

    public static void init() {
        ensureGlobalFontRenderer();
    }

    public static void ensureGlobalFontRenderer() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc != null && mc.gameSettings != null && mc.renderEngine != null) {
            if (!(mc.fontRendererObj instanceof GlobalFontRenderer)) {
                if (mc.fontRendererObj != null) {
                    vanillaFontRenderer = mc.fontRendererObj;
                }
                mc.fontRendererObj = new GlobalFontRenderer(
                        mc.gameSettings,
                        new ResourceLocation("textures/font/ascii.png"),
                        mc.renderEngine,
                        false,
                        vanillaFontRenderer
                );
            }
        }
    }

    public static Font getFont() {
        return Font.getInstance();
    }

    public static Font getFontSettings() {
        return Font.getInstance();
    }

    public static String getGlobalFontName() {
        return Font.getInstance().getSelectedFont();
    }

    public static void setSelectedFont(String fontName) {
        Font.getInstance().setSelectedFont(fontName);
    }

    public static boolean isCustomFont() {
        String name = getGlobalFontName();
        return name != null && !"Minecraft".equalsIgnoreCase(name);
    }

    // Sentinel to mark fonts that failed to load (avoid repeated disk checks)
    private static final CustomFontRenderer MISSING_FONT_SENTINEL = new CustomFontRenderer(
            new java.awt.Font("Dialog", java.awt.Font.PLAIN, 1), false, false);

    public static CustomFontRenderer getFontRenderer(String fileName) {
        if (fileName == null || "Minecraft".equalsIgnoreCase(fileName)) return null;
        CustomFontRenderer cached = fontCache.get(fileName);
        if (cached != null) {
            return cached == MISSING_FONT_SENTINEL ? null : cached;
        }

        File fontFile = new File(FileManager.FONTS_DIR, fileName);
        if (fontFile.exists()) {
            try (FileInputStream fis = new FileInputStream(fontFile)) {
                java.awt.Font awtFont = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, fis).deriveFont(18f);
                CustomFontRenderer cfr = new CustomFontRenderer(awtFont, true, true);
                fontCache.put(fileName, cfr);
                return cfr;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // Cache the miss so we don't repeatedly hit disk
        fontCache.put(fileName, MISSING_FONT_SENTINEL);
        return null;
    }

    public static CustomFontRenderer getConsolasFont() {
        CustomFontRenderer cfr = getFontRenderer("consolas.ttf");
        if (cfr != null) return cfr;
        File file = new File(FileManager.FONTS_DIR, "consolas.ttf");
        if (file.exists()) {
            try (FileInputStream fis = new FileInputStream(file)) {
                java.awt.Font awtFont = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, fis).deriveFont(18f);
                cfr = new CustomFontRenderer(awtFont, true, true);
                fontCache.put("consolas.ttf", cfr);
                return cfr;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        java.awt.Font awtFont = new java.awt.Font("Consolas", java.awt.Font.PLAIN, 18);
        cfr = new CustomFontRenderer(awtFont, true, true);
        fontCache.put("consolas.ttf", cfr);
        return cfr;
    }

    public static GlobalFontRenderer getGlobalFontRenderer() {
        ensureGlobalFontRenderer();
        if (Minecraft.getMinecraft().fontRendererObj instanceof GlobalFontRenderer) {
            return (GlobalFontRenderer) Minecraft.getMinecraft().fontRendererObj;
        }
        return null;
    }

    public static CustomFontRenderer getGlobalFont() {
        if (!isCustomFont()) return null;
        return getFontRenderer(getGlobalFontName());
    }

    public static String stripColorCodes(String text) {
        if (text == null) return null;
        int len = text.length();
        if (len == 0) return text;
        // Fast path: no color codes present
        int idx = text.indexOf('\u00A7');
        if (idx < 0) return text;
        // Slow path: strip color codes with char array scan (zero regex, zero Pattern allocation)
        char[] chars = new char[len];
        int out = 0;
        for (int i = 0; i < len; i++) {
            char c = text.charAt(i);
            if (c == '\u00A7' && i + 1 < len) {
                char next = Character.toLowerCase(text.charAt(i + 1));
                if ((next >= '0' && next <= '9') || (next >= 'a' && next <= 'f') ||
                    next == 'k' || next == 'l' || next == 'm' || next == 'n' || next == 'o' || next == 'r') {
                    i++; // skip the code char
                    continue;
                }
            }
            chars[out++] = c;
        }
        if (out == len) return text; // nothing stripped
        return new String(chars, 0, out);
    }

    // --- ClickGUI 2x Scale Text Rendering ---
    public static void drawString2x(String text, float x, float y, int color) {
        if (text == null || text.isEmpty()) return;
        CustomFontRenderer cfr = getGlobalFont();
        if (cfr != null) {
            cfr.drawStringWithShadow(text, x, y, color);
        } else {
            drawMinecraftString2x(text, x, y, color);
        }
    }

    public static void drawConsolasString2x(String text, float x, float y, int color) {
        if (text == null || text.isEmpty()) return;
        CustomFontRenderer cfr = getConsolasFont();
        if (cfr != null) {
            cfr.drawStringWithShadow(text, x, y, color);
        } else {
            drawString2x(text, x, y, color);
        }
    }

    public static float getConsolasStringWidth2x(String text) {
        if (text == null || text.isEmpty()) return 0;
        CustomFontRenderer cfr = getConsolasFont();
        if (cfr != null) {
            float width = cfr.getStringWidth(text);
            if ("consolas.ttf".equalsIgnoreCase(getGlobalFontName())) {
                float sizeScale = Font.getInstance().size.getValue().floatValue() / 100.0f;
                return width * sizeScale;
            }
            return width;
        }
        return getStringWidth2x(text);
    }

    public static void drawMinecraftString2x(String text, float x, float y, int color) {
        if (text == null || text.isEmpty()) return;
        Font font = Font.getInstance();
        float sizeScale = font.size.getValue().floatValue() / 100.0f;
        float offX = font.offsetX.getValue().floatValue();
        float offY = font.offsetY.getValue().floatValue();
        boolean shadowEnabled = font.shadow.getValue();
        float shadowOffX = font.shadowOffsetX.getValue().floatValue();
        float shadowOffY = font.shadowOffsetY.getValue().floatValue();
        boolean customShadow = font.shadowMode.is("Custom");
        boolean outlineEnabled = font.outline.getValue();
        float outlineWidth = font.outlineWidth.getValue().floatValue();
        String outlineMode = font.outlineColor.getValue();

        int alpha = (color >> 24) & 0xFF;
        if (alpha == 0) alpha = 255;

        FontRenderer fr = vanillaFontRenderer != null ? vanillaFontRenderer : Minecraft.getMinecraft().fontRendererObj;

        GlStateManager.pushMatrix();
        GlStateManager.translate(x + offX, y + offY, 0);
        float totalScale = 2.0f * sizeScale;
        GlStateManager.scale(totalScale, totalScale, 1.0f);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.enableAlpha();

        if (fr != null) {
            String clean = stripColorCodes(text);

            // Outline
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
                fr.drawString(clean, -ow, 0, outColor, false);
                fr.drawString(clean, ow, 0, outColor, false);
                fr.drawString(clean, 0, -ow, outColor, false);
                fr.drawString(clean, 0, ow, outColor, false);
                fr.drawString(clean, -ow, -ow, outColor, false);
                fr.drawString(clean, ow, -ow, outColor, false);
                fr.drawString(clean, -ow, ow, outColor, false);
                fr.drawString(clean, ow, ow, outColor, false);
            }

            // Shadow & Main text
            if (shadowEnabled) {
                if (customShadow) {
                    int shadowColor = font.shadowColor.getRGB();
                    fr.drawString(clean, shadowOffX, shadowOffY, shadowColor, false);
                    fr.drawString(text, 0, 0, color, false);
                } else if (shadowOffX == 1.0f && shadowOffY == 1.0f) {
                    fr.drawString(text, 0, 0, color, true);
                } else {
                    int shadowColor = (alpha << 24) | ((color & 0xFCFCFC) >> 2);
                    fr.drawString(clean, shadowOffX, shadowOffY, shadowColor, false);
                    fr.drawString(text, 0, 0, color, false);
                }
            } else {
                fr.drawString(text, 0, 0, color, false);
            }
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
    }

    public static float getStringWidth2x(String text) {
        if (text == null || text.isEmpty()) return 0;
        CustomFontRenderer cfr = getGlobalFont();
        if (cfr != null) {
            float sizeScale = Font.getInstance().size.getValue().floatValue() / 100.0f;
            return cfr.getStringWidth(text) * sizeScale;
        }
        return getMinecraftStringWidth2x(text);
    }

    public static float getMinecraftStringWidth2x(String text) {
        if (text == null || text.isEmpty()) return 0;
        float sizeScale = Font.getInstance().size.getValue().floatValue() / 100.0f;
        FontRenderer fr = vanillaFontRenderer != null ? vanillaFontRenderer : (Minecraft.getMinecraft() != null ? Minecraft.getMinecraft().fontRendererObj : null);
        if (fr == null) return text.length() * 6.0f * 2.0f * sizeScale;
        return fr.getStringWidth(text) * 2.0f * sizeScale;
    }

    // --- HUD 1x ScaledResolution Text Rendering ---
    public static void drawHudString(String text, float x, float y, int color) {
        if (text == null || text.isEmpty()) return;
        Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(text, x, y, color);
    }

    public static float getHudStringWidth(String text) {
        if (text == null || text.isEmpty()) return 0;
        return Minecraft.getMinecraft().fontRendererObj.getStringWidth(text);
    }
}
