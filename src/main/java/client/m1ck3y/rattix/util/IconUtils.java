package client.m1ck3y.rattix.util;

import org.lwjgl.opengl.Display;

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

    private static BufferedImage loadSvgIcon() {
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
                        int end = content.indexOf("\"", start);
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
}
