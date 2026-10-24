package client.m1ck3y.rattix.features.special;

/**
 * Rattix AntiForge 状态控制，对齐 LiquidBounce features/special/AntiForge。
 */
public class AntiForge {
    private static boolean enabled = true;
    private static boolean blockFML = false;
    private static boolean blockProxy = false;

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean enabled) {
        AntiForge.enabled = enabled;
    }

    public static boolean isBlockFML() {
        return blockFML;
    }

    public static void setBlockFML(boolean blockFML) {
        AntiForge.blockFML = blockFML;
    }

    public static boolean isBlockProxy() {
        return blockProxy;
    }

    public static void setBlockProxy(boolean blockProxy) {
        AntiForge.blockProxy = blockProxy;
    }
}
