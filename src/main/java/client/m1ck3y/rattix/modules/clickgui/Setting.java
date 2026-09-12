package client.m1ck3y.rattix.modules.clickgui;

public abstract class Setting<T> {
    private final String name;
    private final String registryName;
    private final T defaultValue;
    protected T value;
    private String group = "General";
    private Setting<?> parentSetting = null;

    public Setting(String name, T defaultValue) {
        this(name, name.toLowerCase().replace(" ", "_"), defaultValue, "General");
    }

    public Setting(String name, T defaultValue, String group) {
        this(name, name.toLowerCase().replace(" ", "_"), defaultValue, group);
    }

    public Setting(String name, String registryName, T defaultValue, String group) {
        this.name = name;
        this.registryName = registryName;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
        this.group = group;
    }

    public String getName() {
        return name;
    }

    public String getRegistryName() {
        return registryName;
    }

    public String getId() {
        return registryName;
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public void reset() {
        this.value = this.defaultValue;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public Setting<?> getParentSetting() {
        return parentSetting;
    }

    public void setParentSetting(Setting<?> parentSetting) {
        this.parentSetting = parentSetting;
    }

    @SuppressWarnings("unchecked")
    public <S extends Setting<T>> S withParent(Setting<?> parentSetting) {
        this.parentSetting = parentSetting;
        return (S) this;
    }
}
