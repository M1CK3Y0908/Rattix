package client.m1ck3y.rattix.utils.movement;

import net.minecraft.client.Minecraft;

/**
 * Player movement utility methods.
 * Aligned with LiquidBounce utils/movement/MovementUtils.
 */
public class MovementUtils {
    private static final Minecraft mc = Minecraft.getMinecraft();

    public static boolean isMoving() {
        return mc.thePlayer != null && (mc.thePlayer.movementInput.moveForward != 0.0F || mc.thePlayer.movementInput.moveStrafe != 0.0F);
    }
}
