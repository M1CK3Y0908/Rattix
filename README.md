# Rattix

Rattix Client - A customized Forge 1.8.9 Client.

### **UPDATE LOG 090626(1)**

- [*]Refactor **Package** Renamed root package to `client.m1ck3y.rattix` and updated `build.gradle`
- [*]Refactor **ClickGUI** Restructured ClickGUI and components into `client.m1ck3y.rattix.module.clickgui`
- [+]Add **Theme** Added Theme category and SVG icon in ClickGUI navigation
- [*]Fix **ClickGUI** Fixed expand menu layout offset calculation when Theme category is displayed
- [*]Refactor **Font** Separated Font management from Module system into dedicated font controller
- [+]Add **Font System** Custom TrueType Font renderer with outline, custom shadow, and scaling
- [*]Fix **SvgIconHelper** Fixed quadratic bezier path calculation syntax error
- [+]Add **Modules** Complete implementation of Combat, Movement, Render, Player, and HUD modules