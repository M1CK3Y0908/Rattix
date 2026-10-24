package client.m1ck3y.rattix.api;

import client.m1ck3y.rattix.RattixMod;
import client.m1ck3y.rattix.features.module.Category;
import client.m1ck3y.rattix.features.module.Module;
import client.m1ck3y.rattix.features.module.ModuleManager;
import client.m1ck3y.rattix.features.module.modules.ClickGUI;
import client.m1ck3y.rattix.utils.render.SvgIconHelper;

import java.util.Collections;
import java.util.List;

/**
 * Rattix 客户端公用开放 API。
 * 供第三方模组（Other Mods / Addons）在运行时动态添加分类、模块、自定义参数与矢量图标。
 */
public final class RattixAPI {

    private RattixAPI() {
    }

    /**
     * 判断 Rattix 客户端模组是否已加载就绪
     */
    public static boolean isRattixLoaded() {
        return RattixMod.instance != null;
    }

    // ==========================================
    // 分类管理 (Category Management)
    // ==========================================

    /**
     * 注册或获取一个模块分类
     *
     * @param name        分类唯一标识（如 "COMBAT", "PLAYER", "UTILITY"）
     * @param displayName 界面显示的分类标题（如 "Combat", "Player", "Utility"）
     * @return 注册的 Category 对象
     */
    public static Category registerCategory(String name, String displayName) {
        return Category.create(name, displayName);
    }

    /**
     * 注册带有指定内置或已知图标的分类
     *
     * @param name        分类唯一标识
     * @param displayName 界面显示的分类标题
     * @param iconName    内置或已注册的 SVG 图标名称（如 "combat", "movement", "visual", "player", "theme", "ghost", "other"）
     * @return 注册的 Category 对象
     */
    public static Category registerCategory(String name, String displayName, String iconName) {
        return Category.create(name, displayName, iconName);
    }

    /**
     * 注册带有全新自定义矢量 SVG 图标的分类
     *
     * @param name        分类唯一标识
     * @param displayName 界面显示的分类标题
     * @param iconName    自定义图标唯一标识名
     * @param svgPathData SVG 路径数据（SVG &lt;path d="..." /&gt; 中的 d 属性字符串）
     * @return 注册的 Category 对象
     */
    public static Category registerCategoryWithSvg(String name, String displayName, String iconName, String svgPathData) {
        SvgIconHelper.registerSvg(iconName, svgPathData);
        return Category.create(name, displayName, iconName);
    }

    /**
     * 注册带有全新自定义矢量 SVG 图标的分类（带变换微调）
     *
     * @param name        分类唯一标识
     * @param displayName 界面显示的分类标题
     * @param iconName    自定义图标唯一标识名
     * @param svgPathData SVG 路径数据
     * @param translateY  Y 轴位移微调
     * @param scaleY      Y 轴缩放微调（如 -1 用于反转坐标系）
     * @return 注册的 Category 对象
     */
    public static Category registerCategoryWithSvg(String name, String displayName, String iconName, String svgPathData, double translateY, double scaleY) {
        SvgIconHelper.registerSvg(iconName, svgPathData, translateY, scaleY);
        return Category.create(name, displayName, iconName);
    }

    /**
     * 获取所有已注册的分类列表
     */
    public static List<Category> getCategories() {
        return Category.getCategories();
    }

    /**
     * 根据名称或别名查找分类
     */
    public static Category getCategory(String name) {
        return Category.fromName(name);
    }

    // ==========================================
    // 模块管理 (Module Management)
    // ==========================================

    /**
     * 注册一个功能模块实例
     *
     * @param module 继承自 Module 的模块实例
     */
    public static void registerModule(Module module) {
        ModuleManager.register(module);
    }

    /**
     * 通过类类型自动实例化并注册模块（要求具有无参构造函数）
     *
     * @param clazz 模块 Class
     * @return 注册成功的模块实例
     */
    public static <T extends Module> T registerModule(Class<T> clazz) {
        return ModuleManager.register(clazz);
    }

    /**
     * 注销并卸载一个模块
     */
    public static void unregisterModule(Module module) {
        ModuleManager.unregister(module);
    }

    /**
     * 根据名称注销模块
     */
    public static void unregisterModule(String moduleName) {
        ModuleManager.unregister(moduleName);
    }

    /**
     * 根据类获取已注册的模块实例
     */
    public static <T extends Module> T getModule(Class<T> clazz) {
        return ModuleManager.getModule(clazz);
    }

    /**
     * 根据模块名称获取已注册的模块实例
     */
    public static Module getModule(String name) {
        return ModuleManager.getModule(name);
    }

    /**
     * 获取所有已注册的模块列表
     */
    public static List<Module> getModules() {
        return ModuleManager.getRegisteredModules();
    }

    /**
     * 获取指定分类下的所有模块列表
     */
    public static List<Module> getModulesInCategory(Category category) {
        if (category == null) return Collections.emptyList();
        return category.getModules();
    }

    /**
     * 打开 Rattix 终端风格图形界面 (ClickGUI)
     */
    public static void openClickGui() {
        ClickGUI.open();
    }
}
