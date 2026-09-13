package client.m1ck3y.rattix.config;

import com.google.gson.*;
import client.m1ck3y.rattix.utils.FileManager;
import client.m1ck3y.rattix.modules.clickgui.ClickGuiWindow;
import client.m1ck3y.rattix.modules.clickgui.TabInfo;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ClickGuiConfig {
    private static final File CONFIG_FILE = new File(FileManager.RATTIX_DIR, "clickgui.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void save(List<ClickGuiWindow> windows) {
        try {
            if (!FileManager.RATTIX_DIR.exists()) {
                FileManager.RATTIX_DIR.mkdirs();
            }
            JsonObject root = new JsonObject();
            JsonArray winArray = new JsonArray();
            for (ClickGuiWindow win : windows) {
                JsonObject winObj = new JsonObject();
                winObj.addProperty("id", win.getId());
                winObj.addProperty("posX", win.getPosX());
                winObj.addProperty("posY", win.getPosY());
                winObj.addProperty("windowWidth", win.getWindowWidth());
                winObj.addProperty("windowHeight", win.getWindowHeight());
                winObj.addProperty("isMaximized", win.isMaximized());
                winObj.addProperty("isMinimized", win.isMinimized());
                winObj.addProperty("activeTabIndex", win.getActiveTabIndex());

                JsonArray tabArray = new JsonArray();
                for (TabInfo tab : win.getOpenTabs()) {
                    JsonObject tabObj = new JsonObject();
                    tabObj.addProperty("id", tab.getId());
                    tabObj.addProperty("type", tab.getType());
                    tabObj.addProperty("title", tab.getTitle());
                    tabArray.add(tabObj);
                }
                winObj.add("tabs", tabArray);
                winArray.add(winObj);
            }
            root.add("windows", winArray);

            try (Writer writer = new OutputStreamWriter(new FileOutputStream(CONFIG_FILE), "UTF-8")) {
                GSON.toJson(root, writer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<ClickGuiWindow> load() {
        List<ClickGuiWindow> windows = new ArrayList<>();
        if (!CONFIG_FILE.exists()) {
            return windows;
        }
        try (Reader reader = new InputStreamReader(new FileInputStream(CONFIG_FILE), "UTF-8")) {
            JsonObject root = GSON.fromJson(reader, JsonObject.class);
            if (root != null && root.has("windows")) {
                JsonArray winArray = root.getAsJsonArray("windows");
                for (JsonElement el : winArray) {
                    if (!el.isJsonObject()) continue;
                    JsonObject winObj = el.getAsJsonObject();
                    String id = winObj.has("id") ? winObj.get("id").getAsString() : null;
                    float posX = winObj.has("posX") ? winObj.get("posX").getAsFloat() : 100;
                    float posY = winObj.has("posY") ? winObj.get("posY").getAsFloat() : 100;
                    float w = winObj.has("windowWidth") ? winObj.get("windowWidth").getAsFloat() : 960;
                    float h = winObj.has("windowHeight") ? winObj.get("windowHeight").getAsFloat() : 600;
                    boolean isMax = winObj.has("isMaximized") && winObj.get("isMaximized").getAsBoolean();
                    boolean isMin = winObj.has("isMinimized") && winObj.get("isMinimized").getAsBoolean();
                    int activeTab = winObj.has("activeTabIndex") ? winObj.get("activeTabIndex").getAsInt() : 0;

                    List<TabInfo> tabs = new ArrayList<>();
                    if (winObj.has("tabs")) {
                        JsonArray tabArray = winObj.getAsJsonArray("tabs");
                        for (JsonElement tel : tabArray) {
                            if (!tel.isJsonObject()) continue;
                            JsonObject tabObj = tel.getAsJsonObject();
                            String tabId = tabObj.has("id") ? tabObj.get("id").getAsString() : null;
                            String type = tabObj.has("type") ? tabObj.get("type").getAsString() : "console";
                            String title = tabObj.has("title") ? tabObj.get("title").getAsString() : "Console";

                            // 兼容旧分类 Tab（如 render, hud 映射为 visual），过滤已移除的分类（如 combat, player 等）
                            if (!"console".equalsIgnoreCase(type) && !"modules".equalsIgnoreCase(type)) {
                                client.m1ck3y.rattix.modules.manager.Category cat = client.m1ck3y.rattix.modules.manager.Category.fromName(type);
                                if (cat == null) {
                                    continue;
                                }
                                type = cat.name().toLowerCase();
                                if ("render".equalsIgnoreCase(title) || "hud".equalsIgnoreCase(title)) {
                                    title = cat.getDisplayName();
                                }
                            }

                            TabInfo tab = TabInfo.createTab(tabId, type, title);
                            tabs.add(tab);
                        }
                    }

                    if (tabs.isEmpty()) {
                        tabs.add(TabInfo.createPreset("modules"));
                    }

                    if (activeTab < 0 || activeTab >= tabs.size()) {
                        activeTab = 0;
                    }

                    ClickGuiWindow window = new ClickGuiWindow(id, posX, posY, w, h, isMax, isMin, tabs, activeTab);
                    windows.add(window);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return windows;
    }
}
