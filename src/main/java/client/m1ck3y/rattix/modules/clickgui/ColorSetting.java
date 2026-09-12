package client.m1ck3y.rattix.modules.clickgui;

import java.awt.Color;

public class ColorSetting extends Setting<Integer> {
    private float hue = 0.0f;
    private float saturation = 0.0f;
    private float brightness = 0.0f;
    private int alpha = 128;
    private int cachedStaticRgb = 0;

    public final CheckBoxSetting rainbow = new CheckBoxSetting("Rainbow", "rainbow", false);
    public final CheckBoxSetting fade = new CheckBoxSetting("Fade", "fade", false);
    public final CheckBoxSetting astolfo = new CheckBoxSetting("Astolfo", "astolfo", false);
    public final CheckBoxSetting clientColor = new CheckBoxSetting("ClientColor", "client_color", false);

    public ColorSetting(String name, int defaultColor) {
        super(name, defaultColor);
        setColor(defaultColor);
    }

    public ColorSetting(String name, int defaultColor, String group) {
        super(name, defaultColor, group);
        setColor(defaultColor);
    }

    public void setColor(int color) {
        this.value = color;
        this.alpha = (color >> 24) & 0xFF;
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        float[] hsb = Color.RGBtoHSB(r, g, b, null);
        this.hue = hsb[0];
        this.saturation = hsb[1];
        this.brightness = hsb[2];
        this.cachedStaticRgb = color & 0xFFFFFF;
    }

    public int getRGB() {
        int a = this.alpha;
        if (clientColor.getValue()) {
            int base = 0x76B9ED;
            return (a << 24) | (base & 0xFFFFFF);
        }
        if (astolfo.getValue()) {
            int astolfoRgb = getAstolfoColor(0);
            return (a << 24) | (astolfoRgb & 0xFFFFFF);
        }
        if (rainbow.getValue()) {
            float h = ((System.currentTimeMillis() % 4000L) / 4000.0f);
            int rgb = Color.HSBtoRGB(h, saturation, brightness);
            return (a << 24) | (rgb & 0xFFFFFF);
        }
        if (fade.getValue()) {
            float f = 0.4f + 0.6f * (float) (Math.sin(System.currentTimeMillis() / 400.0) * 0.5 + 0.5);
            int rgb = Color.HSBtoRGB(hue, saturation, Math.max(0.1f, Math.min(1.0f, brightness * f)));
            return (a << 24) | (rgb & 0xFFFFFF);
        }
        int rgb = (cachedStaticRgb != 0) ? cachedStaticRgb : Color.HSBtoRGB(hue, saturation, brightness);
        return (a << 24) | (rgb & 0xFFFFFF);
    }

    public static int getAstolfoColor(int offset) {
        float speed = 3000.0f;
        float hue = (System.currentTimeMillis() + offset) % (int) speed;
        while (hue > speed) {
            hue -= speed;
        }
        hue /= speed;
        if (hue > 0.5f) {
            hue = 0.5f - (hue - 0.5f);
        }
        hue += 0.5f;
        return Color.HSBtoRGB(hue, 0.6f, 1.0f);
    }

    public float getHue() {
        return hue;
    }

    public void setHue(float hue) {
        this.hue = Math.max(0.0f, Math.min(1.0f, hue));
        updateValue();
    }

    public float getSaturation() {
        return saturation;
    }

    public void setSaturation(float saturation) {
        this.saturation = Math.max(0.0f, Math.min(1.0f, saturation));
        updateValue();
    }

    public float getBrightness() {
        return brightness;
    }

    public void setBrightness(float brightness) {
        this.brightness = Math.max(0.0f, Math.min(1.0f, brightness));
        updateValue();
    }

    public int getAlpha() {
        return alpha;
    }

    public void setAlpha(int alpha) {
        this.alpha = Math.max(0, Math.min(255, alpha));
        updateValue();
    }

    public boolean isRainbow() {
        return rainbow.getValue();
    }

    public void setRainbow(boolean rainbow) {
        this.rainbow.setValue(rainbow);
    }

    public boolean isFade() {
        return fade.getValue();
    }

    public void setFade(boolean fade) {
        this.fade.setValue(fade);
    }

    public boolean isAstolfo() {
        return astolfo.getValue();
    }

    public void setAstolfo(boolean astolfo) {
        this.astolfo.setValue(astolfo);
    }

    public boolean isClientColor() {
        return clientColor.getValue();
    }

    public void setClientColor(boolean clientColor) {
        this.clientColor.setValue(clientColor);
    }

    private void updateValue() {
        this.cachedStaticRgb = Color.HSBtoRGB(hue, saturation, brightness) & 0xFFFFFF;
        this.value = (alpha << 24) | cachedStaticRgb;
    }
}
