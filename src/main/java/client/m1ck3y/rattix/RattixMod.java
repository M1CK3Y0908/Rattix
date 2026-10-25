package client.m1ck3y.rattix;

import client.m1ck3y.rattix.features.command.CommandManager;
import client.m1ck3y.rattix.file.configs.ConfigManager;
import client.m1ck3y.rattix.event.EventManager;
import client.m1ck3y.rattix.event.KeyEvent;
import client.m1ck3y.rattix.event.Render2DEvent;
import client.m1ck3y.rattix.event.TickEvent;
import client.m1ck3y.rattix.features.module.Module;
import client.m1ck3y.rattix.features.module.ModuleManager;
import client.m1ck3y.rattix.features.module.modules.ClickGUI;
import client.m1ck3y.rattix.script.ScriptManager;
import client.m1ck3y.rattix.file.FileManager;
import client.m1ck3y.rattix.ui.font.FontUtil;
import client.m1ck3y.rattix.utils.render.IconUtils;
import client.m1ck3y.rattix.utils.client.SoundUtil;
import client.m1ck3y.rattix.ui.client.StartupScreen;
import client.m1ck3y.rattix.ui.client.RattixMainMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;

import client.m1ck3y.rattix.ui.elements.FluentGuiButton;
import client.m1ck3y.rattix.utils.render.Win11UiRenderer;
import client.m1ck3y.rattix.utils.ui.GuiScreenBridge;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

@Mod(
    modid = RattixMod.MODID,
    name = RattixMod.NAME,
    version = RattixMod.VERSION,
    acceptedMinecraftVersions = "[1.8.9]",
    clientSideOnly = true
)
public class RattixMod {
    public static final String MODID = "rattix";
    public static final String NAME = "Rattix";
    public static final String VERSION = "1.0.0";

    @Mod.Instance(MODID)
    public static RattixMod instance;

    private static volatile boolean clientStarted = false;
    private static long lastRenderNano = 0;
    public static KeyBinding clickGuiKey;
    private static boolean rshiftPressed = false;

    public static synchronized void startClient() {
        if (clientStarted) return;
        clientStarted = true;

        System.out.println("[Rattix] Initializing Rattix Client v" + VERSION + "...");
        try {
            StartupScreen.draw("Initializing File Manager & Fonts...", 0.30f);
            FileManager.init();
            FontUtil.init();

            StartupScreen.draw("Loading Configurations...", 0.50f);
            ConfigManager.getInstance().init();
            client.m1ck3y.rattix.utils.render.shader.ShaderManager.getInstance();

            StartupScreen.draw("Loading Scripts...", 0.70f);
            ScriptManager.getInstance().init();

            StartupScreen.draw("Registering Modules & Categories...", 0.90f);
            ModuleManager.registerAll();

            if (instance == null) {
                instance = new RattixMod();
            }
            if (clickGuiKey == null) {
                clickGuiKey = new KeyBinding("key.rattix.clickgui", Keyboard.KEY_RSHIFT, "key.categories.rattix");
                try {
                    ClientRegistry.registerKeyBinding(clickGuiKey);
                } catch (Throwable ignored) {
                }
            }
            try {
                MinecraftForge.EVENT_BUS.register(instance);
                FMLCommonHandler.instance().bus().register(instance);
            } catch (Throwable ignored) {
            }

            updateTitleAndIcon();
            StartupScreen.draw("Rattix Client Ready", 1.0f);
            System.out.println("[Rattix] Client initialization completed successfully. Loaded "
                    + ModuleManager.getLoadedModuleCount() + " modules and "
                    + ModuleManager.getLoadedOptionCount() + " settings.");
        } catch (Throwable t) {
            System.err.println("[Rattix] Error during client startup:");
            t.printStackTrace();
        }
    }

