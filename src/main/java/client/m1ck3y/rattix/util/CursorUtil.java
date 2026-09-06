package client.m1ck3y.rattix.util;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

public class CursorUtil {
    public static final int CURSOR_ARROW = 0;
    public static final int CURSOR_SIZE_H = 1;     // ↔ IDC_SIZEWE
    public static final int CURSOR_SIZE_V = 2;     // ↕ IDC_SIZENS
    public static final int CURSOR_SIZE_NWSE = 3;  // ↖↘ IDC_SIZENWSE
    public static final int CURSOR_SIZE_NESW = 4;  // ↗↙ IDC_SIZENESW

    private interface User32 extends Library {
        User32 INSTANCE = (User32) Native.loadLibrary("user32", User32.class);
        Pointer LoadCursorA(Pointer hInstance, int lpCursorName);
        Pointer SetCursor(Pointer hCursor);
    }

    private static boolean initialized = false;
    private static boolean available = false;
    private static Pointer hArrow;
    private static Pointer hSizeWE;
    private static Pointer hSizeNS;
    private static Pointer hSizeNWSE;
    private static Pointer hSizeNESW;
    private static int currentCursorType = -1;

    private static void init() {
        if (initialized) return;
        initialized = true;
        try {
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                hArrow = User32.INSTANCE.LoadCursorA(null, 32512);     // IDC_ARROW
                hSizeWE = User32.INSTANCE.LoadCursorA(null, 32644);    // IDC_SIZEWE
                hSizeNS = User32.INSTANCE.LoadCursorA(null, 32645);    // IDC_SIZENS
                hSizeNWSE = User32.INSTANCE.LoadCursorA(null, 32642);  // IDC_SIZENWSE
                hSizeNESW = User32.INSTANCE.LoadCursorA(null, 32643);  // IDC_SIZENESW
                available = true;
            }
        } catch (Throwable t) {
            available = false;
        }
    }

    public static void setCursor(int type) {
        if (type == currentCursorType) return;
        init();
        if (!available) return;
        currentCursorType = type;
        Pointer h = getCursorPointer(type);
        if (h != null) {
            try {
                User32.INSTANCE.SetCursor(h);
            } catch (Throwable ignored) {}
        }
    }

    private static Pointer getCursorPointer(int type) {
        switch (type) {
            case CURSOR_SIZE_H: return hSizeWE;
            case CURSOR_SIZE_V: return hSizeNS;
            case CURSOR_SIZE_NWSE: return hSizeNWSE;
            case CURSOR_SIZE_NESW: return hSizeNESW;
            default: return hArrow;
        }
    }

    public static void resetCursor() {
        setCursor(CURSOR_ARROW);
    }
}
