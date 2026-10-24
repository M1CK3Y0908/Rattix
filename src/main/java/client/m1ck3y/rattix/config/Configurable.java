package client.m1ck3y.rattix.config;

import java.util.List;

/**
 * Interface representing any object containing configurable settings or values.
 * Aligned with LiquidBounce config/Configurable architecture.
 */
public interface Configurable {
    /**
     * Get all configurable values/settings associated with this object.
     */
    List<Value<?>> getValues();
}
