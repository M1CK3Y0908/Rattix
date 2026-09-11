package client.m1ck3y.rattix.util.font;

import client.m1ck3y.rattix.module.impl.Font;
import client.m1ck3y.rattix.util.FontUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CustomFontRenderer {
    private final java.awt.Font font;
    private final boolean antiAlias;
    private final boolean fractionalMetrics;

    private static final int[] colorCodes = new int[32];
    static {
        for (int i = 0; i < 32; ++i) {
            int j = (i >> 3 & 1) * 85;
            int k = (i >> 2 & 1) * 170 + j;
            int l = (i >> 1 & 1) * 170 + j;
            int i1 = (i & 1) * 170 + j;
            if (i == 6) k += 85;
            if (i >= 16) {
                k /= 4;
                l /= 4;
                i1 /= 4;
            }
            colorCodes[i] = (k & 255) << 16 | (l & 255) << 8 | i1 & 255;
        }
    }

    public static class CharData {
        public int width;
        public int height;
        public int storedX;
        public int storedY;
    }

    private static class PageData {
        public final int textureId;
        public final CharData[] charData = new CharData[256];
        public int fontHeight;

        public PageData(int textureId, int fontHeight) {
            this.textureId = textureId;
            this.fontHeight = fontHeight;
        }
    }

    private final Map<Integer, PageData> pages = new ConcurrentHashMap<>();
    private int baseFontHeight = 18;

    public CustomFontRenderer(java.awt.Font font, boolean antiAlias, boolean fractionalMetrics) {
        this.font = font;
        this.antiAlias = antiAlias;
        this.fractionalMetrics = fractionalMetrics;
        loadPage(0);
    }

    public PageData getPage(int pageIndex) {
        PageData page = pages.get(pageIndex);
        if (page != null) {
            return page;
        }
        return loadPage(pageIndex);
    }

    private synchronized PageData loadPage(int pageIndex) {
        if (pages.containsKey(pageIndex)) {
            return pages.get(pageIndex);
        }

        BufferedImage img = new BufferedImage(512, 512, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) img.getGraphics();
        g.setFont(this.font);
        g.setColor(new Color(255, 255, 255, 0));
        g.fillRect(0, 0, 512, 512);
        g.setColor(Color.WHITE);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, fractionalMetrics ? RenderingHints.VALUE_FRACTIONALMETRICS_ON : RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, antiAlias ? RenderingHints.VALUE_TEXT_ANTIALIAS_ON : RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, antiAlias ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);

        FontMetrics fm = g.getFontMetrics();
        int positionX = 0;
        int positionY = 1;
        int pageMaxHeight = 0;
        int startChar = pageIndex * 256;

        CharData[] charData = new CharData[256];

        for (int i = 0; i < 256; i++) {
            char ch = (char) (startChar + i);
            CharData data = new CharData();
            Rectangle2D bounds = fm.getStringBounds(String.valueOf(ch), g);
            data.width = (int) Math.ceil(bounds.getWidth()) + 2;
            data.height = (int) Math.ceil(bounds.getHeight()) + 2;

            if (positionX + data.width >= 512) {
                positionX = 0;
                positionY += pageMaxHeight + 2;
            }

            if (data.height > pageMaxHeight) {
                pageMaxHeight = data.height;
            }

            data.storedX = positionX;
            data.storedY = positionY;

            if (data.height > 0 && data.width > 0 && positionY + data.height <= 512) {
                g.drawString(String.valueOf(ch), positionX + 1, positionY + fm.getAscent() + 1);
            }

            positionX += data.width + 2;
            charData[i] = data;
        }

        g.dispose();

        int textureId = GlStateManager.generateTexture();
        GlStateManager.bindTexture(textureId);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

        int[] pixels = new int[512 * 512];
        img.getRGB(0, 0, 512, 512, pixels, 0, 512);
        ByteBuffer buffer = BufferUtils.createByteBuffer(512 * 512 * 4);
        for (int y = 0; y < 512; y++) {
            for (int x = 0; x < 512; x++) {
                int pixel = pixels[y * 512 + x];
                buffer.put((byte) ((pixel >> 16) & 0xFF));
                buffer.put((byte) ((pixel >> 8) & 0xFF));
                buffer.put((byte) (pixel & 0xFF));
                buffer.put((byte) ((pixel >> 24) & 0xFF));
            }
        }
        buffer.flip();
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, 512, 512, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

        PageData page = new PageData(textureId, pageMaxHeight);
        System.arraycopy(charData, 0, page.charData, 0, 256);
        if (pageIndex == 0) {
            baseFontHeight = pageMaxHeight;
        }
        pages.put(pageIndex, page);
        return page;
    }

    public static final float BASELINE_OFFSET = -4.0f;

    public void drawStringWithShadow(String text, float x, float y, int color) {
        drawString(text, x, y, color, true, 1.0f);
    }

    public void drawString(String text, float x, float y, int color) {
        drawString(text, x, y, color, false, 1.0f);
    }

    public boolean isConsolas() {
        if (this == FontUtil.getConsolasFont()) return true;
        if (this.font != null) {
            String name = this.font.getName();
            String family = this.font.getFamily();
            if ("Consolas".equalsIgnoreCase(name) || "Consolas".equalsIgnoreCase(family)) {
                return true;
            }
        }
        return false;
    }

    public float drawString(String text, float x, float y, int color, boolean dropShadow, float scale) {
        if (text == null || text.isEmpty()) return 0;

        Font font = FontUtil.getFont();
        boolean isConsolasFont = isConsolas();
        boolean isConsolasGlobal = isConsolasFont && "consolas.ttf".equalsIgnoreCase(FontUtil.getGlobalFontName());
        boolean isConsolasInternal = isConsolasFont && !isConsolasGlobal;

        float sizeScale = (!isConsolasInternal && font != null) ? font.size.getValue().floatValue() / 100.0f : 1.0f;
        float effectiveScale = scale * sizeScale;
        float offX = (!isConsolasInternal && font != null) ? font.offsetX.getValue().floatValue() : 0.0f;
        float consolasNativeOffsetY = isConsolasFont ? 0.5f : 0.0f;
        float offY = ((!isConsolasInternal && font != null) ? font.offsetY.getValue().floatValue() : 0.0f) + consolasNativeOffsetY;
        boolean shadowEnabled = dropShadow && (isConsolasInternal || font == null || font.shadow.getValue());
        float shadowOffX = (!isConsolasInternal && font != null) ? font.shadowOffsetX.getValue().floatValue() : 1.0f;
        float shadowOffY = (!isConsolasInternal && font != null) ? font.shadowOffsetY.getValue().floatValue() : 1.0f;
        String shadowMode = (!isConsolasInternal && font != null) ? font.shadowMode.getValue() : "Vanilla";
        boolean outlineEnabled = !isConsolasInternal && font != null && font.outline.getValue();
        float outlineWidth = font != null ? font.outlineWidth.getValue().floatValue() : 1.0f;
        String outlineMode = font != null ? font.outlineColor.getValue() : "Black";

        int alpha = (color >> 24) & 0xFF;
        if (alpha == 0) alpha = 255;

        // 1. Draw Shadow
        if (shadowEnabled) {
            int shadowColor;
            if (font != null && font.shadowMode.is("Custom")) {
                shadowColor = font.shadowColor.getRGB();
            } else {
                // Vanilla: 25% brightness with full text alpha
                shadowColor = (alpha << 24) | ((color & 0xFCFCFC) >> 2);
            }
            float sOffX = shadowOffX * 0.7f * effectiveScale;
            float sOffY = shadowOffY * 0.7f * effectiveScale;
            renderStringInternal(text, x + offX + sOffX, y + offY + sOffY, shadowColor, effectiveScale, true, shadowMode);
        }

        // 2. Draw Outline
        if (outlineEnabled) {
            int outColor;
            if ("Dark".equalsIgnoreCase(outlineMode)) {
                outColor = (alpha << 24) | ((color & 0xFCFCFC) >> 2);
            } else if ("50% Alpha".equalsIgnoreCase(outlineMode)) {
                outColor = ((alpha / 2) << 24) | 0x000000;
            } else {
                // Black
                outColor = (alpha << 24) | 0x000000;
            }
            float ow = outlineWidth * 0.5f * effectiveScale;
            renderStringInternal(text, x + offX - ow, y + offY, outColor, effectiveScale, false, "Solid");
            renderStringInternal(text, x + offX + ow, y + offY, outColor, effectiveScale, false, "Solid");
            renderStringInternal(text, x + offX, y + offY - ow, outColor, effectiveScale, false, "Solid");
            renderStringInternal(text, x + offX, y + offY + ow, outColor, effectiveScale, false, "Solid");
            renderStringInternal(text, x + offX - ow, y + offY - ow, outColor, effectiveScale, false, "Solid");
            renderStringInternal(text, x + offX + ow, y + offY - ow, outColor, effectiveScale, false, "Solid");
            renderStringInternal(text, x + offX - ow, y + offY + ow, outColor, effectiveScale, false, "Solid");
            renderStringInternal(text, x + offX + ow, y + offY + ow, outColor, effectiveScale, false, "Solid");
        }

        // 3. Draw Main Text — returns the rendered width
        return renderStringInternal(text, x + offX, y + offY, color, effectiveScale, false, null);
    }

    private float renderStringInternal(String text, float x, float y, int color, float scale, boolean isShadow, String shadowMode) {
        if (text == null || text.isEmpty()) return 0;

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y + BASELINE_OFFSET * scale, 0);
        GlStateManager.scale(scale, scale, 1.0f);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.enableTexture2D();
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(516, 0.05F);

        float a = ((color >> 24) & 255) / 255.0F;
        float r = ((color >> 16) & 255) / 255.0F;
        float g = ((color >> 8) & 255) / 255.0F;
        float b = (color & 255) / 255.0F;
        if (a == 0) a = 1.0F;

        float curR = r;
        float curG = g;
        float curB = b;
        float curA = a;

        float currX = 0;
        float currY = 0;
        int lastTextureId = -1;
        boolean inBegin = false;

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\u00A7' && i + 1 < text.length()) {
                if (!"Solid".equals(shadowMode)) {
                    int colorIndex = "0123456789abcdefklmnor".indexOf(Character.toLowerCase(text.charAt(i + 1)));
                    if (colorIndex >= 0 && colorIndex < 16) {
                        int cCode = colorCodes[colorIndex];
                        if (isShadow) {
                            if ("Custom".equalsIgnoreCase(shadowMode)) {
                                Font fMod = FontUtil.getFont();
                                cCode = fMod != null ? fMod.shadowColor.getRGB() : 0x80000000;
                            } else {
                                cCode = ((cCode & 0xFCFCFC) >> 2);
                            }
                        }
                        curR = ((cCode >> 16) & 255) / 255.0F;
                        curG = ((cCode >> 8) & 255) / 255.0F;
                        curB = (cCode & 255) / 255.0F;
                        if ("Custom".equalsIgnoreCase(shadowMode)) {
                            curA = ((cCode >> 24) & 255) / 255.0F;
                            if (curA == 0) curA = 1.0F;
                        }
                    } else if (colorIndex == 21) {
                        curR = r;
                        curG = g;
                        curB = b;
                    }
                }
                i++;
                continue;
            }

            int pageIndex = c / 256;
            PageData page = getPage(pageIndex);
            if (page == null) continue;

            if (page.textureId != lastTextureId) {
                if (inBegin) {
                    tessellator.draw();
                    inBegin = false;
                }
                GlStateManager.bindTexture(page.textureId);
                lastTextureId = page.textureId;
            }

            if (!inBegin) {
                worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
                inBegin = true;
            }

            int charIdx = c % 256;
            CharData data = page.charData[charIdx];
            if (data != null) {
                float srcX = (float) data.storedX / 512.0F;
                float srcY = (float) data.storedY / 512.0F;
                float srcW = (float) data.width / 512.0F;
                float srcH = (float) data.height / 512.0F;

                worldRenderer.pos(currX, currY, 0.0D).tex(srcX, srcY).color(curR, curG, curB, curA).endVertex();
                worldRenderer.pos(currX, currY + data.height, 0.0D).tex(srcX, srcY + srcH).color(curR, curG, curB, curA).endVertex();
                worldRenderer.pos(currX + data.width, currY + data.height, 0.0D).tex(srcX + srcW, srcY + srcH).color(curR, curG, curB, curA).endVertex();
                worldRenderer.pos(currX + data.width, currY, 0.0D).tex(srcX + srcW, srcY).color(curR, curG, curB, curA).endVertex();

                currX += data.width - 2;
            }
        }

        if (inBegin) {
            tessellator.draw();
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        // Removed: GlStateManager.bindTexture(0) — avoids forced texture re-binding between consecutive text draws
        GlStateManager.popMatrix();
        return currX * scale;
    }

    public float getStringWidth(String text) {
        if (text == null || text.isEmpty()) return 0;
        float width = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\u00A7' && i + 1 < text.length()) {
                i++;
                continue;
            }
            int pageIndex = c / 256;
            PageData page = getPage(pageIndex);
            if (page != null) {
                int charIdx = c % 256;
                CharData data = page.charData[charIdx];
                if (data != null) {
                    width += data.width - 2;
                }
            }
        }
        return width;
    }

    public float getCharWidth(char c) {
        int pageIndex = c / 256;
        PageData page = getPage(pageIndex);
        if (page != null) {
            int charIdx = c % 256;
            CharData data = page.charData[charIdx];
            if (data != null) {
                return data.width - 2;
            }
        }
        return 0;
    }

    public int getHeight() {
        return baseFontHeight;
    }

    public void destroy() {
        for (PageData page : pages.values()) {
            if (page.textureId != -1) {
                GlStateManager.deleteTexture(page.textureId);
            }
        }
        pages.clear();
    }
}
