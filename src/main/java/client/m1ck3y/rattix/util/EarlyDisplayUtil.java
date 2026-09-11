package client.m1ck3y.rattix.util;

import org.lwjgl.opengl.Display;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class EarlyDisplayUtil {
    public static final String TITLE = "Rattix";
    private static volatile boolean iconApplied = false;
    private static volatile boolean watcherStarted = false;
    private static ByteBuffer[] cachedBuffers = null;

    static {
        // 当类首次被 JVM 加载时立即尝试初始化
        applyEarly();
    }

    public static void applyEarly() {
        applyTitle();
        applyIcon();
        startEarlyWatcher();
    }

    public static void applyTitle() {
        try {
            if (Display.isCreated()) {
                if (!TITLE.equals(Display.getTitle())) {
                    Display.setTitle(TITLE);
                }
            }
        } catch (Throwable ignored) {}
    }

    public static void applyIcon() {
        try {
            if (Display.isCreated()) {
                ByteBuffer[] icons = loadIconBuffers();
                if (icons != null && icons.length > 0) {
                    Display.setIcon(icons);
                    iconApplied = true;
                }
            }
        } catch (Throwable ignored) {}
    }

    public static synchronized ByteBuffer[] loadIconBuffers() {
        if (cachedBuffers != null) {
            return cachedBuffers;
        }
        try {
            List<ByteBuffer> buffers = new ArrayList<>();
            String[] iconPaths = new String[]{
                    "/assets/rattix/icons/R_16x16.png",
                    "/assets/rattix/icons/R_32x32.png",
                    "/assets/rattix/icons/R_64x64.png",
                    "/assets/rattix/icons/R_128x128.png"
            };

            for (String path : iconPaths) {
                try (InputStream stream = EarlyDisplayUtil.class.getResourceAsStream(path)) {
                    if (stream != null) {
                        ByteBuffer buffer = readImageToBuffer(stream);
                        if (buffer != null) {
                            buffers.add(buffer);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (!buffers.isEmpty()) {
                cachedBuffers = buffers.toArray(new ByteBuffer[0]);
                return cachedBuffers;
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }

    private static ByteBuffer readImageToBuffer(InputStream inputStream) throws IOException {
        BufferedImage image = ImageIO.read(inputStream);
        if (image == null) return null;
        int width = image.getWidth();
        int height = image.getHeight();
        int[] rgb = image.getRGB(0, 0, width, height, null, 0, width);
        ByteBuffer buffer = ByteBuffer.allocateDirect(4 * width * height);
        for (int pixel : rgb) {
            buffer.put((byte) ((pixel >> 16) & 0xFF));
            buffer.put((byte) ((pixel >> 8) & 0xFF));
            buffer.put((byte) (pixel & 0xFF));
            buffer.put((byte) ((pixel >> 24) & 0xFF));
        }
        buffer.flip();
        return buffer;
    }

    public static synchronized void startEarlyWatcher() {
        if (watcherStarted) return;
        watcherStarted = true;

        Thread watcher = new Thread(() -> {
            long startTime = System.currentTimeMillis();
            // 在启动前 30 秒内高频检测并尽早设置标题和图标
            while (System.currentTimeMillis() - startTime < 30000) {
                try {
                    if (Display.isCreated()) {
                        applyTitle();
                        if (!iconApplied) {
                            applyIcon();
                        }
                    }
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    break;
                } catch (Throwable ignored) {}
            }
        }, "Rattix-EarlyDisplayWatcher");
        watcher.setDaemon(true);
        watcher.start();
    }
}
