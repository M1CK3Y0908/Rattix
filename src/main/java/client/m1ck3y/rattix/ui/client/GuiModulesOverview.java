package client.m1ck3y.rattix.ui.client;

import client.m1ck3y.rattix.features.module.Category;
import client.m1ck3y.rattix.features.module.Module;
import client.m1ck3y.rattix.features.module.ModuleManager;
import client.m1ck3y.rattix.features.module.modules.ClickGUI;
import client.m1ck3y.rattix.ui.elements.FluentGuiButton;
import client.m1ck3y.rattix.utils.render.RenderUtil;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * GuiModulesOverview - Embedded and standalone module overview for Rattix Client.
 * Displays categorised modules, live enable toggles, keybind indicators, and ClickGUI launcher.
 */
public class GuiModulesOverview extends GuiScreen {
    private final GuiScreen parentScreen;

    private static final int BTN_CLICKGUI = 100;
    private static final int BTN_DONE = 200;

    private Category selectedCategory = null; // null = ALL
    private float scrollOffset = 0;
    private float maxScroll = 0;

    public GuiModulesOverview(GuiScreen parentScreen) {
        this.parentScreen = parentScreen;
    }

    public GuiScreen getParentScreen() {
        return parentScreen;
    }

    @Override
    public void initGui() {
        buttonList.clear();

        int centerX = width / 2;
        buttonList.add(new FluentGuiButton(BTN_CLICKGUI, width - 150, 10, 135, 20, "Open ClickGUI (RSHIFT)"));
        buttonList.add(new FluentGuiButton(BTN_DONE, centerX - 60, height - 28, 120, 22, "Done"));
    }

