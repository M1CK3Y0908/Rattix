package client.m1ck3y.rattix.config;

/**
 * Common typed Value definitions aligned with LiquidBounce config/Values.
 */
public class Values {

    public static class BoolValue extends Value<Boolean> {
        public BoolValue(String name, Boolean defaultValue) {
            super(name, defaultValue);
        }

        public BoolValue(String name, Boolean defaultValue, String group) {
            super(name, defaultValue, group);
        }

        public void toggle() {
            set(!get());
        }
    }

    public static class IntegerValue extends Value<Integer> {
        private final int minimum;
        private final int maximum;

        public IntegerValue(String name, int defaultValue, int minimum, int maximum) {
            super(name, defaultValue);
            this.minimum = minimum;
            this.maximum = maximum;
        }

        public IntegerValue(String name, int defaultValue, int minimum, int maximum, String group) {
            super(name, defaultValue, group);
            this.minimum = minimum;
            this.maximum = maximum;
        }

        public int getMinimum() {
            return minimum;
        }

        public int getMaximum() {
            return maximum;
        }

        @Override
        public void set(Integer value) {
            super.set(Math.max(minimum, Math.min(maximum, value)));
        }
    }

    public static class FloatValue extends Value<Float> {
        private final float minimum;
        private final float maximum;

        public FloatValue(String name, float defaultValue, float minimum, float maximum) {
            super(name, defaultValue);
            this.minimum = minimum;
            this.maximum = maximum;
        }

        public FloatValue(String name, float defaultValue, float minimum, float maximum, String group) {
            super(name, defaultValue, group);
            this.minimum = minimum;
            this.maximum = maximum;
        }

        public float getMinimum() {
            return minimum;
        }

        public float getMaximum() {
            return maximum;
        }

        @Override
        public void set(Float value) {
            super.set(Math.max(minimum, Math.min(maximum, value)));
        }
    }

    public static class TextValue extends Value<String> {
        public TextValue(String name, String defaultValue) {
            super(name, defaultValue);
        }

        public TextValue(String name, String defaultValue, String group) {
            super(name, defaultValue, group);
        }
    }

    public static class ListValue extends Value<String> {
        private final String[] values;

        public ListValue(String name, String defaultValue, String[] values) {
            super(name, defaultValue);
            this.values = values;
        }

        public ListValue(String name, String defaultValue, String[] values, String group) {
            super(name, defaultValue, group);
            this.values = values;
        }

        public String[] getValues() {
            return values;
        }
    }
}
