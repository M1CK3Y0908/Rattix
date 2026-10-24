package client.m1ck3y.rattix.features.special;

/**
 * Rattix BungeeCordSpoof 控制，对齐 LiquidBounce features/special/BungeeCordSpoof。
 */
public class BungeeCordSpoof {
    private static boolean enabled = false;

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean enabled) {
        BungeeCordSpoof.enabled = enabled;
    }
}
