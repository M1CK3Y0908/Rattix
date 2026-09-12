package client.m1ck3y.rattix.event.events;

import client.m1ck3y.rattix.event.Event;

public class KeyEvent extends Event {
    private final int key;

    public KeyEvent(int key) {
        this.key = key;
    }

    public int getKey() {
        return key;
    }
}
