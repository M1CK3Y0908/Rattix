package client.m1ck3y.rattix.chat;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;

/**
 * Rattix 客户端聊天扩展，对齐 LiquidBounce chat 架构。
 */
public class ClientChat {
    public static final String CHAT_PREFIX = "§a[Rattix] §r";

    public static void print(String message) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer != null) {
            mc.thePlayer.addChatMessage(new ChatComponentText(CHAT_PREFIX + message));
        }
    }

    public static void error(String message) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer != null) {
            mc.thePlayer.addChatMessage(new ChatComponentText(CHAT_PREFIX + "§c" + message));
        }
    }
}
