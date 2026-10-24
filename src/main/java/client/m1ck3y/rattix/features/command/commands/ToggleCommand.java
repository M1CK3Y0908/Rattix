package client.m1ck3y.rattix.features.command.commands;

import client.m1ck3y.rattix.features.command.Command;

import client.m1ck3y.rattix.features.module.Module;
import client.m1ck3y.rattix.features.module.ModuleManager;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;

public class ToggleCommand extends Command {
    public ToggleCommand() {
        super("toggle", "Toggles a module on or off", ".t <module>", "t");
    }

    @Override
    public void execute(String[] args) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) return;

        if (args.length < 2) {
            mc.thePlayer.addChatMessage(new ChatComponentText("§cUsage: " + getSyntax()));
            return;
        }

        String modName = args[1];
        Module module = ModuleManager.getModuleByName(modName);
        if (module != null) {
            module.toggle();
            mc.thePlayer.addChatMessage(new ChatComponentText("§a[Rattix] §f" + module.getName() + " is now " + (module.isEnabled() ? "§aEnabled" : "§cDisabled")));
        } else {
            mc.thePlayer.addChatMessage(new ChatComponentText("§cModule '" + modName + "' not found!"));
        }
    }
}
