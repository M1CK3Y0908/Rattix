package client.m1ck3y.rattix.module.clickgui;

public class NumberSetting extends Setting<Double> {
    private final double min;
    private final double max;
    private final double increment;
    private String cachedDisplayValue;
    private String cachedLabel; // "Name: " prefix

    public NumberSetting(String name, double defaultValue, double min, double max, double increment) {
        super(name, defaultValue);
        this.min = min;
        this.max = max;
        this.increment = increment;
    }

    public NumberSetting(String name, double defaultValue, double min, double max, double increment, String group) {
        super(name, defaultValue, group);
        this.min = min;
        this.max = max;
        this.increment = increment;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public double getIncrement() {
        return increment;
    }

    public void setValue(double value) {
        double precision = 1.0 / this.increment;
        this.value = Math.max(min, Math.min(max, Math.round(value * precision) / precision));
        this.cachedDisplayValue = null; // invalidate cache
    }

    /**
     * Returns a pre-cached formatted display value string.
     * Avoids per-frame String.format() allocation.
     */
    public String getDisplayValue() {
        if (cachedDisplayValue == null) {
            if (getName().equalsIgnoreCase("Size")) {
                cachedDisplayValue = String.format("%.0f%%", getValue());
            } else {
                cachedDisplayValue = String.format("%.1f", getValue());
            }
        }
        return cachedDisplayValue;
    }

    /**
     * Returns a pre-cached "Name: " label string.
     * Avoids per-frame string concatenation.
     */
    public String getLabel() {
        if (cachedLabel == null) {
            cachedLabel = getName() + ": ";
        }
        return cachedLabel;
    }
}
