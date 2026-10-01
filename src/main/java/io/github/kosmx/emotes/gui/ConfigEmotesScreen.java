package io.github.kosmx.emotes.gui;

import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.gui.widget.FastChooseWheelWidget;
import io.github.kosmx.emotes.main.EmoteHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Screen matching Screenshot 3:
 * Left: Search bar + Emote list with player model thumbnails, name, description, author.
 * Right: Keybind controls + FastChooseWheelWidget editor.
 * Bottom: "Open Emotes Folder", "Mod Options", "Done".
 */
public class ConfigEmotesScreen extends GuiScreen {
    protected final GuiScreen parent;
    private EmoteListSlot listSlot;
    private GuiTextField searchField;
    private final List<EmoteHolder> filteredEmotes = new ArrayList<>();
    private int selectedIndex = -1;

    private FastChooseWheelWidget fastChooseWheel;
    private GuiButton setKeyButton;
    private GuiButton resetKeyButton;
    private boolean isListeningForKey = false;

    public ConfigEmotesScreen(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        super.initGui();
        this.buttonList.clear();

        updateFilteredList();

        int listWidth = this.width / 2 - 15;
        int listTop = 32;
        int listBottom = this.height - 30;

        // Search field on the top-left
        this.searchField = new GuiTextField(0, this.fontRenderer, 10, 8, listWidth, 18);
        this.searchField.setMaxStringLength(60);
        this.searchField.setFocused(false);

        // Emote list on left half
        this.listSlot = new EmoteListSlot(this.mc, listWidth, this.height, listTop, listBottom, 38);

        // Right side: keybind buttons
        int rightStartX = this.width / 2 + 5;
        int btnW = Math.min(100, (this.width - rightStartX - 20) / 2);
        int btnH = 20;

        this.setKeyButton = new GuiButton(10, rightStartX, 22, btnW, btnH, getKeyDisplayString());
        this.resetKeyButton = new GuiButton(11, rightStartX + btnW + 6, 22, btnW, btnH, I18n.format("controls.reset"));
        this.buttonList.add(this.setKeyButton);
        this.buttonList.add(this.resetKeyButton);
        updateKeyButtonsState();

        // Right side: interactive FastChooseWheelWidget
        int wheelAvailableHeight = this.height - 32 - 95;
        int wheelAvailableWidth = this.width - rightStartX - 10;
        int wheelSize = Math.max(80, Math.min(wheelAvailableWidth, wheelAvailableHeight));
        int wheelX = rightStartX + (wheelAvailableWidth - wheelSize) / 2;
        int wheelY = 92;

        this.fastChooseWheel = new FastChooseWheelWidget(wheelX, wheelY, wheelSize, true);
        if (getSelectedEmote() != null) {
            this.fastChooseWheel.setSelectedEmoteForConfig(getSelectedEmote());
        }

        // Bottom row: 3 buttons centered
        int bottomBtnW = Math.min(130, (this.width - 40) / 3);
        int bottomY = this.height - 25;
        int gap = 6;
        int totalBottomW = bottomBtnW * 3 + gap * 2;
        int startX = (this.width - totalBottomW) / 2;

        this.buttonList.add(new GuiButton(1, startX, bottomY, bottomBtnW, 20, I18n.format("emotecraft.openFolder")));
        this.buttonList.add(new GuiButton(2, startX + bottomBtnW + gap, bottomY, bottomBtnW, 20, I18n.format("emotecraft.options.options")));
        this.buttonList.add(new GuiButton(3, startX + (bottomBtnW + gap) * 2, bottomY, bottomBtnW, 20, I18n.format("gui.done")));
    }

    private void updateFilteredList() {
        filteredEmotes.clear();
        String query = searchField != null ? searchField.getText().trim().toLowerCase() : "";
        for (EmoteHolder holder : EmoteHolder.list) {
            if (holder != null && holder.name != null) {
                String name = holder.name.getUnformattedText().toLowerCase();
                String author = holder.author != null ? holder.author.getUnformattedText().toLowerCase() : "";
                if (query.isEmpty() || name.contains(query) || author.contains(query)) {
                    filteredEmotes.add(holder);
                }
            }
        }
        java.text.Collator collator = java.text.Collator.getInstance();
        filteredEmotes.sort((a, b) -> {
            String nameA = a.name != null ? a.name.getUnformattedText() : "";
            String nameB = b.name != null ? b.name.getUnformattedText() : "";
            return collator.compare(nameA, nameB);
        });
        if (selectedIndex >= filteredEmotes.size()) {
            selectedIndex = filteredEmotes.size() - 1;
        }
    }

