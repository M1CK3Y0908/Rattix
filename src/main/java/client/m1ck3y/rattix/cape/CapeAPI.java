package client.m1ck3y.rattix.cape;

import java.util.UUID;

/**
 * Cape API interface.
 * Aligned with LiquidBounce cape/CapeAPI.
 */
public class CapeAPI {

    public static boolean hasCape(UUID uuid) {
        return CapeService.hasCape(uuid);
    }

    public static String getCapeUrl(UUID uuid) {
        return CapeService.getCapeUrl(uuid);
    }
}
