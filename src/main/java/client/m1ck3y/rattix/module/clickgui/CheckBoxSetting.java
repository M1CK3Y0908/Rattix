package client.m1ck3y.rattix.module.clickgui;

public class CheckBoxSetting extends Setting<Boolean> {
    private String cachedDisplayText;

    public CheckBoxSetting(String name, String registryName, boolean defaultValue, String group) {
        super(name, registryName, defaultValue, group);
    }

    public CheckBoxSetting(String name, String registryName, boolean defaultValue) {
        super(name, registryName, defaultValue, "General");
    }

    public CheckBoxSetting(String name, boolean defaultValue, String group) {
        super(name, defaultValue, group);
    }

    public CheckBoxSetting(String name, boolean defaultValue) {
        super(name, defaultValue, "General");
    }

    public void toggle() {
        this.value = !Boolean.TRUE.equals(this.value);
        this.cachedDisplayText = null;
    }

    public boolean isEnabled() {
        return Boolean.TRUE.equals(this.value);
    }

    public boolean getState() {
        return Boolean.TRUE.equals(this.value);
    }

    public void setState(boolean state) {
        this.value = state;
        this.cachedDisplayText = null;
    }

    public String getFormattedState() {
        return Boolean.TRUE.equals(this.value) ? "\u00A7atrue" : "\u00A7cfalse";
    }

    public String getDisplayText() {
        if (cachedDisplayText == null) {
            cachedDisplayText = getName() + ": " + getFormattedState();
        }
        return cachedDisplayText;
    }
}
