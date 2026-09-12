package client.m1ck3y.rattix.manager;

import client.m1ck3y.rattix.command.Command;
import client.m1ck3y.rattix.command.impl.BindCommand;
import client.m1ck3y.rattix.command.impl.HelpCommand;
import client.m1ck3y.rattix.command.impl.ToggleCommand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandManager {
    private static final CommandManager INSTANCE = new CommandManager();
    private final List<Command> commands = new ArrayList<>();
    private final Map<String, Command> commandMap = new HashMap<>();
    private String prefix = ".";

    public static CommandManager getInstance() {
        return INSTANCE;
    }

    public CommandManager() {
        registerCommand(new HelpCommand());
        registerCommand(new ToggleCommand());
        registerCommand(new BindCommand());
    }

    public void registerCommand(Command cmd) {
        commands.add(cmd);
        commandMap.put(cmd.getName().toLowerCase(), cmd);
        for (String alias : cmd.getAliases()) {
            commandMap.put(alias.toLowerCase(), cmd);
        }
    }

    public boolean execute(String message) {
        if (message == null || !message.startsWith(prefix)) {
            return false;
        }

        String raw = message.substring(prefix.length()).trim();
        if (raw.isEmpty()) return false;

        String[] args = raw.split("\\s+");
        String commandName = args[0].toLowerCase();
        Command command = commandMap.get(commandName);

        if (command != null) {
            try {
                command.execute(args);
            } catch (Throwable t) {
                t.printStackTrace();
            }
            return true;
        }
        return false;
    }

    public List<Command> getCommands() {
        return commands;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}
