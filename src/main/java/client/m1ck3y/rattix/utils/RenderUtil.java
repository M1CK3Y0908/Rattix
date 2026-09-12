package client.m1ck3y.rattix.utils;

import client.m1ck3y.rattix.modules.Category;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public class RenderUtil {

    // Precomputed Trigonometric Lookup Tables
    private static final float[] SIN_90_STEP6;
    private static final float[] COS_90_STEP6;
    private static final float[] SIN_90_STEP3;
    private static final float[] COS_90_STEP3;
    private static final float[] SIN_360_STEP6;
    private static final float[] COS_360_STEP6;

    private static final int RAINBOW_SEGMENTS = 32;
    private static final int[] RAINBOW_COLORS = new int[RAINBOW_SEGMENTS + 1];

    static {
        // 0 to 90 deg with step 6
        int countStep6 = (90 / 6) + 1; // 16
        SIN_90_STEP6 = new float[countStep6];
        COS_90_STEP6 = new float[countStep6];
        for (int i = 0; i < countStep6; i++) {
            double rad = Math.toRadians(i * 6);
            SIN_90_STEP6[i] = (float) Math.sin(rad);
            COS_90_STEP6[i] = (float) Math.cos(rad);
        }

        // 0 to 90 deg with step 3
        int countStep3 = (90 / 3) + 1; // 31
        SIN_90_STEP3 = new float[countStep3];
        COS_90_STEP3 = new float[countStep3];
        for (int i = 0; i < countStep3; i++) {
            double rad = Math.toRadians(i * 3);
            SIN_90_STEP3[i] = (float) Math.sin(rad);
            COS_90_STEP3[i] = (float) Math.cos(rad);
        }

        // 0 to 360 deg with step 6
        int count360 = (360 / 6) + 1; // 61
        SIN_360_STEP6 = new float[count360];
        COS_360_STEP6 = new float[count360];
        for (int i = 0; i < count360; i++) {
            double rad = Math.toRadians(i * 6);
            SIN_360_STEP6[i] = (float) Math.sin(rad);
            COS_360_STEP6[i] = (float) Math.cos(rad);
        }

        // Rainbow LUT
        for (int i = 0; i <= RAINBOW_SEGMENTS; i++) {
            float h = (float) i / RAINBOW_SEGMENTS;
            RAINBOW_COLORS[i] = java.awt.Color.HSBtoRGB(h, 1.0f, 1.0f);
        }
    }

    // State batching for untextured geometry (rects, fans, polygons)
    private static boolean inBatchUntextured = false;

    public static void beginUntexturedDraw() {
        if (!inBatchUntextured) {
            GlStateManager.enableBlend();
            GlStateManager.disableTexture2D();
            GlStateManager.disableCull();
            GlStateManager.disableAlpha();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            inBatchUntextured = true;
        }
    }

    public static void endUntexturedDraw() {
        if (inBatchUntextured) {
            GlStateManager.enableAlpha();
            GlStateManager.enableCull();
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            inBatchUntextured = false;
        }
    }

    public static void drawRectNoState(float left, float top, float right, float bottom, int color) {
        if (left < right) {
            float i = left;
            left = right;
            right = i;
        }
        if (top < bottom) {
            float j = top;
            top = bottom;
            bottom = j;
        }

        float f3 = (float) (color >> 24 & 255) / 255.0F;
        float f = (float) (color >> 16 & 255) / 255.0F;
        float f1 = (float) (color >> 8 & 255) / 255.0F;
        float f2 = (float) (color & 255) / 255.0F;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        GlStateManager.color(f, f1, f2, f3);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION);
        worldrenderer.pos(left, bottom, 0.0D).endVertex();
        worldrenderer.pos(right, bottom, 0.0D).endVertex();
        worldrenderer.pos(right, top, 0.0D).endVertex();
        worldrenderer.pos(left, top, 0.0D).endVertex();
        tessellator.draw();
    }

    public static void drawRect(float left, float top, float right, float bottom, int color) {
        if (inBatchUntextured) {
            drawRectNoState(left, top, right, bottom, color);
            return;
        }
        if (left < right) {
            float i = left;
            left = right;
            right = i;
        }

        if (top < bottom) {
            float j = top;
            top = bottom;
            bottom = j;
        }

        float f3 = (float) (color >> 24 & 255) / 255.0F;
        float f = (float) (color >> 16 & 255) / 255.0F;
        float f1 = (float) (color >> 8 & 255) / 255.0F;
        float f2 = (float) (color & 255) / 255.0F;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.disableCull();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(f, f1, f2, f3);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION);
        worldrenderer.pos(left, bottom, 0.0D).endVertex();
        worldrenderer.pos(right, bottom, 0.0D).endVertex();
        worldrenderer.pos(right, top, 0.0D).endVertex();
        worldrenderer.pos(left, top, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.enableAlpha();
        GlStateManager.enableCull();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void drawLine(float x1, float y1, float x2, float y2, float lineWidth, int color) {
        float f3 = (float) (color >> 24 & 255) / 255.0F;
        float f = (float) (color >> 16 & 255) / 255.0F;
        float f1 = (float) (color >> 8 & 255) / 255.0F;
        float f2 = (float) (color & 255) / 255.0F;

        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.disableCull();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(f, f1, f2, f3);

        GL11.glLineWidth(lineWidth);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex2f(x1, y1);
        GL11.glVertex2f(x2, y2);
        GL11.glEnd();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);

        GlStateManager.enableAlpha();
        GlStateManager.enableCull();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void drawRectOutline(float x, float y, float width, float height, float lineWidth, int color) {
        float f3 = (float) (color >> 24 & 255) / 255.0F;
        float f = (float) (color >> 16 & 255) / 255.0F;
        float f1 = (float) (color >> 8 & 255) / 255.0F;
        float f2 = (float) (color & 255) / 255.0F;

        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.disableCull();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(f, f1, f2, f3);

        GL11.glLineWidth(lineWidth);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glVertex2f(x, y);
        GL11.glVertex2f(x + width, y);
        GL11.glVertex2f(x + width, y + height);
        GL11.glVertex2f(x, y + height);
        GL11.glEnd();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);

        GlStateManager.enableAlpha();
        GlStateManager.enableCull();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void drawSvgIcon(String iconKey, float x, float y, float size, int color) {
        SvgIconHelper.drawIcon(iconKey, x, y, size, color);
    }

    public static void drawCategoryIcon(Category category, float x, float y, float size, int color) {
        if (category == null) {
            drawTerminalIcon(x, y, size, color);
            return;
        }
        String key = SvgIconHelper.getCategoryKey(category);
        if (key != null) {
            SvgIconHelper.drawIcon(key, x, y, size, color);
        } else {
            drawTerminalIcon(x, y, size, color);
        }
    }

    public static void drawTerminalIcon(float x, float y, float size, int color) {
        float lineWidth = Math.max(1.0f, size * 0.12f);
        float f3 = (float) (color >> 24 & 255) / 255.0F;
        float f = (float) (color >> 16 & 255) / 255.0F;
        float f1 = (float) (color >> 8 & 255) / 255.0F;
        float f2 = (float) (color & 255) / 255.0F;

        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.disableCull();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(f, f1, f2, f3);

        GL11.glLineWidth(lineWidth);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glBegin(GL11.GL_LINES);

        // Chevron >
        GL11.glVertex2f(x + size * 0.12f, y + size * 0.22f);
        GL11.glVertex2f(x + size * 0.46f, y + size * 0.50f);

        GL11.glVertex2f(x + size * 0.46f, y + size * 0.50f);
        GL11.glVertex2f(x + size * 0.12f, y + size * 0.78f);

        // Underline cursor _
        GL11.glVertex2f(x + size * 0.48f, y + size * 0.78f);
        GL11.glVertex2f(x + size * 0.88f, y + size * 0.78f);

        GL11.glEnd();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);

        GlStateManager.enableAlpha();
        GlStateManager.enableCull();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void drawModulesIcon(float x, float y, float size, int color) {
        float lineWidth = Math.max(1.0f, size * 0.10f);
        float f3 = (float) (color >> 24 & 255) / 255.0F;
        float f = (float) (color >> 16 & 255) / 255.0F;
        float f1 = (float) (color >> 8 & 255) / 255.0F;
        float f2 = (float) (color & 255) / 255.0F;

        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.disableCull();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(f, f1, f2, f3);

        GL11.glLineWidth(lineWidth);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glBegin(GL11.GL_LINES);

        // 4 grid blocks / modules icon
        float gap = size * 0.14f;
        float bSize = size * 0.32f;
        float startX = x + size * 0.11f;
        float startY = y + size * 0.11f;

        // Block 1 (Top-Left)
        drawBoxLines(startX, startY, bSize);
        // Block 2 (Top-Right)
        drawBoxLines(startX + bSize + gap, startY, bSize);
        // Block 3 (Bottom-Left)
        drawBoxLines(startX, startY + bSize + gap, bSize);
        // Block 4 (Bottom-Right)
        drawBoxLines(startX + bSize + gap, startY + bSize + gap, bSize);

        GL11.glEnd();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);

        GlStateManager.enableAlpha();
        GlStateManager.enableCull();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static void drawBoxLines(float bx, float by, float s) {
        GL11.glVertex2f(bx, by);
        GL11.glVertex2f(bx + s, by);

        GL11.glVertex2f(bx + s, by);
        GL11.glVertex2f(bx + s, by + s);

        GL11.glVertex2f(bx + s, by + s);
        GL11.glVertex2f(bx, by + s);

        GL11.glVertex2f(bx, by + s);
        GL11.glVertex2f(bx, by);
    }

    public static void drawRoundedRectNoState(float x, float y, float width, float height, float tl, float tr, float br, float bl, int color) {
        float maxRadius = Math.min(width, height) / 2.0f;
        tl = Math.max(0, Math.min(tl, maxRadius));
        tr = Math.max(0, Math.min(tr, maxRadius));
        br = Math.max(0, Math.min(br, maxRadius));
        bl = Math.max(0, Math.min(bl, maxRadius));

        if (tl <= 0 && tr <= 0 && br <= 0 && bl <= 0) {
            drawRectNoState(x, y, x + width, y + height, color);
            return;
        }

        float x2 = x + width;
        float y2 = y + height;
        float a = (float) (color >> 24 & 255) / 255.0F;
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;

        GlStateManager.color(r, g, b, a);

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION);
        worldrenderer.pos(x + width * 0.5f, y + height * 0.5f, 0.0D).endVertex();

        // Top right (0 to 90 deg)
        if (tr > 0) {
            for (int i = 0; i < SIN_90_STEP6.length; i++) {
                worldrenderer.pos(x2 - tr + SIN_90_STEP6[i] * tr, y + tr - COS_90_STEP6[i] * tr, 0.0D).endVertex();
            }
        } else {
            worldrenderer.pos(x2, y, 0.0D).endVertex();
        }

        // Bottom right (90 to 180 deg)
        if (br > 0) {
            for (int i = 0; i < SIN_90_STEP6.length; i++) {
                worldrenderer.pos(x2 - br + COS_90_STEP6[i] * br, y2 - br + SIN_90_STEP6[i] * br, 0.0D).endVertex();
            }
        } else {
            worldrenderer.pos(x2, y2, 0.0D).endVertex();
        }

        // Bottom left (180 to 270 deg)
        if (bl > 0) {
            for (int i = 0; i < SIN_90_STEP6.length; i++) {
                worldrenderer.pos(x + bl - SIN_90_STEP6[i] * bl, y2 - bl + COS_90_STEP6[i] * bl, 0.0D).endVertex();
            }
        } else {
            worldrenderer.pos(x, y2, 0.0D).endVertex();
        }

        // Top left (270 to 360 deg)
        if (tl > 0) {
            for (int i = 0; i < SIN_90_STEP6.length; i++) {
                worldrenderer.pos(x + tl - COS_90_STEP6[i] * tl, y + tl - SIN_90_STEP6[i] * tl, 0.0D).endVertex();
            }
        } else {
            worldrenderer.pos(x, y, 0.0D).endVertex();
        }

        // Close to starting vertex
        if (tr > 0) {
            worldrenderer.pos(x2 - tr, y, 0.0D).endVertex();
        } else {
            worldrenderer.pos(x2, y, 0.0D).endVertex();
        }

        tessellator.draw();
    }

    public static void drawRoundedRect(float x, float y, float width, float height, float radius, int color) {
        drawRoundedRect(x, y, width, height, radius, radius, radius, radius, color);
    }

    public static void drawRoundedRect(float x, float y, float width, float height, float tl, float tr, float br, float bl, int color) {
        if (inBatchUntextured) {
            drawRoundedRectNoState(x, y, width, height, tl, tr, br, bl, color);
            return;
        }

        beginUntexturedDraw();
        drawRoundedRectNoState(x, y, width, height, tl, tr, br, bl, color);
        endUntexturedDraw();
    }

    public static void drawRoundedOutline(float x, float y, float width, float height, float radius, float lineWidth, int color) {
        drawRoundedOutline(x, y, width, height, radius, radius, radius, radius, lineWidth, color);
    }

    public static void drawRoundedOutline(float x, float y, float width, float height, float tl, float tr, float br, float bl, float lineWidth, int color) {
        float maxRadius = Math.min(width, height) / 2.0f;
        tl = Math.max(0, Math.min(tl, maxRadius));
        tr = Math.max(0, Math.min(tr, maxRadius));
        br = Math.max(0, Math.min(br, maxRadius));
        bl = Math.max(0, Math.min(bl, maxRadius));

        float x2 = x + width;
        float y2 = y + height;
        float a = (float) (color >> 24 & 255) / 255.0F;
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;

        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.disableCull();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(r, g, b, a);

        GL11.glLineWidth(lineWidth);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glBegin(GL11.GL_LINE_LOOP);

        // Top right
        if (tr > 0) {
            for (int i = 0; i < SIN_90_STEP3.length; i++) {
                GL11.glVertex2f(x2 - tr + SIN_90_STEP3[i] * tr, y + tr - COS_90_STEP3[i] * tr);
            }
        } else {
            GL11.glVertex2f(x2, y);
        }

        // Bottom right
        if (br > 0) {
            for (int i = 0; i < SIN_90_STEP3.length; i++) {
                GL11.glVertex2f(x2 - br + COS_90_STEP3[i] * br, y2 - br + SIN_90_STEP3[i] * br);
            }
        } else {
            GL11.glVertex2f(x2, y2);
        }

        // Bottom left
        if (bl > 0) {
            for (int i = 0; i < SIN_90_STEP3.length; i++) {
                GL11.glVertex2f(x + bl - SIN_90_STEP3[i] * bl, y2 - bl + COS_90_STEP3[i] * bl);
            }
        } else {
            GL11.glVertex2f(x, y2);
        }

        // Top left
        if (tl > 0) {
            for (int i = 0; i < SIN_90_STEP3.length; i++) {
                GL11.glVertex2f(x + tl - COS_90_STEP3[i] * tl, y + tl - SIN_90_STEP3[i] * tl);
            }
        } else {
            GL11.glVertex2f(x, y);
        }

        GL11.glEnd();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GlStateManager.enableAlpha();
        GlStateManager.enableCull();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void drawDropShadow(float x, float y, float width, float height, float radius, float spread, int color) {
        int baseAlpha = (color >> 24) & 0xFF;
        int rgb = color & 0x00FFFFFF;
        int steps = 5;
        beginUntexturedDraw();
        for (int i = steps; i >= 1; i--) {
            float offset = (float) i / steps * spread;
            float stepAlpha = (float) baseAlpha * (1.0f - (float) (i - 1) / steps) * 0.35f;
            int stepColor = ((int) stepAlpha << 24) | rgb;
            drawRoundedRectNoState(x - offset, y - offset * 0.5f, width + offset * 2.0f, height + offset * 1.5f, radius + offset, radius + offset, radius + offset, radius + offset, stepColor);
        }
        endUntexturedDraw();
    }

    public static void drawCircle(float cx, float cy, float r, int color) {
        float a = (float) (color >> 24 & 255) / 255.0F;
        float red = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;

        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.disableCull();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(red, g, b, a);

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION);
        worldrenderer.pos(cx, cy, 0.0D).endVertex();
        for (int i = 0; i < SIN_360_STEP6.length; i++) {
            worldrenderer.pos(cx + SIN_360_STEP6[i] * r, cy + COS_360_STEP6[i] * r, 0.0D).endVertex();
        }
        tessellator.draw();

        GlStateManager.enableAlpha();
        GlStateManager.enableCull();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static boolean isHovered(float mouseX, float mouseY, float x, float y, float width, float height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public static void enablePixelScissor(float x, float y, float width, float height) {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
        int screenHeight = mc.displayHeight;
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        int sx = (int) Math.floor(x);
        int sy = (int) Math.floor(screenHeight - (y + height));
        int sw = (int) Math.ceil(width + (x - sx));
        int sh = (int) Math.ceil(height + ((screenHeight - (y + height)) - sy));
        GL11.glScissor(Math.max(0, sx), Math.max(0, sy), Math.max(0, sw), Math.max(0, sh));
    }

    private static int cachedScaleFactor = 2;
    private static int lastDisplayWidth = -1;
    private static int lastDisplayHeight = -1;

    public static void enableScissor(float x, float y, float width, float height) {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
        if (mc.displayWidth != lastDisplayWidth || mc.displayHeight != lastDisplayHeight) {
            net.minecraft.client.gui.ScaledResolution sr = new net.minecraft.client.gui.ScaledResolution(mc);
            cachedScaleFactor = sr.getScaleFactor();
            lastDisplayWidth = mc.displayWidth;
            lastDisplayHeight = mc.displayHeight;
        }
        enablePixelScissor(x * cachedScaleFactor, y * cachedScaleFactor, width * cachedScaleFactor, height * cachedScaleFactor);
    }

    public static void disableScissor() {
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    public static void drawHorizontalRainbow(float x, float y, float width, float height) {
        int segments = RAINBOW_SEGMENTS;
        float segW = width / segments;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer wr = tessellator.getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.disableCull();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        for (int i = 0; i < segments; i++) {
            float x1 = x + i * segW;
            float x2 = (i == segments - 1) ? x + width : x + (i + 1) * segW;
            int c1 = RAINBOW_COLORS[i];
            int c2 = RAINBOW_COLORS[i + 1];
            float r1 = ((c1 >> 16) & 255) / 255.0f;
            float g1 = ((c1 >> 8) & 255) / 255.0f;
            float b1 = (c1 & 255) / 255.0f;
            float r2 = ((c2 >> 16) & 255) / 255.0f;
            float g2 = ((c2 >> 8) & 255) / 255.0f;
            float b2 = (c2 & 255) / 255.0f;
            wr.pos(x1, y + height, 0).color(r1, g1, b1, 1.0f).endVertex();
            wr.pos(x2, y + height, 0).color(r2, g2, b2, 1.0f).endVertex();
            wr.pos(x2, y, 0).color(r2, g2, b2, 1.0f).endVertex();
            wr.pos(x1, y, 0).color(r1, g1, b1, 1.0f).endVertex();
        }
        tessellator.draw();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.enableAlpha();
        GlStateManager.enableCull();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void drawHorizontalGradient(float x, float y, float width, float height, int colorLeft, int colorRight) {
        float a1 = ((colorLeft >> 24) & 255) / 255.0f;
        float r1 = ((colorLeft >> 16) & 255) / 255.0f;
        float g1 = ((colorLeft >> 8) & 255) / 255.0f;
        float b1 = (colorLeft & 255) / 255.0f;
        float a2 = ((colorRight >> 24) & 255) / 255.0f;
        float r2 = ((colorRight >> 16) & 255) / 255.0f;
        float g2 = ((colorRight >> 8) & 255) / 255.0f;
        float b2 = (colorRight & 255) / 255.0f;

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer wr = tessellator.getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.disableCull();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        wr.pos(x, y + height, 0).color(r1, g1, b1, a1).endVertex();
        wr.pos(x + width, y + height, 0).color(r2, g2, b2, a2).endVertex();
        wr.pos(x + width, y, 0).color(r2, g2, b2, a2).endVertex();
        wr.pos(x, y, 0).color(r1, g1, b1, a1).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.enableAlpha();
        GlStateManager.enableCull();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
