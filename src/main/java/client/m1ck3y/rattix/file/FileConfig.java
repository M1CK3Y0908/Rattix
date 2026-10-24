package client.m1ck3y.rattix.file;

import java.io.File;
import java.io.IOException;

/**
 * Base configuration file class.
 * Aligned with LiquidBounce file/FileConfig architecture.
 */
public abstract class FileConfig {
    private final File file;

    public FileConfig(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    public abstract void loadConfig() throws IOException;

    public abstract void saveConfig() throws IOException;

    public boolean hasConfig() {
        return file != null && file.exists();
    }

    public void createConfig() throws IOException {
        if (file != null && !file.exists()) {
            if (file.getParentFile() != null && !file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            file.createNewFile();
        }
    }
}
