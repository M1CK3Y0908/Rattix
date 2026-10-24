package client.m1ck3y.rattix.features.special;

/**
 * Rattix 客户端基础修复与防护，对齐 LiquidBounce features/special/ClientFixes。
 */
public class ClientFixes {
    private static boolean nullByteCheck = true;
    private static boolean preventGhostBlocks = true;

    public static boolean isNullByteCheck() {
        return nullByteCheck;
    }

    public static void setNullByteCheck(boolean nullByteCheck) {
        ClientFixes.nullByteCheck = nullByteCheck;
    }

    public static boolean isPreventGhostBlocks() {
        return preventGhostBlocks;
    }

    public static void setPreventGhostBlocks(boolean preventGhostBlocks) {
        ClientFixes.preventGhostBlocks = preventGhostBlocks;
    }
}