    public static void stopClient() {
        try {
            System.out.println("[Rattix] Stopping Rattix Client...");
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        StartupScreen.setupSplashProgress();
        StartupScreen.stopForgeSplash();

        ModMetadata meta = event.getModMetadata();
        meta.autogenerated = false;
        meta.modId = MODID;
        meta.name = NAME;
        meta.version = VERSION;
        meta.description = "Rattix Client - A sleek and powerful CloudClient-style Forge 1.8.9 Mod.";
        meta.authorList = Collections.singletonList("Rattix Team");
        meta.credits = "Built with Minecraft Forge 1.8.9";

        updateTitleAndIcon();
        StartupScreen.draw("Starting Rattix Client...", 0.15f);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        startClient();
    }

    public static void updateTitleAndIcon() {
        try {
            if (Display.isCreated()) {
                Display.setTitle(NAME);
                IconUtils.initLwjglIcon();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static void setWindowIcon() {
        IconUtils.initLwjglIcon();
    }

    public static void handleKey(int key) {
        if (key == 0) return;
        if (!EventManager.getInstance().isEmpty()) {
            EventManager.getInstance().call(new KeyEvent(key));
        }

        Module clickGui = ModuleManager.getModule(ClickGUI.class);
        int guiKey = (clickGui != null && clickGui.getKeyCode() != 0) ? clickGui.getKeyCode() : Keyboard.KEY_RSHIFT;
        int bindKey = (clickGuiKey != null) ? clickGuiKey.getKeyCode() : 0;

        if (key == Keyboard.KEY_RSHIFT || (guiKey != 0 && key == guiKey) || (bindKey != 0 && key == bindKey)) {
            rshiftPressed = true;
            openClickGui();
        } else {
            ModuleManager.onKey(key);
        }
    }

    public static void handleTick() {
        if (!clientStarted) {
            startClient();
        }
        if (!EventManager.getInstance().isEmpty()) {
            EventManager.getInstance().call(new TickEvent());
        }
        if (!NAME.equals(Display.getTitle())) {
            Display.setTitle(NAME);
        }
        ModuleManager.onClientTick();
        FontUtil.ensureGlobalFontRenderer();

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld != null && mc.thePlayer != null) {
            Module clickGui = ModuleManager.getModule(ClickGUI.class);
            int guiKey = (clickGui != null && clickGui.getKeyCode() != 0) ? clickGui.getKeyCode() : Keyboard.KEY_RSHIFT;
            boolean isDown = Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)
                    || (guiKey > 0 && guiKey < Keyboard.KEYBOARD_SIZE && Keyboard.isKeyDown(guiKey));
            if (mc.currentScreen != null || ClickGUI.wasRecentlyClosed()) {
                rshiftPressed = isDown;
            } else {
                if (isDown && !rshiftPressed) {
                    openClickGui();
                }
                rshiftPressed = isDown;
            }
        }
    }

    public static void handleRender2D(float partialTicks) {
        if (!clientStarted) {
            startClient();
        }
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null || mc.thePlayer == null || mc.gameSettings.showDebugInfo) {
            return;
        }

        long now = System.nanoTime();
        if (now - lastRenderNano < 500_000L) {
            return; // Throttle to prevent duplicate draw calls in the same frame
        }
        lastRenderNano = now;

        // Ensure 2D projection matrix and clean OpenGL state for HUD overlay
        mc.entityRenderer.setupOverlayRendering();
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableLighting();
        GlStateManager.enableTexture2D();

        if (!EventManager.getInstance().isEmpty()) {
            EventManager.getInstance().call(new Render2DEvent(partialTicks));
        }
        ModuleManager.onRenderOverlay();

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableBlend();
        GlStateManager.popMatrix();
    }

    public static void openClickGui() {
        try {
            ClickGUI.open();
        } catch (Throwable t) {
            System.err.println("[Rattix] Failed to open ClickGUI:");
            t.printStackTrace();
        }
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (Keyboard.getEventKeyState()) {
            int key = Keyboard.getEventKey();
            if (key == 0) {
                key = Keyboard.getEventCharacter() + 256;
            }
            while (this.clickGuiKey != null && this.clickGuiKey.isPressed()) {
                // Drain accumulated pressTime
            }
            handleKey(key);
        }
    }

    @SubscribeEvent
    public void onClientTick(net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent event) {
        if (event.phase == net.minecraftforge.fml.common.gameevent.TickEvent.Phase.END) {
            handleTick();
        }
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type == RenderGameOverlayEvent.ElementType.ALL) {
            handleRender2D(event.partialTicks);
        }
    }

    @SubscribeEvent
    public void onGuiOpen(net.minecraftforge.client.event.GuiOpenEvent event) {
        StartupScreen.onStartupComplete();
        if (event.gui instanceof GuiMainMenu && !(event.gui instanceof RattixMainMenu)) {
            event.gui = new RattixMainMenu();
            SoundUtil.playStartupSound();
        }
    }

    private final Map<GuiScreen, Map<GuiButton, Boolean>> hiddenButtonsMap = new WeakHashMap<>();

