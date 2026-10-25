package client.m1ck3y.rattix.utils.ui;

import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;
import java.lang.reflect.Method;

/**
 * GuiScreenBridge - Safely accesses protected input methods of GuiScreen across
 * MCP, Searge, and Notch obfuscated environments without fragile refmaps.
 */
public final class GuiScreenBridge {
    private static Method mouseClickedMethod;
    private static Method mouseReleasedMethod;
    private static Method mouseClickMoveMethod;
    private static Method keyTypedMethod;

    static {
        for (Method m : GuiScreen.class.getDeclaredMethods()) {
            String name = m.getName();
            Class<?>[] params = m.getParameterTypes();

            if ((name.equals("mouseClicked") || name.equals("func_73864_a") || name.equals("a"))
                    && params.length == 3 && params[0] == int.class && params[1] == int.class && params[2] == int.class) {
                m.setAccessible(true);
                mouseClickedMethod = m;
            } else if ((name.equals("mouseReleased") || name.equals("func_146286_b") || name.equals("b"))
                    && params.length == 3 && params[0] == int.class && params[1] == int.class && params[2] == int.class) {
                m.setAccessible(true);
                mouseReleasedMethod = m;
            } else if ((name.equals("mouseClickMove") || name.equals("func_146273_a") || name.equals("a"))
                    && params.length == 4 && params[0] == int.class && params[1] == int.class && params[2] == int.class && params[3] == long.class) {
                m.setAccessible(true);
                mouseClickMoveMethod = m;
            } else if ((name.equals("keyTyped") || name.equals("func_73869_a") || name.equals("a"))
                    && params.length == 2 && params[0] == char.class && params[1] == int.class) {
                m.setAccessible(true);
                keyTypedMethod = m;
            }
        }
    }

    private GuiScreenBridge() {}

    public static void mouseClicked(GuiScreen screen, int mouseX, int mouseY, int mouseButton) throws IOException {
        if (screen == null || mouseClickedMethod == null) return;
        try {
            mouseClickedMethod.invoke(screen, mouseX, mouseY, mouseButton);
        } catch (Throwable t) {
            if (t.getCause() instanceof IOException) {
                throw (IOException) t.getCause();
            }
            t.printStackTrace();
        }
    }

    public static void mouseReleased(GuiScreen screen, int mouseX, int mouseY, int state) {
        if (screen == null || mouseReleasedMethod == null) return;
        try {
            mouseReleasedMethod.invoke(screen, mouseX, mouseY, state);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static void mouseClickMove(GuiScreen screen, int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (screen == null || mouseClickMoveMethod == null) return;
        try {
            mouseClickMoveMethod.invoke(screen, mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static void keyTyped(GuiScreen screen, char typedChar, int keyCode) throws IOException {
        if (screen == null || keyTypedMethod == null) return;
        try {
            keyTypedMethod.invoke(screen, typedChar, keyCode);
        } catch (Throwable t) {
            if (t.getCause() instanceof IOException) {
                throw (IOException) t.getCause();
            }
            t.printStackTrace();
        }
    }
}
