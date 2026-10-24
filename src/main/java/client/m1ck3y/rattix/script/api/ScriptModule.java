package client.m1ck3y.rattix.script.api;

/**
 * Script API module representation.
 * Aligned with LiquidBounce script/api/ScriptModule.
 */
public class ScriptModule {
    private final String name;

    public ScriptModule(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