    private List<Module> getFilteredModules() {
        List<Module> all = ModuleManager.getAlphabeticalModules();
        if (selectedCategory == null) return all;
        List<Module> filtered = new ArrayList<>();
        for (Module m : all) {
            if (m.getCategory() == selectedCategory || selectedCategory.getName().equalsIgnoreCase(m.getCategory().getName())) {
                filtered.add(m);
            }
        }
        return filtered;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        FontRenderer fr = fontRendererObj;
        if (fr == null) return;

        // Top Header
        fr.drawStringWithShadow("Rattix Modules", 14, 12, 0xFF76B9ED);
        fr.drawString(ModuleManager.getLoadedModuleCount() + " loaded  •  Click switch to toggle", 14, 24, 0xFF7E8A99);

        // Category Tabs Bar
        float tabX = 14;
        float tabY = 38;
        float tabH = 18;

        // "ALL" tab
        boolean allHover = mouseX >= tabX && mouseX <= tabX + 32 && mouseY >= tabY && mouseY <= tabY + tabH;
        boolean allSelected = (selectedCategory == null);
        RenderUtil.drawRoundedRect(tabX, tabY, 32, tabH, 3.0f, allSelected ? 0xFF2A3D54 : (allHover ? 0x332A3D54 : 0x22181C26));
        fr.drawString("ALL", (int) (tabX + 6), (int) (tabY + 5), allSelected ? 0xFFFFFFFF : 0xFF8F9BA8);
        tabX += 36;

        Category[] cats = Category.values();
        for (Category cat : cats) {
            String name = cat.getDisplayName();
            int strW = fr.getStringWidth(name);
            float btnW = strW + 12;
            boolean hover = mouseX >= tabX && mouseX <= tabX + btnW && mouseY >= tabY && mouseY <= tabY + tabH;
            boolean selected = (selectedCategory == cat);

            RenderUtil.drawRoundedRect(tabX, tabY, btnW, tabH, 3.0f, selected ? 0xFF2A3D54 : (hover ? 0x332A3D54 : 0x22181C26));
            fr.drawString(name, (int) (tabX + 6), (int) (tabY + 5), selected ? 0xFFFFFFFF : 0xFF8F9BA8);
            tabX += btnW + 4;
            if (tabX > width - 40) break;
        }

        // Module Cards Area
        float contentTop = 62;
        float contentBottom = height - 34;
        float contentW = width - 28;
        float contentH = contentBottom - contentTop;

        RenderUtil.enableScissor(14, contentTop, contentW, contentH);

        List<Module> list = getFilteredModules();
        float cardH = 34.0f;
        float gap = 4.0f;
        float totalHeight = list.size() * (cardH + gap);
        maxScroll = Math.max(0, totalHeight - contentH);
        if (scrollOffset > 0) scrollOffset = 0;
        if (scrollOffset < -maxScroll) scrollOffset = -maxScroll;

        float cardY = contentTop + scrollOffset;
        for (Module m : list) {
            if (cardY + cardH >= contentTop && cardY <= contentBottom) {
                boolean cardHover = mouseX >= 14 && mouseX <= 14 + contentW && mouseY >= cardY && mouseY <= cardY + cardH;

                // Card surface
                RenderUtil.drawRoundedRect(14, cardY, contentW, cardH, 5.0f, cardHover ? 0xF21C222E : 0xF2161A23);
                RenderUtil.drawRoundedOutline(14, cardY, contentW, cardH, 5.0f, 1.0f, m.isEnabled() ? 0x6076B9ED : 0x253F6D94);

                // Module name & enabled indicator
                fr.drawStringWithShadow(m.getName(), 24, (int) (cardY + 7), m.isEnabled() ? 0xFFFFFFFF : 0xFFCED5DF);

                // Category & Keybind Tag
                String catName = m.getCategory() != null ? m.getCategory().getDisplayName() : "General";
                String keyName = m.getKeyCode() != 0 ? Keyboard.getKeyName(m.getKeyCode()) : "NONE";
                String tag = catName + "  •  [" + keyName + "]";
                fr.drawString(tag, 24, (int) (cardY + 19), 0xFF6D7987);

                // Description (if room)
                String desc = m.getDescription();
                if (desc != null && !desc.isEmpty() && contentW > 300) {
                    int descX = 170;
                    int maxDescW = (int) (contentW - descX - 70);
                    if (maxDescW > 60) {
                        String trimmed = fr.trimStringToWidth(desc, maxDescW);
                        fr.drawString(trimmed, descX, (int) (cardY + 13), 0xFF8C98A6);
                    }
                }

                // Toggle Switch on right
                float switchW = 42.0f;
                float switchH = 18.0f;
                float switchX = 14 + contentW - switchW - 8.0f;
                float switchY = cardY + (cardH - switchH) / 2.0f;

                boolean swHover = mouseX >= switchX && mouseX <= switchX + switchW && mouseY >= switchY && mouseY <= switchY + switchH;
                int swBg = m.isEnabled() ? 0xFF2A6B9C : 0xFF222733;
                RenderUtil.drawRoundedRect(switchX, switchY, switchW, switchH, switchH / 2.0f, swBg);
                RenderUtil.drawRoundedOutline(switchX, switchY, switchW, switchH, switchH / 2.0f, 1.0f, swHover ? 0xFF76B9ED : 0x403F6D94);

                // Knob
                float knobSize = switchH - 4.0f;
                float knobX = m.isEnabled() ? (switchX + switchW - knobSize - 2.0f) : (switchX + 2.0f);
                float knobY = switchY + 2.0f;
                RenderUtil.drawCircle(knobX + knobSize / 2.0f, knobY + knobSize / 2.0f, knobSize / 2.0f, m.isEnabled() ? 0xFFFFFFFF : 0xFF7A8696);
            }
            cardY += cardH + gap;
        }

        RenderUtil.disableScissor();

        // Scrollbar indicator
        if (maxScroll > 0) {
            float barW = 3.0f;
            float barX = width - 10.0f;
            float trackH = contentH;
            float thumbH = Math.max(16.0f, (contentH / totalHeight) * trackH);
            float thumbY = contentTop + (-scrollOffset / maxScroll) * (trackH - thumbH);
            RenderUtil.drawRoundedRect(barX, thumbY, barW, thumbH, 1.5f, 0x6676B9ED);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        // 1. Check category tabs
        float tabX = 14;
        float tabY = 38;
        float tabH = 18;

        if (mouseX >= tabX && mouseX <= tabX + 32 && mouseY >= tabY && mouseY <= tabY + tabH) {
            selectedCategory = null;
            scrollOffset = 0;
            return;
        }
        tabX += 36;

        Category[] cats = Category.values();
        FontRenderer fr = fontRendererObj;
        for (Category cat : cats) {
            String name = cat.getDisplayName();
            int strW = fr != null ? fr.getStringWidth(name) : 40;
            float btnW = strW + 12;
            if (mouseX >= tabX && mouseX <= tabX + btnW && mouseY >= tabY && mouseY <= tabY + tabH) {
                selectedCategory = cat;
                scrollOffset = 0;
                return;
            }
            tabX += btnW + 4;
            if (tabX > width - 40) break;
        }

        // 2. Check module toggle switches
        float contentTop = 62;
        float contentBottom = height - 34;
        float contentW = width - 28;
        if (mouseY >= contentTop && mouseY <= contentBottom) {
            List<Module> list = getFilteredModules();
            float cardH = 34.0f;
            float gap = 4.0f;
            float cardY = contentTop + scrollOffset;

            for (Module m : list) {
                if (mouseY >= cardY && mouseY <= cardY + cardH) {
                    if (mouseButton == 0) {
                        m.toggle();
                        return;
                    }
                }
                cardY += cardH + gap;
            }
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void handleMouseInput() throws IOException {
        int dWheel = Mouse.getEventDWheel();
        if (dWheel != 0) {
            scrollOffset += (dWheel > 0 ? 30 : -30);
            if (scrollOffset > 0) scrollOffset = 0;
            if (scrollOffset < -maxScroll) scrollOffset = -maxScroll;
        }
        super.handleMouseInput();
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == BTN_CLICKGUI) {
            mc.displayGuiScreen(ClickGUI.getScreenInstance());
        } else if (button.id == BTN_DONE) {
            mc.displayGuiScreen(parentScreen);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) { // Escape
            mc.displayGuiScreen(parentScreen);
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }
}