    @SubscribeEvent(priority = net.minecraftforge.fml.common.eventhandler.EventPriority.HIGHEST)
    public void onDrawScreenPre(GuiScreenEvent.DrawScreenEvent.Pre event) {
        if (event.gui == null) return;
        // If Mixin has hooked GuiButton and is actively drawing, let Mixin handle it natively
        if (Win11UiRenderer.isMixinActive()) return;

        // Fail-safe fallback for external launchers / OptiFine:
        // Temporarily suppress vanilla widgets.png button rendering during GuiScreen.drawScreen
        List<GuiButton> buttons = GuiScreenBridge.getButtonList(event.gui);
        if (buttons == null || buttons.isEmpty()) return;

        Map<GuiButton, Boolean> visibilityMap = new HashMap<>();
        for (GuiButton btn : buttons) {
            if (btn != null && !(btn instanceof FluentGuiButton)) {
                visibilityMap.put(btn, btn.visible);
                btn.visible = false;
            }
        }
        if (!visibilityMap.isEmpty()) {
            hiddenButtonsMap.put(event.gui, visibilityMap);
        }
    }

    @SubscribeEvent(priority = net.minecraftforge.fml.common.eventhandler.EventPriority.LOWEST)
    public void onDrawScreenPost(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (event.gui == null) return;

        // Restore suppressed button visibility and render with Win11 Fluent styling
        Map<GuiButton, Boolean> visibilityMap = hiddenButtonsMap.remove(event.gui);
        if (visibilityMap != null && !visibilityMap.isEmpty()) {
            Minecraft mc = Minecraft.getMinecraft();
            for (Map.Entry<GuiButton, Boolean> entry : visibilityMap.entrySet()) {
                GuiButton btn = entry.getKey();
                boolean wasVisible = entry.getValue();
                btn.visible = wasVisible;
                if (wasVisible) {
                    Win11UiRenderer.drawFluentButton(btn, mc, event.mouseX, event.mouseY);
                    if (Win11UiRenderer.isSliderButton(btn)) {
                        GuiScreenBridge.mouseDragged(btn, mc, event.mouseX, event.mouseY);
                    }
                }
            }
        }

        if (event.gui instanceof GuiMainMenu || event.gui instanceof RattixMainMenu) {
            if (!SoundUtil.hasPlayedStartupSound()) {
                SoundUtil.playStartupSound();
            }
            Minecraft mc = Minecraft.getMinecraft();
            if (mc != null && mc.fontRendererObj != null) {
                String text = ModuleManager.getLoadedModuleCount() + " modules and " + ModuleManager.getLoadedOptionCount() + " settings loaded";
                int textWidth = mc.fontRendererObj.getStringWidth(text);
                int x = event.gui.width - textWidth - 2;
                int y = 2;
                event.gui.drawString(mc.fontRendererObj, text, x, y, -1);
            }
        }
    }

    @SubscribeEvent
    public void onMouseInput(GuiScreenEvent.MouseInputEvent.Pre event) {
        if (event.gui instanceof GuiMainMenu || event.gui instanceof RattixMainMenu) {
            int dWheel = org.lwjgl.input.Mouse.getEventDWheel();
            if (dWheel != 0) {
                Minecraft mc = Minecraft.getMinecraft();
                int mouseX = org.lwjgl.input.Mouse.getEventX() * event.gui.width / Math.max(1, mc.displayWidth);
                int mouseY = event.gui.height - org.lwjgl.input.Mouse.getEventY() * event.gui.height / Math.max(1, mc.displayHeight) - 1;
                if (event.gui instanceof RattixMainMenu) {
                    RattixMainMenu rmm = (RattixMainMenu) event.gui;
                    if (rmm.getWindowManager() != null && rmm.getWindowManager().isMouseOverAnyWindow(mouseX, mouseY)) {
                        return;
                    }
                }
                if (client.m1ck3y.rattix.utils.render.shader.ShaderManager.getInstance().handleMouseWheel(dWheel, mouseX, mouseY)) {
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public void onKeyboardInput(GuiScreenEvent.KeyboardInputEvent.Pre event) {
        if (event.gui instanceof GuiMainMenu || event.gui instanceof RattixMainMenu) {
            if (Keyboard.getEventKeyState()) {
                char c = Keyboard.getEventCharacter();
                int k = Keyboard.getEventKey();
                if (k == 0) k = c + 256;
                if (client.m1ck3y.rattix.utils.render.shader.ShaderManager.getInstance().keyTyped(c, k)) {
                    event.setCanceled(true);
                }
            }
        }
    }

    public CommandManager getCommandManager() {
        return CommandManager.getInstance();
    }

    public EventManager getEventManager() {
        return EventManager.getInstance();
    }
}
