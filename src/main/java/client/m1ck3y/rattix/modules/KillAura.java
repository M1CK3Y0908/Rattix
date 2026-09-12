package client.m1ck3y.rattix.modules;

import client.m1ck3y.rattix.modules.clickgui.BooleanSetting;
import client.m1ck3y.rattix.modules.clickgui.ModeSetting;
import client.m1ck3y.rattix.modules.clickgui.NumberSetting;
import org.lwjgl.input.Keyboard;

public class KillAura extends Register {
    // Movement
    public ModeSetting rotationMode = new ModeSetting("Rotations", "Normal", "Movement", new String[]{"Normal", "Smooth", "Silent", "None"});
    public BooleanSetting moveFix = new BooleanSetting("MovementFix", true, "Movement");
    public BooleanSetting keepSprint = new BooleanSetting("KeepSprint", true, "Movement");

    // Targets
    public NumberSetting range = new NumberSetting("Range", 4.2, 3.0, 6.0, 0.1, "Targets");
    public BooleanSetting targetPlayers = new BooleanSetting("Players", true, "Targets");
    public BooleanSetting targetMobs = new BooleanSetting("Mobs", false, "Targets");
    public BooleanSetting targetAnimals = new BooleanSetting("Animals", false, "Targets");

    // Miscellaneous
    public NumberSetting minCPS = new NumberSetting("Min CPS", 10.0, 1.0, 20.0, 1.0, "Miscellaneous");
    public NumberSetting maxCPS = new NumberSetting("Max CPS", 14.0, 1.0, 20.0, 1.0, "Miscellaneous");
    public BooleanSetting autoBlock = new BooleanSetting("AutoBlock", true, "Miscellaneous");

    // Visuals
    public BooleanSetting targetESP = new BooleanSetting("Target ESP", true, "Visuals");
    public BooleanSetting circleESP = new BooleanSetting("Circle ESP", false, "Visuals");

    public KillAura() {
        super("Automatically attacks entities in range.", Category.COMBAT, Keyboard.KEY_R);
        addSettings(rotationMode, moveFix, keepSprint, range, targetPlayers, targetMobs, targetAnimals, minCPS, maxCPS, autoBlock, targetESP, circleESP);
    }
}
