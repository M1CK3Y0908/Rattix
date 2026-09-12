package client.m1ck3y.rattix.modules.clickgui;

public class CheckBox extends CheckBoxSetting {

    public CheckBox(String name, String registryName, boolean defaultValue, String group) {
        super(name, registryName, defaultValue, group);
    }

    public CheckBox(String name, String registryName, boolean defaultValue) {
        super(name, registryName, defaultValue);
    }

    public CheckBox(String name, boolean defaultValue, String group) {
        super(name, defaultValue, group);
    }

    public CheckBox(String name, boolean defaultValue) {
        super(name, defaultValue);
    }
}
