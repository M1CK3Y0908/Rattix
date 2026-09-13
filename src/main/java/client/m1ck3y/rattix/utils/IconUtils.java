package client.m1ck3y.rattix.utils;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class IconUtils {
    private static ByteBuffer[] cachedFavicon = null;
    private static boolean loaded = false;
    private static int iconTexId = -1;

    public static boolean initLwjglIcon() {
        try {
            ByteBuffer[] favicon = getFavicon();
            if (favicon != null && favicon.length > 0) {
                Display.setIcon(favicon);
                return true;
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return false;
    }

    public static ByteBuffer[] getFavicon() {
        if (loaded) {
            return cachedFavicon;
        }
        loaded = true;
        try {
            BufferedImage original = loadSvgIcon();
            if (original != null) {
                int[] sizes = new int[]{16, 32, 64, 128};
                List<ByteBuffer> buffers = new ArrayList<>();
                for (int s : sizes) {
                    BufferedImage resized = new BufferedImage(s, s, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g = resized.createGraphics();
                    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                    g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g.drawImage(original, 0, 0, s, s, null);
                    g.dispose();

                    ByteBuffer buf = imageToByteBuffer(resized);
                    if (buf != null) {
                        buffers.add(buf);
                    }
                }
                if (!buffers.isEmpty()) {
                    cachedFavicon = buffers.toArray(new ByteBuffer[0]);
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return cachedFavicon;
    }

    public static BufferedImage loadSvgIcon() {
        String[] pngPaths = new String[]{
                "/assets/rattix/icons/R_icon.png",
                "/assets/minecraft/textures/gui/title/mojang.png"
        };
        for (String path : pngPaths) {
            try (InputStream stream = IconUtils.class.getResourceAsStream(path)) {
                if (stream != null) {
                    BufferedImage img = ImageIO.read(stream);
                    if (img != null) {
                        return img;
                    }
                }
            } catch (Throwable ignored) {}
        }

        String[] svgPaths = new String[]{
                "/assets/rattix/icons/R_icon.svg",
                "/assets/rattix/icons/icon.svg",
                "/assets/rattix/R_icon.svg",
                "/R_icon.svg"
        };

        for (String path : svgPaths) {
            try (InputStream stream = IconUtils.class.getResourceAsStream(path)) {
                if (stream != null) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] tmp = new byte[4096];
                    int r;
                    while ((r = stream.read(tmp)) != -1) {
                        baos.write(tmp, 0, r);
                    }
                    String content = new String(baos.toByteArray(), StandardCharsets.UTF_8);
                    int idx = content.indexOf("base64,");
                    if (idx != -1) {
                        int start = idx + "base64,".length();
                        int end = content.indexOf('\"', start);
                        if (end != -1) {
                            String b64 = content.substring(start, end).trim();
                            byte[] imgBytes = Base64.getDecoder().decode(b64);
                            BufferedImage img = ImageIO.read(new ByteArrayInputStream(imgBytes));
                            if (img != null) {
                                return img;
                            }
                        }
                    }
                }
            } catch (Throwable ignored) {}
        }
        return null;
    }

    public static int getIconTextureId() {
        if (iconTexId != -1 && GL11.glIsTexture(iconTexId)) return iconTexId;
        try {
            BufferedImage img = loadSvgIcon();
            if (img == null) return -1;
            int w = img.getWidth();
            int h = img.getHeight();
            ByteBuffer buffer = imageToByteBufferDirect(img);
            if (buffer == null) return -1;

            iconTexId = GL11.glGenTextures();
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, iconTexId);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, w, h, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
            return iconTexId;
        } catch (Throwable t) {
            t.printStackTrace();
            return -1;
        }
    }

    public static void drawIcon(float x, float y, float size) {
        drawIconDirect(x, y, size);
    }

    public static void drawIconDirect(float x, float y, float size) {
        int texId = getIconTextureId();
        if (texId <= 0) return;

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texId);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

        GL11.glBegin(GL11.GL_QUADS);
        GL11.glTexCoord2f(0.0f, 1.0f); GL11.glVertex2f(x, y + size);
        GL11.glTexCoord2f(1.0f, 1.0f); GL11.glVertex2f(x + size, y + size);
        GL11.glTexCoord2f(1.0f, 0.0f); GL11.glVertex2f(x + size, y);
        GL11.glTexCoord2f(0.0f, 0.0f); GL11.glVertex2f(x, y);
        GL11.glEnd();

        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_BLEND);
    }

    private static ByteBuffer imageToByteBuffer(BufferedImage bufferedImage) {
        if (bufferedImage == null) return null;
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        int[] rgb = bufferedImage.getRGB(0, 0, width, height, null, 0, width);
        ByteBuffer byteBuffer = ByteBuffer.allocate(4 * rgb.length);

        for (int pixel : rgb) {
            byteBuffer.putInt((pixel << 8) | ((pixel >>> 24) & 255));
        }

        byteBuffer.flip();
        return byteBuffer;
    }

    private static ByteBuffer imageToByteBufferDirect(BufferedImage bufferedImage) {
        if (bufferedImage == null) return null;
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        int[] rgb = bufferedImage.getRGB(0, 0, width, height, null, 0, width);
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * rgb.length);

        for (int pixel : rgb) {
            byteBuffer.put((byte) ((pixel >> 16) & 0xFF));
            byteBuffer.put((byte) ((pixel >> 8) & 0xFF));
            byteBuffer.put((byte) (pixel & 0xFF));
            byteBuffer.put((byte) ((pixel >> 24) & 0xFF));
        }

        byteBuffer.flip();
        return byteBuffer;
    }
}