    private EmoteHolder getSelectedEmote() {
        if (selectedIndex >= 0 && selectedIndex < filteredEmotes.size()) {
            return filteredEmotes.get(selectedIndex);
        }
        return null;
    }

    private String getKeyDisplayString() {
        if (isListeningForKey) {
            return "> ??? <";
        }
        EmoteHolder selected = getSelectedEmote();
        if (selected != null) {
            Integer key = PlatformTools.getConfig().emoteKeyMap.getR(selected.getUuid());
            if (key != null && key != 0) {
                return Keyboard.getKeyName(key);
            }
        }
        return I18n.format("emotecraft.not_bound", "Not Bound");
    }

    private void updateKeyButtonsState() {
        EmoteHolder selected = getSelectedEmote();
        boolean hasSelected = selected != null;
        if (this.setKeyButton != null) {
            this.setKeyButton.enabled = hasSelected;
            this.setKeyButton.displayString = getKeyDisplayString();
        }
        if (this.resetKeyButton != null) {
            this.resetKeyButton.enabled = hasSelected && PlatformTools.getConfig().emoteKeyMap.containsL(selected.getUuid());
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 1) {
            // Open Emotes Folder
            PlatformTools.openExternalEmotesDir();
        } else if (button.id == 2) {
            // Mod Options
            this.mc.displayGuiScreen(new ModOptionsScreen(this));
        } else if (button.id == 3) {
            // Done
            PlatformTools.saveConfig();
            this.mc.displayGuiScreen(this.parent);
        } else if (button.id == 10) {
            // Set Key
            if (getSelectedEmote() != null) {
                isListeningForKey = true;
                updateKeyButtonsState();
            }
        } else if (button.id == 11) {
            // Reset Key
            EmoteHolder selected = getSelectedEmote();
            if (selected != null) {
                PlatformTools.getConfig().emoteKeyMap.removeL(selected.getUuid());
                PlatformTools.saveConfig();
                isListeningForKey = false;
                updateKeyButtonsState();
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();

        // Left half: list
        if (this.listSlot != null) {
            this.listSlot.drawScreen(mouseX, mouseY, partialTicks);
        }

        // Search field
        this.searchField.drawTextBox();
        if (this.searchField.getText().isEmpty() && !this.searchField.isFocused()) {
            this.fontRenderer.drawStringWithShadow(
                    I18n.format("emotecraft.search", "Search..."),
                    this.searchField.x + 4,
                    this.searchField.y + 5,
                    0x888888
            );
        }

        // Right side: instruction texts matching Screenshot 3
        int rightStartX = this.width / 2 + 5;
        this.fontRenderer.drawStringWithShadow(I18n.format("emotecraft.options.keybind"), rightStartX, 10, 0xDDDDDD);

        this.fontRenderer.drawStringWithShadow(I18n.format("emotecraft.options.fastmenu"), rightStartX, 48, 0xDDDDDD);
        this.fontRenderer.drawStringWithShadow(I18n.format("emotecraft.options.fastmenu2"), rightStartX, 58, 0xDDDDDD);
        this.fontRenderer.drawStringWithShadow(I18n.format("emotecraft.options.fastmenu3"), rightStartX, 70, 0xDDDDDD);

        // Right side: emote wheel widget
        if (this.fastChooseWheel != null) {
            this.fastChooseWheel.render(mouseX, mouseY, partialTicks);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (isListeningForKey) {
            if (keyCode == 1) { // ESC cancels listening
                isListeningForKey = false;
                updateKeyButtonsState();
                return;
            }
            EmoteHolder selected = getSelectedEmote();
            if (selected != null) {
                PlatformTools.getConfig().emoteKeyMap.put(selected.getUuid(), keyCode);
                PlatformTools.saveConfig();
            }
            isListeningForKey = false;
            updateKeyButtonsState();
            return;
        }

        if (this.searchField.textboxKeyTyped(typedChar, keyCode)) {
            updateFilteredList();
            return;
        }

        if (keyCode == Keyboard.KEY_F5) {
            io.github.kosmx.emotes.main.EmoteReloader.reloadEmotes(true, this::updateFilteredList);
            return;
        }

        if (keyCode == 1) {
            PlatformTools.saveConfig();
            this.mc.displayGuiScreen(this.parent);
            return;
        }

        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (this.searchField != null) {
            this.searchField.updateCursorCounter();
        }
        io.github.kosmx.emotes.main.EmoteWatcher.checkAndTick(this::updateFilteredList);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.searchField.mouseClicked(mouseX, mouseY, mouseButton);

        if (this.fastChooseWheel != null) {
            this.fastChooseWheel.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        if (this.listSlot != null) {
            this.listSlot.handleMouseInput();
        }
        int dWheel = Mouse.getEventDWheel();
        if (dWheel != 0 && this.fastChooseWheel != null) {
            int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
            int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
            if (this.fastChooseWheel.isHovered(mouseX, mouseY)) {
                this.fastChooseWheel.handleMouseWheel(dWheel);
            }
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    class EmoteListSlot extends GuiSlot {
        public EmoteListSlot(Minecraft mcIn, int width, int height, int topIn, int bottomIn, int slotHeightIn) {
            super(mcIn, width, height, topIn, bottomIn, slotHeightIn);
            this.setHasListHeader(false, 0);
        }

        @Override
        public int getListWidth() {
            return this.width - 20;
        }

        @Override
        protected int getScrollBarX() {
            return this.width - 6;
        }

        @Override
        protected int getSize() {
            return filteredEmotes.size();
        }

        @Override
        protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY) {
            selectedIndex = slotIndex;
            EmoteHolder selected = getSelectedEmote();
            if (fastChooseWheel != null) {
                fastChooseWheel.setSelectedEmoteForConfig(selected);
            }
            isListeningForKey = false;
            updateKeyButtonsState();
        }

        @Override
        protected boolean isSelected(int slotIndex) {
            return slotIndex == selectedIndex;
        }

        @Override
        protected void drawBackground() {}

        @Override
        protected void drawContainerBackground(net.minecraft.client.renderer.Tessellator tessellator) {}

        @Override
        protected void overlayBackground(int startY, int endY, int startAlpha, int endAlpha) {
            net.minecraft.client.gui.Gui.drawRect(this.left, startY, this.right, endY, 0xD0101010);
        }

        @Override
        protected int getContentHeight() {
            return this.getSize() * this.slotHeight;
        }

        @Override
        protected void drawSlot(int entryID, int insideLeft, int yPos, int insideSlotHeight, int mouseXIn, int mouseYIn, float partialTicks) {
            if (entryID < 0 || entryID >= filteredEmotes.size()) return;
            EmoteHolder holder = filteredEmotes.get(entryID);

            int iconSize = 32;
            int textX = insideLeft + iconSize + 6;
            int nameColor = isSelected(entryID) ? 0xFFFF55 : 0xFFFFFF;

            // Draw thumbnail icon
            ResourceLocation icon = holder.getIconIdentifier();
            if (icon != null) {
                mc.getTextureManager().bindTexture(icon);
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.enableBlend();
                Gui.drawScaledCustomSizeModalRect(insideLeft + 2, yPos + 2, 0.0F, 0.0F, 256, 256, iconSize, iconSize, 256, 256);
            }

            // Draw name
            mc.fontRenderer.drawStringWithShadow(holder.name.getFormattedText(), textX, yPos + 2, nameColor);

            // Draw description (gray, truncated)
            String desc = holder.description != null ? holder.description.getUnformattedText() : "";
            if (!desc.isEmpty()) {
                int maxDescWidth = (ConfigEmotesScreen.this.width / 2 - 15) - textX - 8;
                if (maxDescWidth > 20) {
                    String trimmed = mc.fontRenderer.trimStringToWidth(desc, maxDescWidth);
                    mc.fontRenderer.drawStringWithShadow(trimmed, textX, yPos + 13, 0x888888);
                }
            }

            // Draw author (orange)
            String author = holder.author != null ? holder.author.getUnformattedText() : "";
            if (!author.isEmpty()) {
                String authorLine = I18n.format("emotecraft.emote.author") + author;
                mc.fontRenderer.drawStringWithShadow(authorLine, textX, yPos + 24, 0xFFAA00);
            }
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }
}
