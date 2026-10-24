package client.m1ck3y.rattix.features.special;

/**
 * Auto-reconnect handling.
 * Aligned with LiquidBounce features/special/AutoReconnect.
 */
public class AutoReconnect {
    private static boolean enabled = true;
    private static int delay = 5000;

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean enabled) {
        AutoReconnect.enabled = enabled;
    }

    public static int getDelay() {
        return delay;
    }

    public static void setDelay(int delay) {
        AutoReconnect.delay = delay;
    }
}
