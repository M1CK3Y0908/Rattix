package client.m1ck3y.rattix.script;

import client.m1ck3y.rattix.manager.FileManager;

import java.io.File;

public class ScriptManager {
    private static final ScriptManager INSTANCE = new ScriptManager();

    public static ScriptManager getInstance() {
        return INSTANCE;
    }

    public void init() {
        File scriptsDir = FileManager.SCRIPTS_DIR;
        if (!scriptsDir.exists()) {
            scriptsDir.mkdirs();
        }
    }

    public File getScriptsDirectory() {
        return FileManager.SCRIPTS_DIR;
    }
}
