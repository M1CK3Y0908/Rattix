package client.m1ck3y.rattix.file.configs;

import client.m1ck3y.rattix.file.FileConfig;
import client.m1ck3y.rattix.file.FileManager;

import java.io.File;
import java.io.IOException;

/**
 * Values configuration file.
 * Aligned with LiquidBounce file/configs/ValuesConfig.
 */
public class ValuesConfig extends FileConfig {

    public ValuesConfig() {
        super(new File(FileManager.RATTIX_DIR, "values.json"));
    }

    @Override
    public void loadConfig() throws IOException {
    }

    @Override
    public void saveConfig() throws IOException {
    }
}
