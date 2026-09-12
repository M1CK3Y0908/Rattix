package client.m1ck3y.rattix.event.events;

import client.m1ck3y.rattix.event.Event;

public class Render2DEvent extends Event {
    private final float partialTicks;

    public Render2DEvent(float partialTicks) {
        this.partialTicks = partialTicks;
    }

    public float getPartialTicks() {
        return partialTicks;
    }
}
