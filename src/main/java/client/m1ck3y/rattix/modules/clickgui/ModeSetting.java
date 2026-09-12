package client.m1ck3y.rattix.modules.clickgui;

import java.util.Arrays;
import java.util.List;

public class ModeSetting extends Setting<String> {
    private final List<String> modes;
    private int index;

    public ModeSetting(String name, String defaultMode, String... modes) {
        super(name, defaultMode);
        this.modes = Arrays.asList(modes);
        this.index = this.modes.indexOf(defaultMode);
        if (this.index == -1) {
            this.index = 0;
            if (!this.modes.isEmpty()) {
                this.value = this.modes.get(0);
            }
        }
    }

    public ModeSetting(String name, String defaultMode, String group, String[] modes) {
        super(name, defaultMode, group);
        this.modes = Arrays.asList(modes);
        this.index = this.modes.indexOf(defaultMode);
        if (this.index == -1) {
            this.index = 0;
            if (!this.modes.isEmpty()) {
                this.value = this.modes.get(0);
            }
        }
    }

    public List<String> getModes() {
        return modes;
    }

    public void cycle() {
        if (modes.isEmpty()) return;
        index = (index + 1) % modes.size();
        this.value = modes.get(index);
    }

    @Override
    public void setValue(String value) {
        super.setValue(value);
        if (modes != null) {
            int idx = modes.indexOf(value);
            if (idx != -1) {
                this.index = idx;
            }
        }
    }

    public boolean is(String mode) {
        return this.value.equalsIgnoreCase(mode);
    }
}
