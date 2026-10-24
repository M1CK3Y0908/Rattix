package client.m1ck3y.rattix.file.configs;

import client.m1ck3y.rattix.file.FileConfig;
import client.m1ck3y.rattix.file.FileManager;

import java.io.File;
import java.io.IOException;

/**
 * Module state configuration file.
 * Aligned with LiquidBounce file/configs/ModulesConfig.
 */
public class ModulesConfig extends FileConfig {

    public ModulesConfig() {
        super(new File(FileManager.RATTIX_DIR, "modules.json"));
    }

    @Override
    public void loadConfig() throws IOException {
    }

    @Override
    public void saveConfig() throws IOException {
    }
}
