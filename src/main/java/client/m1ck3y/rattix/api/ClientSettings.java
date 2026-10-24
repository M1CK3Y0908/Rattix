package client.m1ck3y.rattix.api;

/**
 * Client settings API representation.
 * Aligned with LiquidBounce api/ClientSettings.
 */
public class ClientSettings {
    private static String apiUrl = "https://api.rattix.client";

    public static String getApiUrl() {
        return apiUrl;
    }

    public static void setApiUrl(String apiUrl) {
        ClientSettings.apiUrl = apiUrl;
    }
}
