package client.m1ck3y.rattix.utils.client;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;

/**
 * Rattix 客户端工具类，对齐 LiquidBounce utils/client 架构。
 */
public class ClientUtils {
    public static final Minecraft mc = Minecraft.getMinecraft();

    public static void displayChatMessage(String message) {
        if (mc.thePlayer != null) {
            mc.thePlayer.addChatMessage(new ChatComponentText(message));
        }
    }
}
