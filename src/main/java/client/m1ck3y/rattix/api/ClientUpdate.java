package client.m1ck3y.rattix.api;

/**
 * Client update metadata API representation.
 * Aligned with LiquidBounce api/ClientUpdate.
 */
public class ClientUpdate {
    private final String version;
    private final String downloadUrl;

    public ClientUpdate(String version, String downloadUrl) {
        this.version = version;
        this.downloadUrl = downloadUrl;
    }

    public String getVersion() {
        return version;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }
}
