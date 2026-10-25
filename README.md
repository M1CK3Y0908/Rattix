# Rattix

Rattix Client - A customized Forge 1.8.9 Client.

### **UPDATE LOG 102526**

[#]Fix **Custom Main Menu Launcher Disappearance Bug** Resolved the critical issue where custom main menu was displayed in IDEA but disappeared in production launchers (HMCL/PCL/official launcher) by fixing Gradle build refmap overwrite and adding dual Forge/Mixin screen handoffs
[#]Fix **Gradle Refmap Overwrite Pipeline** Updated `build.gradle` so `compileJava.dependsOn processResources` runs first, excluded `mixins.rattix.refmap.json` from `processResources` copy task so stale source files no longer wipe out annotation processor output, and synced generated refmaps back into resources
[#]Fix **Obfuscated Refmap Generation** Correctly generated Searge (`func_147108_a`, `func_73866_w_`) and Notch (`a(Laxu;)V`, `b()V`) refmap mappings for `MixinMinecraft.displayGuiScreen` and `MixinGuiMainMenu.initGui`, eliminating silent `require = 0` drops in production runtimes
[#]Add **Forge GuiOpenEvent Menu Fallback** Added redundant screen replacement in `RattixMod.onGuiOpen`, intercepting `GuiMainMenu` instantiation on the native Forge event bus and redirecting to `RattixMainMenu`, providing 100% fail-safe display across diverse launcher environments and OptiFine setups
[#]Fix **Main Menu Escape Key Handling** Prevented redundant screen reconstruction loops on pressing Escape within `RattixMainMenu`, matching vanilla main menu keyboard semantics
[#]Update **Event Lifecycle Compatibility** Extended `onDrawScreenPost`, `onMouseInput`, and `onKeyboardInput` event subscriptions to explicitly recognize `RattixMainMenu` for seamless module count rendering and shader mouse-wheel switching
[#]Add **Standalone RattixMainMenu Screen** Added dedicated GUI with its own button lifecycle, action dispatch, Escape handling, Shader background integration, and Augustus-inspired rendering without relying on vanilla menu internals
[#]Add **Augustus-Inspired Main Menu Surface** Replaced the vanilla main-menu layout with a custom dark launcher surface featuring a left navigation rail, Rattix branding card, action buttons, version footer, and translucent shader-compatible panels
[#]Refactor **Fluent Design Button States** Reworked GuiButton rendering with Fluent-style low-contrast surfaces, hover reveal overlays, 1px accent borders, disabled colors, rounded 4px geometry, and bottom focus indicators while preserving vanilla interaction dispatch
[#]Update **Fluent Slider Controls** Restyled Forge option sliders with a neutral track and accent thumb consistent with the Fluent control visual language
[#]Add **Fluent TextField Styling** Implemented rounded card styling and active focus borders for GuiTextField controls
[#]Fix **OptiFine Button Rendering Compatibility** Added a generic GuiScreen tail pass that reapplies Win11/UWP button styling after vanilla or OptiFine button rendering while preserving original click behavior
[#]Add **Shared ShaderBackgroundRenderer Architecture** Implemented centralized `ShaderBackgroundRenderer` pipeline connecting `ShaderManager` with all non-in-game GUI screens, featuring automated resolution/mouse coordinate calculations and a 150-microsecond per-frame deduplication protection to eliminate state conflicts and duplicate rendering
[#]Fix **OptiFine GUI Black Screen Compatibility** Added OptiFine detection and switched shader backgrounds to the direct-screen OpenGL path instead of the secondary FBO path, avoiding OptiFine framebuffer rebinding conflicts
[#]Fix **Shader Failure Vanilla Fallback** GUI background injections now cancel vanilla rendering only after a linked shader program is confirmed; shader initialization/render failures automatically fall back to the normal Minecraft background instead of leaving a black screen
[#]Fix **GuiDefaultBackground Shader Replacement** Added a cancellable `drawDefaultBackground` injection to `MixinGuiScreen`, replacing the vanilla dirt texture used by 1.8.9 options, video settings, language, controls, resource-pack, and other settings screens with the active custom GLSL shader background
[#]Fix **Non-Main-Menu Shader Coverage** Forced a fresh shader render at the default-background interception point, ensuring screens that bypass `drawBackground`/`drawWorldBackground` no longer fall back to `options_background.png`
[#]Fix **Refmap Binding & Non-Blocking Injections in GuiSlot** Replaced `@Overwrite` in `MixinGuiSlot` with `@Inject` on `drawScreen(HEAD)`, `drawContainerBackground`, `overlayBackground`, and `drawSelectionBox`, ensuring 100% Searge/Notch refmap binding; completely suppressed Forge container dirt quads and header/footer dirt overlays
[#]Add **Hardware Viewport Scissoring for Scroll Lists** Scoped list entry rendering (`drawSelectionBox`) within `RenderUtil.enableScissor(left, top, width, bottom - top)` in `MixinGuiSlot`, guaranteeing scrollable entries remain perfectly clipped within their containers without relying on vanilla dirt overlay bars
[#]Deploy **Production Reobfuscated Build** Recompiled and validated 100% clean compilation via `./gradlew build`, producing verified release artifact `build/libs/Rattix-1.0.0.jar`

### **UPDATE LOG 102426(3)**

[#]Add **Shared ShaderBackgroundRenderer Architecture** Implemented centralized `ShaderBackgroundRenderer` pipeline connecting `ShaderManager` with all non-in-game GUI screens, featuring automated resolution/mouse coordinate calculations and a 150-microsecond per-frame deduplication protection to eliminate state conflicts and duplicate rendering
[#]Fix **Global Background Interception in GuiScreen** Injected `ShaderBackgroundRenderer.renderCurrentScreen` directly into `MixinGuiScreen.onDrawBackground` and `onDrawWorldBackground`, cleanly replacing vanilla dirt textures across options, video settings, language, disconnect, and connection screens
[#]Fix **Refmap Binding & Non-Blocking Injections in GuiSlot** Replaced `@Overwrite` in `MixinGuiSlot` with `@Inject` on `drawScreen(HEAD)`, `drawContainerBackground`, `overlayBackground`, and `drawSelectionBox`, ensuring 100% Searge/Notch refmap binding; completely suppressed Forge container dirt quads and header/footer dirt overlays
[#]Fix **Multiplayer Server & Singleplayer World List Visuals** Bound direct shader invocation to `GuiSlot.drawScreen` HEAD, ensuring scrolling list screens (`GuiMultiplayer`, `GuiSelectWorld`, `GuiScreenResourcePacks`, `GuiControls`) render the vibrant full-screen shader underneath transparent scissored entries
[#]Fix **World Loading Screen Refmap & Shader Integration** Migrated `MixinLoadingScreenRenderer.setLoadingProgress` from `@Overwrite` to `@Inject(HEAD, cancellable)`, successfully generating Searge `func_73718_a` and Notch `a(I)V` mappings and rendering the un-dimmed shader with modern progress bar
[#]Remove **Background Dimming & Darkening Overlays** Completely eliminated all darkening and tint overlays (`0x88000000` in `MixinGuiScreen` and `MixinLoadingScreenRenderer`, plus `0x55000000` bottom gradient in `MixinGuiMainMenu`), presenting vibrant, crystal-clear GLSL shader visuals without post-processing dimming
[#]Add **Hardware Viewport Scissoring for Scroll Lists** Scoped list entry rendering (`drawSelectionBox`) within `RenderUtil.enableScissor(left, top, width, bottom - top)` in `MixinGuiSlot`, guaranteeing scrollable entries remain perfectly clipped within their containers without relying on vanilla dirt overlay bars
[#]Deploy **Production Reobfuscated Build** Recompiled and validated 100% clean compilation via `./gradlew build`, producing verified release artifact `build/libs/Rattix-1.0.0.jar`

### **UPDATE LOG 102426(2)**

[#]Refactor **1:1 LiquidBounce Module Architecture** Realigned all modules under their respective LiquidBounce category packages (`movement.ToggleSprint`, `render.ClickGUI`, `render.Ambiance`, `render.Font`, `render.Fullbright`, etc.) and created category packages for `combat`, `exploit`, `fun`, `misc`, `player`, and `world`
[#]Refactor **Full 13 Root Package Parity** Fully aligned top-level package architecture across `api`, `cape`, `chat`, `config`, `event`, `features`, `file`, `injection`, `lang`, `script`, `tabs`, `ui`, and `utils` matching LiquidBounce-b100
[#]Refactor **Subpackage Structure Alignment** Aligned `features/command` (`commands`, `shortcuts`, `special`), `features/special`, `file/configs/models`, `ui/client`, `ui/elements`, `ui/font`, `utils/client`, `utils/inventory`, `utils/movement`, `utils/render/animation`, `utils/render/shader`, `utils/timing`, and `utils/ui`
[#]Refactor **Event Bus Parity** Realigned event system with LiquidBounce standard by organizing event classes directly in `client.m1ck3y.rattix.event` alongside `event.async` (matching LiquidBounce `Events.kt` architecture)
[#]Add **LiquidBounce Standard Config & Value System** Integrated `client.m1ck3y.rattix.config` providing `Value`, `Values` (`BoolValue`, `IntegerValue`, `FloatValue`, `ListValue`, `TextValue`), `Configurable`, and `SettingsUtils`
[#]Update **Developer Guide & API Docs** Synchronized `docs/MODULE_DEVELOPMENT_GUIDE.md` and all documentation with the categorized module structure and updated package paths
[#]Fix **Source File Encoding & Compilation** Purged all UTF-8 BOM headers (`\ufeff`) across source files, verifying 100% clean compilation via ForgeGradle (`BUILD SUCCESSFUL`)

### **UPDATE LOG 102426(1)**

[#]Refactor **LiquidBounce Directory Architecture Parity** Realigned Rattix package hierarchy with LiquidBounce-b100 standard architecture: introducing `features.command`, `features.module`, `features.special`, `file`, `file.configs`, `ui.font`, `ui.client`, `utils.render`, and `utils.render.shader`
[#]Refactor **Module Architecture** Reorganized all modules directly into `features.module.modules` (flat structure without category subpackages) while preserving runtime `Category` classification (`COMBAT`, `PLAYER`, `MOVEMENT`, `RENDER`/`VISUAL`, `WORLD`, `MISC`, `EXPLOIT`, `FUN`, `THEME`)
[#]Refactor **Dynamic Module Discovery** Enhanced `ModuleManager` with recursive directory reflection scanning across all subpackages and updated fallback registry
[#]Refactor **File & Config Architecture** Migrated `FileManager` to `client.m1ck3y.rattix.file` and configurations (`ClickGuiConfig`, `ConfigManager`) to `client.m1ck3y.rattix.file.configs`
[#]Refactor **Font & UI Architecture** Migrated font rendering and utilities (`FontUtil`, `CustomFontRenderer`, `GlobalFontRenderer`) to `client.m1ck3y.rattix.ui.font`
[#]Refactor **Render & Shader Architecture** Structured rendering helpers and dynamic shaders under `client.m1ck3y.rattix.utils.render` and `client.m1ck3y.rattix.utils.render.shader`
[+]Add **LiquidBounce Parity Subsystems** Added architectural packages and managers for `features.special` (`ClientFixes`, `AntiForge`, `BungeeCordSpoof`), `cape` (`CapeService`), `chat` (`ClientChat`), `lang` (`LanguageManager`), and `tabs` (`RattixTab`)
[#]Update **API & Documentation** Fully updated `RattixAPI`, mixins, and `docs/MODULE_DEVELOPMENT_GUIDE.md` to reflect the LiquidBounce architecture

### **UPDATE LOG 101826(4)**

[-]Remove **TimeChanger** Completely removed legacy TimeChanger module
[+]Add **Ambiance Module** Added Augustus-style Ambiance module under Visual category
[+]Add **Time & Speed Control** Customizable world time (0 - 24000) and smooth time flow multiplier (0.0x - 20.0x)
[+]Add **Weather Controls** Independent controls for Rain, Snow, and Thunder atmosphere
[+]Add **Snow Effect** Temperature redirection in weather rendering forcing snow particles and textures
[+]Add **Custom World Color** Full RGB world lightmap tinting affecting terrain, blocks, and entity lighting
[+]Add **Custom Fog Color** Real-time fog color override hooking into Forge EntityViewRenderEvent.FogColors
[+]Add **Custom Fog Distance** Precise fog distance slider (10 - 500) overriding OpenGL fog coordinates
[+]Add **MixinEntityRenderer** Mixin injection for lightmap, fog, and weather pipeline
[*]Update **Category & ModuleManager** Updated Visual category and fallback module registry with Ambiance

### **UPDATE LOG 101826(3)**

[*]Refactor **Package Structure** Renamed `modules` folder and package to `module` matching standard client hierarchy
[*]Refactor **Module Location** Moved all feature modules into dedicated `client.m1ck3y.rattix.module.modules` subpackage
[*]Refactor **Architecture** Decoupled base class (`Module`) from static management bus (`ModuleManager`)
[+]Add **ModuleManager** Central module controller handling reflection discovery, lifecycle dispatch, and state tracking
[+]Add **ModuleCommand** Added `.module` (`.mod`, `.m`) chat command with list, info, toggle, bind, set, and reset subcommands
[*]Update **API System** Updated `RattixAPI` facade to interface seamlessly with `Module` and `ModuleManager`
[*]Refactor **Config & System** Migrated `ClickGuiConfig`, `RattixMod`, Mixins, and Font utilities to `module` package
[*]Fix **BOM Encoding** Removed UTF-8 BOM bytes from source files to ensure standard `javac` compilation compatibility

### **UPDATE LOG 101826(2)**

[+]Add **API System** Public API entrypoint (RattixAPI) for third-party Forge mods to register categories and modules
[+]Add **Extensible Categories** Refactored Category to a dynamic registry allowing external mods to create custom tabs
[+]Add **SVG Icon Registry** Added SvgIconHelper.registerSvg for runtime vector SVG rasterization
[+]Add **Dynamic New Tab** ClickGUI New Tab menu (+) dynamically adapts to newly registered categories in real time
[+]Add **Documentation** Created comprehensive developer specification guide (docs/MODULE_DEVELOPMENT_GUIDE.md)
[+]Add **Resolution Setting** 0% - 200% (Default 100%) with FBO bilinear scaling and supersampling
[+]Add **FPS Limit** 0 - 600 FPS (0 = VSync) with frame interval pacing and cached FBO blit reuse
[+]Add **Animation Speed** 0.0x - 10.0x (Default 1.0x) with delta time accumulation and alt-tab freeze protection
[+]Add **Shader Settings Dialog** Fluent popup dialog (⚙) on main menu with interactive drag sliders and reset defaults
[+]Add **MenuShader Sync** Two-way synchronization between ClickGUI MenuShader module and ShaderManager
[*]Fix **Launch Crash** Fixed NoSuchMethodError: Minecraft.getMinecraft() by finalizing jar task with Searge reobfJar
[*]Fix **IMixinService Error** Injected mod jar into AppClassLoader dynamically in RattixLoadingPlugin
[*]Fix **OptiFine Compatibility** Changed drawSplashScreen overwrite to @Inject(at = HEAD) to prevent mod conflicts
[*]Fix **MixinGuiScreen** Replaced invalid shadow fields (width/height/mc) with safe parent instance access
[*]Fix **Refmap Mappings** Completed all Searge and Notch obfuscation mappings in mixins.rattix.refmap.json
[*]Fix **MixinProgressBar** Changed ProgressBar target to class literal to resolve public target warning
[*]Fix **Periodic Lag Spike** Eliminated periodic frame drops by removing frequent GC allocations and temporary objects

### **UPDATE LOG 101826(1)**

[+]Add **Background Shader** Augustus-style dynamic GLSL background shader system (40+ presets)
[+]Add **Shader Switcher** Interactive capsule switcher (< >), active shader name display, and search dialog (≡)
[+]Add **Startup Sound** Async audio player playing welcome_to_rattix.wav on entering main menu
[+]Add **Volume Control** Synced startup sound volume directly with Minecraft Master Volume
[+]Add **Save Defaults** Button action to save current parameters as default parameters with reset confirmation
[*]Refactor **Loaded Info** Moved loaded stats to top-right: "xxx modules and xxx settings loaded"
[*]Extend **Dark Theme** Applied dynamic shader and modern black ricon theme to multiplayer, options, terrain, and loading screens
[-]Remove **Color Preview** Removed redundant floating preview box in ColorSetting for a cleaner layout

### **UPDATE LOG 090626(1)**

- [*]Refactor **Package** Renamed root package to `client.m1ck3y.rattix` and updated `build.gradle`
- [*]Refactor **ClickGUI** Restructured ClickGUI and components into `client.m1ck3y.rattix.module.clickgui`
- [+]Add **Theme** Added Theme category and SVG icon in ClickGUI navigation
- [*]Fix **ClickGUI** Fixed expand menu layout offset calculation when Theme category is displayed
- [*]Refactor **Font** Separated Font management from Module system into dedicated font controller
- [+]Add **Font System** Custom TrueType Font renderer with outline, custom shadow, and scaling
- [*]Fix **SvgIconHelper** Fixed quadratic bezier path calculation syntax error
- [+]Add **Modules** Complete implementation of Combat, Movement, Render, Player, and HUD modules