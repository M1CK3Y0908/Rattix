package client.m1ck3y.rattix.script;

import java.io.File;

/**
 * Client script representation.
 * Aligned with LiquidBounce script/Script.
 */
public class Script {
    private final File scriptFile;
    private final String scriptName;

    public Script(File scriptFile) {
        this.scriptFile = scriptFile;
        this.scriptName = scriptFile != null ? scriptFile.getName() : "Unknown";
    }

    public File getScriptFile() {
        return scriptFile;
    }

    public String getScriptName() {
        return scriptName;
    }
}
