package client.m1ck3y.rattix.config;

import java.util.Collection;

/**
 * Utility class for manipulating settings and values.
 * Aligned with LiquidBounce config/SettingsUtils.
 */
public class SettingsUtils {

    public static Value<?> findValue(Collection<Value<?>> values, String name) {
        if (values == null || name == null) return null;
        for (Value<?> value : values) {
            if (value.getName().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }
}
