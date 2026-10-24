package client.m1ck3y.rattix.script.api;

/**
 * Script API command representation.
 * Aligned with LiquidBounce script/api/ScriptCommand.
 */
public class ScriptCommand {
    private final String name;

    public ScriptCommand(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
