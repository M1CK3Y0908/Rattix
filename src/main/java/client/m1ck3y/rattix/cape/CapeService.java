package client.m1ck3y.rattix.cape;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rattix 披风服务，对齐 LiquidBounce cape 架构。
 */
public class CapeService {
    private static final Map<UUID, String> CAPE_USERS = new ConcurrentHashMap<>();

    public static boolean hasCape(UUID uuid) {
        return uuid != null && CAPE_USERS.containsKey(uuid);
    }

    public static String getCapeUrl(UUID uuid) {
        return uuid != null ? CAPE_USERS.get(uuid) : null;
    }

    public static void registerCape(UUID uuid, String capeUrl) {
        if (uuid != null && capeUrl != null) {
            CAPE_USERS.put(uuid, capeUrl);
        }
    }
}
