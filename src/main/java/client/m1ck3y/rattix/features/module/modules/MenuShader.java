package client.m1ck3y.rattix.features.module.modules;

import client.m1ck3y.rattix.features.module.modules.ClickGUI.CheckBoxSetting;
import client.m1ck3y.rattix.features.module.modules.ClickGUI.NumberSetting;
import client.m1ck3y.rattix.features.module.Category;
import client.m1ck3y.rattix.features.module.Module;
import client.m1ck3y.rattix.utils.render.shader.ShaderManager;

public class MenuShader extends Module {
    public CheckBoxSetting enableShader = new CheckBoxSetting("EnableShader", "enable_shader", true);
    public CheckBoxSetting allMenus = new CheckBoxSetting("AllMenusBackground", "all_menus", true);
    public CheckBoxSetting loadingScreens = new CheckBoxSetting("LoadingScreens", "loading_screens", true);
    public NumberSetting resolution = new NumberSetting("Resolution", 100.0, 0.0, 200.0, 5.0, "Performance");
    public NumberSetting fpsLimit = new NumberSetting("FpsLimit", 0.0, 0.0, 600.0, 10.0, "Performance");
    public NumberSetting speed = new NumberSetting("Speed", 1.0, 0.0, 10.0, 0.1, "Animation");

    private static MenuShader instance;

    public static MenuShader getInstance() {
        return instance;
    }

    public MenuShader() {
        super("MenuShader", "Configure menu and loading screen dynamic GLSL shader background.", Category.VISUAL);
        instance = this;
        addSettings(enableShader, allMenus, loadingScreens, resolution, fpsLimit, speed);
        setEnabled(true);
    }

    @Override
    public void onTick() {
        ShaderManager sm = ShaderManager.getInstance();
        if (sm != null) {
            // Sync settings from module to ShaderManager if changed in ClickGUI
            if (Math.abs(sm.getResolution() - resolution.getValue().doubleValue()) > 0.01) {
                sm.setResolution(resolution.getValue().doubleValue());
            }
            if (Math.abs(sm.getFpsLimit() - fpsLimit.getValue().doubleValue()) > 0.01) {
                sm.setFpsLimit(fpsLimit.getValue().doubleValue());
            }
            if (Math.abs(sm.getSpeed() - speed.getValue().doubleValue()) > 0.01) {
                sm.setSpeed(speed.getValue().doubleValue());
            }
        }
    }

    public void syncFromShaderManager(double res, double fps, double spd) {
        if (resolution != null) resolution.setValue(res);
        if (fpsLimit != null) fpsLimit.setValue(fps);
        if (speed != null) speed.setValue(spd);
    }
}
