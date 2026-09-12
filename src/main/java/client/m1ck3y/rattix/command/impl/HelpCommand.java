package client.m1ck3y.rattix.command.impl;

import client.m1ck3y.rattix.command.Command;
import client.m1ck3y.rattix.command.CommandManager;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;

public class HelpCommand extends Command {
    public HelpCommand() {
        super("help", "Lists all available commands", ".help", "h", "?");
    }

    @Override
    public void execute(String[] args) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) return;

        mc.thePlayer.addChatMessage(new ChatComponentText("§b--- Rattix Commands ---"));
        for (Command cmd : CommandManager.getInstance().getCommands()) {
            mc.thePlayer.addChatMessage(new ChatComponentText("§e." + cmd.getName() + " §7- " + cmd.getDescription()));
        }
    }
}
