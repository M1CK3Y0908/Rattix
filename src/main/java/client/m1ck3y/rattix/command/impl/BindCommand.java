package client.m1ck3y.rattix.command.impl;

import client.m1ck3y.rattix.command.Command;

import client.m1ck3y.rattix.modules.manager.Register;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import org.lwjgl.input.Keyboard;

public class BindCommand extends Command {
    public BindCommand() {
        super("bind", "Binds a module to a key", ".bind <module> <key>", "b");
    }

    @Override
    public void execute(String[] args) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) return;

        if (args.length < 3) {
            mc.thePlayer.addChatMessage(new ChatComponentText("§cUsage: " + getSyntax()));
            return;
        }

        String modName = args[1];
        String keyName = args[2].toUpperCase();
        Register module = Register.getModuleByName(modName);
        if (module == null) {
            mc.thePlayer.addChatMessage(new ChatComponentText("§cModule '" + modName + "' not found!"));
            return;
        }

        int key = Keyboard.getKeyIndex(keyName);
        module.setKey(key);
        mc.thePlayer.addChatMessage(new ChatComponentText("§a[Rattix] §fBound §e" + module.getName() + " §fto §b" + (key == 0 ? "NONE" : keyName)));
    }
}
