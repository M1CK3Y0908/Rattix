package client.m1ck3y.rattix.file.configs.models;

/**
 * Client configuration data model.
 * Aligned with LiquidBounce file/configs/models/ClientConfiguration.
 */
public class ClientConfiguration {
    private String clientTitle = "Rattix";
    private boolean fastRender = true;

    public String getClientTitle() {
        return clientTitle;
    }

    public void setClientTitle(String clientTitle) {
        this.clientTitle = clientTitle;
    }

    public boolean isFastRender() {
        return fastRender;
    }

    public void setFastRender(boolean fastRender) {
        this.fastRender = fastRender;
    }
}
