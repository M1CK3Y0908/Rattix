package client.m1ck3y.rattix.modules.clickgui;

public class BooleanSetting extends CheckBoxSetting {

    public BooleanSetting(String name, String registryName, boolean defaultValue, String group) {
        super(name, registryName, defaultValue, group);
    }

    public BooleanSetting(String name, String registryName, boolean defaultValue) {
        super(name, registryName, defaultValue);
    }

    public BooleanSetting(String name, boolean defaultValue, String group) {
        super(name, defaultValue, group);
    }

    public BooleanSetting(String name, boolean defaultValue) {
        super(name, defaultValue);
    }
}
