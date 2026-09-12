package client.m1ck3y.rattix.config;

import client.m1ck3y.rattix.manager.FileManager;

import java.io.File;

public class ConfigManager {
    private static final ConfigManager INSTANCE = new ConfigManager();

    public static ConfigManager getInstance() {
        return INSTANCE;
    }

    public void init() {
        FileManager.init();
    }

    public File getConfigDirectory() {
        return FileManager.RATTIX_DIR;
    }

    public File getProfilesDirectory() {
        return FileManager.PROFILES_DIR;
    }
}
