# Rattix 客户端模块与分类二次开发接入规范 (v1.0.0)

本指南专为需要向 **Rattix 客户端** 扩展自定义分类（Category）与功能模块（Module）的第三方 Forge 模组开发者编写。

---

## 一、 架构与设计原则

Rattix 采用高内聚、易扩展的响应式模块总线系统。第三方模组可以通过统一门面类 [`RattixAPI`](file:///f:/Projects/Rattix/src/main/java/client/m1ck3y/rattix/api/RattixAPI.java) 注册独立的分类与功能，并在客户端终端图形界面（ClickGUI）中自动完成渲染、交互与设置持久化。

```mermaid
flowchart LR
    A["第三方模组 (External Mod)"] -->|1. 注册分类 / SVG 图标| B["Category & SvgIconHelper"]
    A -->|2. 注册模块 (Module)| C["ModuleManager.register()"]
    B --> D["ClickGUI 终端界面"]
    C --> D
    C --> E["Tick & Render 总线调度"]
    C --> F["配置与重置系统 (Default Settings)"]
```

### 核心特性
- **动态分类扩展**：无需侵入修改 Rattix 源码，随时动态注入全新分类，自动生成窗口 Tab 与菜单。
- **高保真矢量渲染**：支持向新分类绑定内置 SVG 图标，或动态传入纯文本 SVG `<path d="..." />` 路径数据，实时由 GPU 渲染为抗锯齿图标。
- **自动布局与多级参数树**：模块参数系统支持分组（Group）、滑条、开关、模式、多模式颜色选择器，以及基于 `withParent` 的分支折叠树。
- **生产环境混淆兼容**：原生适配 Forge 1.8.9（MCP stable_22 / Searge 混淆环境），保证独立客户端加载稳定性。

---

## 二、 快速上手：API 接入门面

推荐所有外部模组统一通过 [`RattixAPI`](file:///f:/Projects/Rattix/src/main/java/client/m1ck3y/rattix/api/RattixAPI.java) 提供的静态方法进行交互：

| API 方法 | 说明 |
| :--- | :--- |
| `RattixAPI.isRattixLoaded()` | 检查当前游戏环境中 Rattix 客户端是否已成功载入 |
| `RattixAPI.registerCategory(name, displayName)` | 注册新分类（默认通用图标） |
| `RattixAPI.registerCategory(name, displayName, iconName)` | 注册新分类并指定已知/内置图标（如 `"combat"`, `"player"` 等） |
| `RattixAPI.registerCategoryWithSvg(name, displayName, icon, svgD)` | 注册新分类并绑定全新的 SVG 矢量图形 |
| `RattixAPI.registerModule(Module module)` | 注册功能模块实例 |
| `RattixAPI.registerModule(Class<T> moduleClass)` | 自动实例化并注册模块类（要求公有无参构造） |
| `RattixAPI.unregisterModule(Module module)` | 注销并卸载功能模块 |
| `RattixAPI.getModule(Class<T> clazz)` | 获取已注册的模块实例 |
| `RattixAPI.openClickGui()` | 打开 Rattix ClickGUI 界面 |

---

## 三、 分类开发规范 (Category Specification)

### 1. 内置分类
Rattix 对齐 LiquidBounce 标准客户端分类体系，内置并支持以下核心分类：
- `Category.COMBAT`（战斗/PVP 功能，图标：`combat`）
- `Category.PLAYER`（玩家个人辅助，图标：`player`）
- `Category.MOVEMENT`（移动与机动性功能，图标：`movement`）
- `Category.RENDER` / `Category.VISUAL`（视觉渲染与 HUD 模块，图标：`visual`）
- `Category.WORLD`（世界与环境交互，图标：`other`）
- `Category.MISC`（杂项工具功能，图标：`other`）
- `Category.EXPLOIT`（漏洞/协议特性，图标：`ghost`）
- `Category.FUN`（娱乐与趣味模块，图标：`other`）
- `Category.THEME`（主题与字体专用配置页面，图标：`theme`）

### 2. 内置可用图标
注册新分类时可以直接复用以下内置高分辨率矢量图标：
- `"combat"`（剑与盾牌交叉图标，适合战斗/PVP）
- `"movement"`（奔跑小人图标，适合机动性模块）
- `"visual"`（眼睛图标，适合渲染与 HUD 模块）
- `"player"`（玩家角色半身像图标，适合个人辅助类）
- `"ghost"`（幽灵图标，适合隐身/Ghost/Exploit 辅助）
- `"theme"`（调色盘图标，适合外观配置）
- `"other"`（立方体方块图标，默认通用图标）

### 3. 创建全新自定义分类示例
```java
// 方式 A：使用内置战斗图标创建 "COMBAT" 分类
Category combatCat = RattixAPI.registerCategory("COMBAT", "Combat", "combat");

// 方式 B：使用全新的自定义 SVG 矢量路径数据注册分类
String mySvgPath = "M10 20 L30 40 L50 10 Z..."; // 任意标准的 SVG path 字符串
Category utilityCat = RattixAPI.registerCategoryWithSvg("UTILITY", "Utility", "my_custom_icon", mySvgPath);
```

注册完成后，ClickGUI 窗口的 `+` (新建 Tab) 下拉菜单将自动实时加入该分类，点击即可开启独立 Tab 查看模块。

---

## 四、 模块开发规范 (Module Specification)

所有模块必须直接或间接继承 [`client.m1ck3y.rattix.features.module.Module`](file:///f:/Projects/Rattix/src/main/java/client/m1ck3y/rattix/features/module/Module.java)。

### 1. 生命周期回调方法
| 回调方法 | 触发时机 | 最佳实践 |
| :--- | :--- | :--- |
| `onEnable()` | 模块被玩家或快捷键启用时触发 | 适合初始化资源、播放提示音、监听特定事件 |
| `onDisable()` | 模块被停用时触发 | **必须在此彻底清理修改状态**（如恢复视野、还原计时器、重置按键状态） |
| `onTick()` | 当模块启用时，在每个客户端渲染主循环末尾触发 | 适合执行业务逻辑、物理模拟计算；**切忌高频创建临时对象** |
| `onRender2D()` | 当模块启用且未开启 F3 Debug 界面时，在 HUD 绘制阶段触发 | 适合绘制屏幕文字、HUD 指示器、自绘组件 |
| `onRender3D()` | 当模块启用时在世界 3D 渲染阶段触发 | 适合绘制 ESP、Trace、方块高亮 |

### 2. 参数类型系统 (Settings)
Rattix 提供了一整套自动生成 Fluent UI 组件的参数体系（位于 `client.m1ck3y.rattix.features.module.modules.render.ClickGUI.*`）：

1. **`CheckBoxSetting` (布尔开关)**
   ```java
   public final CheckBoxSetting sprintParticles = new CheckBoxSetting("Particles", true, "Effects");
   ```
2. **`NumberSetting` (浮点 / 整数滑条)**
   ```java
   // 参数：显示名, 默认值, 最小值, 最大值, 步长, 分组名
   public final NumberSetting maxCPS = new NumberSetting("Max CPS", 12.0, 1.0, 20.0, 1.0, "Combat");
   ```
3. **`ModeSetting` (多模式切换)**
   ```java
   // 参数：显示名, 默认模式, 分组名, 候选列表
   public final ModeSetting mode = new ModeSetting("Mode", "Dynamic", "General", new String[]{"Static", "Dynamic", "Pulse"});
   ```
4. **`ColorSetting` (现代全功能色彩调节器)**
   ```java
   // 自动支持纯色拖拽 (Hue/Sat/Bri/Alpha)、Rainbow、Fade、Astolfo 以及一键跟随全局主题色
   public final ColorSetting hudColor = new ColorSetting("HUD Color", 0xFF38B6FF, "Visuals");
   ```
5. **参数二级联动树 (`withParent`)**
   ```java
   // 当 modeSwinging 为 true 时，ClickGUI 会在右侧通过层级树干将 swingingSpeed 展开在其下方
   public final CheckBoxSetting modeSwinging = new CheckBoxSetting("Swinging", true, "Animation");
   public final NumberSetting swingingSpeed = new NumberSetting("Speed", 1.0, 0.1, 5.0, 0.1, "Animation").withParent(modeSwinging);
   ```

---

## 五、 完整开发示例

### 示例 1：创建标准外部模块

```java
package com.example.addon.modules;

import client.m1ck3y.rattix.features.module.Category;
import client.m1ck3y.rattix.features.module.Module;
import client.m1ck3y.rattix.features.module.modules.render.ClickGUI.CheckBoxSetting;
import client.m1ck3y.rattix.features.module.modules.render.ClickGUI.ModeSetting;
import client.m1ck3y.rattix.features.module.modules.render.ClickGUI.NumberSetting;
import org.lwjgl.input.Keyboard;

public class AutoSprintHelper extends Module {

    // 1. 定义设置项
    public final ModeSetting sprintMode = new ModeSetting("Mode", "Omni", "General", new String[]{"Legit", "Omni", "Rage"});
    public final CheckBoxSetting keepSprint = new CheckBoxSetting("Keep Sprint", true, "Combat");
    public final NumberSetting minStamina = new NumberSetting("Min Hunger", 6.0, 0.0, 20.0, 1.0, "Limits");

    public AutoSprintHelper() {
        // 参数：模块名、描述、所属分类、默认绑定按键（0 为不绑定）
        super("SprintHelper", "Automatically keeps the player sprinting under flexible conditions.", Category.MOVEMENT, Keyboard.KEY_NONE);
        
        // 2. 注册参数
        addSettings(sprintMode, keepSprint, minStamina);
    }

    @Override
    public void onEnable() {
        // 模块开启时的初始化逻辑
    }

    @Override
    public void onDisable() {
        // 模块关闭时，务必恢复原生按键状态
        if (mc.thePlayer != null) {
            mc.thePlayer.setSprinting(false);
        }
    }

    @Override
    public void onTick() {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        // 根据设置执行自定义逻辑
        if (mc.thePlayer.getFoodStats().getFoodLevel() >= minStamina.getValue()) {
            if (mc.thePlayer.movementInput.moveForward > 0.0F) {
                mc.thePlayer.setSprinting(true);
            }
        }
    }
}
```

---

### 示例 2：外部模组在 Forge 阶段进行注册

在第三方模组主类的 `FMLInitializationEvent`（或 `FMLPostInitializationEvent`）中调用注册：

```java
package com.example.addon;

import client.m1ck3y.rattix.api.RattixAPI;
import client.m1ck3y.rattix.features.module.Category;
import com.example.addon.modules.AutoSprintHelper;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

@Mod(modid = "rattixaddon", name = "Rattix Example Addon", version = "1.0.0", dependencies = "required-after:rattix")
public class ExampleAddonMod {

    @EventHandler
    public void init(FMLInitializationEvent event) {
        // 1. 检查 Rattix 是否加载
        if (!RattixAPI.isRattixLoaded()) {
            System.out.println("[ExampleAddon] Rattix is not detected, skipping registration.");
            return;
        }

        // 2. （可选）创建并注册一个自定义分类，带有战斗剑图标
        Category addonCat = RattixAPI.registerCategory("ADDON", "Addon Features", "combat");

        // 3. 注册新模块（将直接显示在 ClickGUI 中）
        RattixAPI.registerModule(new AutoSprintHelper());

        System.out.println("[ExampleAddon] Successfully hooked into Rattix Client!");
    }
}
```

---

## 六、 编码规范与性能底线 (Best Practices & DOs and DON'Ts)

> [!IMPORTANT]
> **垃圾回收 (GC) 与 0 掉帧承诺**
> Rattix 专注于极致的 FPS 优化与流畅度表现。所有扩展模块必须严格遵守无频繁内存分配原则：
> - 严禁在 `onTick()` 与 `onRender2D()` 中循环创建临时 `new Object()`、字符串拼接或创建集合。
> - 预分配固定缓冲对象、向量或颜色缓存。

> [!TIP]
> **默认参数重置功能 (Default Parameters)**
> Rattix GUI 支持一键将模块所有设置重置为默认值，也支持玩家保存自定义初始值。因此，所有参数的 `defaultValue` 请务必设定为最稳定、最推荐的游戏初始值。

> [!WARNING]
> **线程隔离与 OpenGL 上下文**
> - 所有 HUD 渲染逻辑必须位于 `onRender2D()` 回调内，且在退出前保证使用 `GlStateManager.resetColor()` 或还原矩阵堆栈（`pushMatrix / popMatrix`）。
> - 严禁在异步线程中直接调用 `Minecraft` 原生渲染方法或调用 LWJGL 函数。

> [!CAUTION]
> **防御性编程**
> - 在访问 `mc.thePlayer` 与 `mc.theWorld` 时始终做空值检查（`!= null`），因为当玩家退出服务器或切换世界时，这些对象会被销毁重置。
