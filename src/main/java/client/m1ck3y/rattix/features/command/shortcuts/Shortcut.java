package client.m1ck3y.rattix.features.command.shortcuts;

import java.util.List;

/**
 * Command shortcut definition.
 * Aligned with LiquidBounce features/command/shortcuts/Shortcut.
 */
public class Shortcut {
    private final String name;
    private final List<String> commands;

    public Shortcut(String name, List<String> commands) {
        this.name = name;
        this.commands = commands;
    }

    public String getName() {
        return name;
    }

    public List<String> getCommands() {
        return commands;
    }
}
