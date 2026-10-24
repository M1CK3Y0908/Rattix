package client.m1ck3y.rattix.ui.font;

/**
 * Global font registry access.
 * Aligned with LiquidBounce ui/font/Fonts.
 */
public class Fonts {
    public static CustomFontRenderer getFont(String fontName) {
        return FontUtil.getFontRenderer(fontName);
    }

    public static CustomFontRenderer getConsolas() {
        return FontUtil.getConsolasFont();
    }
}
