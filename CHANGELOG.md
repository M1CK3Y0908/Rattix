### **CHANGELOG 102526(2)**

[#]Add **Windowed Secondary Page Architecture (ClickGUI Style)** Reworked all client secondary subpages (Singleplayer, Multiplayer, Options, Client Settings, Modules) into floating, draggable, resizable, maximizable, and minimizable desktop windows (`SubpageWindow`, `WindowManager`) matching ClickGUI's design patterns and multi-window focus architecture
[#]Add **8-Directional Window Resizing & Windows Hardware Cursors** Implemented smooth 8-way edge and corner window resizing with dynamic border detection (`RESIZE_N`, `RESIZE_S`, `RESIZE_W`, `RESIZE_E`, `RESIZE_NW`, `RESIZE_NE`, `RESIZE_SW`, `RESIZE_SE`) and native Windows cursor switching (`CursorUtil.setCursor`) using IDC hardware cursors (`CURSOR_SIZE_H`, `CURSOR_SIZE_V`, `CURSOR_SIZE_NWSE`, `CURSOR_SIZE_NESW`)
[#]Add **Window Controls & Desktop Taskbar Docking** Added Win11 fluent window control buttons (Minimize `-`, Maximize/Restore `□`/`❐`, Close `×`) with hover reveals; minimized windows dock smoothly onto the bottom desktop taskbar as compact interactive chips that can be restored with a single click or closed directly
[#]Add **Deep Screen History Navigation Stack & Seamless In-Window Flow** Implemented an internal `Stack<GuiScreen>` inside `SubpageWindow` allowing infinite nested subpage navigation (e.g. `GuiOptions` -> `GuiVideoSettings` -> `GuiDetailSettings` or `GuiSelectWorld` -> `GuiCreateWorld` -> `GuiCustomizeWorldScreen`) inside the floating window; clicking Cancel or pressing Escape pops parent screens naturally without disrupting the desktop canvas
[#]Add **Seamless Game Launch Full-Screen Transition** Configured `WindowManager.onDisplayGuiScreen` and `MixinMinecraft.displayGuiScreen` to recognize world loading and server connection screens (`GuiDownloadTerrain`, `GuiConnecting`, `GuiMemoryErrorScreen`), allowing smooth full-screen transition into the game while managing subpages in desktop windows
[#]Add **Intersecting Nested Viewport Scissor Stack** Upgraded `RenderUtil` with an automated `scissorStack` (`java.util.Deque<int[]>`) and `resetScissor()`: nested scissoring between `SubpageWindow` content areas and internal `GuiSlot` scrolling boxes automatically calculates geometric intersections, guaranteeing that list items never bleed outside window frames and restoring parent scissor boundaries on list exit
[#]Add **Embedded GuiScreen Background & Input Decoupling** Added `SubpageWindow.isRenderingEmbedded` global state flags; injected into `MixinGuiScreen` (`drawDefaultBackground`, `drawBackground`, `drawWorldBackground`) and `MixinGuiSlot` (`drawScreen`, `drawBackground`, `drawContainerBackground`, `overlayBackground`) to suppress full-screen shaders inside windows in favor of dark acrylic surfaces (`0xF212151D`), and suppressed duplicate full-screen click dispatches in `handleMouseInput`
[#]Add **Cross-Version Input Bridge (GuiScreenBridge)** Created `GuiScreenBridge` with cached, accessible method reflection for `mouseClicked`, `mouseReleased`, `mouseClickMove`, and `keyTyped`, enabling zero-overhead invocation of protected `GuiScreen` input handlers across MCP mappings, Searge names, and production Notch obfuscation
[#]Add **Dedicated GuiModulesOverview Subpage** Implemented modern `GuiModulesOverview` equipped with category navigation pills (`ALL`, `COMBAT`, `MOVEMENT`, `RENDER`, `PLAYER`, `WORLD`, `MISC`), real-time module search/filtering, live toggle switches, keybind tags, module descriptions, mouse-wheel scrolling, and a direct ClickGUI launcher shortcut
[#]Add **Interactive GuiClientConfiguration Subpage** Upgraded `GuiClientConfiguration` to provide instant configuration of background shaders (prev/next cycling), shader rendering speed, internal viewport resolution, ClickGUI rebind listener, and performance tweaks
[#]Add **Augustus Main Menu Left Rail Window Integration** Integrated `WindowManager` into `AugustusMainMenuRenderer` and `RattixMainMenu`: clicking "Home" minimizes all windows back to the hero card, "Modules" opens the windowed module browser, and "Settings" launches client configuration; left rail items display dynamic active focus indicators and hover reveals
[#]Fix **Main Menu Mouse-Wheel Shader Event Conflict** Updated `RattixMod.onMouseInput` to check `WindowManager.isMouseOverAnyWindow`: scrolling inside floating windows or list slots no longer triggers background shader switching
[#]Deploy **Validated Reobfuscated Build** Compiled and reobfuscated release binary `build/libs/Rattix-1.0.0.jar` with verified refmaps and 100% clean Gradle execution

### **CHANGELOG 102526**

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

### **CHANGELOG 102426(3)**

[#]Add **Shared ShaderBackgroundRenderer Architecture** Implemented centralized `ShaderBackgroundRenderer` pipeline connecting `ShaderManager` with all non-in-game GUI screens, featuring automated resolution/mouse coordinate calculations and a 150-microsecond per-frame deduplication protection to eliminate state conflicts and duplicate rendering
[#]Fix **Global Background Interception in GuiScreen** Injected `ShaderBackgroundRenderer.renderCurrentScreen` directly into `MixinGuiScreen.onDrawBackground` and `onDrawWorldBackground`, cleanly replacing vanilla dirt textures across options, video settings, language, disconnect, and connection screens
[#]Fix **Refmap Binding & Non-Blocking Injections in GuiSlot** Replaced `@Overwrite` in `MixinGuiSlot` with `@Inject` on `drawScreen(HEAD)`, `drawContainerBackground`, `overlayBackground`, and `drawSelectionBox`, ensuring 100% Searge/Notch refmap binding; completely suppressed Forge container dirt quads and header/footer dirt overlays
[#]Fix **Multiplayer Server & Singleplayer World List Visuals** Bound direct shader invocation to `GuiSlot.drawScreen` HEAD, ensuring scrolling list screens (`GuiMultiplayer`, `GuiSelectWorld`, `GuiScreenResourcePacks`, `GuiControls`) render the vibrant full-screen shader underneath transparent scissored entries
[#]Fix **World Loading Screen Refmap & Shader Integration** Migrated `MixinLoadingScreenRenderer.setLoadingProgress` from `@Overwrite` to `@Inject(HEAD, cancellable)`, successfully generating Searge `func_73718_a` and Notch `a(I)V` mappings and rendering the un-dimmed shader with modern progress bar
[#]Remove **Background Dimming & Darkening Overlays** Completely eliminated all darkening and tint overlays (`0x88000000` in `MixinGuiScreen` and `MixinLoadingScreenRenderer`, plus `0x55000000` bottom gradient in `MixinGuiMainMenu`), presenting vibrant, crystal-clear GLSL shader visuals without post-processing dimming
[#]Add **Hardware Viewport Scissoring for Scroll Lists** Scoped list entry rendering (`drawSelectionBox`) within `RenderUtil.enableScissor(left, top, width, bottom - top)` in `MixinGuiSlot`, guaranteeing scrollable entries remain perfectly clipped within their containers without relying on vanilla dirt overlay bars
[#]Deploy **Production Reobfuscated Build** Recompiled and validated 100% clean compilation via `./gradlew build`, producing verified release artifact `build/libs/Rattix-1.0.0.jar`

### **CHANGELOG 102426(2)**

[#]Fix **ClickGUI Debounce Protection** Resolved instant-close bug where pressing RSHIFT closed ClickGUI within 5-15ms; added 250ms open timestamp cooldown in `ClickGUI.Screen.keyTyped` to ignore key bounce and repeat events, along with 250ms close debounce in `ClickGUI.wasRecentlyClosed`
[#]Fix **Mod Container Singleton & Static Key Handling** Converted `rshiftPressed` and `clickGuiKey` to static fields in `RattixMod`, ensuring keybind registration and RSHIFT state tracking function even in development or mixin-only runtimes where Forge mod container instantiation is bypassed
[#]Fix **2D HUD Render Pipeline & GL State** Re-established clean 2D projection matrix and OpenGL state in `RattixMod.handleRender2D` via `mc.entityRenderer.setupOverlayRendering()`, matrix isolation, blend state, alpha testing, and lighting disable; eliminated rendering corruption, black boxes, and invisible text across all in-game HUD modules
[#]Fix **Overlay Render Event Dispatch** Restricted Forge `RenderGameOverlayEvent.Post` listener strictly to `ElementType.ALL` and tuned frame throttling to 500μs, preventing mid-frame element conflicts and dropped HUD draw calls
[#]Fix **HUD Text Blend State** Ensured blend mode and alpha test are explicitly enabled in `FontUtil.drawHudString` prior to font rendering, guaranteeing smooth anti-aliased text rendering with color codes
[#]Fix **HUD Element Coordinate Collision** Adjusted `Keystrokes` vertical position from Y:40 to Y:58, completely eliminating overlap with `PingDisplay` (Y:40) and creating a clean vertical cascade (`FPS` -> `CPS` -> `Ping` -> `Keystrokes`)
[#]Update **Default Visual Module States** Enabled `Keystrokes` and `ArmorStatus` by default upon client startup
[#]Fix **Shader Configuration BOM Character** Removed UTF-8 BOM headers from `shaders.txt` and added runtime `\uFEFF` sanitization in `ShaderManager`, resolving shader loading failure warnings on startup
[#]Deploy **Gradle Reobfuscated Build** Recompiled and verified 100% clean build with zero warnings via `./gradlew build`, producing verified artifact `build/libs/Rattix-1.0.0.jar`

### **CHANGELOG 102426(1)**

[#]Refactor **1:1 LiquidBounce Module Architecture** Realigned all modules under their respective LiquidBounce category packages (`movement.ToggleSprint`, `render.ClickGUI`, `render.Ambiance`, `render.Font`, `render.Fullbright`, etc.) and created category packages for `combat`, `exploit`, `fun`, `misc`, `player`, and `world`
[#]Refactor **Full 13 Root Package Parity** Fully aligned top-level package architecture across `api`, `cape`, `chat`, `config`, `event`, `features`, `file`, `injection`, `lang`, `script`, `tabs`, `ui`, and `utils` matching LiquidBounce-b100
[#]Refactor **Subpackage Structure Alignment** Aligned `features/command` (`commands`, `shortcuts`, `special`), `features/special`, `file/configs/models`, `ui/client`, `ui/elements`, `ui/font`, `utils/client`, `utils/inventory`, `utils/movement`, `utils/render/animation`, `utils/render/shader`, `utils/timing`, and `utils/ui`
[#]Refactor **Event Bus Parity** Realigned event system with LiquidBounce standard by organizing event classes directly in `client.m1ck3y.rattix.event` alongside `event.async` (matching LiquidBounce `Events.kt` architecture)
[#]Add **LiquidBounce Standard Config & Value System** Integrated `client.m1ck3y.rattix.config` providing `Value`, `Values` (`BoolValue`, `IntegerValue`, `FloatValue`, `ListValue`, `TextValue`), `Configurable`, and `SettingsUtils`
[#]Update **Developer Guide & API Docs** Synchronized `docs/MODULE_DEVELOPMENT_GUIDE.md` and all documentation with the categorized module structure and updated package paths
[#]Fix **Source File Encoding & Compilation** Purged all UTF-8 BOM headers (`\ufeff`) across source files, verifying 100% clean compilation via ForgeGradle (`BUILD SUCCESSFUL`)
[#]Refactor **LiquidBounce Directory Architecture Parity** Realigned Rattix package hierarchy with LiquidBounce-b100 standard architecture: introducing `features.command`, `features.module`, `features.special`, `file`, `file.configs`, `ui.font`, `ui.client`, `utils.render`, and `utils.render.shader`
[#]Refactor **Module Architecture** Reorganized all modules directly into `features.module.modules` (flat structure without category subpackages) while preserving runtime `Category` classification (`COMBAT`, `PLAYER`, `MOVEMENT`, `RENDER`/`VISUAL`, `WORLD`, `MISC`, `EXPLOIT`, `FUN`, `THEME`)
[#]Refactor **Dynamic Module Discovery** Enhanced `ModuleManager` with recursive directory reflection scanning across all subpackages and updated fallback registry
[#]Refactor **File & Config Architecture** Migrated `FileManager` to `client.m1ck3y.rattix.file` and configurations (`ClickGuiConfig`, `ConfigManager`) to `client.m1ck3y.rattix.file.configs`
[#]Refactor **Font & UI Architecture** Migrated font rendering and utilities (`FontUtil`, `CustomFontRenderer`, `GlobalFontRenderer`) to `client.m1ck3y.rattix.ui.font`
[#]Refactor **Render & Shader Architecture** Structured rendering helpers and dynamic shaders under `client.m1ck3y.rattix.utils.render` and `client.m1ck3y.rattix.utils.render.shader`
[+]Add **LiquidBounce Parity Subsystems** Added architectural packages and managers for `features.special` (`ClientFixes`, `AntiForge`, `BungeeCordSpoof`), `cape` (`CapeService`), `chat` (`ClientChat`), `lang` (`LanguageManager`), and `tabs` (`RattixTab`)
[#]Update **API & Documentation** Fully updated `RattixAPI`, mixins, and `docs/MODULE_DEVELOPMENT_GUIDE.md` to reflect the LiquidBounce architecture

### **UPDATE LOG 101826(7)**

[*]Refactor **Augustus Weather Architecture** Realigned Ambiance weather handling with Augustus's proven architectural pipeline, driving precipitation state via `World.getRainStrength`, `World.setRainStrength`, and biome environmental parameters
[*]Fix **Custom Rain Activation** Fixed an issue where custom rain would not trigger under Vanilla/Clear server weather; `isRainingActive` now reliably engages client-side rainfall when `CustomRain` is enabled or `Weather` is set to `Rain`/`Snow`
[*]Fix **Server Weather Overwrite** Injected `World.setRainStrength` in `MixinWorld` to prevent incoming server packets from resetting client-side weather states while Ambiance is active
[*]Fix **Rain Drop Jitter & Particle Glitches** Completely eliminated frame-to-frame particle vibrating/jittering by replacing unseeded per-frame random calls with deterministic coordinate-based hash offsets
[*]Refactor **Custom Rain Rendering Pipeline** Enhanced `renderCustomRainSnow` with stable UV texture coordinate mapping, proportional `RainSize` quad scaling, ARGB color tinting (`RainColor`), alpha opacity blending (`RainOpacity`), and smooth density distribution (`RainDensity`)
[*]Fix **OptiFine & Shader Compatibility** Seamlessly falls back to Augustus-style native weather rendering when `CustomRain` is unchecked, preserving 100% compatibility with OptiFine, shaders, and custom sky packs
[*]Fix **Obfuscation Refmap Packaging** Generated complete 8,930-byte `mixins.rattix.refmap.json` mapping all Searge and Notch obfuscated targets (`bfk.class`, `adm.class`, `ady.class`) in the packaged release jar
[*]Deploy **Client Mod Build** Recompiled and deployed reobfuscated jar to `D:\Minecraft\.minecraft\versions\Raven b4\mods\Rattix-1.0.0.jar`

### **UPDATE LOG 101826(6)**

[*]Standardize **Setting Naming Convention** Unified all setting display names across the client to PascalCase (`MinDamage` format) with strictly no whitespace
[*]Refactor **Weather Mode Setting** Migrated weather controls to a single-choice mode selector (`Weather: Vanilla, Clear, Rain, Snow`)
[+]Add **Rain Customizer** Added full rain rendering customization engine in `Ambiance` module
[+]Add **RainSize Setting** Customizable raindrop streak width and vertical scale multiplier (`0.1x - 5.0x`)
[+]Add **RainColor Setting** Full RGB custom raindrop coloration with color picker and palette support
[+]Add **RainOpacity Setting** Real-time raindrop transparency and alpha adjustment (`0.0% - 100.0%`)
[+]Add **RainDensity Setting** Particle density control supporting light sparse drizzles (`0.1x`) up to torrential multi-streak downpours (`5.0x`)
[+]Add **Custom Rain Pipeline** Injected custom rain vertex generator into `MixinEntityRenderer.renderRainSnow` with GL state protection and lightmap coordination
[*]Refactor **Config Backward Compatibility** Added space-insensitive key matching in `ClickGuiConfig` ensuring existing configs migrate smoothly without losing color preferences

### **UPDATE LOG 101826(5)**

[*]Fix **Sky Flickering** Intercepted `World.getWorldTime` and `World.getCelestialAngle` to eliminate rapid sky flashing caused by server `S03PacketTimeUpdate` packets
[*]Fix **Snow Effect** Injected into `BiomeGenBase` and `World.getRainStrength` to reliably force falling snow and particles across all biomes even with OptiFine
[*]Refactor **World Color** Refactored World Color from global lighting tint to plant/vegetation color override (grass blocks, tall grass, ferns, reeds, all leaves, vines, and lily pads)
[+]Add **Real-Time Chunk Reload** Automatically reloads chunk meshes (`loadRenderers`) on plant color change for instant visual feedback without reloading world
[*]Fix **Fog Distance Crash** Resolved `IllegalArgumentException` by removing uncancelable `RenderFogEvent` cancellation and managing OpenGL fog cleanly in `MixinEntityRenderer`
[+]Add **RavenXD Lightning Engine** Integrated dynamic client-side lightning strike engine spawning realistic `EntityLightningBolt` effects targeting open-sky entities and high terrain
[+]Add **Lightning Modes** Added 6 selectable strike frequencies (`Vanilla`, `Uncommon`, `Common`, `Frequent`, `Intense`, `Chaotic`)
[+]Add **Positional Thunder Audio** Added 3D positional `ambient.weather.thunder` sound effects with pitch randomization (`0.8x - 1.0x`) and anti-clipping cooldown control
[+]Add **Weather State Backup** Seamlessly backs up server world time, rain state, and thunder state, restoring them smoothly upon module disable
[*]Refactor **Weather Setting** Consolidated weather controls into a single-choice mode selector: `Weather: Vanilla, Clear, Rain, Snow`
[+]Add **Plant Color Mixins** Added `MixinBiomeColorHelper`, `MixinColorizerFoliage`, `MixinColorizerGrass`, and `MixinBiomeGenBase` for full foliage color coverage

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

[*]Refactor **Package** Renamed root package to `client.m1ck3y.rattix` and updated `build.gradle`
[*]Refactor **ClickGUI** Restructured ClickGUI and components into `client.m1ck3y.rattix.module.clickgui`
[+]Add **Theme** Added Theme category and SVG icon in ClickGUI navigation
[*]Fix **ClickGUI** Fixed expand menu layout offset calculation when Theme category is displayed
[*]Refactor **Font** Separated Font management from Module system into dedicated font controller
[+]Add **Font System** Custom TrueType Font renderer with outline, custom shadow, and scaling
[*]Fix **SvgIconHelper** Fixed quadratic bezier path calculation syntax error
[+]Add **Modules** Complete implementation of Combat, Movement, Render, Player, and HUD modules
