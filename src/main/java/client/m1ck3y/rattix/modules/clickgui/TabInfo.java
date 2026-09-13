package client.m1ck3y.rattix.modules.clickgui;

import client.m1ck3y.rattix.modules.manager.Category;
import client.m1ck3y.rattix.modules.manager.Register;

import java.util.*;

public class TabInfo {
    private final String id;
    private final String type;
    private String title;
    private final Category category; // null for console

    // Terminal State (for console tabs)
    private final List<String> terminalOutput = new ArrayList<>();
    private String currentInput = "";
    private final List<String> commandHistory = new ArrayList<>();
    private int historyPointer = -1;
    private float terminalScrollY = 0;
    private float maxTerminalScroll = 0;

    // Module View State (for category tabs)
    private Register selectedModule = null;
    private String selectedThemeItem = "Font";
    private final Map<String, Boolean> collapsedGroups = new HashMap<>();
    private Register bindingModule = null;
    private float moduleListScrollY = 0;

    public TabInfo(String id, String type, String title, Category category) {
        this.id = (id != null && !id.isEmpty()) ? id : UUID.randomUUID().toString();
        this.type = type;
        this.title = title;
        this.category = category;

        if (isModulesTab()) {
            List<Register> mods = Register.getAlphabeticalModules();
            if (!mods.isEmpty()) {
                selectedModule = mods.get(0);
            }
        } else if (category == null) {
            initTerminalWelcome();
        } else {
            List<Register> mods = category.getModules();
            if (!mods.isEmpty()) {
                selectedModule = mods.get(0);
            }
        }
    }

    public boolean isModulesTab() {
        return "modules".equalsIgnoreCase(type);
    }

    private void initTerminalWelcome() {
        if (terminalOutput.isEmpty()) {
            terminalOutput.add("Console");
            terminalOutput.add("Copyright (C) Microsoft Corporation. All rights reserved.");
            terminalOutput.add("");
            terminalOutput.add("\u00A7a[Rattix Client]\u00A7r v1.0.0 initialized. Type \u00A7ehelp\u00A7r or \u00A7elist\u00A7r to get started.");
            terminalOutput.add("");
        }
    }

    public static TabInfo createPreset(String type) {
        String t = (type == null) ? "console" : type.toLowerCase();
        if ("modules".equalsIgnoreCase(t)) {
            return new TabInfo(UUID.randomUUID().toString(), "modules", "Modules", null);
        }
        Category cat = Category.fromName(t);
        if (cat != null) {
            return new TabInfo(UUID.randomUUID().toString(), cat.name().toLowerCase(), cat.getDisplayName(), cat);
        }
        return new TabInfo(UUID.randomUUID().toString(), "console", "Console", null);
    }

    public static TabInfo createTab(String id, String type, String title) {
        String t = (type == null) ? "console" : type.toLowerCase();
        if ("modules".equalsIgnoreCase(t)) {
            String tabTitle = (title != null && !title.isEmpty()) ? title : "Modules";
            return new TabInfo(id, "modules", tabTitle, null);
        }
        Category cat = Category.fromName(t);
        if (cat != null) {
            String tabTitle = (title != null && !title.isEmpty()) ? title : cat.getDisplayName();
            return new TabInfo(id, cat.name().toLowerCase(), tabTitle, cat);
        }
        String tabTitle = (title != null && !title.isEmpty()) ? title : "Console";
        return new TabInfo(id, "console", tabTitle, null);
    }

    public String getId() { return id; }
    public String getType() { return type; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Category getCategory() { return category; }

    public List<String> getTerminalOutput() { return terminalOutput; }
    public String getCurrentInput() { return currentInput; }
    public void setCurrentInput(String currentInput) { this.currentInput = currentInput; }
    public List<String> getCommandHistory() { return commandHistory; }
    public int getHistoryPointer() { return historyPointer; }
    public void setHistoryPointer(int historyPointer) { this.historyPointer = historyPointer; }
    public float getTerminalScrollY() { return terminalScrollY; }
    public void setTerminalScrollY(float terminalScrollY) { this.terminalScrollY = terminalScrollY; }
    public float getMaxTerminalScroll() { return maxTerminalScroll; }
    public void setMaxTerminalScroll(float maxTerminalScroll) { this.maxTerminalScroll = maxTerminalScroll; }

    public Register getSelectedModule() { return selectedModule; }
    public void setSelectedModule(Register selectedModule) { this.selectedModule = selectedModule; }
    public Map<String, Boolean> getCollapsedGroups() { return collapsedGroups; }
    public Register getBindingModule() { return bindingModule; }
    public void setBindingModule(Register bindingModule) { this.bindingModule = bindingModule; }
    public float getModuleListScrollY() { return moduleListScrollY; }
    public void setModuleListScrollY(float moduleListScrollY) { this.moduleListScrollY = moduleListScrollY; }

    public String getSelectedThemeItem() { return selectedThemeItem; }
    public void setSelectedThemeItem(String selectedThemeItem) { this.selectedThemeItem = selectedThemeItem; }
}
