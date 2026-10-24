package client.m1ck3y.rattix.utils.timing;

/**
 * Millisecond timer.
 * Aligned with LiquidBounce utils/timing/MSTimer.
 */
public class MSTimer {
    private long time = -1L;

    public boolean hasTimePassed(long ms) {
        return System.currentTimeMillis() - time >= ms;
    }

    public void reset() {
        time = System.currentTimeMillis();
    }
}
