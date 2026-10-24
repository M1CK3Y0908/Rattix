package client.m1ck3y.rattix.features.module;

import client.m1ck3y.rattix.features.command.Command;
import client.m1ck3y.rattix.features.module.modules.ClickGUI.BooleanSetting;
import client.m1ck3y.rattix.features.module.modules.ClickGUI.CheckBoxSetting;
import client.m1ck3y.rattix.features.module.modules.ClickGUI.ColorSetting;
import client.m1ck3y.rattix.features.module.modules.ClickGUI.ModeSetting;
import client.m1ck3y.rattix.features.module.modules.ClickGUI.NumberSetting;
import client.m1ck3y.rattix.features.module.modules.ClickGUI.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import org.lwjgl.input.Keyboard;

import java.util.List;

/**
 * Rattix 模块指令处理器 (ModuleCommand)。
 * 支持在聊天栏通过 .module / .mod / .m 指令查看、切换模块、配置按键绑定与实时修改参数。
 */
public class ModuleCommand extends Command {

    public ModuleCommand() {
        super("module", "Configure modules, toggle status, bind keys, and change settings", ".module <module> [toggle|bind|set <setting> <val>|list]", "m", "mod");
    }

    @Override
    public void execute(String[] args) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) return;

        if (args.length < 2) {
            sendHelp(mc);
            return;
        }

        String sub = args[1].toLowerCase();

        // 1. 列出所有模块
        if ("list".equalsIgnoreCase(sub) || "l".equalsIgnoreCase(sub)) {
            List<Module> all = ModuleManager.getAlphabeticalModules();
            StringBuilder sb = new StringBuilder("§a[Rattix] §fModules (" + all.size() + "): ");
            for (int i = 0; i < all.size(); i++) {
                Module m = all.get(i);
                sb.append(m.isEnabled() ? "§a" : "§7").append(m.getName());
                if (i < all.size() - 1) {
                    sb.append("§f, ");
                }
            }
            mc.thePlayer.addChatMessage(new ChatComponentText(sb.toString()));
            return;
        }

        // 2. 根据名称查找模块
        Module module = ModuleManager.getModuleByName(args[1]);
        if (module == null) {
            mc.thePlayer.addChatMessage(new ChatComponentText("§c[Rattix] Module '" + args[1] + "' not found! Type §e.module list §cto see all modules."));
            return;
        }

        // 3. 仅输入模块名：查看模块详情与参数
        if (args.length == 2) {
            String keyName = module.getKeyCode() == 0 ? "None" : Keyboard.getKeyName(module.getKeyCode());
            mc.thePlayer.addChatMessage(new ChatComponentText("§a[Rattix] §6" + module.getName() + " §7[" + module.getCategory().getDisplayName() + "]"));
            mc.thePlayer.addChatMessage(new ChatComponentText(" §7Status: " + (module.isEnabled() ? "§aEnabled" : "§cDisabled") + " §7| Key: §b" + keyName));
            if (!module.getDescription().isEmpty()) {
                mc.thePlayer.addChatMessage(new ChatComponentText(" §7Description: §f" + module.getDescription()));
            }

            List<Setting<?>> settings = module.getSettings();
            if (!settings.isEmpty()) {
                StringBuilder ssb = new StringBuilder(" §7Settings: ");
                for (int i = 0; i < settings.size(); i++) {
                    Setting<?> s = settings.get(i);
                    ssb.append("§e").append(s.getName()).append("§7(§f").append(s.getValue()).append("§7)");
                    if (i < settings.size() - 1) {
                        ssb.append(", ");
                    }
                }
                mc.thePlayer.addChatMessage(new ChatComponentText(ssb.toString()));
            }
            mc.thePlayer.addChatMessage(new ChatComponentText(" §7Commands: §e.m " + module.getName() + " toggle §7| §e.m " + module.getName() + " bind <key> §7| §e.m " + module.getName() + " set <setting> <val>"));
            return;
        }

        String action = args[2].toLowerCase();

        // 4. 切换启闭状态
        if ("toggle".equalsIgnoreCase(action) || "t".equalsIgnoreCase(action)) {
            module.toggle();
            mc.thePlayer.addChatMessage(new ChatComponentText("§a[Rattix] §f" + module.getName() + " is now " + (module.isEnabled() ? "§aEnabled" : "§cDisabled")));
            return;
        }

        // 5. 绑定按键
        if ("bind".equalsIgnoreCase(action) || "b".equalsIgnoreCase(action)) {
            if (args.length < 4) {
                mc.thePlayer.addChatMessage(new ChatComponentText("§c[Rattix] Usage: .module " + module.getName() + " bind <key|none>"));
                return;
            }
            String keyStr = args[3].toUpperCase();
            if ("NONE".equalsIgnoreCase(keyStr) || "CLEAR".equalsIgnoreCase(keyStr) || "0".equals(keyStr)) {
                module.setKeyCode(0);
                mc.thePlayer.addChatMessage(new ChatComponentText("§a[Rattix] §fUnbound key for §6" + module.getName()));
            } else {
                int key = Keyboard.getKeyIndex(keyStr);
                if (key == 0) {
                    mc.thePlayer.addChatMessage(new ChatComponentText("§c[Rattix] Unknown key: " + keyStr));
                } else {
                    module.setKeyCode(key);
                    mc.thePlayer.addChatMessage(new ChatComponentText("§a[Rattix] §fBound §6" + module.getName() + " §fto key §b" + Keyboard.getKeyName(key)));
                }
            }
            return;
        }

        // 6. 重置模块参数
        if ("reset".equalsIgnoreCase(action)) {
            module.resetSettings();
            mc.thePlayer.addChatMessage(new ChatComponentText("§a[Rattix] §fReset all settings for §6" + module.getName() + " §fto defaults."));
            return;
        }

        // 7. 设置参数值
        if ("set".equalsIgnoreCase(action) || "s".equalsIgnoreCase(action)) {
            if (args.length < 5) {
                mc.thePlayer.addChatMessage(new ChatComponentText("§c[Rattix] Usage: .module " + module.getName() + " set <settingName> <value>"));
                return;
            }
            String settingName = args[3];
            String valStr = args[4];

            Setting<?> targetSetting = null;
            for (Setting<?> s : module.getSettings()) {
                if (s.getName().equalsIgnoreCase(settingName) || s.getName().replaceAll("\\s+", "").equalsIgnoreCase(settingName)) {
                    targetSetting = s;
                    break;
                }
            }

            if (targetSetting == null) {
                mc.thePlayer.addChatMessage(new ChatComponentText("§c[Rattix] Setting '" + settingName + "' not found in module " + module.getName()));
                return;
            }

            try {
                if (targetSetting instanceof BooleanSetting) {
                    boolean val = Boolean.parseBoolean(valStr) || "1".equals(valStr) || "yes".equalsIgnoreCase(valStr) || "on".equalsIgnoreCase(valStr);
                    ((BooleanSetting) targetSetting).setValue(val);
                    mc.thePlayer.addChatMessage(new ChatComponentText("§a[Rattix] §fSet §e" + targetSetting.getName() + " §fto §b" + val));
                } else if (targetSetting instanceof CheckBoxSetting) {
                    boolean val = Boolean.parseBoolean(valStr) || "1".equals(valStr) || "yes".equalsIgnoreCase(valStr) || "on".equalsIgnoreCase(valStr);
                    ((CheckBoxSetting) targetSetting).setValue(val);
                    mc.thePlayer.addChatMessage(new ChatComponentText("§a[Rattix] §fSet §e" + targetSetting.getName() + " §fto §b" + val));
                } else if (targetSetting instanceof NumberSetting) {
                    double num = Double.parseDouble(valStr);
                    NumberSetting ns = (NumberSetting) targetSetting;
                    num = Math.max(ns.getMin(), Math.min(ns.getMax(), num));
                    ns.setValue(num);
                    mc.thePlayer.addChatMessage(new ChatComponentText("§a[Rattix] §fSet §e" + targetSetting.getName() + " §fto §b" + num));
                } else if (targetSetting instanceof ModeSetting) {
                    ModeSetting ms = (ModeSetting) targetSetting;
                    boolean found = false;
                    for (String mode : ms.getModes()) {
                        if (mode.equalsIgnoreCase(valStr)) {
                            ms.setValue(mode);
                            found = true;
                            break;
                        }
                    }
                    if (found) {
                        mc.thePlayer.addChatMessage(new ChatComponentText("§a[Rattix] §fSet §e" + targetSetting.getName() + " §fto §b" + ms.getValue()));
                    } else {
                        mc.thePlayer.addChatMessage(new ChatComponentText("§c[Rattix] Invalid mode! Available: " + String.join(", ", ms.getModes())));
                    }
                } else if (targetSetting instanceof ColorSetting) {
                    ColorSetting cs = (ColorSetting) targetSetting;
                    int color;
                    if (valStr.startsWith("#")) {
                        color = (int) Long.parseLong(valStr.substring(1), 16);
                    } else if (valStr.startsWith("0x") || valStr.startsWith("0X")) {
                        color = (int) Long.parseLong(valStr.substring(2), 16);
                    } else {
                        color = (int) Long.parseLong(valStr);
                    }
                    cs.setColor(color);
                    mc.thePlayer.addChatMessage(new ChatComponentText("§a[Rattix] §fSet §e" + targetSetting.getName() + " §fto color §b#" + Integer.toHexString(color).toUpperCase()));
                } else {
                    mc.thePlayer.addChatMessage(new ChatComponentText("§c[Rattix] Unsupported setting type: " + targetSetting.getClass().getSimpleName()));
                }
            } catch (Exception e) {
                mc.thePlayer.addChatMessage(new ChatComponentText("§c[Rattix] Invalid value format: " + e.getMessage()));
            }
            return;
        }

        sendHelp(mc);
    }

    private void sendHelp(Minecraft mc) {
        mc.thePlayer.addChatMessage(new ChatComponentText("§e[Rattix] Module Command Usage:"));
        mc.thePlayer.addChatMessage(new ChatComponentText(" §7.module list §f- List all modules"));
        mc.thePlayer.addChatMessage(new ChatComponentText(" §7.module <module> §f- View module information"));
        mc.thePlayer.addChatMessage(new ChatComponentText(" §7.module <module> toggle §f- Toggle module"));
        mc.thePlayer.addChatMessage(new ChatComponentText(" §7.module <module> bind <key> §f- Bind key to module"));
        mc.thePlayer.addChatMessage(new ChatComponentText(" §7.module <module> set <setting> <val> §f- Set parameter"));
        mc.thePlayer.addChatMessage(new ChatComponentText(" §7.module <module> reset §f- Reset settings to defaults"));
    }
}
