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
