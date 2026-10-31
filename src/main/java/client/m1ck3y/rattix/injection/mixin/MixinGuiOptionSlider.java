package client.m1ck3y.rattix.injection.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiOptionSlider;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.MathHelper;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;

@Mixin(GuiOptionSlider.class)
public abstract class MixinGuiOptionSlider extends GuiButton {

    private static Field sliderValueField;
    private static Field draggingField;
    private static Field optionsField;

    static {
        try {
            sliderValueField = ReflectionHelper.findField(GuiOptionSlider.class, "sliderValue", "field_146134_p");
            sliderValueField.setAccessible(true);
        } catch (Throwable ignored) {}
        try {
            draggingField = ReflectionHelper.findField(GuiOptionSlider.class, "dragging", "field_146135_q");
            draggingField.setAccessible(true);
        } catch (Throwable ignored) {}
        try {
            optionsField = ReflectionHelper.findField(GuiOptionSlider.class, "options", "field_146133_q");
            optionsField.setAccessible(true);
        } catch (Throwable ignored) {}
    }

    public MixinGuiOptionSlider(int buttonId, int x, int y, String buttonText) {
        super(buttonId, x, y, buttonText);
    }

    /**
     * Updates option slider value when dragging without drawing vanilla widgets.png knob texture
     */
    @Inject(method = "mouseDragged", at = @At("HEAD"), cancellable = true)
    protected void onMouseDragged(Minecraft mc, int mouseX, int mouseY, CallbackInfo ci) {
        GuiOptionSlider self = (GuiOptionSlider) (Object) this;
        if (self.visible) {
            boolean dragging = false;
            try {
                if (draggingField != null) dragging = draggingField.getBoolean(self);
            } catch (Throwable ignored) {}

            if (dragging) {
                float sliderValue = (float) (mouseX - (self.xPosition + 4)) / (float) (self.width - 8);
                sliderValue = MathHelper.clamp_float(sliderValue, 0.0F, 1.0F);
                try {
                    if (optionsField != null) {
                        GameSettings.Options options = (GameSettings.Options) optionsField.get(self);
                        if (options != null) {
                            float f = options.denormalizeValue(sliderValue);
                            mc.gameSettings.setOptionFloatValue(options, f);
                            sliderValue = options.normalizeValue(f);
                            self.displayString = mc.gameSettings.getKeyBinding(options);
                        }
                    }
                    if (sliderValueField != null) {
                        sliderValueField.setFloat(self, sliderValue);
                    }
                } catch (Throwable ignored) {}
            }
            ci.cancel();
        }
    }
}
