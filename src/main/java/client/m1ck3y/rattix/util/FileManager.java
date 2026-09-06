package client.m1ck3y.rattix.util;

import net.minecraft.client.Minecraft;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FileManager {
    public static final File RATTIX_DIR = new File(Minecraft.getMinecraft().mcDataDir, "rattix");
    public static final File PROFILES_DIR = new File(RATTIX_DIR, "profiles");
    public static final File SCRIPTS_DIR = new File(RATTIX_DIR, "scripts");
    public static final File ASSETS_DIR = new File(RATTIX_DIR, "assets");
    public static final File FONTS_DIR = new File(ASSETS_DIR, "fonts");
    public static final File AUDIO_DIR = new File(ASSETS_DIR, "audio");

    public static void init() {
        if (!RATTIX_DIR.exists()) RATTIX_DIR.mkdirs();
        if (!PROFILES_DIR.exists()) PROFILES_DIR.mkdirs();
        if (!SCRIPTS_DIR.exists()) SCRIPTS_DIR.mkdirs();
        if (!ASSETS_DIR.exists()) ASSETS_DIR.mkdirs();
        if (!FONTS_DIR.exists()) FONTS_DIR.mkdirs();
        if (!AUDIO_DIR.exists()) AUDIO_DIR.mkdirs();

        extractDefaultFont();
    }

    private static void extractDefaultFont() {
        File consolasFile = new File(FONTS_DIR, "consolas.ttf");
        if (!consolasFile.exists()) {
            try (InputStream in = FileManager.class.getResourceAsStream("/assets/rattix/fonts/consolas.ttf")) {
                if (in != null) {
                    try (OutputStream out = new FileOutputStream(consolasFile)) {
                        byte[] buffer = new byte[8192];
                        int bytesRead;
                        while ((bytesRead = in.read(buffer)) != -1) {
                            out.write(buffer, 0, bytesRead);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static List<File> cachedTtfFiles = null;
    private static long lastTtfScanTime = 0;
    private static final long TTF_CACHE_DURATION_MS = 30000;

    public static List<File> getTtfFiles() {
        return getTtfFiles(false);
    }

    public static synchronized List<File> getTtfFiles(boolean forceRefresh) {
        long now = System.currentTimeMillis();
        if (cachedTtfFiles == null || forceRefresh || (now - lastTtfScanTime > TTF_CACHE_DURATION_MS)) {
            List<File> files = new ArrayList<>();
            if (FONTS_DIR.exists() && FONTS_DIR.isDirectory()) {
                File[] list = FONTS_DIR.listFiles((dir, name) -> name.toLowerCase().endsWith(".ttf"));
                if (list != null) {
                    Arrays.sort(list, (f1, f2) -> f1.getName().compareToIgnoreCase(f2.getName()));
                    files.addAll(Arrays.asList(list));
                }
            }
            cachedTtfFiles = Collections.unmodifiableList(files);
            lastTtfScanTime = now;
        }
        return cachedTtfFiles;
    }

    public static void refreshTtfFiles() {
        getTtfFiles(true);
    }

    public static void openFolder(File folder) {
        try {
            if (folder.exists() && Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(folder);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
