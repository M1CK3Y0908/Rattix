package client.m1ck3y.rattix.util;

import org.lwjgl.opengl.Display;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
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
            List<ByteBuffer> buffers = new ArrayList<>();
            String[] possiblePaths = new String[]{
                    "/assets/rattix/icons/R_16x16.png",
                    "/assets/rattix/icons/R_32x32.png",
                    "/assets/rattix/icons/R_64x64.png",
                    "/assets/rattix/icons/R_128x128.png",
                    "/assets/rattix/icons/icon_16x16.png",
                    "/assets/rattix/icons/icon_32x32.png",
                    "/assets/rattix/icons/icon_64x64.png"
            };

            for (String path : possiblePaths) {
                try (InputStream stream = IconUtils.class.getResourceAsStream(path)) {
                    if (stream != null) {
                        ByteBuffer buf = readImageToBuffer(stream);
                        if (buf != null) {
                            buffers.add(buf);
                        }
                    }
                } catch (Throwable ignored) {}
            }

            if (!buffers.isEmpty()) {
                cachedFavicon = buffers.toArray(new ByteBuffer[0]);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return cachedFavicon;
    }

    private static ByteBuffer readImageToBuffer(InputStream imageStream) throws IOException {
        if (imageStream == null) return null;
        BufferedImage bufferedImage = ImageIO.read(imageStream);
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
