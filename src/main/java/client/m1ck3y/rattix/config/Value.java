package client.m1ck3y.rattix.config;

/**
 * Base configurable value/setting class.
 * Aligned with LiquidBounce config/Value architecture.
 *
 * @param <T> value data type
 */
public class Value<T> {
    private final String name;
    protected T value;
    protected T defaultValue;
    private String group;
    private Value<?> parent;

    public Value(String name, T defaultValue) {
        this(name, defaultValue, "General");
    }

    public Value(String name, T defaultValue, String group) {
        this.name = name;
        this.value = defaultValue;
        this.defaultValue = defaultValue;
        this.group = group;
    }

    public String getName() {
        return name;
    }

    public T get() {
        return value;
    }

    public T getValue() {
        return value;
    }

    public void set(T value) {
        this.value = value;
        onChanged(value);
    }

    public void setValue(T value) {
        set(value);
    }

    public T getDefault() {
        return defaultValue;
    }

    public void reset() {
        this.value = defaultValue;
    }

    public String getGroup() {
        return group;
    }

    public Value<T> setGroup(String group) {
        this.group = group;
        return this;
    }

    public Value<?> getParent() {
        return parent;
    }

    public Value<T> withParent(Value<?> parent) {
        this.parent = parent;
        return this;
    }

    protected void onChanged(T newValue) {
    }
}
