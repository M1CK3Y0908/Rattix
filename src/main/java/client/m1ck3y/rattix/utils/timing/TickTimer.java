package client.m1ck3y.rattix.utils.timing;

/**
 * Tick counter timer.
 * Aligned with LiquidBounce utils/timing/TickTimer.
 */
public class TickTimer {
    private int ticks = 0;

    public void update() {
        ticks++;
    }

    public void reset() {
        ticks = 0;
    }

    public boolean hasTimePassed(int ticks) {
        return this.ticks >= ticks;
    }
}
